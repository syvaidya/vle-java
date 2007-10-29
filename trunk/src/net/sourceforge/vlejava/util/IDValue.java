/*
 * Data class to store ID-value pairs.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.util;

/**
 * Data class to store ID-value pairs.
 */
public class IDValue
{
    public int ID = -1;
    public String value = null;

    public IDValue()
    {
    }

    public IDValue(int ID, String value)
    {
        this.ID = ID;
        this.value = value;
    }

    public String toString()
    {
        return value;
    }
}
