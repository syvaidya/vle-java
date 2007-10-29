/*
 * Interface for Fugacity Calculator. To add user-defined fugacity calculation
 * method, implement this interface.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle.calc;

import net.sourceforge.vlejava.vle.*;

/**
 * Interface for Fugacity Calculator. To add user-defined fugacity calculation
 * method, implement this interface.
 */
public interface IFugacityCalculator
{
    public void setContext(VLEContext context);
    public double[] calculateFugacity(double T, double P, double[] Psat);
}
