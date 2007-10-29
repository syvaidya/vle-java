/*
 * Data class to store component data.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

/**
 * Data class to store component data.
 */
public class ComponentData
{
    public int compId   = -1;
    public String name  = null;
    public double Tc    = Constants.DOUBLE_NULL;
    public double Pc    = Constants.DOUBLE_NULL;
    public double Zc    = Constants.DOUBLE_NULL;
    public double Vc    = Constants.DOUBLE_NULL;
    public double omega = Constants.DOUBLE_NULL;
    public double antA  = Constants.DOUBLE_NULL;
    public double antB  = Constants.DOUBLE_NULL;
    public double antC  = Constants.DOUBLE_NULL;

    public boolean isCriticalDataAvailable = true;
    public boolean isAntoineDataAvailable  = true;


    public void init()
    {
        if (Tc   == Constants.DOUBLE_NULL) isCriticalDataAvailable = false;
        if (antA == Constants.DOUBLE_NULL) isAntoineDataAvailable  = false;

        Vc = Constants.R_J_PER_KMOL_K * Zc * Tc / Pc;
    }

    public String toString()
    {
        return name;
    }
}
