package com.entradahealth.entrada.core.remote.exceptions;

/**
 * Thrown when "oh noes, the web service ain't happy".
 *
 * @author edr
 * @since 5 Sep 2012
 */
public class ServiceException extends Exception
{
    public ServiceException()
    {
    }

    
    public ServiceException(String s)
    {
        //super(s);
    }

    public ServiceException(String s, Throwable throwable)
    {
        //super(s, throwable);
    }

    public ServiceException(Throwable throwable)
    {
        //super(throwable);
    }
    
}
