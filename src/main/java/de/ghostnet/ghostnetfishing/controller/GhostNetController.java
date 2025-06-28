package de.ghostnet.ghostnetfishing.controller;

/*
 * ---------------------------------------- IMPORTS ------------------------------------------------
 *  ► Alle Klassen, die wir benötigen, um:
 *      • auf unsere Datenbank (Repositories) zuzugreifen
 *      • einen MVC-Controller in Spring zu definieren
 *      • URL-Endpunkte (Mappings) und View-Model-Objekte zu verwenden
 * ----------------------------------------------------------------------------------------------- */
import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;
import org.springframework.stereotype.Controller;             // macht die Klasse zu einem MVC-Controller
import org.springframework.ui.Model;                        // Tasche für Daten, die an die View gehen
import org.springframework.web.bind.annotation.*;           // @GetMapping, @PostMapping, @PathVariable …
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 *  GHOST-NET-CONTROLLER  🕸️
 * ----------------------------------------------------------------------------
 *  • Spring MVC-Controller, der alles rund um Geisternetze behandelt.
 *  • Jedes Mapping (URL-Pfad) beginnt mit "/netze"   →  siehe @RequestMapping.
 *  • Enthält 5 öffentliche Handler-Methoden (GET/POST) + 2 private Helfer.
 *  • Keine „magischen“ Framework-Features: alles ist explizit kommentiert.
 * ----------------------------------------------------------------------------
 */
@Controller                  // Spring legt beim Start ein einziges Objekt dieser Klasse an
@RequestMapping("/netze")     // Präfix für **alle** Methoden-URLs in dieser Klasse
public class GhostNetController {

    /* =========================================================================
       1) REPOSITORIES – Türen zur Datenbank
       ========================================================================= */

    private final GhostNetRepository ghostNets;   // Zugriff auf Tabelle "ghost_net"
    private final PersonRepository   persons;     // Zugriff auf Tabelle "person"

    /**
     * Konstruktor-Injection  🡒  Beste Praxis:
     *   ► Abhängigkeiten (Repositories) werden *einmal* beim Erzeugen gesetzt,
     *   ► Felder bleiben final (= unveränderbar)  → Thread-safe & testbar.
     */
    public GhostNetController(GhostNetRepository ghostNets,
                              PersonRepository persons) {
        this.ghostNets = ghostNets;
        this.persons   = persons;
    }

    /* =========================================================================
       2) HANDLER: FORMULAR ANZEIGEN
       ========================================================================= */

    /**
     * GET  /netze/erfassen
     * ----------------------------------------------------------------------------
     *  • Zeigt das leere Erfassungs-Formular.
     *  • Wir legen ein neues GhostNet-Objekt + leere Person ins Model,
     *    damit Thymeleaf die Felder binden kann.
     */
    @GetMapping("/erfassen")
    public String showForm(Model model) {
        GhostNet net = new GhostNet();          // leeres Domain-Objekt
        net.setReportingPerson(new Person());   // leere Person → Formularfelder

        model.addAttribute("ghostNet", net);    // Schlüssel "ghostNet" in View verfügbar
        return "netze-erfassen";                // Thymeleaf-Template unter resources/templates
    }

    /* =========================================================================
       3) HANDLER: FORMULAR ABSENDEN
       ========================================================================= */

    /**
     * POST  /netze/erfassen
     * ----------------------------------------------------------------------------
     *  • Speichert ein neues Geisternetz.
     *  • @ModelAttribute konvertiert alle Formularfelder automatisch in ein
     *    GhostNet-Objekt.
     *  • RedirectAttributes erzeugt eine Flash-Message für den nächsten Request.
     */
    @PostMapping("/erfassen")
    public String saveNet(@ModelAttribute GhostNet ghostNet,
                          RedirectAttributes flash) {

        handleReportingPerson(ghostNet);     // meldende Person auswerten
        ghostNet.setStatus(Status.GEMELDET); // neuer Eintrag startet immer mit Status „GEMELDET“
        ghostNets.save(ghostNet);            // INSERT in Tabelle ghost_net

        flash.addFlashAttribute("success", true); // einmaliges „Speichern ok“-Flag
        return "redirect:/netze/erfassen";        // Post/Redirect/Get – verhindert Doppelpost
    }

    /* =========================================================================
       4) HANDLER: LISTE ALLER NETZE
       ========================================================================= */

    /**
     * GET  /netze/liste
     * ----------------------------------------------------------------------------
     *  • Holt alle Geisternetze aus der DB und gibt sie an die View.
     */
    @GetMapping("/liste")
    public String list(Model model) {
        model.addAttribute("netze", ghostNets.findAll()); // SELECT * FROM ghost_net
        return "netze-liste";                             // zeigt Tabelle in der View
    }

    /* =========================================================================
       5) HANDLER: NETZ ZUR BERGUNG ÜBERNEHMEN
       ========================================================================= */

    /**
     * POST  /netze/uebernehmen/{id}
     * ----------------------------------------------------------------------------
     *  • Eine Person erklärt sich bereit, das Netz zu bergen.
     *  • id          kommt aus der URL      ⇒ @PathVariable
     *  • name/phone  kommen aus dem Formular ⇒ @RequestParam
     */
    @PostMapping("/uebernehmen/{id}")
    public String takeOver(@PathVariable Long id,
                           @RequestParam String name,
                           @RequestParam String phone) {

        GhostNet net   = ghostNets.findById(id).orElseThrow();          // Netz oder 404
        Person   saver = findOrCreate(name, phone, Role.BERGEND);       // Person holen/erstellen

        net.setRecoveringPerson(saver);                                 // Beziehung setzen
        net.setStatus(Status.BERGUNG_BEVORSTEHEND);                     // Status-Update
        ghostNets.save(net);                                            // UPDATE ghost_net

        return "redirect:/netze/liste";                                 // zurück zur Tabelle
    }

    /* =========================================================================
       6) HANDLER: NETZ ALS GEBORGEN MELDEN
       ========================================================================= */

    /**
     * POST  /netze/geborgen/{id}
     * ----------------------------------------------------------------------------
     *  • Netz wurde tatsächlich geborgen → finaler Status „GEBORGEN“.
     */
    @PostMapping("/geborgen/{id}")
    public String markRecovered(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String phone) {

        GhostNet net   = ghostNets.findById(id).orElseThrow();
        Person   saver = findOrCreate(name, phone, Role.BERGEND);

        net.setRecoveringPerson(saver);   // ersetzt evtl. frühere Person
        net.setStatus(Status.GEBORGEN);   // Endstatus
        ghostNets.save(net);

        return "redirect:/netze/liste";
    }

    /* =========================================================================
       7) PRIVATE HILFSMETHODEN
       ========================================================================= */

    /**
     * Prüft die meldende Person:
     *  • anonym            → nicht speichern, Beziehung auf null setzen
     *  • existiert bereits → verknüpfen
     *  • neu               → speichern und verknüpfen
     */
    private void handleReportingPerson(GhostNet net) {
        Person reporter = net.getReportingPerson();

        if (reporter == null || reporter.isAnonymous()) { // Fall: Anonym oder gar nicht vorhanden
            net.setReportingPerson(null);
            return;
        }

        reporter.setRole(Role.MELDEND);

        Optional<Person> existing =
                persons.findByNameAndPhone(reporter.getName(), reporter.getPhone());

        if (existing.isPresent()) {                       // Fall: schon in DB
            net.setReportingPerson(existing.get());
        } else {                                          // Fall: neu
            persons.save(reporter);
            net.setReportingPerson(reporter);
        }
    }

    /**
     * Utility-Methode:
     *  • Sucht Person anhand von Name + Telefon.
     *  • Wenn vorhanden  → zurückgeben.
     *  • Wenn nicht da   → neue Person mit passender Rolle anlegen + speichern.
     */
    private Person findOrCreate(String name, String phone, Role role) {
        return persons.findByNameAndPhone(name, phone)
                      .orElseGet(() -> {
                          Person p = new Person();
                          p.setName(name);
                          p.setPhone(phone);
                          p.setAnonymous(false);
                          p.setRole(role);
                          return persons.save(p);
                      });
    }
}
