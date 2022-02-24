package fi.hiq.reference.spring_boot_reference.repository;

import fi.hiq.reference.spring_boot_reference.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends JpaRepository<Example, Long> {
}
