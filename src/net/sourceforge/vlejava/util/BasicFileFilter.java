/*
 * A very minimal implementation of FileFilter for JFileChooser.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.util;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * A very minimal implementation of FileFilter for JFileChooser.
 */
public class BasicFileFilter extends FileFilter
{
    String ext = null;
    String desc = null;

    public BasicFileFilter(String extension, String description)
    {
        if (extension != null)
            ext = extension.toLowerCase();
        desc = description;
    }

    public boolean accept(File f)
    {
        if (f != null)
        {
            if (f.isDirectory())
                return true;

            if (f.getName().toLowerCase().endsWith(ext))
                return true;
        }

        return false;
    }

    public String getDescription()
    {
        return desc;
    }
}
