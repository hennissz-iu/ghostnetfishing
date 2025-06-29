package de.ghostnet.ghostnetfishing.service;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GhostNetServiceBergungTest {

    private GhostNetRepository nets;
    private PersonRepository   persons;
    private GhostNetService    service;

    private GhostNet demoNet;
    private static final Long NET_ID = 1L;

    @BeforeEach
    void init() {
        nets    = mock(GhostNetRepository.class);
        persons = mock(PersonRepository.class);
        service = new GhostNetService(nets, persons);

        demoNet = new GhostNet();
        demoNet.setId(NET_ID);
        demoNet.setStatus(Status.GEMELDET);

        when(nets.findById(NET_ID)).thenReturn(Optional.of(demoNet));
    }

    @Test
    void takeOverLegtNeuePersonAn() {
        when(persons.findByNameAndPhone("Anna","111")).thenReturn(Optional.empty());
        when(persons.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        service.takeOver(NET_ID, "Anna", "111");

        verify(persons).save(any(Person.class));
        verify(nets).save(demoNet);

        Person p = demoNet.getRecoveringPerson();
        assertNotNull(p);
        assertEquals("Anna", p.getName());
        assertEquals(Role.BERGEND, p.getRole());
        assertEquals(Status.BERGUNG_BEVORSTEHEND, demoNet.getStatus());
    }

    @Test
    void markRecoveredVerwendetExistierendePerson() {
        Person exist = new Person();
        exist.setId(42L);
        exist.setName("Tom");
        exist.setPhone("222");
        exist.setRole(Role.BERGEND);

        when(persons.findByNameAndPhone("Tom","222")).thenReturn(Optional.of(exist));

        service.markRecovered(NET_ID, "Tom", "222");

        verify(persons, never()).save(any());
        verify(nets).save(demoNet);

        assertSame(exist, demoNet.getRecoveringPerson());
        assertEquals(Status.GEBORGEN, demoNet.getStatus());
    }
}
