package fi.hiq.reference.spring_boot_reference.service.impl;

import fi.hiq.reference.spring_boot_reference.service.ClockService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class ClockServiceImpl implements ClockService {

  @Override
  public Instant now() {
    return Instant.now();
  }

  @Override
  public Date nowDate() {
    return Date.from(this.now());
  }

}
