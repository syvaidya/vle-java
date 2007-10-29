/*
 * Localized message handler for VLE.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;


/**
 * Localized message handler for VLE.
 */
public class MessageHandler
{
    static ResourceBundle messages = null;

    static
    {
        messages = ResourceBundle.getBundle("net.sourceforge.vlejava.message.MessagesBundle", Locale.getDefault());
    }

    public static String getString(String key)
    {
        return messages.getString(key);
    }

    public static String getString(String key, Object[] parameters)
    {
        return MessageFormat.format(messages.getString(key), parameters);
    }
}
