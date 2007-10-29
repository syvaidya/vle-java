/*
 * Math Utilities for VLE.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.util;

/**
 * Math Utilities for VLE.
 */
public class MathUtils
{
    public static double square(double num)
    {
        return (num * num);
    }

    public static double cubeRoot(double num)
    {
        if (num < 0)
            return -Math.exp(Math.log(Math.abs(num)) / 3);
        else
            return Math.exp(Math.log(num) / 3);
    }
}
