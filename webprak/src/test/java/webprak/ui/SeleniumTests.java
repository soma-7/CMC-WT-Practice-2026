package webprak.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SeleniumTests {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @Autowired
    private webprak.DAO.ClientDAO clientDAO;
    @Autowired
    private webprak.DAO.ProfileDAO profileDAO;
    @Autowired
    private webprak.DAO.OperationDAO operationDAO;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/google-chrome-stable");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Common scenarios tests

    @Test
    void testProfileListFiltering() {
        Long clientId = clientDAO.createClient("{\"test\":\"filter\"}");
        String phone = UUID.randomUUID().toString().substring(0, 10);
        Long profileId = profileDAO.createProfile(clientId, "SeleniumTestUser", phone, null);
        assertNotNull(profileId);

        driver.get(url("/profiles"));

        WebElement searchInput = driver.findElement(By.name("search"));
        searchInput.sendKeys(phone);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table tbody")));

        WebElement tableBody = driver.findElement(By.cssSelector("table.table tbody"));
        assertTrue(tableBody.getText().contains(phone));

        profileDAO.deleteById(profileId);
        clientDAO.deleteById(clientId);
    }

    @Test
    void testOperationsFilterByProfile() {
        Long clientId = clientDAO.createClient("{\"test\":\"opfilter\"}");
        Long profileId = profileDAO.createProfile(clientId, "OpTestUser", "+70000000001", null);
        Long operationId = operationDAO.createOperation(profileId, "deposit", null, 100.0, "Тестовая операция Selenium");
        driver.get(url("/operations?profileId=" + profileId));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table tbody tr")));

        WebElement tableBody = driver.findElement(By.cssSelector("table.table tbody"));
        assertTrue(tableBody.getText().contains("Тестовая операция Selenium"));

        operationDAO.deleteById(operationId);
        profileDAO.deleteById(profileId);
        clientDAO.deleteById(clientId);
    }

    @Test
    void testRegisterNewClient() {
        driver.get(url("/clients/add"));

        String testInfo = "{\"passport\":\"SELENIUM_TEST\"}";
        String testName = "SeleniumClient";
        String testPhone = "555-" + System.currentTimeMillis() % 100000;

        driver.findElement(By.id("info")).sendKeys(testInfo);
        driver.findElement(By.id("name")).sendKeys(testName);
        driver.findElement(By.id("phone")).sendKeys(testPhone);
        driver.findElement(By.id("other")).sendKeys("{}");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/clients/"));

        String heading = driver.findElement(By.tagName("h1")).getText();
        assertTrue(heading.contains("Клиент #"));

        String currentUrl = driver.getCurrentUrl();
        Long clientId = Long.parseLong(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        clientDAO.deleteById(clientId);
    }

    @Test
    void testDepositBalance() {
        Long clientId = clientDAO.createClient("{\"test\":\"deposit\"}");
        Long profileId = profileDAO.createProfile(clientId, "DepositUser", "777-000-1234", null);

        driver.get(url("/operations/add"));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(4));
        driver.findElement(By.id("profileId")).sendKeys(profileId.toString());
        Select typeSelect = new Select(driver.findElement(By.id("type")));
        typeSelect.selectByValue("deposit");
        driver.findElement(By.id("balanceChange")).sendKeys("250.00");
        driver.findElement(By.id("description")).sendKeys("Selenium deposit test");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/operations"));
        driver.get(url("/profiles/" + profileId));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        driver.get(url("/profiles"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("search")));
        driver.findElement(By.name("search")).sendKeys("777-000-1234");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table tbody tr")));
        WebElement table = driver.findElement(By.cssSelector("table.table tbody"));
        assertTrue(table.getText().contains("250.0"));

        profileDAO.deleteById(profileId);
        clientDAO.deleteById(clientId);
    }

    @Test
    void testDeleteClient() {
        Long clientId = clientDAO.createClient("{\"test\":\"delete\"}");
        assertNotNull(clientId);

        driver.get(url("/clients/" + clientId));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form[action*='delete']")));

        WebElement deleteButton = driver.findElement(By.cssSelector("button.btn-danger"));
        deleteButton.click();
        Alert alert = driver.switchTo().alert();
        alert.accept();

        wait.until(ExpectedConditions.urlToBe(url("/clients")));

        assertNull(clientDAO.getById(clientId));
    }

    // Error scenarios tests

    @Test
    void testNegativeBalanceError() {
        Long clientId = clientDAO.createClient("{\"test\":\"negative\"}");
        Long profileId = profileDAO.createProfile(clientId, "BalanceUser", "111-000-1111", null);

        driver.get(url("/operations/add"));

        driver.findElement(By.id("profileId")).sendKeys(profileId.toString());
        new Select(driver.findElement(By.id("type"))).selectByValue("withdrawal");
        driver.findElement(By.id("balanceChange")).sendKeys("-500.00");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(driver.getCurrentUrl().contains("/error"));

        profileDAO.deleteById(profileId);
        clientDAO.deleteById(clientId);
    }

    @Test
    void testDuplicatePhoneError() {
        Long clientId = clientDAO.createClient("{\"test\":\"duplicate\"}");
        String phone = "222-333-4444";
        profileDAO.createProfile(clientId, "User1", phone, null);

        driver.get(url("/profiles/add"));
        driver.findElement(By.id("clientId")).sendKeys(clientId.toString());
        driver.findElement(By.id("name")).sendKeys("User2");
        driver.findElement(By.id("phone")).sendKeys(phone);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(driver.getCurrentUrl().contains("/error"));

        clientDAO.deleteById(clientId);
    }

    @Test
    void testAddProfileWithInvalidClient() {
        driver.get(url("/profiles/add"));
        driver.findElement(By.id("clientId")).sendKeys("9999999");
        driver.findElement(By.id("name")).sendKeys("Ghost");
        driver.findElement(By.id("phone")).sendKeys("555-000-5555");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(driver.getCurrentUrl().contains("/error"));
    }
}