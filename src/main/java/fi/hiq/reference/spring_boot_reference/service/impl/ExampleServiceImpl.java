package fi.hiq.reference.spring_boot_reference.service.impl;

import fi.hiq.reference.spring_boot_reference.entity.Example;
import fi.hiq.reference.spring_boot_reference.repository.ExampleRepository;
import fi.hiq.reference.spring_boot_reference.service.ExampleService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExampleServiceImpl implements ExampleService {

  @Resource
  ExampleRepository exampleRepository;

  @Override
  public Example save(Example example) {
    log.info("Saving new Example");

    return exampleRepository.save(example);
  }

  @Override
  public List<Example> findAll() {
    log.info("Returning all Examples");

    return exampleRepository.findAll();
  }

  @Override
  public Optional<Example> findById(Long id) {
    log.info("Returning Example with ID: {}", id);

    Optional<Example> example = Optional.empty();
    if (id != null) {
      example = exampleRepository.findById(id);
    }
    return example;
  }
}
