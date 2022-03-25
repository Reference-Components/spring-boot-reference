package fi.hiq.reference.spring_boot_reference.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ClockServiceTest {
  @Resource
  private ClockService clockService;

  @Test
  void testNow() {
    Instant beforeNow = Instant.now();
    Instant now = clockService.now();
    Instant afterNow = Instant.now();

    assertNotNull(now);
    assertFalse(beforeNow.isAfter(now));
    assertFalse(afterNow.isBefore(now));
  }

  @Test
  void testNowDate() {
    Date beforeNow = new Date();
    Date now = clockService.nowDate();
    Date afterNow = new Date();

    assertNotNull(now);
    assertFalse(beforeNow.after(now));
    assertFalse(afterNow.before(now));
  }

}