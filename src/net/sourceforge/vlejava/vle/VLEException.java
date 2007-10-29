/*
 * Generic exception class for VLE.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.vle;

/**
 * Generic exception class for VLE.
 */
public class VLEException extends Exception
{
    public VLEException()
    {
        super();
    }

    public VLEException(String message)
    {
        super(message);
    }
}
