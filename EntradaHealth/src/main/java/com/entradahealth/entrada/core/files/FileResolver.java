package com.entradahealth.entrada.core.files;

import java.io.File;

/**
 * A wrapper for resolving paths into File objects. (The Android
 * one should handle resolving into external storage.)
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class FileResolver
{
    private final File baseDir;

    /*public FileResolver(String baseDirPath)
    {
        this.baseDir = new File(baseDirPath);
    }*/

    public FileResolver(File baseDir)
    {
        this.baseDir = baseDir;
    }

    public File resolve(String path)
    {
        return new File(baseDir, path);
    }
}
