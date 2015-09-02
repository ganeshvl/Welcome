package com.entradahealth.entrada.android.app.personal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import android.util.Log;

import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DatabaseProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDatabaseProvider;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectProvider;
import com.google.common.collect.ImmutableMap;

/**
 * Encapsulates all "user" data that will be stored in the AndroidState,
 * allowing for easier synchronization while providing all information.
 * Whoever holds the lock on the object returned by AndroidState.getUserState()
 * can do stuff. Whoever doesn't, can't. The methods in this class are not
 * synchronized precisely for that reason: consumers should lock it, rather
 * than risk it locking itself, because critical sections will be larger than
 * a single method.
 *
 * This class is immutable because I'd rather see it destroyed and recreated
 * in cases of extreme weirdness (specifically, in the case of adding or deleting
 * accounts; a small wait time for the user is better than having to balance potentially
 * dangerous issues).
 *
 * @author edr
 * @since 5 Nov 2012
 */
public class UserState
{
    private UserPrivate userData;
    private Map<Account, DatabaseProvider> accountDatabases;
    private Map<String, Account> accountNames;
    private Map<String, SMDatabaseProvider> usersDatabases;

    private boolean disposed = false;
    private EntradaApplication application;

    public UserState(UserPrivate user) throws AccountException, InvalidPasswordException, DomainObjectWriteException {
        userData = user;
        application = (EntradaApplication) EntradaApplication.getAppContext();
        String dictatorId = application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID);
        Collection<Account> accounts = userData.getAccounts();

        accountDatabases = new HashMap<Account, DatabaseProvider>();
        accountNames = new HashMap<String, Account>();
        for (Account a : accounts)
        {
            	if(a.getName().equals(dictatorId)) {
		            DatabaseProvider provider = new DatabaseProvider(userData.openAccountDatabase(a.getName()));
		            accountDatabases.put(a, provider);
		            accountNames.put(a.getName(), a);
            }
        }
    }

    public void addAccount(String dictator){
        Collection<Account> accounts = userData.getAccounts();
    	for (Account a : accounts)
        {
				try {
					if(a.getName().equals(dictator)) {
						DatabaseProvider provider = new DatabaseProvider(userData.openAccountDatabase(a.getName()));
			            accountDatabases.put(a, provider);
			            accountNames.put(a.getName(), a);
					}
				} catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AccountException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidPasswordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
    }
    
    public void setSMUser() throws DomainObjectWriteException, AccountException, InvalidPasswordException{
        EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
        String userName = application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN);
       	SMDatabaseProvider smprovider = new SMDatabaseProvider(userData.openUserDatabase(userName));
        ImmutableMap.Builder<String, SMDatabaseProvider> smbuilder = ImmutableMap.builder();
       	smbuilder.put(userName, smprovider);
       	Log.d("","smbuilder--"+ userName + " smprovider"+  smprovider);
        usersDatabases = smbuilder.build();
       	Log.d("","usersDatabases.size--"+ usersDatabases.size()+"--"+userName);
    }
    
    public UserPrivate getUserData()
    {
        return userData;
    }

    public Collection<String> getAccountNames()
    {
        return accountNames.keySet();
    }

    public Collection<Account> getAccounts()
    {
        return accountDatabases.keySet();
    }

    @Nullable
    public DomainObjectProvider getProvider(String name)
    {
        Account a = accountNames.get(name);
        return (a != null) ? getProvider(a) : null;
    }

    public SMDomainObjectProvider getSMProvider(String userName){
    	try {
        	Log.d("",""+ "username--"+userName);
        	Log.d("","userdatabases--"+ usersDatabases.size() + " userdatabases--"+usersDatabases.get(userName));
        	return usersDatabases.get(userName);
		} catch (Exception e) {
			try {
				setSMUser();
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			return usersDatabases.get(userName);
		}
    }
    
    @Nullable
    public DomainObjectProvider getProvider(Account account)
    {
        return accountDatabases.get(account);
    }

    @Nullable
    public Account getAccount(String name)
    {
        return accountNames.get(name);
    }

    public Account getCurrentAccount()
    {
        String currentAccount = this.getUserData().getStateValue(UserPrivate.StateKeys.CURRENT_ACCOUNT);

        Collection<String> names = this.getAccountNames();
        if (currentAccount == null || !names.contains(currentAccount))
        {
            //Preconditions.checkState(names.size() > 0, "no accounts when current account requested!");
        	if(names.size()==0){
        		return null;
        	}

            try{
            	EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
            	currentAccount = application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID);
            } catch(Exception ex){
            	currentAccount = names.iterator().next();         	
            }
            
        }

        this.getUserData().setStateValue(UserPrivate.StateKeys.CURRENT_ACCOUNT, currentAccount);

        return this.accountNames.get(currentAccount);
    }

    public void setCurrentAccount(String accountName)
    {
        setCurrentAccount(getAccount(accountName));
    }
    public void setCurrentAccount(Account account)
    {
        this.getUserData().setStateValue(UserPrivate.StateKeys.CURRENT_ACCOUNT, account.getName());
        try
        {
            this.getUserData().save();
        }
        catch (Exception ex)
        {
            Log.e("Entrada-UserState", "Exception in setCurrentAccount: ", ex);
        }
    }

    public void doDebugDump()
    {
        // ahahaha this method is the worst.

        UserPrivate up = getUserData();
        System.err.println("User name: " + up.getName());
        System.err.println("User display name: " + up.getDisplayName());

        System.err.println("Settings:");
        for (Map.Entry<String, String> entry : up.getSettingsMap().entrySet())
        {
            System.err.println(entry.getKey() + " = " + entry.getValue());
        }
        System.err.println("State:");
        for (Map.Entry<String, String> entry : up.getStateMap().entrySet())
        {
            System.err.println(entry.getKey() + " = " + entry.getValue());
        }

        for (Account account : this.getAccounts())
        {
            System.err.println("For account \"" + account.getDisplayName() + "\" (" + account + "):");

            DomainObjectProvider provider = this.getProvider(account);

            System.err.println("Patient count: " + provider.getPatients().size());
            System.err.println("Job count: " + provider.getJobs().size());
            System.err.println("JobType count: " + provider.getJobTypes().size());
            System.err.println("Encounter count: " + provider.getEncounters().size());
            System.err.println("Queue count: " + provider.getQueues().size());
        }

        System.err.flush();
    }

    public boolean isDisposed()
    {
        return disposed;
    }
    public void dispose()
    {
        if (disposed) return;
        disposed = true;

        for(DatabaseProvider provider : accountDatabases.values())
        {
            provider.close();
        }
    }
}
