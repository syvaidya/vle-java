/*
 * Class to store constants.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

/**
 * Class to store constants.
 */
public class Constants
{
    public final static double KPA_PER_MMHG      = 0.133322;
    public final static double EPSILON           = 1E-5;
    public final static double R_J_PER_KMOL_K    = 8314.472;
    public final static double R_J_PER_MOL_K     = 8.314472;
    public final static double K_C_DIFF          = 273.16;

    public final static String NOT_AVAILABLE     = "na";
    public final static double DOUBLE_NULL       = Double.NaN;
    public final static int    UNIFAC_GROUP_NUM  = 50;

    public final static String COMP_DATA_FILE    = "config/Component.dat";
    public final static String DEMO_DATA_FILE    = "config/Demo.dat";
    public final static String GROUP_DATA_FILE   = "config/Group.dat";
    public final static String UNIFAC_DATA_FILE  = "config/Unifac.dat";
    public final static String ACTIVITY_MAP_FILE = "config/activity.properties";
    public final static String FUGACITY_MAP_FILE = "config/fugacity.properties";
}
