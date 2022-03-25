package fi.hiq.reference.spring_boot_reference.service;

import java.time.Instant;
import java.util.Date;

public interface ClockService {
  Instant now();

  Date nowDate();
}
