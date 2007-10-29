/*
 * Data class to store Unifac group data.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

/**
 * Data class to store Unifac group data.
 */
public class GroupData
{
    public String groupName  = null;
    public int    uniGroupNo = 0;
    public int    k          = 0;
    public double qk         = 0;
    public double rk         = 0;

    public String toString()
    {
        return groupName;
    }
}
