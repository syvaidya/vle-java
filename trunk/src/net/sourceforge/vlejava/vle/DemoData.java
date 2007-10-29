/*
 * Data class to store demo data.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

import java.util.HashMap;

/**
 * Data class to store demo data.
 */
public class DemoData
{
    public int     compID1     = 0;
    public int     compID2     = 0;
    public HashMap actParamMap = new HashMap();

    public String toString()
    {
        StringBuffer sbfOut = new StringBuffer("");

        sbfOut.append("DemoData: ");
        sbfOut.append("compID1 = "    ).append(compID1    ).append(", ");
        sbfOut.append("compID2 = "    ).append(compID2    ).append(", ");
        sbfOut.append("actParamMap = ").append(actParamMap);

        return sbfOut.toString();
    }
}
