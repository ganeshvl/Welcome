package com.entradahealth.entrada.core.auth.providers;

import com.entradahealth.entrada.android.app.personal.files.AndroidFileResolver;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.files.FileResolver;
import com.entradahealth.entrada.core.files.FileUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

/**
 * A user provider...based on the file system. Uses the FileResolver
 * to determine the location of the directory tree.
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class FileSystemUserProvider implements UserProvider
{
    private static final FileResolver resolver = new AndroidFileResolver();
    @Override
    public ImmutableList<String> getUsers()
    {
        File userDir = resolver.resolve("Users");

        File[] files = userDir.listFiles(FileUtils.DIRECTORY_FILTER);
        List<String> users = Lists.newArrayListWithCapacity(files.length);

        for(File f : files)
        {
            users.add(FilenameUtils.removeExtension(
                    FilenameUtils.getBaseName(f.toString())));
        }

        return ImmutableList.copyOf(users);
    }

    @Override
    public User getUser(String username, String password) throws InvalidPasswordException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public User createUser(String username, String password)
    {
        throw new UnsupportedOperationException();
    }
}
