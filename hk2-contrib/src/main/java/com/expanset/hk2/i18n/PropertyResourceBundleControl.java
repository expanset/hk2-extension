package com.expanset.hk2.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ResourceBundle.Control}, that loads {@link ResourceBundle} from properties file (like java.properties).
 * <p>Reloading in case of the configuration file change is supported.</p>
 */
public class PropertyResourceBundleControl extends ResourceBundle.Control {

	protected final String resourceFolder;
    
    protected final String encoding;

    protected final long timeToLive;
    
    private final static Logger log = LoggerFactory.getLogger(PropertyResourceBundleControl.class);

    /**
     * @param resourceFolder Folder with resource files.
     * @param encoding File encoding.
     * @param timeToLive Time (milliseconds) through which verification of the file on need of reset is allowed.
     */
    public PropertyResourceBundleControl(
    		@Nonnull String resourceFolder, 
    		@Nullable String encoding, 
    		long timeToLive) {
		Validate.notEmpty(resourceFolder, "resourceFolder");
    	
    	this.resourceFolder = resourceFolder;
        this.encoding = encoding;
        this.timeToLive = timeToLive;
    }

    @Override
    public ResourceBundle newBundle(
    		String baseName, 
    		Locale locale,
    		String format, 
    		ClassLoader loader,
    		boolean reload)
    				throws IllegalAccessException, InstantiationException, IOException {
		if(locale == null) {
			locale = Locale.getDefault();
		}
    	
        final Path resourceFullName = getResourceFullName(baseName, locale);
        if(!Files.exists(resourceFullName)) {
        	log.trace("Resource file {} not found", resourceFullName);
        	
        	return null;
        }
        
        final PropertyResourceBundle resourceBundle = 
        		new PropertyResourceBundle(
        				new InputStreamReader(Files.newInputStream(resourceFullName), encoding));
        
        log.trace("Resources loaded form file {}", resourceFullName);
        
        return resourceBundle;
    }
    
	@Override
	public boolean needsReload(
			String baseName, 
			Locale locale, 
			String format,
			ClassLoader loader, 
			ResourceBundle bundle, 
			long loadTime) {
		if(locale == null) {
			locale = Locale.getDefault();
		}
		
		try {
			final Path resourceFullName = 
					getResourceFullName(baseName, locale);
			final BasicFileAttributeView attributes = 
					Files.getFileAttributeView(resourceFullName, BasicFileAttributeView.class);
			final long lastModified = 
					attributes.readAttributes().lastModifiedTime().toMillis();
			final boolean needReload = 
					lastModified >= loadTime;

			log.trace("Resource file {} need reloading", resourceFullName);
			
			return needReload;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getTimeToLive(String baseName, Locale locale) {
		return timeToLive;
	}

	private Path getResourceFullName(String baseName, Locale locale) {
		assert StringUtils.isNotEmpty(baseName);
		assert locale != null;
		
		final String bundleName = toBundleName(baseName, locale);
        final String resourceName = toResourceName(bundleName, "properties");
		
        return Paths.get(resourceFolder, resourceName);
	}
}
