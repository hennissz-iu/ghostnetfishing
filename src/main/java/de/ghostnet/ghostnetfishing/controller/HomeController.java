package de.ghostnet.ghostnetfishing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home"; // verweist auf home.html in src/main/resources/templates
    }
}