/*
 * Dialog for displaying chart.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import com.sun.image.codec.jpeg.*;
import osbaldeston.image.BMP;
import com.keypoint.PngEncoder;
import net.sourceforge.vlejava.util.*;
import net.sourceforge.vlejava.util.chart.*;

/**
 * Dialog for displaying chart.
 */
public class ChartDialog extends JDialog
    implements ActionListener, AWTEventListener
{
    private final int BMP_FORMAT = 0;
    private final int JPG_FORMAT = 1;
    private final int PNG_FORMAT = 2;
    private Chart chart = null;

    public ChartDialog(Chart chart)
    {
        this.chart = chart;
        setTitle(MessageHandler.getString("chartTitle"));
        getContentPane().add(chart);
        setSize(600, 500);

        JMenuBar menuBar = null;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menu = new JMenu(MessageHandler.getString("menuFile"));
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menuItem = new JMenuItem(MessageHandler.getString("menuSaveAsImage"), KeyEvent.VK_S);
        menuItem.setActionCommand("SAVE");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ev)
    {
        String action = ev.getActionCommand();

        try
        {
            if (action.equals("SAVE"))
            {
                saveAsImage();
            }
        }
        catch (Exception ex)
        {
            VLE.handleException(ex);
        }
    }

    public void eventDispatched(AWTEvent event)
    {
        KeyEvent ev = (KeyEvent) event;

        if (ev.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            dispose();
        }
    }

    private void saveAsImage()
        throws Exception
    {
        int retval = 0;
        String name = null;
        File file = null;
        JFileChooser chooser = new JFileChooser();
        BasicFileFilter jpgFilter = new BasicFileFilter(".jpg", MessageHandler.getString("filterJPEG"));
        BasicFileFilter bmpFilter = new BasicFileFilter(".bmp", MessageHandler.getString("filterBMP"));
        BasicFileFilter pngFilter = new BasicFileFilter(".png", MessageHandler.getString("filterPNG"));
        BasicFileFilter currFilter = null;

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(bmpFilter);
        chooser.addChoosableFileFilter(jpgFilter);
        chooser.addChoosableFileFilter(pngFilter);

        retval = chooser.showSaveDialog(this);

        if (retval == JFileChooser.APPROVE_OPTION)
        {
            file = chooser.getSelectedFile();
            name = file.getName();
            currFilter = (BasicFileFilter) chooser.getFileFilter();

            if (name.indexOf(".") == -1)
            {
                if (currFilter.equals(bmpFilter))
                {
                    file = new File(file.getAbsolutePath() + ".bmp");
                }
                else if (currFilter.equals(jpgFilter))
                {
                    file = new File(file.getAbsolutePath() + ".jpg");
                }
                else if (currFilter.equals(pngFilter))
                {
                    file = new File(file.getAbsolutePath() + ".png");
                }
            }

            if (file.exists())
            {
                retval = JOptionPane.showConfirmDialog(this, file.getName() + ": " +
                    MessageHandler.getString("errFileExists"),
					MessageHandler.getString("msgFileExists"),
                    JOptionPane.YES_NO_OPTION);

                if (retval != JOptionPane.YES_OPTION)
                    return;
            }

            if (currFilter.equals(bmpFilter))
            {
                saveChart(file, BMP_FORMAT);
            }
            else if (currFilter.equals(jpgFilter))
            {
                saveChart(file, JPG_FORMAT);
            }
            else if (currFilter.equals(pngFilter))
            {
                saveChart(file, PNG_FORMAT);
            }
        }
    }

    private void saveChart(File file, int format)
        throws Exception
    {
        BufferedImage image = null;
        OutputStream out = null;
        JPEGImageEncoder jpgEncoder = null;
        JPEGEncodeParam param = null;
        PngEncoder pngEncoder = null;
        BMP bmp = null;

        image = chart.getChartImage();

        switch(format)
        {
            case BMP_FORMAT:
                bmp = new BMP(image);
                bmp.write(file);
                break;

            case JPG_FORMAT:
                out = new BufferedOutputStream(new FileOutputStream(file));
                jpgEncoder = JPEGCodec.createJPEGEncoder(out);

                param = jpgEncoder.getDefaultJPEGEncodeParam(image);
                param.setQuality(1.0f, false);
                jpgEncoder.setJPEGEncodeParam(param);

                jpgEncoder.encode(image);
                out.close();
                break;

            case PNG_FORMAT:
                out = new BufferedOutputStream(new FileOutputStream(file));
                pngEncoder =  new PngEncoder(image, PngEncoder.NO_ALPHA, 0 /* filter */, 1 /* compressionLevel */);

                out.write(pngEncoder.pngEncode());
                out.close();
                break;
        }
    }
}
