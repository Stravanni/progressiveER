/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */

package Utilities;

import java.io.*;

/**
 *
 * @author gap2
 */

public class SerializationUtilities {

    public static Object loadSerializedObject(String fileName) {
        Object object = null;
        try {
            InputStream file = new FileInputStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                object = input.readObject();
            } finally {
                input.close();
            }
        } catch (ClassNotFoundException cnfEx) {
            System.err.println(fileName);
            cnfEx.printStackTrace();
        } catch (IOException ioex) {
            System.err.println(fileName);
            ioex.printStackTrace();
        }

        return object;
    }

    public static Object loadSerializedObjectWithExceptions(String fileName) throws Exception {
        InputStream file = new FileInputStream(fileName);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);
        Object object = input.readObject();
        input.close();

        return object;
    }

    public static void storeSerializedObject(Object object, String outputPath) {
        try {
            OutputStream file = new FileOutputStream(outputPath);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            try {
                output.writeObject(object);
            } finally {
                output.close();
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }
}