package at.ac.tuwien.sepr.groupphase.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import org.springframework.stereotype.Repository;

@Repository
public interface UiConfigRepository extends JpaRepository<UiConfig, Long> {

}
