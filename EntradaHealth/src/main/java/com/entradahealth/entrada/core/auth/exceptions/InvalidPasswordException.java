package com.entradahealth.entrada.core.auth.exceptions;

/**
 * Thrown when a given password fails to decrypt an H2 database
 * (in our primary case, because the user put in the wrong PIN).
 *
 * @author edr
 * @since 28 Aug 2012
 */
public class InvalidPasswordException extends Exception
{
    public InvalidPasswordException()
    {
    }

    public InvalidPasswordException(String s)
    {
        super(s);
    }

    public InvalidPasswordException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public InvalidPasswordException(Throwable throwable)
    {
        super(throwable);
    }
}
