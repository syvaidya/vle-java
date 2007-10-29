/*
 * Interface for Activity Calculator. To add user-defined activity calculation
 * method, implement this interface.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle.calc;

import java.util.ArrayList;
import net.sourceforge.vlejava.vle.*;

/**
 * Interface for Activity Calculator. To add user-defined activity calculation
 * method, implement this interface.
 */
public interface IActivityCalculator
{
    public void setContext(VLEContext context);
    public double[] calculateActivity(Object[] params);
    public ArrayList getParamList(int numOfComps);
    public boolean isNonBinarySystemAllowed();
}
