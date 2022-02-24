package fi.hiq.reference.spring_boot_reference.config;

import fi.hiq.reference.spring_boot_reference.job.RarelyExecutingJob;
import fi.hiq.reference.spring_boot_reference.job.RepeatingExampleJob;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.JobFactory;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

// <#SCHEDULING>
@Configuration
public class SchedulingConfiguration {

  // Datasource is needed for setting Quartz to persist job data
  // Not needed if storing job data in memory
  @Resource
  private DataSource dataSource;
  @Resource
  private QuartzProperties quartzProperties;

  @Bean
  public JobDetail repeatingJobDetail() {
    return JobBuilder.newJob()
        .ofType(RepeatingExampleJob.class)
        .withIdentity("repeating_example_job")
        .storeDurably()
        .withDescription("Job that runs often")
        .build();
  }

  @Bean
  public JobDetail rarelyExecutingJobDetail() {
    return JobBuilder.newJob()
        .ofType(RarelyExecutingJob.class)
        .withIdentity("rarely_executing_example_job")
        .storeDurably()
        .withDescription("Job that runs rarely")
        .build();
  }

  @Bean
  public Trigger repeatingTrigger() {
    return TriggerBuilder.newTrigger()
        .forJob(repeatingJobDetail())
        .withIdentity("repeating_trigger_key")
        .withDescription("Trigger that triggers often")
        .withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(10))
        .build();
  }

  @Bean
  public Trigger rarelyTriggeredTrigger() {
    return TriggerBuilder.newTrigger()
        .forJob(rarelyExecutingJobDetail())
        .withIdentity("rare_trigger_key")
        .withDescription("Trigger that triggers rarely")
        .withSchedule(simpleSchedule().repeatForever().withIntervalInHours(12))
        .startAt(DateBuilder.futureDate(6, DateBuilder.IntervalUnit.HOUR))
        .build();
  }

  @Bean
  public SchedulerFactoryBean scheduler(JobFactory jobFactory) {
    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

    schedulerFactory.setJobFactory(jobFactory);
    schedulerFactory.setJobDetails(repeatingJobDetail(), rarelyExecutingJobDetail());
    schedulerFactory.setTriggers(repeatingTrigger(), rarelyTriggeredTrigger());

    // Apply additional Quartz properties
    Properties properties = new Properties();
    properties.putAll(quartzProperties.getProperties());
    schedulerFactory.setQuartzProperties(properties);

    // Need to set DataSource if persisting job data. Not needed if storing job data in memory.
    schedulerFactory.setDataSource(dataSource);

    return schedulerFactory;
  }

  @Bean
  public JobFactory springBeanJobFactory() {
    return new SpringBeanJobFactory();
  }
}
