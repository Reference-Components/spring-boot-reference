package fi.hiq.reference.spring_boot_reference.service;

import fi.hiq.reference.spring_boot_reference.entity.Example;

import java.util.List;
import java.util.Optional;

public interface ExampleService {

  Example save(Example example);

  List<Example> findAll();

  Optional<Example> findById(Long id);
}
