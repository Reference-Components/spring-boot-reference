package fi.hiq.reference.spring_boot_reference.controller;

import fi.hiq.reference.spring_boot_reference.entity.Example;
import fi.hiq.reference.spring_boot_reference.service.ExampleService;
import fi.hiq.reference.spring_boot_reference.util.SecurityContextUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
public class ExampleController {

  @Resource
  private ExampleService exampleService;

  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok(
        String.format("This is response from '/'. Request was made by '%s'.", SecurityContextUtil.getUsernameOfRequestClient()));
  }

  @GetMapping("/examples")
  public ResponseEntity<?> getExamples() {
    return ResponseEntity.ok(exampleService.findAll());
  }

  @GetMapping("/examples/{id}")
  public ResponseEntity<?> getExample(@PathVariable Long id) {
    Optional<Example> example = exampleService.findById(id);

    if (example.isPresent()) {
      return ResponseEntity.ok(example.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/examples")
  public ResponseEntity<?> addExample(@RequestBody @Valid Example example,
                                      UriComponentsBuilder uriComponentsBuilder) {
    example.setExampleId(null);

    example = exampleService.save(example);

    URI location = uriComponentsBuilder.path(String.format("/examples/%d", example.getExampleId())).build().toUri();
    return ResponseEntity.created(location).build();
  }

}
