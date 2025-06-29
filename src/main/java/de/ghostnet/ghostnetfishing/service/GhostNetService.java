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

    /* ---------- Meldung ---------------------------------------------------- */

    public void report(GhostNet net) {
        handleReportingPerson(net);
        net.setStatus(Status.GEMELDET);
        nets.save(net);
    }

    @Transactional(readOnly = true)
    public List<GhostNet> findAll() {
        return nets.findAll();
    }

    /* ---------- Bergung (neu: save-Aufruf) --------------------------------- */

    public void takeOver(Long netId, String name, String phone) {
        GhostNet net   = nets.findById(netId).orElseThrow();
        Person   diver = findOrCreate(name, phone, Role.BERGEND);

        net.setRecoveringPerson(diver);
        net.setStatus(Status.BERGUNG_BEVORSTEHEND);
        nets.save(net);
    }

    public void markRecovered(Long netId, String name, String phone) {
        GhostNet net   = nets.findById(netId).orElseThrow();
        Person   diver = findOrCreate(name, phone, Role.BERGEND);

        net.setRecoveringPerson(diver);
        net.setStatus(Status.GEBORGEN);
        nets.save(net);
    }

    /* ---------- Helper ----------------------------------------------------- */

    private void handleReportingPerson(GhostNet net) {
        Person reporter = net.getReportingPerson();
        if (reporter == null || reporter.isAnonymous()) {
            net.setReportingPerson(null);
            return;
        }
        reporter.setRole(Role.MELDEND);

        Optional<Person> existing =
                persons.findByNameAndPhone(reporter.getName(), reporter.getPhone());

        existing.ifPresentOrElse(net::setReportingPerson,
                                 () -> net.setReportingPerson(persons.save(reporter)));
    }

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
