package com.entradahealth.entrada.core.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

/**
 * Yet another helper class--this one for files!
 *
 * @author edr
 * @since 11 Sep 2012
 */
public class FileUtils
{
    private FileUtils() { }

    public static final FileFilter DIRECTORY_FILTER =
            new FileFilter() {
                @Override
                public boolean accept(File file)
                {
                    return file.isDirectory();
                }
            };


    /**
     * By default File#delete fails for non-empty directories, it works like "rm".
     * We need something a little more brutual - this does the equivalent of "rm -r"
     * @param path Root File Path
     * @return true iff the file and all sub files/directories have been removed
     * @throws FileNotFoundException
     */
    public static boolean deleteRecursive(File path) throws FileNotFoundException
    {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && FileUtils.deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
}
