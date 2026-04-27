package webprak.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", "CMC-WT-Practice-2026");
        model.addAttribute("readmeText",
                "small description");
        return "index";
    }
}
