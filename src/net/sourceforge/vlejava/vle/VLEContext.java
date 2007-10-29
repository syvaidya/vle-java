/*
 * This class provides the context for the application.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

import java.util.*;
import java.io.*;
import net.sourceforge.vlejava.vle.calc.*;
import net.sourceforge.vlejava.util.MessageHandler;

/**
 * This class provides the context for the application.
 */
public class VLEContext
{
    private static ArrayList actMethodList = new ArrayList();
    private static HashMap actMethodMap = new HashMap();
    private static ArrayList fugMethodList = new ArrayList();
    private static HashMap fugMethodMap = new HashMap();

    private int activityMethod = -1;
    private int fugacityMethod = -1;
    private int numOfComps = 0;
    private double[] liqMoleFracs = null;
    private double[] vapMoleFracs = null;
    private ComponentData[] components = null;
    private Object[] actMethodParams = null;
    private IActivityCalculator actCalculator = null;
    private IFugacityCalculator fugCalculator = null;

    /**
     * Initialize the context for the application.
     */
    public static void initialize()
    {
        fillListAndMaps(Constants.ACTIVITY_MAP_FILE, actMethodList, actMethodMap);
        fillListAndMaps(Constants.FUGACITY_MAP_FILE, fugMethodList, fugMethodMap);
    }

    /**
     * Generic method to load the properties files.
     */
    private static void fillListAndMaps(String fileName, ArrayList list, HashMap map)
    {
        int count = 0;
        String propName = null;
        String value = null;
        Enumeration enum = null;
        Properties props = new Properties();

        try
        {
            props.load(new FileInputStream(fileName));
        }
        catch(Exception ex)
        {
            System.out.println(MessageHandler.getString("errLoadFileFail") + ": " + fileName);
            System.exit(2);
        }

        enum = props.propertyNames();

        while (enum.hasMoreElements())
        {
            propName = (String) enum.nextElement();
            value = (String) props.get(propName);

            list.add(value);
            map.put(new Integer(count), propName);

            count++;
        }
    }

    /**
     * Returns the list of activity methods supported by application.
     */
    public static ArrayList getActivityMethodList()
    {
        return actMethodList;
    }

    /**
     * Returns the list of fugacity methods supported by application.
     */
    public static ArrayList getFugacityMethodList()
    {
        return fugMethodList;
    }

    /**
     * Returns the number of components in current system.
     */
    public int getNumOfComps()
    {
        return numOfComps;
    }

    /**
     * Returns the currently selected activity method.
     */
    public int getActivityMethod()
    {
        return activityMethod;
    }

    /**
     * Sets the activity method for calculations.
     * @param method Method ID
     * @param params Optional parameter list for the methods
     */
    public void setActivityMethod(int method, Object[] params)
        throws VLEException
    {
        activityMethod = method;
        actMethodParams = params;

        actCalculator = getActivityCalculator(activityMethod);

        if ((!actCalculator.isNonBinarySystemAllowed()) && (numOfComps != 2))
            throw new VLEException(MessageHandler.getString("errBinActCalcNotAllow"));

        actCalculator.setContext(this);
    }

    /**
     * Returns the currently selected fugacity method.
     */
    public int getFugacityMethod()
    {
        return fugacityMethod;
    }

    /**
     * Sets the fugacity method for calculations.
     * @param method Method ID
     */
    public void setFugacityMethod(int method)
    {
        fugacityMethod = method;

        fugCalculator = getFugacityCalculator(fugacityMethod);
        fugCalculator.setContext(this);
    }

    /**
     * Returns the list of components in the current system.
     */
    public ComponentData[] getComponents()
    {
        return components;
    }

    /**
     * Sets the list of components for the current system.
     */
    public void setComponents(ComponentData[] compData)
    {
        components = compData;
        numOfComps = compData.length;
        liqMoleFracs = new double[numOfComps];
        vapMoleFracs = new double[numOfComps];
    }

    /**
     * Returns the vapour mole fractions of the components.
     */
    public double[] getVapourMoleFractions()
    {
        return vapMoleFracs;
    }

    /**
     * Sets the vapour mole fractions of the components.
     */
    public void setVapourMoleFractions(double[] fractions)
    {
        vapMoleFracs = fractions;
    }

    /**
     * Returns the liquid mole fractions of the components.
     */
    public double[] getLiquidMoleFractions()
    {
        return liqMoleFracs;
    }

    /**
     * Sets the liquid mole fractions of the components.
     */
    public void setLiquidMoleFractions(double[] fractions)
    {
        liqMoleFracs = fractions;
    }


    /**
     * Calculates the BUBL P at the given temperature.
     */
    public double calcBUBLP(double T)
    {
        int i = 0;
        double P = 0;
        double Ptemp = 0;

        double[] Psat = calcPSat(T);
        double[] gamma = calculateActivity();
        double[] phi = new double[numOfComps];

        for (i = 0; i < numOfComps; i++)
        {
            phi[i] = 1;
            P += liqMoleFracs[i] * gamma[i] * Psat[i];
        }

        do
        {
            Ptemp = P;
            for (i = 0; i < numOfComps; i++)
                vapMoleFracs[i] = liqMoleFracs[i] * gamma[i] * Psat[i] / phi[i] / P;

            phi = calculateFugacity(T, P, Psat);

            P = 0;
            for (i = 0; i < numOfComps; i++)
                P += liqMoleFracs[i] * gamma[i] * Psat[i] / phi[i];
        }
        while(Math.abs(P - Ptemp) > Constants.EPSILON);

        return P;
    }

    /**
     * Calculates the DEW P at the given temperature.
     */
    public double calcDEWP(double T)
    {
        int i = 0;
        boolean flag = false;
        double P = 0;
        double Ptemp = 0;
        double sum = 0;

        double[] Psat = calcPSat(T);
        double[] phi = new double[numOfComps];
        double[] gamma = new double[numOfComps];
        double[] tmpGamma = new double[numOfComps];

        for (i = 0; i < numOfComps; i++)
        {
            phi[i] = 1;
            gamma[i] = 1;
            P += vapMoleFracs[i] * phi[i] / gamma[i] / Psat[i];
        }

        P = 1 / P;

        do
        {
            Ptemp = P;
            phi = calculateFugacity(T, P, Psat);

            flag = true;

            while (flag)
            {
                sum = 0;

                for (i = 0; i < numOfComps; i++)
                {
                    tmpGamma[i] = gamma[i];
                    liqMoleFracs[i] = vapMoleFracs[i] * phi[i] * P / gamma[i] / Psat[i];
                    sum += liqMoleFracs[i];
                }

                for (i = 0; i < numOfComps; i++)
                    liqMoleFracs[i] = liqMoleFracs[i] / sum;

                gamma = calculateActivity();

                for (i = 0; i < numOfComps; i++)
                    if (Math.abs(gamma[i] - tmpGamma[i]) < Constants.EPSILON) flag = false;
            }

            P = 0;
            for (i = 0; i < numOfComps; i++)
                P += vapMoleFracs[i] * phi[i] / gamma[i] / Psat[i];
            P = 1 / P;
        }
        while (Math.abs(P - Ptemp) > Constants.EPSILON);

        return P;
    }

    /**
     * Calculates the BUBL T at the given pressure.
     */
    public double calcBUBLT(double P)
    {
        int i = 0;
        double T = 0;
        double Ttemp = 0;
        double sum = 0;

        double[] Tsat = calcTSat(P);
        double[] Psat = new double[numOfComps];
        double[] phi = new double[numOfComps];
        double[] gamma = new double[numOfComps];

        for (i = 0; i < numOfComps; i++)
        {
            phi[i] = 1;
            gamma[i] = 1;
            T += liqMoleFracs[i] * Tsat[i];
        }

        Psat = calcPSat(T);
        gamma = calculateActivity();

        for (i = 0; i < numOfComps; i++)
            sum += liqMoleFracs[i] * gamma[i] * Psat[i] / phi[i] / Psat[0];
        Psat[0] = P / sum;

        do
        {
            Ttemp = T;
            Psat = calcPSat(T);

            for (i = 0; i < numOfComps; i++)
                vapMoleFracs[i] = liqMoleFracs[i] * gamma[i] * Psat[i] / phi[i] / P;

            gamma = calculateActivity();
            phi = calculateFugacity(T, P, Psat);

            sum = 0;
            for (i = 0; i < numOfComps; i++)
                sum += liqMoleFracs[i] * gamma[i] * Psat[i] / phi[i] / Psat[0];
            Psat[0] = P / sum;

            T = calcTSat(Psat[0])[0];
        }
        while (Math.abs(T - Ttemp) > Constants.EPSILON);

        return T;
    }

    /**
     * Calculates the DEW T at the given pressure.
     */
    public double calcDEWT(double P)
    {
        int i = 0;
        boolean flag = false;
        double T = 0;
        double Ttemp = 0;
        double sum = 0;

        double[] Tsat = calcTSat(P);
        double[] Psat = new double[numOfComps];
        double[] phi = new double[numOfComps];
        double[] gamma = new double[numOfComps];
        double[] tmpGamma = new double[numOfComps];

        for (i = 0; i < numOfComps; i++)
        {
            phi[i] = 1;
            gamma[i] = 1;
            T += vapMoleFracs[i] * Tsat[i];
        }

        Psat = calcPSat(T);

        for (i = 0; i < numOfComps; i++)
            sum += vapMoleFracs[i] * phi[i] * Psat[0] / gamma[i] / Psat[i];
        Psat[0] = P * sum;
        T = calcTSat(Psat[0])[0];

        Psat = calcPSat(T);
        phi = calculateFugacity(T, P, Psat);

        for (i = 0; i < numOfComps; i++)
            liqMoleFracs[i] = vapMoleFracs[i] * phi[i] * P / gamma[i] / Psat[0];
        gamma = calculateActivity();

        sum = 0;
        for (i = 0; i < numOfComps; i++)
            sum += vapMoleFracs[i] * phi[i] * Psat[0] / gamma[i] / Psat[i];
        Psat[0] = P * sum;
        T = calcTSat(Psat[0])[0];

        do
        {
            Ttemp = T;
            Psat = calcPSat(T);
            phi = calculateFugacity(T, P, Psat);

            flag = true;

            while (flag)
            {
                sum = 0;

                for (i = 0; i < numOfComps; i++)
                {
                    tmpGamma[i] = gamma[i];
                    liqMoleFracs[i] = vapMoleFracs[i] * phi[i] * P / gamma[i] / Psat[i];
                    sum += liqMoleFracs[i];
                }

                for (i = 0; i < numOfComps; i++)
                    liqMoleFracs[i] = liqMoleFracs[i] / sum;

                gamma = calculateActivity();

                for (i = 0; i < numOfComps; i++)
                    if (Math.abs(gamma[i] - tmpGamma[i]) < Constants.EPSILON) flag = false;
            }

            sum = 0;
            for (i = 0; i < numOfComps; i++)
                sum += vapMoleFracs[i] * phi[i] * Psat[0] / gamma[i] / Psat[i];
            Psat[0] = P * sum;
            T = calcTSat(Psat[0])[0];
        }
        while (Math.abs(T - Ttemp) > Constants.EPSILON);

        return T;
    }

    /**
     * Calculates the flash point compositions at given P and T.
     */
    public double calcFlashPoint(double P, double T)
        throws VLEException
    {
        boolean flag = true;
        int i = 0;
        double bublP = 0;
        double dewP = 0;
        double vapFraction = 0;
        double vapFracTemp = 0;
        double f = 0;
        double df = 0;
        double sum = 0;
        double[] k = new double[numOfComps];
        double[] mixMoleFracs = new double[numOfComps];
        double[] liqMoleFracsTemp = new double[numOfComps];
        double[] vapMoleFracsTemp = new double[numOfComps];
        double[] Psat = new double[numOfComps];
        double[] phi = new double[numOfComps];
        double[] gamma = new double[numOfComps];

        for (i = 0; i < numOfComps; i++)
            mixMoleFracs[i] = liqMoleFracs[i];
        bublP = calcBUBLP(T);

        for (i = 0; i < numOfComps; i++)
            vapMoleFracs[i] = mixMoleFracs[i];
        dewP = calcDEWP(T);

        if (P > bublP)
            throw new VLEException(MessageHandler.getString("errMixSuperHeated") + " "
                + MessageHandler.getString("errFlashCalcNP"));

        if (P < dewP)
            throw new VLEException(MessageHandler.getString("errMixSubCooled") + " "
                + MessageHandler.getString("errFlashCalcNP"));

        for (i = 0; i < numOfComps; i++)
        {
            phi[i] = 1;
            liqMoleFracs[i] = mixMoleFracs[i];
            vapMoleFracs[i] = mixMoleFracs[i];
        }

        Psat = calcPSat(T);
        gamma = calculateActivity();
        phi = calculateFugacity(T, P, Psat);
        vapFraction = 0.5;

        while (flag)
        {
            vapFracTemp = vapFraction;

            for (i = 0; i < numOfComps; i++)
            {
                liqMoleFracsTemp[i] = liqMoleFracs[i];
                vapMoleFracsTemp[i] = vapMoleFracs[i];
                k[i] = gamma[i] * Psat[i] / phi[i] / P;
            }

            f = 0;
            df = 0;

            for (i = 0; i < numOfComps; i++)
            {
                f += mixMoleFracs[i] * (k[i] - 1) / (1 + vapFraction * (k[i] - 1));
                df -= mixMoleFracs[i] * (k[i] - 1) * (k[i] - 1) / (1 + vapFraction * (k[i] - 1)) / (1 + vapFraction * (k[i] - 1));
            }

            vapFraction -= f / df;
            sum = 0;

            for (i = 0; i < numOfComps; i++)
            {
                liqMoleFracs[i] = mixMoleFracs[i] / (1 + vapFraction * (k[i] - 1));
                vapMoleFracs[i] = k[i] * liqMoleFracs[i];
                sum += Math.abs(liqMoleFracs[i] - liqMoleFracsTemp[i]) + Math.abs(vapMoleFracs[i] - vapMoleFracsTemp[i]);
            }

            gamma = calculateActivity();
            phi = calculateFugacity(T, P, Psat);

            if ((Math.abs(vapFraction - vapFracTemp) < Constants.EPSILON) && (sum < Constants.EPSILON))
                flag = false;
        }

        return vapFraction;
    }

    /**
     * Calculates the saturation (vapour) pressure at given temperature
     * using Antoine Equation.
     */
    private double[] calcPSat(double T)
    {
        double[] Psat = new double[numOfComps];

        for (int i = 0; i < numOfComps; i++)
        {
            ComponentData comp = components[i];
            Psat[i] = Constants.KPA_PER_MMHG * Math.pow(10, comp.antA - comp.antB / (comp.antC + T));
        }

        return Psat;
    }

    /**
     * Calculates the saturation temperature at given pressure
     * using Antoine Equation.
     */
    private double[] calcTSat(double P)
    {
        double[] Tsat = new double[numOfComps];

        for (int i = 0; i < numOfComps; i++)
        {
            ComponentData comp = components[i];
            Tsat[i] = comp.antB / (comp.antA - Math.log(P / Constants.KPA_PER_MMHG) / Math.log(10)) - comp.antC;
        }

        return Tsat;
    }

    /**
     * Calculates the activity coefficient depending upon the currently
     * selected activity method.
     */
    private double[] calculateActivity()
    {
        return actCalculator.calculateActivity(actMethodParams);
    }

    /**
     * Calculates the fugacity coefficient depending upon the currently
     * selected fugacity method.
     */
    private double[] calculateFugacity(double T, double P, double[] Psat)
    {
        return fugCalculator.calculateFugacity(T, P, Psat);
    }

    /**
     * Returns the instance of actual activity calculator class
     * given the method ID.
     */
    public static IActivityCalculator getActivityCalculator(int activityMethod)
    {
        IActivityCalculator calculator = null;
        String className = (String) actMethodMap.get(new Integer(activityMethod));

        try
        {
            calculator = (IActivityCalculator) Class.forName(className).newInstance();
        }
        catch (Exception ex)
        {
            System.out.println(MessageHandler.getString("errActCalcClassLoadFail") + ": " + className);
            System.exit(3);
        }

        return calculator;
    }

    /**
     * Returns the instance of actual fugacity calculator class
     * given the method ID.
     */
    public static IFugacityCalculator getFugacityCalculator(int fugacityMethod)
    {
        IFugacityCalculator calculator = null;
        String className = (String) fugMethodMap.get(new Integer(fugacityMethod));

        try
        {
            calculator = (IFugacityCalculator) Class.forName(className).newInstance();
        }
        catch (Exception ex)
        {
            System.out.println(MessageHandler.getString("errFugCalcClassLoadFail") + ": " + className);
            System.exit(3);
        }

        return calculator;
    }
}
