package fi.hiq.reference.spring_boot_reference.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

// <#SCHEDULING>
@Component
@Slf4j
@DisallowConcurrentExecution
public class RarelyExecutingJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("This job runs rarely!");
  }
}
