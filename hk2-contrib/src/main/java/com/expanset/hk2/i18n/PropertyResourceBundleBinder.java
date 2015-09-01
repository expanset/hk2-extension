package com.expanset.hk2.i18n;

import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Injection binder for using {@link ResourceBundle} loaded from properties file (like java.properties).
 */
public class PropertyResourceBundleBinder extends ResourceBundleBinder {

	protected final String fileName;
	
	protected final String encoding;
		
	protected final long timeToLive;

	private static final String DEFAULT_ENCODING = "utf-8";
	
	/**
	 * @param fileName Path and file name of file with resources.
	 * @param encoding File encoding.
     * @param timeToLive Time (milliseconds) through which verification of the file on need of reset is allowed.
	 */
	public PropertyResourceBundleBinder(
			@Nonnull String fileName,
			long timeToLive,
			@Nullable String encoding) {
		Validate.notEmpty(fileName, "fileName");
				
		this.fileName = fileName;
		this.encoding = StringUtils.isNotEmpty(encoding) ? encoding : DEFAULT_ENCODING;
		this.timeToLive = timeToLive;
	}
	
	@Override
	protected void configure() {
		super.configure();
		
		final String baseName = 
				Paths.get(fileName).getFileName().toString();
		final ResourceBundle.Control control = 
				createResourceBundleControl(fileName, encoding, timeToLive);
		
		bind(createResourceBundleProvider(baseName, control))
			.to(ResourceBundleProvider.class);
	}

	protected ResourceBundle.Control createResourceBundleControl(
			@Nonnull String fileName, 
			@Nullable String encoding, 
			long timeToLive) {
		Validate.notEmpty(fileName, "fileName");

		return new PropertyResourceBundleControl(
				Paths.get(fileName).getParent().toString(), encoding, timeToLive);
	}
	
	protected ResourceBundleProvider createResourceBundleProvider(
			@Nonnull String baseName,
			@Nonnull ResourceBundle.Control control) {
		Validate.notEmpty(baseName, "baseName");
		Validate.notNull(control, "control");

		return new DefaultResourceBundleProvider(baseName, control);
	}
}
