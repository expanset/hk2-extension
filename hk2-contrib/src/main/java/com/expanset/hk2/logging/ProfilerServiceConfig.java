package com.expanset.hk2.logging;

/**
 * {@link ProfilerService} configuration.
 */
public class ProfilerServiceConfig {

	public ProfilerServiceConfig() {
	}

	/**
	 * @return Name of MCD key, default - 'opId'.
	 */
	public String getMDCKey() {
		return mdcKey;
	}

	public void setMDCKey(String mdcKey) {
		this.mdcKey = mdcKey;
	}

	/**
	 * @return Log record pattern for starting profile scope.
	 */
	public String getStartPrint() {
		return startPrint;
	}

	public void setStartPrint(String startPrint) {
		this.startPrint = startPrint;
	}

	/**
	 * @return Log record pattern for completing profile scope.
	 */	
	public String getCompletedPrint() {
		return completedPrint;
	}

	public void setCompletedPrint(String completedPrint) {
		this.completedPrint = completedPrint;
	}

	/**
	 * @return Log record pattern for completing profile scope without using MDC.
	 */	
	public String getCompletedNoMDCPrint() {
		return completedNoMDCPrint;
	}

	public void setCompletedNoMDCPrint(String completedNoMDCPrint) {
		this.completedNoMDCPrint = completedNoMDCPrint;
	}

	/**
	 * @return Log record pattern for completing measured profile scope.
	 */			
	public String getMeasureDurationPrint() {
		return measureDurationPrint;
	}

	public void setMeasureDurationPrint(String measureDurationPrint) {
		this.measureDurationPrint = measureDurationPrint;
	}	
	
	/**
	 * @return Log record pattern for completing measured profile scope without using MDC.
	 */		
	public String getMeasureDurationNoMDCPrint() {
		return measureDurationNoMDCPrint;
	}

	public void setMeasureDurationNoMDCPrint(String measureDurationNoMDCPrint) {
		this.measureDurationNoMDCPrint = measureDurationNoMDCPrint;
	}

	/**
	 * @return Default log level for profile operations (default  - DEBUG).
	 */
	public LogLevel getDefaultLogLevel() {
		return defaultLogLevel;
	}

	public void setDefaultLogLevel(LogLevel defaultLogLevel) {
		this.defaultLogLevel = defaultLogLevel;
	}

	private String mdcKey = "opId";

	private String startPrint = "start {}";
	
	private String completedPrint = "completed";
	
	private String completedNoMDCPrint = "completed {}";
	
	private String measureDurationPrint = "completed, elapsed: {}ms";
	
	private String measureDurationNoMDCPrint = "completed {}, elapsed: {}ms";
	
	private LogLevel defaultLogLevel = LogLevel.DEBUG;
}
