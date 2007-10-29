/*
 * Peng-Robinson equation implementation for fugacity calculation.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle.calc;

import net.sourceforge.vlejava.vle.*;
import net.sourceforge.vlejava.util.MathUtils;

/**
 * Peng-Robinson equation implementation for fugacity calculation.
 */
public class PRFugacityCalculator implements IFugacityCalculator
{
    private final static double COEF_A = 0.457;
    private final static double COEF_B = 0.077;
    private final static double COEF_P = 1.414;

    private VLEContext context = null;

    public void setContext(VLEContext context)
    {
        this.context = context;
    }

    public double[] calculateFugacity(double T, double P, double[] Psat)
    {
        int i;
        int j;

        double ao = 0;
        double bo = 0;
        double prA = 0;
        double prB = 0;
        double prC = 0;
        double prD = 0;
        double prP = 0;
        double prQ = 0;
        double prR = 0;
        double prZ = 0;
        double capA = 0;
        double capB = 0;
        double prTheta = 0;
        double tmpVal = 0;
        double lnPhi = 0;
        double lnPsat = 0;

        double[] vapMoleFracs = context.getVapourMoleFractions();
        int numOfComps = context.getNumOfComps();
        ComponentData[] components = context.getComponents();

        double[] a = new double[numOfComps];
        double[] b = new double[numOfComps];
        double[] phi = new double[numOfComps];


        for (i = 0; i < numOfComps; i++)
        {
            a[i] = COEF_A * MathUtils.square(Constants.R_J_PER_MOL_K * components[i].Tc) / components[i].Pc / 1E5;
            b[i] = COEF_B * Constants.R_J_PER_MOL_K * components[i].Tc / components[i].Pc / 1E5;
        }

        ao = 0;
        bo = 0;

        for (i = 0; i < numOfComps; i++)
        {
            for (j = 0; j < numOfComps; j++)
                ao += vapMoleFracs[i] * vapMoleFracs[j] * Math.sqrt(a[i] * a[j]);

            bo += vapMoleFracs[i] * b[i];
        }

        capB = bo * P * 1000 / (Constants.R_J_PER_MOL_K * (T + Constants.K_C_DIFF));
        capA = ao * P * 1000 / MathUtils.square(Constants.R_J_PER_MOL_K * (T + Constants.K_C_DIFF));

        prA = capB - 1;
        prB = capA - 2 * capB - 3 * capB * capB;
        prC = -capA * capB + capB * capB * (capB + 1);
        prP = prB - (prA * prA / 3);
        prQ = (2 * prA * prA * prA / 27) - (prA * prB / 3) + prC;
        prD = (prQ * prQ / 4) + (prP * prP * prP / 27);

        if (prD > 0)
        {
            prZ = MathUtils.cubeRoot(-prQ / 2 + Math.sqrt(prD))
                + MathUtils.cubeRoot(-prQ / 2 - Math.sqrt(prD)) - prA / 3;
        }
        else if (prD == 0)
        {
            prZ = MathUtils.cubeRoot(prQ / 2) - prA / 3;
            /*tmpVal = -2 * MathUtils.cubeRoot(prQ / 2) - prA / 3;

            if (prZ < tmpVal) prZ = tmpVal;*/
        }
        else if (prD < 0)
        {
            prR = Math.sqrt(-prP * prP * prP / 27);
            tmpVal = -prQ / 2 * Math.sqrt(-27 / prP / prP / prP);
            prTheta = Math.atan(Math.sqrt(1 - MathUtils.square(tmpVal)) / tmpVal);
            prZ = 2 * MathUtils.cubeRoot(prR) * Math.cos(prTheta / 3) - prA / 3;

            tmpVal = 2 * MathUtils.cubeRoot(prR) * Math.cos((2 * Math.PI + prTheta) / 3) - prA / 3;
            if (prZ < tmpVal) prZ = tmpVal;

            tmpVal = 2 * MathUtils.cubeRoot(prR) * Math.cos((4 * Math.PI + prTheta) / 3) - prA / 3;
            if (prZ < tmpVal) prZ = tmpVal;
        }

        for (i = 0; i < numOfComps; i++)
        {
            tmpVal = ao * (b[i] / bo - 2 * Math.sqrt(a[i] / ao)) * Math.log((prZ + capB * (COEF_P + 1)) / (prZ - capB * (COEF_P - 1))) / (2 * COEF_P * bo * Constants.R_J_PER_MOL_K * (T + Constants.K_C_DIFF));
            lnPhi = b[i] * (prZ - 1) / bo - Math.log(prZ - bo) + tmpVal;
            lnPsat = b[i] * P * Psat[i] / MathUtils.square(Constants.R_J_PER_MOL_K * 1E5 * (T + Constants.K_C_DIFF));

            phi[i] = Math.exp(lnPhi - lnPsat);
        }

        return phi;
    }
}
