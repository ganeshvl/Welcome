package com.entradahealth.entrada.core.auth.exceptions;

/**
 * As might be a little bit obvious from the name, this exception
 * is thrown if a user is invalid: missing user account, invalid
 * file formats, etc.
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class UserLoadException extends Exception
{
    public UserLoadException()
    {
    }

    public UserLoadException(String s)
    {
        super(s);
    }

    public UserLoadException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public UserLoadException(Throwable throwable)
    {
        super(throwable);
    }
}
