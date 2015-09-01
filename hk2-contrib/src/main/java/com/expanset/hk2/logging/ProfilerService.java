package com.expanset.hk2.logging;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Service for profile methods or parts of code.
 * <p>Default MDC key name is 'opId'. You may use it in log record, there is a logback sample:</p>
 * <pre>
 * %d{HH:mm:ss.SSS} [%thread] %X{opId} %-5level %logger{36} - %msg%n
 * </pre>
 */
@Service
@Contract
public class ProfilerService {

	protected final ProfilerServiceConfig config;

	protected final static AtomicLong longCorrelationId = new AtomicLong();	
	
	protected final static Logger defaultLog = LoggerFactory.getLogger(ProfilerService.class);
	
	@Inject
	public ProfilerService(@Optional ProfilerServiceConfig config) {
		if(config == null) {
			config = new ProfilerServiceConfig();
		}
		this.config = config;
	}	

	/**
	 * Starts new profiling scope (writes 'start' and 'complete' of current scope).
	 * @param log Log to write profiling output.
	 * @param name Name of operation.
	 * @param logLevel Log level to output log records.
	 * @return Scope control.
	 */
	public AutoCloseable startScope(
			@Nullable Logger log,
			@Nonnull String name,
			@Nullable LogLevel logLevel) {
		return new ProfilerScope(
				log,
				name,
				logLevel,
				null,
				null,
				false);
	}
	
	/**
	 * Starts new profiling scope (writes 'start' and 'complete' of current scope with specified ID for MDC).
	 * @param log Log to write profiling output.
	 * @param name Name of operation.
	 * @param logLevel Log level to output log records.
	 * @param correlationId Current ID of operation.
	 * @param measureDuration true - measure duration of current operation.
	 * @return Scope control.
	 */
	public AutoCloseable startScope(
			@Nullable Logger log,
			@Nonnull String name,
			@Nullable LogLevel logLevel,
			@Nullable Object correlationId,
			boolean measureDuration) {
		return new ProfilerScope(
				log,
				name,
				logLevel,
				correlationId,
				null,
				measureDuration);
	}	
	
	/**
	 * Starts new profiling scope (writes 'start' and 'complete' of current scope with generated ID for MDC).
	 * @param log Log to write profiling output.
	 * @param name Name of operation.
	 * @param logLevel Log level to output log records.
	 * @param correlationIdType Type of operation ID for creation if necessary.
	 * @return Scope control.
	 */
	public AutoCloseable startScope(
			@Nullable Logger log,
			@Nonnull String name,
			@Nullable LogLevel logLevel,
			@Nullable CorrelationIdType correlationIdType) {
		return new ProfilerScope(
				log,
				name,
				logLevel,
				null,
				correlationIdType,
				false);
	}

	/**
	 * Starts new profiling scope (writes 'start' and 'complete' of current scope with generated ID for MDC).
	 * @param log Log to write profiling output.
	 * @param name Name of operation.
	 * @param logLevel Log level to output log records.
	 * @param correlationIdType Type of operation ID for creation if necessary.
	 * @param measureDuration true - measure duration of current operation.
	 * @return Scope control.
	 */	
	public AutoCloseable startScope(
			@Nullable Logger log,
			@Nonnull String name,
			@Nullable LogLevel logLevel,
			@Nullable CorrelationIdType correlationIdType,
			boolean measureDuration) {
		return new ProfilerScope(
				log,
				name,
				logLevel,
				null,
				correlationIdType,
				measureDuration);
	}

	/**
	 * Starts new profiling scope.
	 * @param log Log to write profiling output.
	 * @param name Name of operation.
	 * @param logLevel Log level to output log records.
	 * @param correlationId Current ID of operation.
	 * @param correlationIdType Type of operation ID for creation if necessary.
	 * @param measureDuration true - measure duration of current operation.
	 * @return Scope control.
	 */
	public AutoCloseable startScope(
			@Nullable Logger log,
			@Nonnull String name,
			@Nullable LogLevel logLevel,
			@Nullable Object correlationId,
			@Nullable CorrelationIdType correlationIdType,
			boolean measureDuration) {
		return new ProfilerScope(
				log,
				name,
				logLevel,
				correlationId,
				correlationIdType,
				measureDuration);
	}
	
	protected class ProfilerScope implements AutoCloseable {
		
		protected final Logger log;
		
		protected final String name;
		
		protected final LogLevel logLevel; 
		
		protected final CorrelationIdType correlationIdType;
		
		protected final boolean useMDC;
		
		protected final boolean measureDuration;
		
		protected final String prevCorrelationId;

		protected final StopWatch stopWatch;
		
		public ProfilerScope(
				Logger log,
				String name,
				LogLevel logLevel,
				Object correlationId,
				CorrelationIdType correlationIdType,
				boolean measureDuration) {
			this.log = log; 
			this.name = name;
			this.logLevel = logLevel;
			this.correlationIdType = correlationIdType;
			this.measureDuration = measureDuration;

			if(correlationId == null) {
				if(correlationIdType != null && correlationIdType != CorrelationIdType.NONE) {
					if(correlationIdType == CorrelationIdType.SIMPLE) {
						correlationId = longCorrelationId.addAndGet(1);
					} else if(correlationIdType == CorrelationIdType.UUID) {
						correlationId = UUID.randomUUID();
					}
				}
			}
			
			if(correlationId != null) {
				useMDC = true;
				prevCorrelationId = MDC.get(config.getMDCKey());
				if(StringUtils.isNotEmpty(prevCorrelationId)) {
					MDC.put(config.getMDCKey(), prevCorrelationId + " > " + correlationId.toString());
				} else {
					MDC.put(config.getMDCKey(), correlationId.toString());
				}
			} else {
				useMDC = false;
				prevCorrelationId = null;
			}
			
			if(logLevel == null || logLevel == LogLevel.DEBUG) {
				logLevel = config.getDefaultLogLevel();
			}
			
			if(log == null) {
				log = defaultLog;
			}
			
			if(logLevel == LogLevel.TRACE) {
				log.trace(config.getStartPrint(), name);
			} else if(logLevel == LogLevel.DEBUG) {
				log.debug(config.getStartPrint(), name);
			}  else if(logLevel == LogLevel.INFO) {
				log.info(config.getStartPrint(), name);
			}
			
			if(measureDuration) {
				stopWatch = new StopWatch();
				stopWatch.start();
			} else {
				stopWatch = null;
			}
		}
		
		@Override
		public void close() 
				throws Exception {
			
			if(stopWatch != null) {
				stopWatch.stop();
			}
			
			if(useMDC) {
				if(stopWatch != null) {
					if(logLevel == LogLevel.TRACE) {
						log.trace(config.getMeasureDurationPrint(), stopWatch.getTime());
					} else if(logLevel == LogLevel.DEBUG) {
						log.debug(config.getMeasureDurationPrint(), stopWatch.getTime());
					}  else if(logLevel == LogLevel.INFO) {
						log.info(config.getMeasureDurationPrint(), stopWatch.getTime());
					}					
				} else {
					if(logLevel == LogLevel.TRACE) {
						log.trace(config.getCompletedPrint());
					} else if(logLevel == LogLevel.DEBUG) {
						log.debug(config.getCompletedPrint());
					}  else if(logLevel == LogLevel.INFO) {
						log.info(config.getCompletedPrint());
					}										
				}
			} else {
				if(stopWatch != null) {
					if(logLevel == LogLevel.TRACE) {
						log.trace(config.getMeasureDurationNoMDCPrint(), name, stopWatch.getTime());
					} else if(logLevel == LogLevel.DEBUG) {
						log.debug(config.getMeasureDurationNoMDCPrint(), name, stopWatch.getTime());
					}  else if(logLevel == LogLevel.INFO) {
						log.info(config.getMeasureDurationNoMDCPrint(), name, stopWatch.getTime());
					}					
				} else {
					if(logLevel == LogLevel.TRACE) {
						log.trace(config.getCompletedNoMDCPrint(), name);
					} else if(logLevel == LogLevel.DEBUG) {
						log.debug(config.getCompletedNoMDCPrint(), name);
					}  else if(logLevel == LogLevel.INFO) {
						log.info(config.getCompletedNoMDCPrint(), name);
					}										
				}
			}
			
			if(useMDC) {
				if(StringUtils.isNotEmpty(prevCorrelationId)) {
					MDC.put(config.getMDCKey(), prevCorrelationId);
				} else {
					MDC.remove(config.getMDCKey());
				}
			}
		}
	} 
}
