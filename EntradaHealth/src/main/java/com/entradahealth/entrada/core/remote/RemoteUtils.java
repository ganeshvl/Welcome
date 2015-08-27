package com.entradahealth.entrada.core.remote;

import com.entradahealth.entrada.core.collect.Pair;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: ed
 * Date: 10/11/12
 * Time: 4:38 PM
 * To change this template use File | Settings | File Templates.
 */
public final class RemoteUtils
{
    private RemoteUtils() {}

    private static HttpURLConnection postConnection(URL url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        return conn;
    }

    public static Pair<Integer, String> doPostConnectionWithJson(URL url,
                                                                 JsonNode json) throws ServiceException
    {
        try
        {
            String content = json.toString();

            HttpURLConnection conn = postConnection(url);

            conn.setFixedLengthStreamingMode(content.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(content);
            out.close();

            int code = conn.getResponseCode();
            String str;
            try
            {
                str = CharStreams.toString(new InputStreamReader(conn.getInputStream(), Charsets.UTF_8));
            }
            catch (Exception ex)
            {
                str = null;
            }

            return new Pair<Integer, String>(code, str);
        }
        catch (Exception ex)
        {
            throw new ServiceException(ex);
        }
    }
}
