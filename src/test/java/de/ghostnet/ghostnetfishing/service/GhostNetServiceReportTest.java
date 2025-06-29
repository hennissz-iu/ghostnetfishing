package de.ghostnet.ghostnetfishing.service;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GhostNetServiceReportTest {

    private GhostNetRepository nets;
    private PersonRepository   persons;
    private GhostNetService    service;

    @BeforeEach
    void init() {
        nets    = mock(GhostNetRepository.class);
        persons = mock(PersonRepository.class);
        service = new GhostNetService(nets, persons);
    }

    @Test
    void neuePersonWirdGespeichert() {
        Person neu = new Person();
        neu.setName("Lisa");
        neu.setPhone("123");

        GhostNet net = new GhostNet();
        net.setReportingPerson(neu);

        when(persons.findByNameAndPhone("Lisa","123")).thenReturn(Optional.empty());
        when(persons.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        service.report(net);

        verify(persons).save(neu);
        verify(nets).save(net);
        assertEquals(Status.GEMELDET, net.getStatus());
    }

    @Test
    void existierendePersonWirdVerknuepft() {
        Person exist = new Person();
        exist.setId(42L);
        exist.setName("Tom");
        exist.setPhone("456");

        Person form = new Person();
        form.setName("Tom");
        form.setPhone("456");

        GhostNet net = new GhostNet();
        net.setReportingPerson(form);

        when(persons.findByNameAndPhone("Tom","456")).thenReturn(Optional.of(exist));

        service.report(net);

        verify(persons, never()).save(any());
        assertSame(exist, net.getReportingPerson());
        verify(nets).save(net);
    }

    @Test
    void anonymePersonWirdIgnoriert() {
        Person anon = new Person();
        anon.setAnonymous(true);

        GhostNet net = new GhostNet();
        net.setReportingPerson(anon);

        service.report(net);

        verify(persons, never()).save(any());
        assertNull(net.getReportingPerson());
        verify(nets).save(net);
    }
}
