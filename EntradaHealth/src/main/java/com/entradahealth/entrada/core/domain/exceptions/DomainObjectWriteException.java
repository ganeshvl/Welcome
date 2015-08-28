package com.entradahealth.entrada.core.domain.exceptions;

/**
 * Thrown in the regrettable circumstance of a DomainObjectWriter failing
 * to write domain objects to the thingy.
 *
 * @author edr
 * @since 5 Sep 2012
 */
public class DomainObjectWriteException extends Exception
{
    public DomainObjectWriteException()
    {
    }

    public DomainObjectWriteException(String s)
    {
        super(s);
    }

    public DomainObjectWriteException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public DomainObjectWriteException(Throwable throwable)
    {
        super(throwable);
    }
}
