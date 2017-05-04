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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */

public class FileUtilities {
    
    public static List<String> getFileLines(String filePath) throws Exception {
        final List<String> lines = new ArrayList<String>();

        final BufferedReader reader = new BufferedReader(new FileReader(filePath));
        for(String line; (line = reader.readLine()) != null; ) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }
}