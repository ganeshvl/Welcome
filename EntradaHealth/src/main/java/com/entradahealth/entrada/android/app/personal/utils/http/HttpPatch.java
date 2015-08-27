package com.entradahealth.entrada.android.app.personal.utils.http;

import org.apache.http.client.methods.HttpPut;

import java.net.URI;

public class HttpPatch extends HttpPut
{
    public HttpPatch()
    {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public HttpPatch(URI uri)
    {
        super(uri);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public HttpPatch(String uri)
    {
        super(uri);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getMethod()
    {
        return "PATCH";
    }
}
