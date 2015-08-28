package com.entradahealth.entrada.core.db;

/**
 * Runtime wrapper class around the JDBC setup failures, which we can't
 * really recover from (yet?) to avoid littering code with cleanup issues.
 *
 * @author edr
 * @since 28 Aug 2012
 */
public class H2SetupException extends RuntimeException
{
    public H2SetupException(String s)
    {
        super(s);
    }

    public H2SetupException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public H2SetupException(Throwable throwable)
    {
        super(throwable);
    }
}
