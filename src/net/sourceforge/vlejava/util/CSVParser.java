/*
 * CSV Parsing utilities.
 *
 * Author: Samir Vaidya (mailto: syvaidya@yahoo.com)
 * Copyright (c) Samir Vaidya
 */

package net.sourceforge.vlejava.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * This class has methods to parse comma separated values
 * and generate data structure for the same.
 */
public class CSVParser
{
    /**
     * This method parses the given file as a CSV and returns the
     * arraylist of hashmaps.
     *
     * @param fileName Name of the file
     * @return ArrayList of HashMaps containing the fields of each row
     */
    public static ArrayList parseCSVFile(String fileName)
        throws IOException, FileNotFoundException
    {
        String lineRead = null;
        StringBuffer buffer = new StringBuffer("");
        FileInputStream fis = null;

        try
        {
            FileReader reader = new FileReader(fileName);
            BufferedReader buffReader = new BufferedReader(reader);

            while ((lineRead = buffReader.readLine()) != null)
            {
                buffer.append(lineRead).append("\n");
            }
        }
        finally
        {
            try
            {
                if (fis != null) fis.close();
            }
            catch(Exception ex)
            {
            }
        }

        return parseCSV(buffer.toString());
    } // parseCSVFile

    /**
     * This method takes a String of Comma seperated data (individual rows
     * seperated by newline i.e. '\n' and returns an ArrayList of HashMaps
     * containing the fields of each individual row.
     *
     * @param csvString String object containing data in CSV format
     * @return ArrayList of HashMaps containing the fields of each row
     */
    public static ArrayList parseCSV(String csvString)
    {
        String[] listOfRows = null;  // String array for the rows in the data
        String[] rowValues = null;   // String array for values in each row
        String[] valueNames = null;  // String array for names of the columns
        int numOfRows = 0;           // The number of rows of data
        int numOfCols = 0;           // The number of columns in the data
        int count = 0;               // Loop variable
        int innerCount = 0;          // Loop variable
        int columnCount = 0;         // Loop variable
        HashMap rowValueMap = null;  // Hashmap for a row of data
        ArrayList listOfMaps = null; // ArrayList of Hashmaps of data rows

        // Get array of the individual rows of the data in CSV format Strings
        listOfRows = splitString(csvString, "\n");

        // Get an Array of Strings for the names of columns of data
        valueNames = getArrayOfRows(listOfRows[0]);

        // Get the number of columns of data
        numOfCols = valueNames.length;

        // Get the number of rows of data
        numOfRows = listOfRows.length;

        listOfMaps = new ArrayList();

        // Loop throught the individual rows of data
        for (count = 1; count < numOfRows ; count ++)
        {
            // Get the Array of Strings of the values in each field of the row
            rowValues = getArrayOfRows(listOfRows[count]);
            columnCount = rowValues.length;
            rowValueMap = new HashMap();

            // Loop through each of the fields in each row
            for (innerCount = 0; innerCount < columnCount; innerCount ++)
            {
                // Add each field to the HashMap, using the field name as key
                rowValueMap.put(valueNames[innerCount], rowValues[innerCount]);
            }

            // Add the Hashmap of fields in each row to the ArrayList of rows
            listOfMaps.add(rowValueMap);
        }

        return listOfMaps;
    } // parseCSV


    /**
     * This method takes a String of Comma seperated data treating it
     * as a single row of data.
     *
     * @param csvString String object representing data of one single row in
     *        CSV format
     * @return String array with the values of the fields in each row
     */
    private static String[] getArrayOfRows(String inputString)
    {
        final String CSV_DELIMITER = "~";  // Temporary CSV delimiter

        int inputLen = 0;                  // String length
        int numOfCols = 0;                 // Number of columns
        int count = 0;                     // Loop variable
        boolean isQuoteCountEven = true;   // Quote count even or not
        char parsedChar = '\0';            // Temporary variable
        String parseableString = null;     // Temporary variable
        StringBuffer outBuffer = null;     // Temporary variable
        String csvValues[] = null;         // Array of Strings for the CSVs

        // Get the number of bytes in the String of comma seperated values
        inputLen = inputString.length();
        outBuffer =  new StringBuffer("");

        // Loop throught the individual characters in the String
        for (count = 0 ; count < inputLen ; count++)
        {
            // Get the character
            parsedChar = inputString.charAt(count);

            // If the character is a comma and seperator of data
            // i.e. not the part of a String within double quotes
            if ((parsedChar == ',') && isQuoteCountEven)
            {
                // Add the designated delimiter in place of the comma
                outBuffer.append(CSV_DELIMITER);
            }
            // Else if the character is a double quote
            else if (parsedChar == '\"')
            {
                // Reverse the flag value
                isQuoteCountEven = ! isQuoteCountEven;

                // Append the character to the temporary StringBuffer
                outBuffer.append(parsedChar);
            }
            else
            {
                // Append the character to the temporary StringBuffer
                outBuffer.append(parsedChar);
            }
        }

        parseableString = outBuffer.toString();

        // If first element is comma then it will give problems so pefix space
        if (parseableString.charAt(0) == '~')
        {
            parseableString = " " + parseableString;
        }

        // If last ellement then also append
        if (parseableString.charAt(parseableString.length() - 1) == '~')
        {
            parseableString = parseableString + " ";
        }

        // Replace consecutive delimiters with space seperated delimiters
        // (function ignores consecutive delimiters )
        parseableString = replaceTag(parseableString, "~~", "~ ~");

        // Another replace to handle two consecutive blanks
        parseableString = replaceTag(parseableString, "~~", "~ ~");

        // Obtain the CSVs from the string into an Array of Strings
        csvValues = splitString(parseableString, CSV_DELIMITER);

        // Get the number of columns of data
        numOfCols = csvValues.length;

        // Loop through each of the fields in the row
        for (count = 0; count < numOfCols; count ++)
        {
            // Call method to create usable strings from raw string obtained
            // from the String of Comma Seperated Values
            csvValues[count] = removeCSVFormatting(csvValues[count]);
        }

        return csvValues;
    } // getArrayOfRows

    /**
     * This method takes a String and returns it minus some formatting
     * elements needed to represent data as Comma Seperated Values
     *
     * @param untrimmedString String with CSV formatting
     * @return String without CSV formatting
     */
    private static String removeCSVFormatting(String untrimmedString)
    {
        String strTrimmed = null;   // Variable used for string manipulation

        // Replace '""' with '"' (excel format double quotes)
        strTrimmed = replaceTag(untrimmedString, "\"\"", "\"");

        // Remove extra spaces
        strTrimmed = strTrimmed.trim();

        // If string is bounded by '"' remove leading and trailing '"'s
        if ((strTrimmed.length() != 0)
            && (strTrimmed.charAt(0) == '\"')
            && (strTrimmed.charAt(strTrimmed.length() - 1) == '\"'))
        {
            strTrimmed = strTrimmed.substring(1, strTrimmed.length() - 1);
        }

        // Return the trimmed string
        return strTrimmed;

    } // removeCSVFormatting

    /**
     * Splits a String into pieces according to a delimiter.
     *
     * @param str The string to split
     * @param delim The delimiter
     * @return an array of strings containing the pieces
     */
    private static String[] splitString(String str, String delim)
    {
        // Use ArrayList to hold the split strings
        ArrayList list = new ArrayList();

        // Use a StringTokenizer to do the splitting
        StringTokenizer tokenizer = new StringTokenizer(str, delim);

        while (tokenizer.hasMoreTokens())
        {
            list.add(tokenizer.nextToken());
        }

        String[] ret = new String[list.size()];

        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = (String) list.get(i);
        }

        return ret;
    } // splitString

    /**
     * This method replaces the a given tag in the String
     * with another specified string.
     */
    private static String replaceTag(String container, String tag,
                                        String replacement)
    {
        final String BLANK = "";
        StringBuffer buffer = new StringBuffer("");
        int start = 0;
        int end = 0;

        // If null or blank return null
        if (container == null || container.trim().length () == 0)
        {
            return container;
        }

        // If replacement tag is null then substitute it with null string
        if (replacement == null)
        {
            replacement = BLANK;
        }

        // Loop through container to find and replace tag
        end = container.indexOf(tag);

        while (end != -1)
        {
            buffer.append(container.substring(start, end));
            buffer.append(replacement);
            start = end + tag.length();
            end = container.indexOf(tag, start);
        }

        buffer.append(container.substring(start));
        return buffer.toString();

    } // replaceTag

} // class
