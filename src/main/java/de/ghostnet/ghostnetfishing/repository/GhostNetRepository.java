package de.ghostnet.ghostnetfishing.repository;

import de.ghostnet.ghostnetfishing.model.GhostNet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GhostNetRepository extends JpaRepository<GhostNet, Long> {
    List<GhostNet> findByStatus(GhostNet.Status status);
}
