package com.entradahealth.entrada.android.app.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.db.H2SetupException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.google.common.base.Preconditions;

import javax.annotation.CheckForNull;

/**
 * A holder for global application state. I know it's hideous. I
 * know it makes everyone sad. It makes me sad too. It's not my
 * fault. It's not my fault. It's not my fault.
 *
 *
 *
 *
 *
 * It's Jackson's fault.
 *
 * @author edr
 * @since 24 Sep 2012
 */
public class AndroidState
{
    private AndroidState() { }

    private UserState userState;

    @CheckForNull
    public UserState getUserState()
    {
    	if(userState == null){
    		UserPrivate user;
			try {
				SharedPreferences sp = EntradaApplication.getAppContext().getSharedPreferences("Entrada",
						Context.MODE_WORLD_READABLE);
				user = User.getPrivateUserInformation(BundleKeys.cur_uname, sp.getString("PIN_SAVED", "1111"));
				Log.d("ENTRADA-USER-PERF", "creating user state.");
	            userState = new UserState(user);
			} catch (UserLoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPasswordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AccountException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
        return userState;
    }

    public UserState createUserState(UserPrivate user)
            throws AccountException, InvalidPasswordException
    {
        try
        {
            Log.d("ENTRADA-USER-PERF", "creating user state.");
            userState = new UserState(user);
            Log.d("ENTRADA-USER-PERF", "user state created.");
            return userState;
        }
        catch (H2SetupException ex)
        {
            throw new InvalidPasswordException(ex);
        } catch (DomainObjectWriteException e) {
            throw new AccountException("Unable to perform database schema updates.", e);
        }
    }

    public void clearUserState()
    {
        if (userState == null) return;
//        synchronized (userState)
//        {
            userState.dispose();
            userState = null;
//        }
    }


    private static class Holder
    {
        public static final AndroidState instance = new AndroidState();
    }
    public static AndroidState getInstance()
    {
        return Holder.instance;
    }
}
