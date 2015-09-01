package com.expanset.hk2.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.errors.ExceptionAdapter;

@Service
@Contract
public class ScheduleDrivenService implements InstanceLifecycleListener, PreDestroy, JobFactory, JobListener {
	
    private final static Filter FILTER = new Filter() {

        @Override
        public boolean matches(Descriptor d) {
            return d.getQualifiers().contains(ScheduleDriven.class.getName());
        }
    };
	
    @Inject
	protected ServiceLocator serviceLocator;
    
    @Inject
    @Optional
    protected SchedulerFactory schedulerFactory;

    @Inject
    @Optional
    protected Configuration configuration;
    
    protected Scheduler scheduler;   
    
    protected final ConcurrentMap<Pair<JobKey, Class<? extends Job>>, ScheduleSubscription> subscriptions = new ConcurrentHashMap<>();
    
    private static final Logger log = LoggerFactory.getLogger(ScheduleDrivenService.class);
    
	@PostConstruct
	public void start() 
			throws Exception {
		if(schedulerFactory == null) {
			if(configuration != null) {
				schedulerFactory = new StdSchedulerFactory(
						ConfigurationConverter.getProperties(configuration));
			} else {
				schedulerFactory = new StdSchedulerFactory();
			}
			
		}
		scheduler = schedulerFactory.getScheduler();
		scheduler.setJobFactory(this);
		scheduler.getListenerManager().addJobListener(this);	
		scheduler.start();
	}
	
	@Override
	public void preDestroy() {
		ExceptionAdapter.run(() -> scheduler.shutdown(true));
	}		
    
	@Override
	public Filter getFilter() {
		return FILTER;
	}

	@Override
	public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
		if(lifecycleEvent.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION) {			
			ExceptionAdapter.run(() -> startListening(lifecycleEvent.getLifecycleObject()));
		} else if(lifecycleEvent.getEventType() == InstanceLifecycleEventType.PRE_DESTRUCTION) {
			stopListening(lifecycleEvent.getLifecycleObject());
		}
	}
	
	@Override
	public String getName() {
		return ScheduleDrivenService.class.getName();
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		log.info("jobExecutionVetoed, class: {}, nextRun: {}", 
				context.getJobDetail().getJobClass(), context.getNextFireTime());
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		log.info("jobToBeExecuted, class: {}, prevDate: {}", 
				context.getJobDetail().getJobClass(), context.getPreviousFireTime());
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException error) {
		if(error != null) {
			log.error("jobWasExecuted(failed), class: {}, elapsed: {}ms, nextRun: {}", 
					context.getJobDetail().getJobClass(), context.getJobRunTime(), context.getNextFireTime(), error);
		} else {
			log.info("jobWasExecuted, class: {}, elapsed: {}ms, nextRun: {}", 
					context.getJobDetail().getJobClass(), context.getJobRunTime(), context.getNextFireTime());
		}
	}

	@Override
	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler)
			throws SchedulerException {		
		final Pair<JobKey, Class<? extends Job>> key = Pair.of(
				 bundle.getJobDetail().getKey(),
				 bundle.getJobDetail().getJobClass());		
		
		final ScheduleSubscription subscription = subscriptions.get(key);
		return subscription;
	}	
		
	protected void startListening(Object scheduleDrivenObject) 
			throws SchedulerException {
		final ScheduleDriven ann = scheduleDrivenObject.getClass().getAnnotation(ScheduleDriven.class);
		
		@SuppressWarnings("unchecked")
		final Pair<JobKey, Class<? extends Job>> key = Pair.of(
				new JobKey(ann.name(), ann.group()), 
				(Class<? extends Job>)scheduleDrivenObject.getClass());
		
		@SuppressWarnings("unchecked")
		final ScheduleSubscription subscription = 
				subscriptions.computeIfAbsent(key, (currentKey) -> 
					ExceptionAdapter.get(() -> 
						new ScheduleSubscription(ann, (Class <? extends Job>)scheduleDrivenObject.getClass())));
		subscription.add((Job)scheduleDrivenObject);
	}

	protected void stopListening(Object scheduleDrivenObject) {
		final ScheduleDriven ann = scheduleDrivenObject.getClass().getAnnotation(ScheduleDriven.class);
		
		@SuppressWarnings("unchecked")
		final Pair<JobKey, Class<? extends Job>> key = Pair.of(
				new JobKey(ann.name(), ann.group()), 
				(Class<? extends Job>)scheduleDrivenObject.getClass());
		
		subscriptions.computeIfPresent(key, (currentKey, subscription) -> {
			if(subscription != null) {
				subscription.remove((Job)scheduleDrivenObject);
				if(subscription.hasJobs()) {
					return subscription;
				}	
				ExceptionAdapter.run(() -> subscription.close());
			}
			return null;
		});
	}
	
	protected class ScheduleSubscription implements Job {
		
		protected final JobKey jobKey;
		
		protected final Class <? extends Job> jobClass;
		
		protected final List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

		public ScheduleSubscription(ScheduleDriven ann, Class <? extends Job> jobClass) 
				throws SchedulerException {
			this.jobKey = new JobKey(ann.name(), ann.group());
			this.jobClass = jobClass;
			
			String cronSchedule = ann.expression();
			if(StringUtils.isEmpty(cronSchedule)) {
				if(configuration == null) {
					throw new IllegalStateException("Need o use Configuration");
				}
				cronSchedule = configuration.getString(ann.expressionProperty());
			}
			
			final JobDetail jobDetail = JobBuilder.newJob(jobClass) 
				    .withIdentity(ann.name(), ann.group()) 
				    .build(); 
			final Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(ann.name() + "Trigger", ann.group()) 
					.startNow() 
					.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))            
					.build(); 
			final Date nextStart = scheduler.scheduleJob(jobDetail, trigger);
			
			log.debug("Added schedule driven object {}, name: {}, group: {}, nextStart: {}",
					jobClass, ann.name(), ann.group(), nextStart);
		}

		public void close() 
				throws SchedulerException {
			scheduler.deleteJob(jobKey);
			
			log.debug("Removed schedule driven object {}, name: {}, group: {}",
					jobClass, jobKey.getName(), jobKey.getGroup());
		}

		public boolean hasJobs() {
			return jobs.size() != 0;
		}
		
		public void add(Job job) {
			jobs.add(job);
		}

		public void remove(Job job) {
			jobs.remove(job);
		}

		@Override
		public void execute(JobExecutionContext context)
				throws JobExecutionException {
			final Job[] jobsToExecute = jobs.toArray(new Job[0]);
			for (int i = 0; i < jobsToExecute.length; i++) {
				jobsToExecute[i].execute(context);
			}
		}		
	} 
}
