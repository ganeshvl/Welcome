package com.entradahealth.entrada.android.app.personal.utils;

import java.io.File;

import javax.annotation.CheckForNull;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;

import android.content.Context;
import android.os.Environment;
/**
 * Helper methods that wrap some of the grosser Android-y bits.
 *
 * @author edr
 * @since 28 Aug 2012
 */
public class AndroidUtils
{

    /**
     * Eventually we might want to use ctx.getExternalFilesDir(null), but this
     * makes developing easier 'cause I can actually get to the folders without
     * bouncing through four or five layers.
     */

    public static File ENTRADA_DIR = new File(EntradaApplication.getAppContext().getExternalFilesDir(null), "/Entrada");

    //public static File ENTRADA_DIR = new File(Environment.getExternalStorageDirectory(), "/Entrada");


    /**
     * @return a File corresponding to the external storage directory or null on failure.
     */
    @CheckForNull
    public static File getExternalStorageLocation()
    {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                ? ENTRADA_DIR
                : null;
    }

    @CheckForNull
    public static File getTempFileLocation()
    {
        File f = getExternalStorageLocation();
        File t =  f != null ? new File(f, "temp") : null;
        if (t != null && !t.exists()) t.mkdirs();

        return t;
    }
}
