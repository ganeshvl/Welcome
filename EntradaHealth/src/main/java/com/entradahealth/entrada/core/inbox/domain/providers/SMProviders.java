package com.entradahealth.entrada.core.inbox.domain.providers;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.joda.time.Instant;

/**
 * Internal helper class for shared code for providers.
 */
final class SMProviders {
	private SMProviders() {
	}

	public static final Splitter SEARCH_TEXT_SPLITTER = Splitter.on(" ")
			.omitEmptyStrings().trimResults();

	public static final Joiner COMMA_JOINER = Joiner.on(',').skipNulls();

	public static final Joiner SQL_OR_JOINER = Joiner.on(" OR ").skipNulls();

    public static Long makeTempEncounterId()
    {
        return -Instant.now().getMillis();
    }
    public static String makeTempJobNumber()
    {
        return "T" + String.valueOf(Instant.now().getMillis());
    }
}
