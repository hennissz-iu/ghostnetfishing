package de.ghostnet.ghostnetfishing.controller;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.service.GhostNetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC-Controller: delegiert Geschäfts­aktionen an GhostNetService
 * und steuert Navigation + View-Model.
 */
@Controller
@RequestMapping("/netze")
public class GhostNetController {

    /* Dependency Injection ------------------------------------------------ */
    private final GhostNetService service;

    public GhostNetController(GhostNetService service) {
        this.service = service;
    }

    /* -------- Formular anzeigen / speichern -------- */

    @GetMapping("/erfassen")
    public String showForm(Model model) {
        GhostNet net = new GhostNet();
        net.setReportingPerson(new Person());
        model.addAttribute("ghostNet", net);
        return "netze-erfassen";
    }

    /* Post/Redirect/Get: Nach erfolgreicher Speicherung folgt ein 302-Redirect */
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
                                @RequestParam String phone,
                                RedirectAttributes flash) {
        try {
            service.markRecovered(id, name, phone);
            flash.addFlashAttribute("success", "Netz als geborgen gemeldet.");
        } catch (IllegalStateException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/netze/liste";
    }
}
