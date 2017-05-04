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

public class RestaurantProfile extends AbstractProfile {
    
    private String address;
    private String city;
    private String name;
    private String phone;
    private String type;

    public RestaurantProfile(EntityProfile profile) {
        super(profile.getEntityUrl());
        
        address = "";
        city = "";
        name = "";
        phone = "";
        type = "";
        for (Attribute attribute : profile.getAttributes()) {
            String an = attribute.getName().toString().trim();
            if (an.equals("addr")) {
                address = attribute.getValue().toString().trim();
            } else if (an.equals("city")) {
                city = attribute.getValue().toString().trim();
            } else if (an.equals("name")) {
                name = attribute.getValue();
            } else if (an.equals("phone")) {
                phone = attribute.getValue();
            } else if (an.equals("type")) {
                type = attribute.getValue();
            } 
        }
    }

    private String getAddressMetaphone() {
        String addrMetaphone = doubleMetaphone.encode(address.replaceAll("[^\\w]", ""));
        if (addrMetaphone != null && addrMetaphone.length() > 3) {
            addrMetaphone = addrMetaphone.substring(0, 3);
        }
        return addrMetaphone;
    }
    
    @Override
    public String getBlockingKey(int bkId) {
        switch (bkId) {
            case 0:
                return getPhonePrefix() + getTypeMetaphone();
            case 1:
                return getNameMetaphone() + getCityMetaphone();
            case 2:
                return getCityMetaphone() + getPhonePrefix();
            case 3:
                return getAddressMetaphone() + getCityMetaphone();
            case 4:
                return getTypeMetaphone() + getPhonePrefix();
            case 5:
                return getAddressMetaphone() + getTypeMetaphone();
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
    
    private String getCityMetaphone() {
        String cityMetaphone = doubleMetaphone.encode(city.replaceAll("[^\\w]", ""));
        if (cityMetaphone != null && cityMetaphone.length() > 3) {
            cityMetaphone = cityMetaphone.substring(0, 3);
        }
        return cityMetaphone;
    }
    
    private String getNameMetaphone() {
        String nameMetaphone = doubleMetaphone.encode(name.replaceAll("[^\\w]", ""));
        if (nameMetaphone != null && nameMetaphone.length() > 3) {
            nameMetaphone = nameMetaphone.substring(0, 3);
        }
        return nameMetaphone;
    }
    
    private String getPhonePrefix() {
         return phone.substring(0, 3);
    }
    
    private String getTypeMetaphone() {
        String typeMetaphone = doubleMetaphone.encode(type.replaceAll("[^\\w]", ""));
        if (typeMetaphone != null && typeMetaphone.length() > 3) {
            typeMetaphone = typeMetaphone.substring(0, 3);
        }
        return typeMetaphone;
    }

}
