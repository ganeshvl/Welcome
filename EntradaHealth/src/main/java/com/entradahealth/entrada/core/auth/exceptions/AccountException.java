package com.entradahealth.entrada.core.auth.exceptions;

/**
 * SOMEONE SET US UP THE...account...BOMB.
 *
 * @author edr
 * @since 13 Sep 2012
 */
public class AccountException extends Exception
{
    public AccountException()
    {
    }

    public AccountException(String s)
    {
        super(s);
    }

    public AccountException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public AccountException(Throwable throwable)
    {
        super(throwable);
    }
}
