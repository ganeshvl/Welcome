package com.entradahealth.entrada.android.app.personal.utils;

/**
 * Test exception just for ACRA.
 */
public class DebuggingException extends RuntimeException
{
    public DebuggingException()
    {
    }

    public DebuggingException(String detailMessage)
    {
        super(detailMessage);
    }

    public DebuggingException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

    public DebuggingException(Throwable throwable)
    {
        super(throwable);
    }
}
