package com.expanset.hk2.i18n;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.RememberOptions;

/**
 * Controls {@link Locale}  lifecycle, bind {@link Locale} to the current thread.
 */
@Service
public class ThreadScopeLocaleManager implements LocaleManager {

	protected final ThreadLocal<Locale> localeInThread = new ThreadLocal<>(); 
	
	@Override
	public AutoCloseable beginScope(@Nonnull Locale locale) {
		Validate.notNull(locale, "locale");
		
		localeInThread.set(locale);
		
		return new AutoCloseable() {
			@Override
			public void close() 
					throws Exception {
				localeInThread.remove();
			}
		};	
	}
	
	@Override
	public Locale getCurrentLocale() {
		return localeInThread.get();
	}
	
	@Override
	public void saveLocale(@Nonnull Locale locale, @Nullable RememberOptions rememberOptions) {
		// It isn't necessary save locale for the current thread.
	}

	@Override
	public void removeLocale(@Nullable RememberOptions rememberOptions) {
		// It isn't necessary save locale for the current thread.
	}
}
