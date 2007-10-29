/*
 * Dataset class for charting tool.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.util.chart;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * Dataset class for charting tool.
 */
public class Dataset
{
    private Color lineColor = Color.green;
    private Point2D.Double[] ptArray = null;
    private double minX = +1E10;
    private double minY = +1E10;
    private double maxX = -1E10;
    private double maxY = -1E10;

    public Dataset(int numOfPoints)
    {
        ptArray = new Point2D.Double[numOfPoints];
    }

    public int getNumOfPoints()
    {
        return ptArray.length;
    }

    public Point2D.Double getValue(int index)
    {
        return ptArray[index];
    }

    public void setValue(int index, double x, double y)
    {
        if (ptArray[index] == null)
            ptArray[index] = new Point2D.Double();

        ptArray[index].setLocation(x, y);

        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
    }

    public Color getLineColor()
    {
        return lineColor;
    }

    public void setLineColor(Color lineColor)
    {
        this.lineColor = lineColor;
    }

    public double getMinX()
    {
        return minX;
    }

    public double getMaxX()
    {
        return maxX;
    }

    public double getMinY()
    {
        return minY;
    }

    public double getMaxY()
    {
        return maxY;
    }
}
