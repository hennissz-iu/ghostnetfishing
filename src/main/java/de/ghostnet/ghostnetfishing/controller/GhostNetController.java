package de.ghostnet.ghostnetfishing.controller;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/netze")
public class GhostNetController {

    private final GhostNetRepository ghostNetRepository;
    private final PersonRepository personRepository;

    public GhostNetController(GhostNetRepository ghostNetRepository, PersonRepository personRepository) {
        this.ghostNetRepository = ghostNetRepository;
        this.personRepository = personRepository;
    }

    // Formular anzeigen
    @GetMapping("/erfassen")
    public String zeigeErfassungsformular(Model model) {
        GhostNet ghostNet = new GhostNet();
        ghostNet.setReportingPerson(new Person());
        model.addAttribute("ghostNet", ghostNet);
        return "netze-erfassen";
    }

    // Formular absenden
    @PostMapping("/erfassen")
    public String erfasseNetz(@ModelAttribute GhostNet ghostNet,
                              RedirectAttributes redirectAttributes) {

        if (ghostNet.getReportingPerson() != null) {
            Person person = ghostNet.getReportingPerson();

            if (person.isAnonymous()) {
                // Keine Zuordnung bei anonymer Meldung
                ghostNet.setReportingPerson(null);
            } else {
                person.setRole(Role.MELDEND);

                Optional<Person> existingPerson = personRepository.findByNameAndPhone(person.getName(), person.getPhone());
                if (existingPerson.isPresent()) {
                    ghostNet.setReportingPerson(existingPerson.get());
                } else {
                    personRepository.save(person);
                    ghostNet.setReportingPerson(person);
                }
            }
        }

        ghostNet.setStatus(Status.GEMELDET);
        ghostNetRepository.save(ghostNet);

        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/netze/erfassen";
    }

    // Liste anzeigen
    @GetMapping("/liste")
    public String zeigeNetzListe(Model model) {
        model.addAttribute("netze", ghostNetRepository.findAll());
        return "netze-liste";
    }

    // Übernahme zur Bergung
    @PostMapping("/uebernehmen/{id}")
    public String uebernehmeNetz(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String phone) {

        GhostNet netz = ghostNetRepository.findById(id).orElseThrow();

        Optional<Person> existing = personRepository.findByNameAndPhone(name, phone);
        Person person = existing.orElseGet(() -> {
            Person p = new Person();
            p.setName(name);
            p.setPhone(phone);
            p.setAnonymous(false);
            p.setRole(Role.BERGEND);
            return personRepository.save(p);
        });

        netz.setRecoveringPerson(person);
        netz.setStatus(Status.BERGUNG_BEVORSTEHEND);
        ghostNetRepository.save(netz);

        return "redirect:/netze/liste";
    }

    // Als geborgen melden
    @PostMapping("/geborgen/{id}")
    public String alsGeborgenMelden(@PathVariable Long id,
                                    @RequestParam String name,
                                    @RequestParam String phone) {

        GhostNet netz = ghostNetRepository.findById(id).orElseThrow();

        Optional<Person> existing = personRepository.findByNameAndPhone(name, phone);
        Person person = existing.orElseGet(() -> {
            Person p = new Person();
            p.setName(name);
            p.setPhone(phone);
            p.setAnonymous(false);
            p.setRole(Role.BERGEND);
            return personRepository.save(p);
        });

        netz.setRecoveringPerson(person);  // ersetzt ggf. bisherige bergende Person
        netz.setStatus(Status.GEBORGEN);
        ghostNetRepository.save(netz);

        return "redirect:/netze/liste";
    }
}
