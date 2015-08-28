package com.entradahealth.entrada.core.json;

/**
 * Thrown if the JSON we're parsing is unexpectedly wacky.
 *
 * @author edr
 * @since 29 Aug 2012
 */
public class JsonFormatException extends Exception
{
    public JsonFormatException()
    {
    }

    public JsonFormatException(String s)
    {
        super(s);
    }

    public JsonFormatException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public JsonFormatException(Throwable throwable)
    {
        super(throwable);
    }
}
