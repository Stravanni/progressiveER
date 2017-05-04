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

import DataStructures.Attribute;
import DataStructures.EntityProfile;

public class CensusProfile extends AbstractProfile {

    private String givenName;
    private String middleInitial;
    private String suburb;
    private String surname;
    private String zipcode;

    public CensusProfile(EntityProfile profile) {
        super(profile.getEntityUrl());

        givenName = "";
        middleInitial = "";
        suburb = "";
        surname = "";
        zipcode = "";
        for (Attribute attribute : profile.getAttributes()) {
            String an = attribute.getName().toString().trim();
            if (an.equals("Attr1")) {
                givenName = attribute.getValue();
            } else if (an.equals("Attr2")) {
                surname = attribute.getValue();
            } else if (an.equals("Attr3")) {
                middleInitial = attribute.getValue().toString().trim();
            } else if (an.equals("Attr4")) {
                zipcode = attribute.getValue().toString().trim();
            } else if (an.equals("Attr5")) {
                suburb = attribute.getValue();
            }
        }
    }

    @Override
    public String getBlockingKey(int bkId) {
        switch (bkId) {
            case 0:
                return getSurnameMetaphone() + getGivenNameMetaphone();
            case 1:
                return getSuburbMetaphone() + zipcode;
            case 2:
                return getSurnameMetaphone() + middleInitial + zipcode;
            case 3:
                return getGivenNameMetaphone() + getSuburbMetaphone();
            case 4:
                return getSuburbMetaphone() + getSurnameMetaphone();
            case 5:
                return zipcode + getGivenNameMetaphone();
            default:
                return null;
        }
    }

    @Override
    public String[] getBlockingKeys(int bkId) {
        String[] keys = new String[2];
        switch (bkId) {
            case 0:
                keys[0] = "a1#@#" + getBlockingKey(0);
                keys[1] = "a2#@#" + getBlockingKey(1);
                return keys;
            case 1:
                /*System.out.println("ok");*/
                keys[0] = "a1#@#" + getBlockingKey(4);
                keys[1] = "a2#@#" + getBlockingKey(5);
                return keys;
            default:
                return null;
        }
    }

    private String getGivenNameMetaphone() {
        String givenMetaphone = doubleMetaphone.encode(givenName.replaceAll("[^\\w]", ""));
        if (givenMetaphone != null && givenMetaphone.length() > 3) {
            givenMetaphone = givenMetaphone.substring(0, 3);
        }
        return givenMetaphone;
    }

    private String getSuburbMetaphone() {
        String subMetaphone = doubleMetaphone.encode(suburb.replaceAll("[^\\w]", ""));
        if (subMetaphone != null && subMetaphone.length() > 3) {
            subMetaphone = subMetaphone.substring(0, 3);
        }
        return subMetaphone;
    }

    private String getSurnameMetaphone() {
        String surnameMetaphone = doubleMetaphone.encode(surname.replaceAll("[^\\w]", ""));
        if (surnameMetaphone != null && surnameMetaphone.length() > 3) {
            surnameMetaphone = surnameMetaphone.substring(0, 3);
        }
        return surnameMetaphone;
    }

}
