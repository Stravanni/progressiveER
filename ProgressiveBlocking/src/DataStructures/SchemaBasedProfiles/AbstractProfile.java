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

package DataStructures.SchemaBasedProfiles;

import org.apache.commons.codec.language.DoubleMetaphone;

public abstract class AbstractProfile {

    private final String entityUrl;
    protected final DoubleMetaphone doubleMetaphone;

    public AbstractProfile(String url) {
        entityUrl = url;
        doubleMetaphone = new DoubleMetaphone();
    }

    public abstract String getBlockingKey(int bkId);

    public abstract String[] getBlockingKeys(int bkId);

    public String getEntityUrl() {
        return entityUrl;
    }
}