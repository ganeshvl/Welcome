package com.entradahealth.entrada.core.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.files.AndroidFileResolver;
import com.entradahealth.entrada.core.auth.UserPrivate.PrivateInfo;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.crypt.BlowfishUtils;
import com.entradahealth.entrada.core.files.FileResolver;
import com.entradahealth.entrada.core.files.FileUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
/**
 * A user attached to the app. Users have a has-many relationship
 * with accounts (account databases handle the domain objects in
 * domain.*).
 *
 * The User class represents publicly-available user information.
 * UserPrivate, which subclasses User, handles accounts and other
 * sensitive information (and, to create it, requires the user's
 * PIN).
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class User implements Comparable<User>
{
    protected static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // TODO: image?
    // TODO: userid/role?

    protected final String name;
    protected String displayName;

    @JsonCreator
    public User(@JsonProperty("name") String name,
                @JsonProperty("display_name") String displayName)
    {
        this.name = name;
        this.displayName = displayName;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("display_name")
    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public File getUserDirectory() { return getUserDirectory(name); }


    public void save() throws IOException
    {
        File dir = getUserDirectory(name);
        File pubFile = new File(dir, "public.dat");
        User us = new User(name, displayName);
        String plaintext = mapper.writeValueAsString(us);
        SharedPreferences sp = EntradaApplication.getAppContext().getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        byte[] encrypted = BlowfishUtils.encrypt(plaintext, sp.getString("PIN_SAVED", "1111"));
        Files.write(encrypted, pubFile);
    }

    @Override
    public int compareTo(User user)
    {
        return this.getName().compareTo(user.getName());
    }

    @Override
    public String toString()
    {
        String dn = getDisplayName();
        return (dn != null ? dn : "null") + " (" + getName() + ")";
    }

    /**
     * Returns a list of all users, sorted by username.
     * @return a list of all users, sorted by username...really.
     * @throws com.entradahealth.entrada.core.auth.exceptions.UserLoadException when badness occurs.
     */
    public static ImmutableList<User> getUsers() throws UserLoadException
    {
        File userRoot = getUserRoot();

        File[] userDirs = userRoot.listFiles(FileUtils.DIRECTORY_FILTER);

        if (userDirs == null || userDirs.length == 0) return ImmutableList.of();

        List<User> users = Lists.newArrayListWithCapacity(userDirs.length);

        Arrays.sort(userDirs, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
        //Collections.sort(users);
        for(File dir : userDirs)
        {
        	File[] contents = dir.listFiles();
        	// the directory file is not really a directory..
        	if (contents == null || contents.length == 0) {
        		try {
					FileUtils.deleteRecursive(dir);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else {
        		File ddir = getUserDirectory(dir.getName());
                File pubFile = new File(ddir, "public.dat");
                if(pubFile.exists())
                	users.add(getPublicUserInformation(dir.getName()));
                else{
                	try {
    					FileUtils.deleteRecursive(dir);
    				} catch (FileNotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                }
        	}
        	//users.add(getPublicUserInformation(dir.getName()));
        }
        //Collections.sort(users);

        return ImmutableList.copyOf(users);
    }

    public static User getPublicUserInformation(String name) throws UserLoadException
    {
        try
        {
            File dir = getUserDirectory(name);
            File pubFile = new File(dir, "public.dat");

            byte[] encrypted = Files.toByteArray(pubFile);
            User us = null;
            
            try
            {
				SharedPreferences sp = EntradaApplication.getAppContext().getSharedPreferences("Entrada",
						Context.MODE_WORLD_READABLE);
                String decrypted = BlowfishUtils.decrypt(encrypted, sp.getString("PIN_SAVED", "1111"));
                us = mapper.readValue(decrypted, User.class);
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
            return us;
            
        }
        catch (Exception e)
        {
            throw new UserLoadException(e);
        }
    }

    public static UserPrivate getPrivateUserInformation(String name, String password)
            throws UserLoadException, InvalidPasswordException
    {
        return UserPrivate.loadPrivate(name, password);
    }
    
    
    public static UserPrivate createNewUser(String name, String displayName, String password)
            throws IOException
    {
        return UserPrivate.buildUser(new User(name, displayName), password);
    }

    private static final FileResolver resolver = new AndroidFileResolver();
    public static File getUserRoot()
    {
        return resolver.resolve("Users");
    }

    // public in case we decide we want images, etc. later; the Android app can
    // pick them directly (uses Android-specific classes so can't really be in
    // core).
    public static File getUserDirectory(String name)
    {
        return new File(getUserRoot(), name);
    }
}
