package de.ghostnet.ghostnetfishing.controller;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.service.GhostNetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC-Controller: delegiert alle Geschäfts­aktionen an GhostNetService
 * und steuert nur noch Navigation + View-Model.
 */
@Controller
@RequestMapping("/netze")
public class GhostNetController {

    private final GhostNetService service;

    public GhostNetController(GhostNetService service) {
        this.service = service;
    }

    /* -------- Formular anzeigen / speichern -------- */

    @GetMapping("/erfassen")
    public String showForm(Model model) {
        GhostNet net = new GhostNet();
        net.setReportingPerson(new de.ghostnet.ghostnetfishing.model.Person());
        model.addAttribute("ghostNet", net);
        return "netze-erfassen";
    }

    @PostMapping("/erfassen")
    public String saveNet(@ModelAttribute GhostNet ghostNet,
                          RedirectAttributes flash) {

        service.report(ghostNet);
        flash.addFlashAttribute("success", true);
        return "redirect:/netze/erfassen";
    }

    /* -------- Liste -------- */

    @GetMapping("/liste")
    public String list(Model model) {
        model.addAttribute("netze", service.findAll());
        return "netze-liste";
    }

    /* -------- Aktionen -------- */

    @PostMapping("/uebernehmen/{id}")
    public String takeOver(@PathVariable Long id,
                           @RequestParam String name,
                           @RequestParam String phone) {

        service.takeOver(id, name, phone);
        return "redirect:/netze/liste";
    }

    @PostMapping("/geborgen/{id}")
    public String markRecovered(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String phone) {

        service.markRecovered(id, name, phone);
        return "redirect:/netze/liste";
    }
}
