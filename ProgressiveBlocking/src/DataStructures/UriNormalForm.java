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

package DataStructures;

import java.io.Serializable;

/**
 * created on 10.12.2009
 * by gap2
 */

public class UriNormalForm implements Serializable {

    static final long serialVersionUID = 407040964912912345L;

    private String infix;
    private String prefix;
    private String suffix;

    public UriNormalForm () {
        infix = "";
        prefix = "";
        suffix = "";
    }

    public String getInfix() {
        return infix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return "\nPrefix\t:\t" + prefix +
               "\nInfix\t:\t" + infix +
               "\nSuffix\t:\t" + suffix;
    }
}