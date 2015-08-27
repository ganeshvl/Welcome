package com.entradahealth.entrada.core.remote;

import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.core.domain.senders.UploadData;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;

/**
 * Base interface for the remote API.
 *
 * @author edr
 * @since 5 Sep 2012
 */
public interface RemoteService
{
    /**
     *
     * @throws com.entradahealth.entrada.core.remote.exceptions.ServiceException
     */
    SyncData retrieveServiceData() throws ServiceException;
    void sendServiceData(UploadData data) throws ServiceException;

    int getRemoteVersion() throws ServiceException;

    boolean isValidHost();
    //boolean isValidAuthentication() throws ServiceException;
    String authenticate(String Username, String Password, String UDID) throws ServiceException;
    void dictateAPIVersio();
	List<Dictator> getAssociatedDictators(String sessionToken) throws ServiceException;
	
	// Method to get the Resource details used in the schedule module.
	List<Resource> getResourceNames(String sessionToken, String dictator) throws ServiceException;
}
