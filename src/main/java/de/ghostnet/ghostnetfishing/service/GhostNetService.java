package de.ghostnet.ghostnetfishing.service;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GhostNetService {

    private final GhostNetRepository nets;
    private final PersonRepository  persons;

    public GhostNetService(GhostNetRepository nets, PersonRepository persons) {
        this.nets    = nets;
        this.persons = persons;
    }
    
    

	/* ---------- Leseoperationen ------------------------------------------- */
	
	@Transactional(readOnly = true)
	public List<GhostNet> findAll() {
	    return nets.findAll();
	}

	
    /* ---------- Meldung ---------------------------------------------------- */

    public void report(GhostNet net) {
        handleReportingPerson(net);
        net.setStatus(Status.GEMELDET);
        nets.save(net);
    }


    /* ---------- Bergung  --------------------------------- */

    public void takeOver(Long netId, String name, String phone) {
        GhostNet net   = nets.findById(netId).orElseThrow();
        Person   diver = findOrCreate(name, phone, Role.BERGEND);

        net.setRecoveringPerson(diver);
        net.setStatus(Status.BERGUNG_BEVORSTEHEND);
        nets.save(net);
    }
    
    
    /* ---------- als Geborgen melden ------------------------ */
    public void markRecovered(Long netId, String name, String phone) {
        GhostNet net   = nets.findById(netId).orElseThrow();
        Person diver = find(name, phone).orElseThrow();

        /* Prüfen, ob es derselbe Übernehmer ist */
        if (!net.getRecoveringPerson().equals(diver)) {
            throw new IllegalStateException("Nur die übernehmende Person darf das Netz als geborgen melden.");
        }

        /* Status umstellen & speichern */
        net.setStatus(Status.GEBORGEN);
        nets.save(net);
    }


    /* ---------- Helper: meldende Person verknüpfen ------------------------ */
    private void handleReportingPerson(GhostNet net) {
        Person reporter = net.getReportingPerson();

        // 1) anonyme Meldung → Beziehung löschen
        if (reporter == null || reporter.isAnonymous()) {
            net.setReportingPerson(null);
            return;
        }

        // 2) nach Name + Telefon suchen
        Optional<Person> existing = persons.findByNameAndPhone(reporter.getName(), reporter.getPhone());

        if (existing.isPresent()) {
            // Person existiert bereits; bestehende Rolle bleibt unverändert
            net.setReportingPerson(existing.get());
        } else {
            // 3) neue Person mit Rolle MELDEND anlegen
            reporter.setRole(Role.MELDEND);
            net.setReportingPerson(persons.save(reporter));
        }
    }

    
    /* ---------- Helper: Person in DB finden oder erstellen -------------------------------------- */
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
    
    
    
    /* ---------- Helper: Person in DB finden -------------------------------------- */
    private Optional<Person> find(String name, String phone) {
        return persons.findByNameAndPhone(name, phone);
    }
}
