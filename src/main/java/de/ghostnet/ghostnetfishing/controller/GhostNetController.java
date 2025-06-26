package de.ghostnet.ghostnetfishing.controller;

/* ---------------------------------------------------------------------------
   = Imports ================================================================
   Jeder Import wird nur einmal aufgeführt; Spring kümmert sich um das Wiring.
   ------------------------------------------------------------------------ */
import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;

import org.springframework.stereotype.Controller;           // MVC-Controller
import org.springframework.ui.Model;                    // Daten für die View
import org.springframework.web.bind.annotation.*;       // Mapping-Annotationen
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * MVC-Controller für alle Endpunkte unter /netze
 *  - GET  /netze/erfassen  : Formular anzeigen
 *  - POST /netze/erfassen  : Meldung speichern
 *  - GET  /netze/liste     : Liste zeigen
 *  - POST /netze/uebernehmen/{id} : Netz zur Bergung übernehmen
 *  - POST /netze/geborgen/{id}    : Netz als geborgen markieren
 */
@Controller                      // Registriert die Klasse bei Spring MVC
@RequestMapping("/netze")         // Präfix für alle Methoden-URLs
public class GhostNetController {

    /* ---------------------------------------------------------------------
       = Abhängigkeiten ===================================================== */
    private final GhostNetRepository ghostNetRepository;   // JPA-Repo für Netze
    private final PersonRepository   personRepository;     // JPA-Repo für Personen

    /**
     * Konstruktor-Injection (empfohlene Praxis) ✔️
     *  - verhindert Field-Injection-Probleme
     *  - macht Abhängigkeiten immutabel & testbar
     */
    public GhostNetController(GhostNetRepository ghostNetRepository,
                              PersonRepository personRepository) {
        this.ghostNetRepository = ghostNetRepository;
        this.personRepository   = personRepository;
    }

    /* ---------------------------------------------------------------------
       = 1. Formular zum Melden eines Geisternetzes anzeigen =============== */
    @GetMapping("/erfassen")
    public String zeigeErfassungsformular(Model model) {
        GhostNet ghostNet = new GhostNet();     // Leeres Domain-Objekt
        ghostNet.setReportingPerson(new Person()); // Leere Person für Formularbindung
        model.addAttribute("ghostNet", ghostNet);  // Objekt an Thymeleaf-View binden
        return "netze-erfassen";                   // View-Template-Name (resources/templates)
    }

    /* ---------------------------------------------------------------------
       = 2. Meldung verarbeiten =========================================== */
    @PostMapping("/erfassen")
    public String erfasseNetz(@ModelAttribute GhostNet ghostNet,      // Formulardaten
                              RedirectAttributes redirectAttributes) {// Flash-Attr. für Redirect

        /* ---------- 2.1 Meldende Person verarbeiten ------------------- */
        if (ghostNet.getReportingPerson() != null) {
            Person meldendePerson = ghostNet.getReportingPerson();

            if (meldendePerson.isAnonymous()) {
                // Benutzer:in wollte anonym bleiben -> keine DB-Person zuordnen
                ghostNet.setReportingPerson(null);
            } else {
                // Rolle setzen, falls Name & Telefon angegeben wurden
                meldendePerson.setRole(Role.MELDEND);

                // Prüfen, ob es die Person schon gibt (Name + Telefon sind unique)
                Optional<Person> vorhanden =
                        personRepository.findByNameAndPhone(meldendePerson.getName(),
                                                             meldendePerson.getPhone());

                // Existierende Person verwenden oder neue speichern
                Person gespeichertePerson = vorhanden.orElseGet(() ->
                        personRepository.save(meldendePerson));

                ghostNet.setReportingPerson(gespeichertePerson);
            }
        }

        /* ---------- 2.2 Status & Netz speichern ----------------------- */
        ghostNet.setStatus(Status.GEMELDET);      // Neuen Status setzen
        ghostNetRepository.save(ghostNet);        // Ab in die Datenbank

        /* ---------- 2.3 Redirect (Post/Redirect/Get-Pattern) ---------- */
        redirectAttributes.addFlashAttribute("success", true); // Ein-Mal-Flag
        return "redirect:/netze/erfassen";      // Browser-Redirect
    }

    /* ---------------------------------------------------------------------
       = 3. Übersichtsliste aller Netze anzeigen ========================= */
    @GetMapping("/liste")
    public String zeigeNetzListe(Model model) {
        model.addAttribute("netze", ghostNetRepository.findAll()); // Alle Netze abfragen
        return "netze-liste";                                      // View-Template
    }

    /* ---------------------------------------------------------------------
       = 4. Netz zur Bergung übernehmen ================================== */
    @PostMapping("/uebernehmen/{id}")
    public String uebernehmeNetz(@PathVariable Long   id,       // Pfad-Variable
                                 @RequestParam  String name,    // Formular-Parameter
                                 @RequestParam  String phone) {

        GhostNet netz = ghostNetRepository.findById(id)         // Netz suchen
                         .orElseThrow();                        // 404, wenn nicht vorhanden

        // Bergende Person (existierend oder neu) ermitteln
        Person bergendePerson = findeOderErstellePerson(name, phone, Role.BERGEND);

        netz.setRecoveringPerson(bergendePerson);               // Zuordnen
        netz.setStatus(Status.BERGUNG_BEVORSTEHEND);            // Status setzen
        ghostNetRepository.save(netz);                          // Speichern

        return "redirect:/netze/liste";                         // Zurück zur Liste
    }

    /* ---------------------------------------------------------------------
       = 5. Netz als geborgen markieren ================================== */
    @PostMapping("/geborgen/{id}")
    public String alsGeborgenMelden(@PathVariable Long   id,
                                    @RequestParam  String name,
                                    @RequestParam  String phone) {

        GhostNet netz = ghostNetRepository.findById(id).orElseThrow();

        Person bergendePerson = findeOderErstellePerson(name, phone, Role.BERGEND);

        netz.setRecoveringPerson(bergendePerson);   // überschreibt evt. frühere Zuordnung
        netz.setStatus(Status.GEBORGEN);            // End-Status
        ghostNetRepository.save(netz);

        return "redirect:/netze/liste";
    }

    /* ---------------------------------------------------------------------
       = 6. Hilfsmethode: Person wiederverwenden oder anlegen ============ */
    private Person findeOderErstellePerson(String name, String phone, Role role) {
        return personRepository.findByNameAndPhone(name, phone)
                .orElseGet(() -> {
                    Person p = new Person();
                    p.setName(name);
                    p.setPhone(phone);
                    p.setAnonymous(false);  // explizit, weil sie Kontakt hinterlässt
                    p.setRole(role);
                    return personRepository.save(p);
                });
    }
}