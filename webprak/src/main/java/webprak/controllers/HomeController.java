package webprak.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", "CMC-WT-Practice-2026");
        model.addAttribute("readmeText",
                "small description");
        return "index";
    }

    @GetMapping("/error")
    public String errorPage(@RequestParam(required = false) String message, Model model) {
        model.addAttribute("message", message != null ? message : "Неизвестная ошибка");
        return "error";
    }
}
