package com.expanset.hk2.i18n;

import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.jvnet.hk2.annotations.Contract;

import com.expanset.common.RememberOptions;
import com.expanset.common.errors.ExceptionAdapter;

/**
 * Controls {@link Locale} lifecycle.
 */
@Contract
public interface LocaleManager {
	
	/**
	 * Start {@link Locale} scope.
	 * @param locale {@link Locale} to use in current scope.
	 * @return Scope control.
	 */
	public AutoCloseable beginScope(@Nonnull Locale locale);
	
	/**
	 * Call code with the specified {@link Locale}.
	 * @param locale {@link Locale} to use in the current scope.
	 * @param runnable Code to run in the current scope. 
	 */
	default void run(@Nonnull Locale locale, @Nonnull Runnable runnable) {
		Validate.notNull(locale, "locale");
		Validate.notNull(runnable, "runnable");
		
		ExceptionAdapter.run(() -> {
			try(AutoCloseable scope = beginScope(locale)) {
				runnable.run();
			}
		});
	}

	/**
	 * Call code with specified {@link Locale}.
	 * @param locale {@link Locale} to use in the current scope.
	 * @param supplier Code to run in the current scope.
	 * @return Result of code run. 
	 * @param <T> Type of returning value.
	 */
	default <T> T get(@Nonnull Locale locale, @Nonnull Supplier<T> supplier) {
		Validate.notNull(locale, "locale");
		Validate.notNull(supplier, "supplier");
		
		return ExceptionAdapter.get(() -> {
			try(AutoCloseable scope = beginScope(locale)) {
				return supplier.get();
			}
		});
	}
	
	/**
	 * @return {@link Locale} in the current session.
	 */
	public Locale getCurrentLocale();

	/**
	 * Bind {@link Locale} to current session.
	 * @param locale {@link Locale} to bind in the session.
	 * @param rememberOptions Options how to remember {@link Locale} in the session.
	 */
	void saveLocale(@Nonnull Locale locale, @Nullable RememberOptions rememberOptions);
	
	/**
	 * Remove {@link Locale} from current session.
	 * @param rememberOptions Options how to remember {@link Locale} in the session.
	 */
	void removeLocale(@Nullable RememberOptions rememberOptions);
		 
	/**
	 * Bind {@link Locale} to the current session.
	 * @param locale {@link Locale} to bind in the session.
	 */
	default void saveLocale(@Nonnull Locale locale) {
		saveLocale(locale, null);
	}

	/**
	 * Remove {@link Locale} from the current session.
	 */
	default void removeLocale() {
		removeLocale(null);
	}
}
