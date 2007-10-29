/*
 * Vapour Liquid Equilibrium - Main class.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import net.sourceforge.vlejava.util.*;
import net.sourceforge.vlejava.util.chart.*;
import net.sourceforge.vlejava.vle.calc.*;

/**
 * Vapour Liquid Equilibrium - Main class.
 */
public class VLE extends VLEFrame
{
    private final int NUM_OF_POINTS = 200;
    private static VLE thisFrame = null;

    private final static int CHART_TYPE_PXY = 0;
    private final static int CHART_TYPE_TXY = 1;
    private final static int FRAC_TYPE_LIQ  = 10;
    private final static int FRAC_TYPE_VAP  = 11;
    private final static int FRAC_TYPE_MIX  = 12;

    private int             numOfComps      = 0;
    private ArrayList       compListData    = new ArrayList();
    private HashMap         groupListData   = new HashMap();
    private ArrayList       demoListData    = new ArrayList();
    private HashMap         actParamMap     = new HashMap();
    private DecimalFormat   decimalFormat   = new DecimalFormat("#.#####");
    private ParamTableModel actParamModel   = null;
    private CompTableModel  compModel       = null;
    private VLEContext      context         = null;
    private Listener        listener        = new Listener();

    private double[][] unifacData = new double[Constants.UNIFAC_GROUP_NUM + 1][Constants.UNIFAC_GROUP_NUM + 1];

    /**
     * Main method.
     * @param args - command line arguments
     */
    public static void main(String[] args)
    {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e) {}

        VLEContext.initialize();
        thisFrame = new VLE();
    }

    /**
     * Default constructor.
     */
    public VLE()
    {
        super();
        setTitle(MessageHandler.getString("titleVLE"));

        context = new VLEContext();
        loadProperties();
        initGUI();

        setVisible(true);
    }

    /**
     * This method initializes the GUI components of
     * the frame.
     */
    private void initGUI()
    {
        int i;
        ArrayList actMethods;
        ArrayList fugMethods;
        StringBuffer buffer;
        DemoData demoData = null;
        ComponentData compData = null;

        actMethods = VLEContext.getActivityMethodList();
        fugMethods = VLEContext.getFugacityMethodList();

        for (i = 0; i < actMethods.size(); i++)
        {
            actMethodList.addItem(actMethods.get(i));
        }

        for (i = 0; i < fugMethods.size(); i++)
        {
            fugMethodList.addItem(fugMethods.get(i));
        }

        compModel = new CompTableModel();
        compTable.setModel(compModel);

        actParamModel = new ParamTableModel();
        paramTable.setModel(actParamModel);
        updateParamTable();

        compTable.getTableHeader().setReorderingAllowed(false);
        paramTable.getTableHeader().setReorderingAllowed(false);
        setTableColumnWidths();

        temperature.setColumns(10);
        pressure.setColumns(10);
        temperature.setDocument(new DoubleDocument());
        pressure.setDocument(new DoubleDocument());

        ((JTextField) ((DefaultCellEditor)  compTable.getDefaultEditor(Double.class)).getComponent()).setDocument(new DoubleDocument());
        ((JTextField) ((DefaultCellEditor) paramTable.getDefaultEditor(Double.class)).getComponent()).setDocument(new DoubleDocument());

        this.addWindowListener(listener);

        BUBLPButton.addActionListener(listener);
        BUBLTButton.addActionListener(listener);
        DEWPButton.addActionListener(listener);
        DEWTButton.addActionListener(listener);
        FlashButton.addActionListener(listener);
        PxyDiagButton.addActionListener(listener);
        TxyDiagButton.addActionListener(listener);
        fillButton.addActionListener(listener);
        addCompButton.addActionListener(listener);
        removeCompButton.addActionListener(listener);
        exitMenuItem.addActionListener(listener);

        actMethodList.addItemListener(listener);
    }

    /**
     * This method loads various types of data from
     * the properties files.
     */
    private void loadProperties()
    {
        loadComponentData();
        loadDemoData();
        loadGroupData();
        loadUnifacData();
    }

    /**
     * This method loads the component data, which includes data
     * like component name, critical data, Antoine's constants, etc.
     */
    private void loadComponentData()
    {
        int count = 0;
        ComponentData data = null;
        HashMap map = null;
        ArrayList listData = null;
        ArrayList tmpData = new ArrayList();

        try
        {
            listData = CSVParser.parseCSVFile(Constants.COMP_DATA_FILE);
        }
        catch(IOException ioEx)
        {
            System.err.println(MessageHandler.getString("errFileRead") + ": "
                + Constants.COMP_DATA_FILE);
            System.exit(1);
        }

        for (count = 0; count < listData.size(); count++)
        {
            data = new ComponentData();
            map = (HashMap) listData.get(count);

            try
            {
                data.name   = (String) map.get("Name");
                data.compId = Integer.parseInt((String) map.get("ID"));
                data.Tc     = readDouble((String) map.get("Tc"));
                data.Pc     = readDouble((String) map.get("Pc")) * 100;
                data.Zc     = readDouble((String) map.get("Zc"));
                data.omega  = readDouble((String) map.get("Omega"));
                data.antA   = readDouble((String) map.get("Antoine-A"));
                data.antB   = readDouble((String) map.get("Antoine-B"));
                data.antC   = readDouble((String) map.get("Antoine-C"));

                data.init();
            }
            catch (NumberFormatException ex)
            {
                System.err.println(MessageHandler.getString("errInvalidDataFile")
                    + ": " + Constants.COMP_DATA_FILE);
                System.exit(1);
            }

            compListData.add(data);
        }
    }

    /**
     * This method loads the data for some predifined binary systems.
     * It includes parameters for various methods like Margule, Virial,
     * Redlich-Kwong, etc.
     */
    private void loadDemoData()
    {
        int count = 0;
        DemoData data = null;
        HashMap map = null;
        ArrayList listData = null;
        Iterator mapKeys = null;
        String keyName = null;

        try
        {
            listData = CSVParser.parseCSVFile(Constants.DEMO_DATA_FILE);
        }
        catch(IOException ioEx)
        {
            System.err.println(MessageHandler.getString("errFileRead") + ": "
                + Constants.DEMO_DATA_FILE);
            System.exit(1);
        }

        demoListData.clear();

        for (count = 0; count < listData.size(); count++)
        {
            data = new DemoData();
            map = (HashMap) listData.get(count);
            mapKeys = map.keySet().iterator();

            try
            {
                while (mapKeys.hasNext())
                {
                    keyName = (String) mapKeys.next();

                    if (keyName.equals("Comp1"))
                    {
                        data.compID1 = Integer.parseInt((String) map.get(keyName));
                    }
                    else if (keyName.equals("Comp2"))
                    {
                        data.compID2 = Integer.parseInt((String) map.get(keyName));
                    }
                    else
                    {
                        data.actParamMap.put(keyName, new Double((String) map.get(keyName)));
                    }
                }
            }
            catch (NumberFormatException ex)
            {
                System.err.println(MessageHandler.getString("errInvalidDataFile")
                    + ": " + Constants.DEMO_DATA_FILE);
                System.exit(1);
            }

            demoListData.add(data);
        }
    }

    /**
     * This method loads the data for list of the predefined groups
     * for UNIQUAC method.
     */
    private void loadGroupData()
    {
        int count = 0;
        GroupData data = null;
        HashMap map = null;
        ArrayList listData = null;

        try
        {
            listData = CSVParser.parseCSVFile(Constants.GROUP_DATA_FILE);
        }
        catch(IOException ioEx)
        {
            System.err.println(MessageHandler.getString("errFileRead") + ": "
                + Constants.GROUP_DATA_FILE);
            System.exit(1);
        }

        groupListData.clear();

        for (count = 0; count < listData.size(); count++)
        {
            data = new GroupData();
            map = (HashMap) listData.get(count);

            try
            {
                data.groupName  = (String) map.get("Name");
                data.uniGroupNo = Integer.parseInt((String) map.get("No"));
                data.k          = Integer.parseInt((String) map.get("k"));
                data.qk         = readDouble((String) map.get("qk"));
                data.rk         = readDouble((String) map.get("rk"));
            }
            catch (NumberFormatException ex)
            {
                System.err.println(MessageHandler.getString("errInvalidDataFile")
                    + ": " + Constants.GROUP_DATA_FILE);
                System.exit(1);
            }

            groupListData.put(data.groupName, data);
        }
    }

    /**
     * This method loads the data for UNIFAC method.
     */
    private void loadUnifacData()
    {
        int row = 0;
        int col = 0;
        HashMap map = null;
        ArrayList listData = null;
        String data = null;

        try
        {
            listData = CSVParser.parseCSVFile(Constants.UNIFAC_DATA_FILE);
        }
        catch(IOException ioEx)
        {
            System.err.println(MessageHandler.getString("errFileRead") + ": "
                + Constants.UNIFAC_DATA_FILE);
            System.exit(1);
        }

        try
        {
            for (row = 1; row <= Constants.UNIFAC_GROUP_NUM; row++)
            {
                map = (HashMap) listData.get(row - 1);

                for (col = 1; col <= Constants.UNIFAC_GROUP_NUM; col++)
                {
                    try
                    {
                        unifacData[row][col] = readDouble((String) map.get(Integer.toString(col)));
                    }
                    catch (NumberFormatException ex)
                    {
                        System.err.println(MessageHandler.getString("errInvalidDataFile")
                            + ": " + Constants.UNIFAC_DATA_FILE);
                        System.exit(1);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            System.err.println(MessageHandler.getString("errInvalidDataFile")
                    + ": " + Constants.UNIFAC_DATA_FILE);
            System.exit(1);
        }
    }

    /**
     * Special method to parse strings for double values.
     * It handles the null values.
     */
    private double readDouble(String value)
        throws NumberFormatException
    {
        if (value == null)
            return Constants.DOUBLE_NULL;
        else if (value.toLowerCase().equals(Constants.NOT_AVAILABLE))
            return Constants.DOUBLE_NULL;
        else
            return Double.parseDouble(value);
    }

    /**
     * Sets the column widths of the tables, so that it looks proper.
     */
    private void setTableColumnWidths()
    {
        final int MF_WIDTH = 85;

        int width = compScrollPane.getWidth() - 2 * MF_WIDTH - 4;
        compTable.getColumnModel().getColumn(0).setPreferredWidth(width);
        compTable.getColumnModel().getColumn(1).setPreferredWidth(MF_WIDTH);
        compTable.getColumnModel().getColumn(2).setPreferredWidth(MF_WIDTH);

        paramTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        paramTable.getColumnModel().getColumn(1).setPreferredWidth(120);
    }

    /**
     * Updates the activity parameter table.
     */
    private void updateParamTable()
    {
        int i = 0;
        Double value = null;

        IActivityCalculator calc = VLEContext.getActivityCalculator(actMethodList.getSelectedIndex());
        ArrayList paramList = calc.getParamList(numOfComps);
        actParamModel.setRowCount(0);

        for (i = 0; i < paramList.size(); i++)
        {
            value = (Double) actParamMap.get(paramList.get(i));
            actParamModel.addRow(new Object[] { paramList.get(i), value });
        }
    }

    /**
     * This method is called when any component is added or removed.
     */
    private void compDataChanged()
    {
        int i = 0;
        int count = 0;
        IActivityCalculator calc = null;
        ArrayList paramList = null;

        numOfComps = compTable.getRowCount();
        actParamMap.clear();

        if (numOfComps < 2)
        {
            BUBLPButton.setEnabled(false);
            DEWPButton.setEnabled(false);
            BUBLTButton.setEnabled(false);
            DEWTButton.setEnabled(false);
            FlashButton.setEnabled(false);
            PxyDiagButton.setEnabled(false);
            TxyDiagButton.setEnabled(false);
        }
        else
        {
            BUBLPButton.setEnabled(true);
            DEWPButton.setEnabled(true);
            BUBLTButton.setEnabled(true);
            DEWTButton.setEnabled(true);
            FlashButton.setEnabled(true);

            if (numOfComps == 2)
            {
                PxyDiagButton.setEnabled(true);
                TxyDiagButton.setEnabled(true);
            }
            else
            {
                PxyDiagButton.setEnabled(false);
                TxyDiagButton.setEnabled(false);
            }
        }

        for (count = 0; count < actMethodList.getItemCount(); count++)
        {
            calc = VLEContext.getActivityCalculator(count);
            paramList = calc.getParamList(numOfComps);

            for (i = 0; i < paramList.size(); i++)
                actParamMap.put(paramList.get(i), null);
        }
    }

    /**
     * This method is takes the values from the UI and fills the
     * VLEContext object parameters.
     */
    private void fillContextValues()
        throws VLEException
    {
        int i = 0;
        int numOfParams = 0;
        ComponentData[] comps = null;
        double[] liqMoleFracs = null;
        double[] vapMoleFracs = null;
        Double[] actParams = null;

        comps = new ComponentData[numOfComps];
        liqMoleFracs = new double[numOfComps];
        vapMoleFracs = new double[numOfComps];

        numOfParams = actParamModel.getRowCount();
        actParams = new Double[numOfParams];

        for (i = 0; i < numOfComps; i++)
        {
            comps[i] = (ComponentData) compModel.getValueAt(i, 0);

            if (compModel.getValueAt(i, 1) != null)
                liqMoleFracs[i] = ((Double) compModel.getValueAt(i, 1)).doubleValue();

            if (compModel.getValueAt(i, 2) != null)
                vapMoleFracs[i] = ((Double) compModel.getValueAt(i, 2)).doubleValue();
        }

        for (i = 0; i < numOfParams; i++)
        {
            actParams[i] = (Double) actParamModel.getValueAt(i, 1);
        }

        context.setComponents(comps);
        context.setActivityMethod(actMethodList.getSelectedIndex(), actParams);
        context.setFugacityMethod(fugMethodList.getSelectedIndex());
        context.setLiquidMoleFractions(liqMoleFracs);
        context.setVapourMoleFractions(vapMoleFracs);
    }

    /**
     * This method validates the mole fraction values in component table.
     * @param fracType Flag to indicate which fraction is to be checked
     */
    private void validateMoleFractions(int fracType)
        throws VLEException
    {
        int i = 0;
        int col = 0;
        double totFrac = 0;
        Double value = null;
        String desc = null;

        switch (fracType)
        {
            case FRAC_TYPE_LIQ:
                col = 1;
                desc = MessageHandler.getString("lblLiquid");
                break;

            case FRAC_TYPE_VAP:
                col = 2;
                desc = MessageHandler.getString("lblVapour");
                break;

            case FRAC_TYPE_MIX:
                col = 1;
                desc = MessageHandler.getString("lblMixture");
                break;
        }

        if (compTable.isEditing())
        {
            compTable.getCellEditor(compTable.getEditingRow(), compTable.getEditingColumn()).stopCellEditing();
        }

        for (i = 0; i < numOfComps; i++)
        {
            value = (Double) compModel.getValueAt(i, col);

            if (value == null)
            {
                if (fracType != FRAC_TYPE_MIX)
                    throw new VLEException(MessageHandler.getString("errMoleFracMandatory", new Object[] {desc}));
                else
                    throw new VLEException(MessageHandler.getString("errMoleFracMandatoryMix", new Object[] {desc}));
            }

            if (value.doubleValue() <= 0)
            {
                if (fracType != FRAC_TYPE_MIX)
                    throw new VLEException(MessageHandler.getString("errMoleFracNotPositive", new Object[] {desc}));
                else
                    throw new VLEException(MessageHandler.getString("errMoleFracNotPositive", new Object[] {desc})
                        + " " + MessageHandler.getString("errFillInLiqColumn"));
            }

            totFrac += value.doubleValue();
        }

        if (totFrac != 1.0)
            throw new VLEException(MessageHandler.getString("errMoleFracSumOne", new Object[] {desc}));
    }

    /**
     * This method is validates the activity parameter values.
     */
    private void validateActParams()
        throws VLEException
    {
        int i = 0;
        int numOfParams = 0;

        numOfParams = paramTable.getRowCount();

        if (paramTable.isEditing())
        {
            paramTable.getCellEditor(paramTable.getEditingRow(), paramTable.getEditingColumn()).stopCellEditing();
        }

        for (i = 0; i < numOfParams; i++)
        {
            if (actParamModel.getValueAt(i, 1) == null)
                throw new VLEException(MessageHandler.getString("errMoleFracSumOne"));
        }
    }

    /**
     * This method displays the dialog for new component addition in the system.
     */
    private void addComponent()
    {
        int i = 0;
        ComponentData compData = null;

        Object[] outValues = InputDialog.show(this, MessageHandler.getString("VLE"),
            MessageHandler.getString("msgSelectComp") + ":", compListData.toArray(), true);
        if ((outValues == null) || (outValues.length == 0)) return;

        for (i = 0; i < outValues.length; i++)
        {
            compData = (ComponentData) outValues[i];
            compModel.addRow(new Object[] { compData, new Double(0), new Double(0) });
        }

        compDataChanged();
        updateParamTable();
    }

    /**
     * This method removes the currently selected component from the system.
     */
    private void removeComponent()
    {
        int i = 0;
        int[] rows = compTable.getSelectedRows();

        if (rows.length == 0) return;
        Arrays.sort(rows);

        for (i = 0; i < rows.length; i++)
        {
            compModel.removeRow(rows[i] - i);
        }

        compDataChanged();
        updateParamTable();
    }

    /**
     * This method is called when demo system is selected.
     */
    private void fillDemoData()
    {
        int i;
        DemoData demoData = null;
        StringBuffer buffer = null;
        IDValue selectedValue = null;

        int numOfDemos = demoListData.size();
        IDValue[] list = new IDValue[numOfDemos];

        for (i = 0; i < numOfDemos; i++)
        {
            buffer = new StringBuffer("");
            demoData = (DemoData) demoListData.get(i);

            buffer.append(((ComponentData) compListData.get(demoData.compID1)).name);
            buffer.append(" / ");
            buffer.append(((ComponentData) compListData.get(demoData.compID2)).name);

            list[i] = new IDValue(i, buffer.toString());
        }

        Object[] outValue = InputDialog.show(this, MessageHandler.getString("VLE"),
            MessageHandler.getString("msgSelectDemoSys") + ":", list, false);
        if ((outValue == null) || (outValue.length == 0)) return;

        selectedValue = (IDValue) outValue[0];

        compModel.setRowCount(0);
        demoData = (DemoData) demoListData.get(selectedValue.ID);
        compModel.addRow(new Object[] { compListData.get(demoData.compID1), new Double(0), new Double(0) });
        compModel.addRow(new Object[] { compListData.get(demoData.compID2), new Double(0), new Double(0) });

        compDataChanged();
        actParamMap.putAll(demoData.actParamMap);
        updateParamTable();
    }

    /**
     * Method for BUBL P calculation.
     */
    private void calcBUBLP()
    {
        int i = 0;
        double T = 0;
        double[] vapFracs = null;
        String P = null;
        StringBuffer buffer = null;

        try
        {
            validateMoleFractions(FRAC_TYPE_LIQ);
            validateActParams();

            if ((temperature.getText() == null) || (temperature.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterBUBLPTemp"));

            fillContextValues();

            T = Double.parseDouble(temperature.getText());
            P = decimalFormat.format(context.calcBUBLP(T));
            vapFracs = context.getVapourMoleFractions();

            buffer = new StringBuffer("");
            buffer.append("<html><font face='Arial, Sans-Serif' size=-1>");
            buffer.append("<b>BUBL P</b> = ").append(P).append(" ").append(MessageHandler.getString("lblkPa"));
            buffer.append("<br><br>").append(MessageHandler.getString("lblVapCompMoleFracs")).append(": <ul>");

            for (i = 0; i < numOfComps; i++)
            {
                buffer.append("<li>").append(compModel.getValueAt(i, 0)).append(" = ");
                buffer.append(decimalFormat.format(vapFracs[i])).append("</li>");
            }

            buffer.append("</ul></font></html>");

            JOptionPane.showMessageDialog(this, buffer.toString(), MessageHandler.getString("msgVLECalc"), JOptionPane.INFORMATION_MESSAGE);

            pressure.setText(P);

            for (i = 0; i < numOfComps; i++)
                compModel.setValueAt(new Double(decimalFormat.format(vapFracs[i])), i, 2);
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * Method for BUBL T calculation.
     */
    private void calcBUBLT()
    {
        int i = 0;
        double P = 0;
        double[] vapFracs = null;
        String T = null;
        StringBuffer buffer = null;

        try
        {
            validateMoleFractions(FRAC_TYPE_LIQ);
            validateActParams();

            if ((pressure.getText() == null) || (pressure.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterBUBLTPres"));

            fillContextValues();

            P = Double.parseDouble(pressure.getText());
            T = decimalFormat.format(context.calcBUBLT(P));
            vapFracs = context.getVapourMoleFractions();

            buffer = new StringBuffer("");
            buffer.append("<html><font face='Arial, Sans-Serif' size=-1>");
            buffer.append("<b>BUBL T</b> = ").append(T).append(" ").append(MessageHandler.getString("lblDegC"));
            buffer.append(" <br><br>").append(MessageHandler.getString("lblVapCompMoleFracs")).append(": <ul>");

            for (i = 0; i < numOfComps; i++)
            {
                buffer.append("<li>").append(compModel.getValueAt(i, 0)).append(" = ");
                buffer.append(decimalFormat.format(vapFracs[i])).append("</li>");
            }

            buffer.append("</ul></font></html>");

            JOptionPane.showMessageDialog(this, buffer.toString(), MessageHandler.getString("msgVLECalc"), JOptionPane.INFORMATION_MESSAGE);

            temperature.setText(T);

            for (i = 0; i < numOfComps; i++)
                compModel.setValueAt(new Double(decimalFormat.format(vapFracs[i])), i, 2);
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * Method for DEW P calculation.
     */
    private void calcDEWP()
    {
        int i = 0;
        double T = 0;
        double[] liqFracs = null;
        String P = null;
        StringBuffer buffer = null;

        try
        {
            validateMoleFractions(FRAC_TYPE_VAP);
            validateActParams();

            if ((temperature.getText() == null) || (temperature.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterDEWPTemp"));

            fillContextValues();

            T = Double.parseDouble(temperature.getText());
            P = decimalFormat.format(context.calcDEWP(T));
            liqFracs = context.getLiquidMoleFractions();

            buffer = new StringBuffer("");
            buffer.append("<html><font face='Arial, Sans-Serif' size=-1>");
            buffer.append("<b>DEW P</b> = ").append(P).append(" ").append(MessageHandler.getString("lblkPa"));
            buffer.append("<br><br>").append(MessageHandler.getString("lblLiqCompMoleFracs")).append(": <ul>");

            for (i = 0; i < numOfComps; i++)
            {
                buffer.append("<li>").append(compModel.getValueAt(i, 0)).append(" = ");
                buffer.append(decimalFormat.format(liqFracs[i])).append("</li>");
            }

            buffer.append("</ul></font></html>");

            JOptionPane.showMessageDialog(this, buffer.toString(), MessageHandler.getString("msgVLECalc"), JOptionPane.INFORMATION_MESSAGE);

            pressure.setText(P);

            for (i = 0; i < numOfComps; i++)
                compModel.setValueAt(new Double(decimalFormat.format(liqFracs[i])), i, 1);
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * Method for DEW T calculation.
     */
    private void calcDEWT()
    {
        int i = 0;
        double P = 0;
        double[] liqFracs = null;
        String T = null;
        StringBuffer buffer = null;

        try
        {
            validateMoleFractions(FRAC_TYPE_VAP);
            validateActParams();

            if ((pressure.getText() == null) || (pressure.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterDEWTPres"));

            fillContextValues();

            P = Double.parseDouble(pressure.getText());
            T = decimalFormat.format(context.calcDEWT(P));
            liqFracs = context.getLiquidMoleFractions();

            buffer = new StringBuffer("");
            buffer.append("<html><font face='Arial, Sans-Serif' size=-1>");
            buffer.append("<b>DEW T</b> = ").append(T).append(" ").append(MessageHandler.getString("lblDegC"));
            buffer.append("<br><br>").append(MessageHandler.getString("lblLiqCompMoleFracs")).append(": <ul>");

            for (i = 0; i < numOfComps; i++)
            {
                buffer.append("<li>").append(compModel.getValueAt(i, 0)).append(" = ");
                buffer.append(decimalFormat.format(liqFracs[i])).append("</li>");
            }

            buffer.append("</ul></font></html>");

            JOptionPane.showMessageDialog(this, buffer.toString(), MessageHandler.getString("msgVLECalc"), JOptionPane.INFORMATION_MESSAGE);

            temperature.setText(T);

            for (i = 0; i < numOfComps; i++)
                compModel.setValueAt(new Double(decimalFormat.format(liqFracs[i])), i, 1);
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * Calculates the flash point compositions at given T and P.
     */
    private void calcFlashPoint()
    {
        int i = 0;
        double P = 0;
        double T = 0;
        double vapFraction = 0;
        double[] liqFracs = null;
        double[] vapFracs = null;
        StringBuffer buffer = null;

        try
        {
            validateMoleFractions(FRAC_TYPE_MIX);
            validateActParams();

            if ((pressure.getText() == null) || (pressure.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterFlashPres"));

            if ((temperature.getText() == null) || (temperature.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterFlashTemp"));

            fillContextValues();
            P = Double.parseDouble(pressure.getText());
            T = Double.parseDouble(temperature.getText());

            vapFraction = context.calcFlashPoint(P, T);
            liqFracs = context.getLiquidMoleFractions();
            vapFracs = context.getVapourMoleFractions();

            buffer = new StringBuffer("");
            buffer.append("<html><font face='Arial, Sans-Serif' size=-1><b>");
            buffer.append(MessageHandler.getString("lblVapFraction")).append("</b> = ");
            buffer.append(decimalFormat.format(vapFraction)).append("<br><br>");
            buffer.append(MessageHandler.getString("lblLiqCompMoleFracs")).append(": <ul>");

            for (i = 0; i < numOfComps; i++)
            {
                buffer.append("<li>").append(compModel.getValueAt(i, 0)).append(" = ");
                buffer.append(decimalFormat.format(liqFracs[i])).append("</li>");
            }

            buffer.append("</ul><br>").append(MessageHandler.getString("lblLiqCompMoleFracs")).append(": <ul>");

            for (i = 0; i < numOfComps; i++)
            {
                buffer.append("<li>").append(compModel.getValueAt(i, 0)).append(" = ");
                buffer.append(decimalFormat.format(vapFracs[i])).append("</li>");
            }

            buffer.append("</ul></font></html>");
            JOptionPane.showMessageDialog(this, buffer.toString(), MessageHandler.getString("msgVLECalc"), JOptionPane.INFORMATION_MESSAGE);

            for (i = 0; i < numOfComps; i++)
            {
                compModel.setValueAt(new Double(decimalFormat.format(liqFracs[i])), i, 1);
                compModel.setValueAt(new Double(decimalFormat.format(vapFracs[i])), i, 2);
            }
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * Method for P-xy diagram.
     */
    private void preparePxyDiagram()
    {
        int i = 0;
        double T = 0;

        try
        {
            validateActParams();

            if ((temperature.getText() == null) || (temperature.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterPXYTemp"));

            T = Double.parseDouble(temperature.getText());

            fillContextValues();
            plotChart(CHART_TYPE_PXY, T);
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * Method for T-xy diagram.
     */
    private void prepareTxyDiagram()
    {
        int i = 0;
        double P = 0;

        try
        {
            validateActParams();

            if ((pressure.getText() == null) || (pressure.getText().trim().equals("")))
                throw new VLEException(MessageHandler.getString("errEnterTXYPres"));

            P = Double.parseDouble(pressure.getText());

            fillContextValues();
            plotChart(CHART_TYPE_TXY, P);
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    /**
     * This method plots the Pxy or Txy diagram depending upon the values.
     */
    private void plotChart(int chartType, double PorT)
    {
        int i = 0;
        boolean diffGoingDown = false;
        boolean azeoExists = false;
        double BUBL = 0;
        double DEW = 0;
        double prevDiff = 1E10;
        double diff = 1E10;
        double[] liqMoleFracs = null;
        double[] vapMoleFracs = null;
        Dataset bublSet = null;
        Dataset dewSet = null;
        String comp1 = null;
        String comp2 = null;
        Point2D.Double azeotrope = new Point2D.Double();
        Point2D.Double x0 = new Point2D.Double();
        Point2D.Double x1 = new Point2D.Double();

        bublSet = new Dataset(NUM_OF_POINTS + 1);
        dewSet  = new Dataset(NUM_OF_POINTS + 1);

        bublSet.setLineColor(Color.red);
        dewSet.setLineColor(Color.green);

        for(i = 0; i <= NUM_OF_POINTS; i++)
        {
            liqMoleFracs = new double[] { (double) i / NUM_OF_POINTS, 1 - (double) i / NUM_OF_POINTS };
            vapMoleFracs = new double[] { (double) i / NUM_OF_POINTS, 1 - (double) i / NUM_OF_POINTS };

            context.setLiquidMoleFractions(liqMoleFracs);
            BUBL = (chartType == CHART_TYPE_PXY) ? context.calcBUBLP(PorT) : context.calcBUBLT(PorT);
            bublSet.setValue(i, liqMoleFracs[0], BUBL);

            context.setVapourMoleFractions(vapMoleFracs);
            DEW = (chartType == CHART_TYPE_PXY) ? context.calcDEWP(PorT) : context.calcDEWT(PorT);
            dewSet.setValue(i, vapMoleFracs[0], DEW);

            if (i == 0)
                x0.setLocation(liqMoleFracs[0], BUBL);

            if (i == NUM_OF_POINTS)
                x1.setLocation(liqMoleFracs[0], BUBL);

            diff = Math.abs(BUBL - DEW);

            if ((i != 0) && (i != NUM_OF_POINTS))
            {
                if ((!azeoExists) && (diff < prevDiff))
                {
                    diffGoingDown = true;
                    azeotrope.setLocation(vapMoleFracs[0], DEW);
                }
                else if (diffGoingDown)
                {
                    azeoExists = true;
                }
            }

            prevDiff = diff;
        }

        comp1 = ((ComponentData) compModel.getValueAt(0, 0)).name;
        comp2 = ((ComponentData) compModel.getValueAt(1, 0)).name;

        Chart chart = new Chart();
        chart.addDataset(bublSet);
        chart.addDataset(dewSet);
        chart.setTitle(MessageHandler.getString("lbl" + ((chartType == CHART_TYPE_PXY) ? "P" : "T") + "XYDiagram"));
        chart.setSubTitle(comp1 + "(1) / " + comp2 + "(2)");
        chart.setXAxisLabel("x, y (" + comp1 + ")");
        chart.setYAxisLabel((chartType == CHART_TYPE_PXY) ? "P (" + MessageHandler.getString("lblkPa") + ")"
				: "T (" + MessageHandler.getString("lblDegC") + ")");

        chart.hilitePoint(x0, Chart.AXIS_NONE, Chart.AXIS_LEFT );
        chart.hilitePoint(x1, Chart.AXIS_NONE, Chart.AXIS_RIGHT);

        if (azeoExists)
        {
            chart.hilitePoint(azeotrope, Chart.AXIS_BOTTOM, Chart.AXIS_LEFT);
        }

        new ChartDialog(chart);
    }

    /**
     * This method exits the application.
     */
    private void close()
    {
        System.exit(0);
    }

    /**
     * This method handles all the exceptions in the application.
     */
    public static void handleException(Exception ex)
    {
        String msg = ex.getMessage();

        if ((msg == null) || (msg.trim().equals("")))
        {
            StringWriter writer = new StringWriter();

            ex.printStackTrace(new PrintWriter(writer));
            msg = writer.toString();
        }

        JOptionPane.showMessageDialog(thisFrame, msg, MessageHandler.getString("msgError"), JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Common listener class.
     */
    class Listener implements ActionListener, ItemListener, WindowListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            String action = ev.getActionCommand();

            if (action.equals("DEMO_SYS"))
            {
                fillDemoData();
            }
            else if (action.equals("ADD_COMP"))
            {
                addComponent();
            }
            else if (action.equals("REMOVE_COMP"))
            {
                removeComponent();
            }
            else if (action.equals("BUBLP"))
            {
                calcBUBLP();
            }
            else if (action.equals("DEWP"))
            {
                calcDEWP();
            }
            else if (action.equals("BUBLT"))
            {
                calcBUBLT();
            }
            else if (action.equals("DEWT"))
            {
                calcDEWT();
            }
            else if (action.equals("PXY_DIAG"))
            {
                preparePxyDiagram();
            }
            else if (action.equals("TXY_DIAG"))
            {
                prepareTxyDiagram();
            }
            else if (action.equals("FLASH"))
            {
                calcFlashPoint();
            }
            else if (action.equals("EXIT"))
            {
                close();
            }
        }

        public void itemStateChanged(ItemEvent ev)
        {
            if ((ev.getStateChange() == ItemEvent.SELECTED) &&
                (ev.getItemSelectable().equals(actMethodList)))
            {
                updateParamTable();
            }
        }

        public void windowClosing(WindowEvent ev)
        {
            close();
        }

        public void windowActivated(WindowEvent ev) {}
        public void windowClosed(WindowEvent ev) {}
        public void windowDeactivated(WindowEvent ev) {}
        public void windowDeiconified(WindowEvent ev) {}
        public void windowIconified(WindowEvent ev) {}
        public void windowOpened(WindowEvent ev) {}
    }

    /**
     * TableModel implementation for activity parameters table.
     */
    class ParamTableModel extends DefaultTableModel
    {
        public ParamTableModel()
        {
            super(new Object[] { MessageHandler.getString("lblParameter"), MessageHandler.getString("lblValue") }, 0);
        }

        public Class getColumnClass(int col)
        {
            switch(col)
            {
                case 0:
                    return String.class;

                case 1:
                    return Double.class;

                default:
                    return Object.class;
            }
        }

        public boolean isCellEditable(int row, int col)
        {
            return (col == 1);
        }
    }

    /**
     * TableModel implementation for components table.
     */
    class CompTableModel extends DefaultTableModel
    {
        public CompTableModel()
        {
            super(new Object[] { MessageHandler.getString("lblComponent"),
                MessageHandler.getString("lblLiqMoleFrac"), MessageHandler.getString("lblVapMoleFrac") }, 0);
        }

        public Class getColumnClass(int col)
        {
            switch(col)
            {
                case 0:
                    return Object.class;

                case 1:
                case 2:
                    return Double.class;

                default:
                    return Object.class;
            }
        }

        public boolean isCellEditable(int row, int col)
        {
            return (col != 0);
        }
    }
}
