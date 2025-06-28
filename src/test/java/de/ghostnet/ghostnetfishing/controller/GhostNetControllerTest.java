package de.ghostnet.ghostnetfishing.controller;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GhostNetControllerTest {

    private GhostNetRepository  ghostNetRepo;
    private PersonRepository    personRepo;
    private RedirectAttributes  redirect;
    private GhostNetController  controller;

    @BeforeEach
    void setUp() {
        ghostNetRepo = mock(GhostNetRepository.class);
        personRepo   = mock(PersonRepository.class);
        redirect     = mock(RedirectAttributes.class);

        controller = new GhostNetController(ghostNetRepo, personRepo);
    }

    @Test
    void neuePersonWirdGespeichert() {
        Person neu = new Person();
        neu.setName("Lisa");
        neu.setPhone("123");
        neu.setAnonymous(false);

        GhostNet netz = new GhostNet();
        netz.setReportingPerson(neu);

        when(personRepo.findByNameAndPhone("Lisa","123")).thenReturn(Optional.empty());

        controller.saveNet(netz, redirect);

        verify(personRepo).save(neu);                 // neue Person gespeichert
        verify(ghostNetRepo).save(netz);              // Netz gespeichert
        assertEquals(Status.GEMELDET, netz.getStatus());
    }

    @Test
    void existierendePersonWirdVerwendet() {
        Person exist = new Person();
        exist.setId(42L);
        exist.setName("Tom");
        exist.setPhone("456");

        Person form = new Person();
        form.setName("Tom");
        form.setPhone("456");
        form.setAnonymous(false);

        GhostNet netz = new GhostNet();
        netz.setReportingPerson(form);

        when(personRepo.findByNameAndPhone("Tom","456")).thenReturn(Optional.of(exist));

        controller.saveNet(netz, redirect);

        verify(personRepo, never()).save(any());      // nichts Neues gespeichert
        assertSame(exist, netz.getReportingPerson()); // exist. Person verknüpft
        verify(ghostNetRepo).save(netz);
    }

    @Test
    void anonymePersonWirdNichtGespeichert() {
        Person anonym = new Person();
        anonym.setAnonymous(true);

        GhostNet netz = new GhostNet();
        netz.setReportingPerson(anonym);

        controller.saveNet(netz, redirect);

        verify(personRepo, never()).save(any());
        assertNull(netz.getReportingPerson());        // Feld wurde geleert
        verify(ghostNetRepo).save(netz);
    }
}
