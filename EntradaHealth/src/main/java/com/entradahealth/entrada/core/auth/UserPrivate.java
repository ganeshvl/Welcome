package com.entradahealth.entrada.core.auth;

import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.crypt.BlowfishUtils;
import com.entradahealth.entrada.core.db.H2Utils;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DatabaseProvider;
import com.entradahealth.entrada.core.files.FileUtils;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDatabaseProvider;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Contains a User and sensitive information: last login state,
 * accounts, and so on. To load a UserPrivate from disk, the app
 * requires the user to put in their PIN (or, perhaps later, their
 * password--it amounts to the same thing) which is used as the
 * key for account databases as well as private.dat.
 *
 * @author edr
 * @since 12 Sep 2012
 */
public class UserPrivate extends User
{
    private static final Pattern ACCOUNT_NAME_PATTERN =
            Pattern.compile("[0-9a-zA-Z-_]{1,}");


    // Anything in these will be layered in as the state/setting defaults
    // and will be overridden by specifics.
    private static final ImmutableMap<String, String> DEFAULT_SETTINGS;
    private static final ImmutableMap<String, String> DEFAULT_STATE;
    static {
        DEFAULT_SETTINGS = ImmutableMap.<String, String>builder()
                .build();
        DEFAULT_STATE = ImmutableMap.<String, String>builder()
                .build();
    }

    /**
     * The DECRYPTED password for this user. I'm not at all wild about
     * this floating around, but I need the password to decrypt the
     * account databases without asking for it again. Plus--it's Java,
     * if someone really wants to pick a password they already can when
     * it's being input. It doesn't appreciably decrease security.
     */
    private final String password;
    private final Map<String, String> state;
    private final Map<String, String> settings;
    private final Map<String, Account> accounts;

    UserPrivate(User userBase,
                String password,
                PrivateInfo privateInfo)
    {
        super(userBase.name, userBase.displayName);

        Map<String, Account> accs = Maps.newHashMapWithExpectedSize(privateInfo.accounts.size());
        for (Account a : privateInfo.accounts) accs.put(a.getName(), a);
        this.accounts = accs;


        this.password = password;
        this.state = Maps.newHashMap(DEFAULT_STATE);
        this.state.putAll(privateInfo.state);
        this.settings = Maps.newHashMap(DEFAULT_SETTINGS);
        this.settings.putAll(privateInfo.settings);
    }



    public String getStateValue(String key)
    {
        return state.get(key);
    }

    public void setStateValue(String key, String value)
    {
        state.put(key, value);
    }
    public Map<String, String> getStateMap()
    {
        return Collections.unmodifiableMap(this.state);
    }

    public String getSettingsValue(String key)
    {
        return settings.get(key);
    }
    public void setSettingsValue(String key, String value)
    {
        settings.put(key, value);
    }
    public Map<String, String> getSettingsMap()
    {
        return Collections.unmodifiableMap(this.settings);
    }

    public Collection<Account> getAccounts()
    {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public boolean matchPassword(String testPassword)
    {
        return password.equals(testPassword);
    }
    
    public String getPassword(){
		return password;
    	
    }

    public Cipher getCipherForUser(int mode) throws BlowfishUtils.EncryptionException {
        return BlowfishUtils.getCipher(mode, this.password);
    }

    @Override
    public void save() throws IOException
    {
        File userDir = getUserDirectory(name);
        if (!userDir.exists()) userDir.mkdirs();

        File accountsDir = getUserAccountsDir();
        if (!accountsDir.exists()) accountsDir.mkdirs();

        super.save(); // -r shipping!

        File privFile = new File(userDir, "private.dat");

        // TODO: add method that doesn't perpetuate default state/settings values.
        //       This will bake the current values in DEFAULT_{STATE,SETTINGS} into
        //       the user account, which will suck if we want to change the defaults
        //       later. We should have a method that compares the data to be saved to
        //       the defaults and omit them if they're the same. I'll do that later.

        PrivateInfo p = new PrivateInfo(state, settings,
                                        Lists.newArrayList(accounts.values()));

        String plaintext = mapper.writeValueAsString(p);

        byte[] encrypted = BlowfishUtils.encrypt(plaintext, this.password);

        Files.write(encrypted, privFile);
    }

    /**
     * Opens a connection pool to the account in question.
     * @param accountName The name of the account.
     * @return a connection pool to the database for that account.
     * @throws com.entradahealth.entrada.core.auth.exceptions.AccountException Account by that name doesn't exist.
     * @throws InvalidPasswordException Account password is incorrect.
     */
    public Connection openAccountDatabase(String accountName)
            throws AccountException, InvalidPasswordException
    {
        File accountDir = new File(getUserAccountsDir(), accountName);

        if (!accountDir.exists())
        {
            throw new AccountException("Account doesn't have directory structure: " +
                                               accountName);
        }

        File dbFile = new File(accountDir, "data");

        return H2Utils.openEncryptedDatabase(dbFile, this.password);
    }

    public Connection openUserDatabase(String userName)
            throws AccountException, InvalidPasswordException
    {
    	File dbFile = new File(getUserRoot(), "user_"+userName);
        return H2Utils.openEncryptedDatabase(dbFile, this.password);
    }

    public void deleteAccount(Account deletedAccount) throws AccountException, FileNotFoundException
    {
        accounts.remove(deletedAccount.getName());

        File accountDir = new File(getUserAccountsDir(), deletedAccount.getName());

        if (!accountDir.exists())
        {
            throw new AccountException("Account doesn't have directory structure: " +
                                               deletedAccount);
        }

        FileUtils.deleteRecursive(accountDir);
    }
    public void deleteUser(String user) throws FileNotFoundException{
    	File userDir = getUserDirectory(user);
    	FileUtils.deleteRecursive(userDir);
    }
    
    public void renameFile(String user) throws FileNotFoundException{
    	File userDir = getUserDirectory(user);
    	final File to = new File("temp");
    	userDir.renameTo(to);
    	//to.delete();
    	//userDir.delete();
    }
  
    public Account createAccount(String name, String displayName,
                                 String remoteUsername, String remotePassword,
                                 String remoteApiHost, String clinicCode,
                                 String smUsername, String smPassword)
            throws AccountException, DomainObjectWriteException
    {
        File accountDir = new File(getUserAccountsDir(), name);

        if (accountDir.exists())
        {
            throw new AccountException("Account '" + name + "' already exists for user.");
        }

        if (!ACCOUNT_NAME_PATTERN.matcher(name).matches())
        {
            throw new AccountException("Invalid account name: " + name);
        }

        accountDir.mkdirs();

        File dbFile = new File(accountDir, "data");

        Connection conn = null;
//        try
//        {
            conn = H2Utils.openEncryptedDatabase(dbFile, this.password);
//        }
//        catch (InvalidPasswordException e)
//        {
//            throw new RuntimeException("shouldn't be happening...", e);
//        }

        DatabaseProvider provider = new DatabaseProvider(conn);
        provider.close();


        Account a = new Account(name, displayName, remoteUsername, remotePassword, remoteApiHost, clinicCode, smUsername, smPassword, Maps.newHashMap());

        this.accounts.put(a.getName(), a);

        return a;
    }

	public void createUser(String name)
	throws AccountException, DomainObjectWriteException
	{
		File dbFile = new File(getUserRoot(), "user_"+name);
		if(dbFile.exists()){
			return;
		}
		
		Connection conn = null;
		conn = H2Utils.openEncryptedDatabase(dbFile, this.password);
		
		SMDatabaseProvider provider = new SMDatabaseProvider(conn);
		provider.close();
	}    
    
    public File getUserAccountsDir()
    {
        return new File(getUserDirectory(name), "accounts");
    }

    static UserPrivate buildUser(User userBase, String password) throws IOException
    {
        PrivateInfo p = new PrivateInfo(Maps.<String,String>newHashMap(),
                                        Maps.<String,String>newHashMap(),
                                        Lists.<Account>newArrayList());
        UserPrivate privUser = new UserPrivate(userBase, password, p);
        privUser.save();

        return privUser;
    }

    static UserPrivate loadPrivate(String name, String password)
            throws InvalidPasswordException, UserLoadException
    {
        try
        {
            File userDir = getUserDirectory(name);
            File privFile = new File(userDir, "private.dat");
            User userBase = getPublicUserInformation(name);

            byte[] encrypted = Files.toByteArray(privFile);
            PrivateInfo p = null;
            try
            {
                String decrypted = BlowfishUtils.decrypt(encrypted, password);

                p = mapper.readValue(decrypted, PrivateInfo.class);
            }
            catch (BlowfishUtils.EncryptionException ex)
            {
                // any failure in here means either a corrupted private.dat or
                // a bad password. I can't think of a way to determine if it's
                // a corrupted file as opposed to a failed decryption; from a
                // crypto perspective, the two mean the same thing. So, until we
                // see a situation where this happens, I'm going to assume it's
                // an invalid password.

                throw new InvalidPasswordException(ex);
            }
            catch (JsonParseException ex)
            {
                throw new InvalidPasswordException(ex);
            }

            return new UserPrivate(userBase, password, p);
        }
        catch (InvalidPasswordException e)
        {
            throw e;
        }
        catch (UserLoadException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new UserLoadException(e);
        }
    }
    
    
    public static class PrivateInfo
    {
        final Map<String, String> state;
        final Map<String, String> settings;
        final List<Account> accounts;

        @JsonCreator
        public PrivateInfo(@JsonProperty("state") Map<String, String> state,
                           @JsonProperty("settings") Map<String, String> settings,
                           @JsonProperty("accounts") List<Account> accounts)
        {
            this.state = state;
            this.settings = settings;
            this.accounts = accounts;
        }

        @JsonProperty("state")
        public Map<String, String> getState()
        {
            return state;
        }

        @JsonProperty("settings")
        public Map<String, String> getSettings()
        {
            return settings;
        }

        @JsonProperty("accounts")
        public List<Account> getAccounts()
        {
            return accounts;
        }
    }

    public static final class StateKeys
    {
        private StateKeys() {}

        public static final String CURRENT_ACCOUNT = "current-account";
    }
}
