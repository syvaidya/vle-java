/*
 * VanLaar equation implementation for activity calculation.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle.calc;

import java.util.*;
import net.sourceforge.vlejava.vle.*;
import net.sourceforge.vlejava.util.MathUtils;

/**
 * VanLaar equation implementation for activity calculation.
 */
public class VanlaarActivityCalculator implements IActivityCalculator
{
    private VLEContext context = null;

    public void setContext(VLEContext context)
    {
        this.context = context;
    }

    public double[] calculateActivity(Object[] params)
    {
        double A12 = ((Double) params[0]).doubleValue();
        double A21 = ((Double) params[1]).doubleValue();

        double[] gamma = new double[2];
        double[] x = context.getLiquidMoleFractions();

        gamma[0] = Math.exp(A12 / MathUtils.square(1 + (A12 * x[0]) / (A21 * x[1])));
        gamma[1] = Math.exp(A21 / MathUtils.square(1 + (A21 * x[1]) / (A12 * x[0])));

        return gamma;
    }

    public ArrayList getParamList(int numOfComps)
    {
        ArrayList paramList = new ArrayList();

        if (numOfComps != 2) return paramList;

        paramList.add("VanLaar-a12");
        paramList.add("VanLaar-a21");

        return paramList;
    }

    public boolean isNonBinarySystemAllowed()
    {
        return false;
    }
}
