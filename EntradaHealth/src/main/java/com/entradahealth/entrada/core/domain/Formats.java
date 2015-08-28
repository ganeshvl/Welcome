package com.entradahealth.entrada.core.domain;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Contains date, number, etc. formatters for domain objects.
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class Formats
{
    private Formats() { }

    /**
     * The MM/DD/YYYY format used in our DOB stuff.
     */
    public static final DateTimeFormatter DATE_OF_BIRTH =
            DateTimeFormat.shortDate();
}
