package de.ghostnet.ghostnetfishing.repository;

import de.ghostnet.ghostnetfishing.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByNameAndPhone(String name, String phone);
}
