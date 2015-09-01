package com.expanset.hk2.logging;

/**
 * Log level for log output.
 */
public enum LogLevel {

	/**
	 * Use default log level from {@link ProfilerServiceConfig}.
	 */
	DEFAULT,
	
	/**
	 * Log with INFO level.
	 */
	INFO,
	
	/**
	 * Log with DEBUG level.
	 */
	DEBUG,
	
	/**
	 * Log with TRACE level.
	 */
	TRACE
}
