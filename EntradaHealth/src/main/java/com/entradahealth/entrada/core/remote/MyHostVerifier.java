package com.entradahealth.entrada.core.remote;

import javax.net.ssl.SSLException;

public class MyHostVerifier extends org.apache.http.conn.ssl.AbstractVerifier {

    String[] allowHost = {"apientrada.quickblox.com", "api.quickblox.com", "dictateapi.entradahealth.net", "moosejaw.entradahealth.net", "dictateapi-qa.entradahealth.net", "dictateapi-sales.entradahealth.net", "dictateapi-qa2.entradahealth.net"}; 

    @Override

    public void verify(String host, String[] cns, 
	String[] subjectAlts) throws SSLException {
        // If the host is any the hosts to be allowed, return, else throw exception 
        for (int i=0; i < allowHost.length; i++) {
             if (host == allowHost[i])
                return;
        }
         //throw SSLException;
    }
}
