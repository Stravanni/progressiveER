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

/**
 *
 * @author gap2
 */

public class SyntheticProfile extends AbstractProfile {
    
    private String address;
    private String age;
    private String givenName;
    private String postcode;
    private String suburb;
    private String surname;

    public SyntheticProfile(EntityProfile profile) {
        super(profile.getEntityUrl());

        address = "";
        age = "";
        givenName = "";
        postcode = "";
        suburb = "";
        surname = "";
        for (Attribute attribute : profile.getAttributes()) {
            String an = attribute.getName().trim();
            if (an.equals("address_1")) {
                address = attribute.getValue();
            } else if (an.equals("age")) {
                age = attribute.getValue().trim();
            } else if (an.equals("given_name")) {
                givenName = attribute.getValue();
            } else if (an.equals("postcode")) {
                postcode = attribute.getValue().trim();
            } else if (an.equals("suburb")) {
                suburb = attribute.getValue().trim();
            } else if (an.equals("surname")) {
                surname = attribute.getValue();
            } 
        }
    }

    private String getAddressMetaphone() {
        String addressMetaphone = doubleMetaphone.encode(address.replaceAll("[^\\w]", ""));
        if (addressMetaphone != null && addressMetaphone.length() > 3) {
            addressMetaphone = addressMetaphone.substring(0, 3);
        }
        return addressMetaphone;
    }
    
    @Override
    public String getBlockingKey(int bkId) {
        switch (bkId) {
            case 0:
                return getSurnameMetaphone() + getAddressMetaphone() + postcode;
            case 1:
                return getGivenNameMetaphone() + getAddressMetaphone() + postcode;
            case 2:
                return getSurnameMetaphone() + getSuburbMetaphone() + age;
            case 3:
                return getGivenNameMetaphone() + getSuburbMetaphone() + age;
            case 4:
                return getSurnameMetaphone() + getAddressMetaphone() + age;
            case 5:
                return getGivenNameMetaphone() + getSuburbMetaphone() + postcode;
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
                keys[0] = "a1#@#" + getBlockingKey(0);
                keys[1] = "a2#@#" + getBlockingKey(1);
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
    
    private String getSurnameMetaphone() {
        String surnameMetaphone = doubleMetaphone.encode(surname.replaceAll("[^\\w]", ""));
        if (surnameMetaphone != null && surnameMetaphone.length() > 3) {
            surnameMetaphone = surnameMetaphone.substring(0, 3);
        }
        return surnameMetaphone;
    }
    
    private String getSuburbMetaphone() {
        String subMetaphone = doubleMetaphone.encode(suburb.replaceAll("[^\\w]", ""));
        if (subMetaphone != null && subMetaphone.length() > 3) {
            subMetaphone = subMetaphone.substring(0, 3);
        }
        return subMetaphone;
    }
}
