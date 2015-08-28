package com.entradahealth.entrada.core.auth.providers;

import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.google.common.collect.ImmutableList;

/**
 * Used to list, access, and create user accounts.
 *
 * @author edr
 * @since 11 Sep 2012
 */
public interface UserProvider
{
    ImmutableList<String> getUsers();

    User getUser(String username, String password) throws InvalidPasswordException;

    User createUser(String username, String password);
}
