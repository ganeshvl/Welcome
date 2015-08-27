package com.entradahealth.entrada.android.app.personal.files;

import com.entradahealth.entrada.android.app.personal.utils.AndroidUtils;
import com.entradahealth.entrada.core.files.FileResolver;

/**
 * Resolves paths inside the Entrada directory on external storage.
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class AndroidFileResolver extends FileResolver
{
    public AndroidFileResolver()
    {
        super(AndroidUtils.ENTRADA_DIR);
    }
}
