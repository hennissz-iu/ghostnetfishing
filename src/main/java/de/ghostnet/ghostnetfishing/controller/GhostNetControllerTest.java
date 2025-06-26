import de.ghostnet.ghostnetfishing.controller.GhostNetController;
import de.ghostnet.ghostnetfishing.model.GhostNet;
import de.ghostnet.ghostnetfishing.model.Person;
import de.ghostnet.ghostnetfishing.model.Person.Role;
import de.ghostnet.ghostnetfishing.model.GhostNet.Status;
import de.ghostnet.ghostnetfishing.repository.GhostNetRepository;
import de.ghostnet.ghostnetfishing.repository.PersonRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GhostNetControllerTest {

    private GhostNetRepository ghostNetRepo;
    private PersonRepository personRepo;
    private GhostNetController controller;

    @BeforeEach
    void setup() {
        ghostNetRepo = mock(GhostNetRepository.class);
        personRepo = mock(PersonRepository.class);
        controller = new GhostNetController(ghostNetRepo, personRepo);
    }

    @Test
    void testNeuePersonWirdGespeichert() {
        // Given
        Person neu = new Person();
        neu.setName("Lisa");
        neu.setPhone("123");
        neu.setAnonymous(false);

        GhostNet netz = new GhostNet();
        netz.setReportingPerson(neu);

        when(personRepo.findByNameAndPhone("Lisa", "123")).thenReturn(Optional.empty());

        // When
        controller.erfasseNetz(netz, null);

        // Then
        verify(personRepo).save(neu);
        assertEquals(Status.GEMELDET, netz.getStatus());
        verify(ghostNetRepo).save(netz);
    }

    @Test
    void testExistierendePersonWirdVerwendet() {
        // Given
        Person exist = new Person();
        exist.setId(42L);
        exist.setName("Tom");
        exist.setPhone("456");

        GhostNet netz = new GhostNet();
        Person meldung = new Person();
        meldung.setName("Tom");
        meldung.setPhone("456");
        meldung.setAnonymous(false);
        netz.setReportingPerson(meldung);

        when(personRepo.findByNameAndPhone("Tom", "456")).thenReturn(Optional.of(exist));

        // When
        controller.erfasseNetz(netz, null);

        // Then
        verify(personRepo, never()).save(any());  // Keine neue Person speichern
        assertEquals(exist, netz.getReportingPerson());
        verify(ghostNetRepo).save(netz);
    }

    @Test
    void testAnonymePersonWirdNichtGespeichert() {
        // Given
        Person anonym = new Person();
        anonym.setAnonymous(true);  // wichtig!

        GhostNet netz = new GhostNet();
        netz.setReportingPerson(anonym);

        // When
        controller.erfasseNetz(netz, null);

        // Then
        assertNull(netz.getReportingPerson());  // Wird entfernt
        verify(personRepo, never()).save(any());
        verify(ghostNetRepo).save(netz);
    }
}
