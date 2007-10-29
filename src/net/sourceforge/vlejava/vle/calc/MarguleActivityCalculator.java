/*
 * Margule's equation implementation for activity calculation.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle.calc;

import java.util.*;
import net.sourceforge.vlejava.vle.*;

/**
 * Margule's equation implementation for activity calculation.
 */
public class MarguleActivityCalculator implements IActivityCalculator
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

        gamma[0] = Math.exp(x[1] * x[1] * (A12 + 2 * (A21 - A12) * x[0]));
        gamma[1] = Math.exp(x[0] * x[0] * (A21 + 2 * (A12 - A21) * x[1]));

        return gamma;
    }

    public ArrayList getParamList(int numOfComps)
    {
        ArrayList paramList = new ArrayList();

        if (numOfComps != 2) return paramList;

        paramList.add("Margule-a12");
        paramList.add("Margule-a21");

        return paramList;
    }

    public boolean isNonBinarySystemAllowed()
    {
        return false;
    }
}
