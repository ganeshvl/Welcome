package com.entradahealth.entrada.core.remote;

import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.entradahealth.entrada.core.domain.senders.UploadData;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Local service for reading JSON responses from the file system. Used in test.
 *
 * @author edr
 * @since 5 Sep 2012
 */
public class JsonLocalService implements RemoteService
{
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * The URI for the web server we're pulling data from.
     */
    public final URI uri;

    public JsonLocalService(String uri) throws URISyntaxException
    {
        this.uri = new URI(uri);
    }

    public SyncData retrieveServiceData() throws ServiceException
    {
        String scheme = this.uri.getScheme().toUpperCase();
        String jsonContent = null;

        try
        {
            Preconditions.checkArgument("FILE".equals(scheme), "uri must be a file:// uri");

            jsonContent = Files.toString(new File(uri.getPath()), Charsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException(e);
        }


        try {
            return mapper.readValue(jsonContent, SyncData.class);
        } catch (IOException e) {
            throw new ServiceException("Could not deserialize SyncData.", e);
        }
    }

    public void sendServiceData(UploadData data) throws ServiceException
    {

    }

    @Override
    public int getRemoteVersion() throws ServiceException
    {
        return 1;
    }

    @Override
    public boolean isValidHost()
    {
        return true;
    }

//    @Override
//    public boolean isValidAuthentication() throws ServiceException
//    {
//        return true;
//    }
	
	@Override
	public String authenticate(String Username, String Password, String UDID) throws ServiceException{
		return "";
	}
	
	@Override
	public void dictateAPIVersio()
    {

    }

	@Override
	public List<Dictator> getAssociatedDictators(String sessionToken)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Resource> getResourceNames(String sessionToken, String dictator)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}
}
