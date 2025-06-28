package de.ghostnet.ghostnetfishing.controller;

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

/**
 * Prüft:
 *  1) takeOver()  ➜ Netz übernimmt bergende Person & Status „BERGUNG_BEVORSTEHEND“
 *  2) markRecovered() ➜ Netz wird geborgen & Status „GEBORGEN“
 */
class GhostNetControllerBergungTest {

    private GhostNetRepository ghostNetRepo;
    private PersonRepository   personRepo;
    private GhostNetController controller;

    private GhostNet demoNet;                   // wird in beiden Tests genutzt
    private static final Long NET_ID = 1L;

    @BeforeEach
    void init() {
        ghostNetRepo = mock(GhostNetRepository.class);
        personRepo   = mock(PersonRepository.class);
        controller   = new GhostNetController(ghostNetRepo, personRepo);

        demoNet = new GhostNet();
        demoNet.setId(NET_ID);
        demoNet.setStatus(Status.GEMELDET);

        when(ghostNetRepo.findById(NET_ID)).thenReturn(Optional.of(demoNet));
    }

    /** Szenario 1: Person übernimmt ein Netz (takeOver). */
    @Test
    void personKannNetzUebernehmen() {
        // Person ist noch nicht in der DB → leer
        when(personRepo.findByNameAndPhone("Anna", "111"))
                .thenReturn(Optional.empty());

        // WICHTIG: save(...) soll das gleiche Person-Objekt zurückgeben
        when(personRepo.save(any(Person.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // --- Aktion ------------------------------------------------------
        controller.takeOver(NET_ID, "Anna", "111");

        // --- Erwartungen -------------------------------------------------
        verify(personRepo).save(any(Person.class));
        verify(ghostNetRepo).save(demoNet);

        Person p = demoNet.getRecoveringPerson();
        assertNotNull(p, "RecoveringPerson sollte gesetzt sein");
        assertEquals("Anna", p.getName());
        assertEquals("111", p.getPhone());
        assertEquals(Role.BERGEND, p.getRole());
        assertEquals(Status.BERGUNG_BEVORSTEHEND, demoNet.getStatus());
    }


    /** Szenario 2: Existierende Person meldet Netz als geborgen (markRecovered). */
    @Test
    void personKannNetzAlsGeborgenMelden() {
        // Person existiert bereits in der DB
        Person existing = new Person();
        existing.setId(42L);
        existing.setName("Tom");
        existing.setPhone("222");
        existing.setRole(Role.BERGEND);

        when(personRepo.findByNameAndPhone("Tom", "222"))
                .thenReturn(Optional.of(existing));

        // --- Aktion ------------------------------------------------------
        controller.markRecovered(NET_ID, "Tom", "222");

        // --- Erwartungen -------------------------------------------------
        verify(personRepo, never()).save(any());               // keine neue Person angelegt
        verify(ghostNetRepo).save(demoNet);

        assertSame(existing, demoNet.getRecoveringPerson(),    // exakt dieselbe Instanz
                   "RecoveringPerson sollte das vorhandene Objekt sein");
        assertEquals(Status.GEBORGEN, demoNet.getStatus());
    }
}
