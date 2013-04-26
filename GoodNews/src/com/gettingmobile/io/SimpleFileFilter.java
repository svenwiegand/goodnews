package com.gettingmobile.io;

import java.io.File;
import java.io.FileFilter;

public class SimpleFileFilter implements FileFilter {
    public static final String ALIAS_THIS = ".";
    public static final String ALIAS_PARENT = "..";
    private final boolean files;
    private final boolean directories;
    private final boolean excludeThisAndParent;

    public SimpleFileFilter(boolean files, boolean directories, boolean excludeThisAndParent) {
        this.files = files;
        this.directories = directories;
        this.excludeThisAndParent = excludeThisAndParent;
    }

    public SimpleFileFilter(boolean files, boolean directories) {
        this(files, directories, true);
    }

    @Override
    public boolean accept(File f) {
        return (f.isFile() && files) || (f.isDirectory() && directories && (!excludeThisAndParent ||
                (!ALIAS_THIS.equals(f.getName()) && !ALIAS_PARENT.equals(f.getName()))));
    }
}
