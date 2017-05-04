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

public class CoraProfile extends AbstractProfile {

    private String affiliation;
    private String author;
    private String location;
    private String title;
    private String venue;
    private String year;

    public CoraProfile(EntityProfile profile) {
        super(profile.getEntityUrl());

        affiliation = "";
        author = "";
        location = "";
        title = "";
        venue = "";
        year = "";
        for (Attribute attribute : profile.getAttributes()) {
            String an = attribute.getName().toString().trim();
            if (an.equals("affiliation")) {
                affiliation = attribute.getValue().toString().trim();
            } else if (an.equals("author")) {
                author = attribute.getValue();
            } else if (an.equals("address")) {
                location = attribute.getValue().toString().trim();
            } else if (an.equals("title")) {
                title = attribute.getValue();
            } else if (an.equals("venue")) {
                venue = attribute.getValue();
            } else if (an.equals("year")) {
                year = attribute.getValue().toString().trim();
            }
        }
    }

    private String getAffiliationMetaphone() {
        String afMetaphone = doubleMetaphone.encode(affiliation.replaceAll("[^\\w]", ""));
        if (afMetaphone != null && afMetaphone.length() > 3) {
            afMetaphone = afMetaphone.substring(0, 3);
        }
        return afMetaphone;
    }

    private String getAuthorMetaphone() {
        String authorMetaphone = doubleMetaphone.encode(author.replaceAll("[^\\w]", ""));
        if (authorMetaphone != null && authorMetaphone.length() > 3) {
            authorMetaphone = authorMetaphone.substring(0, 3);
        }
        return authorMetaphone;
    }

    @Override
    public String getBlockingKey(int bkId) {
        switch (bkId) {
            case 0:
                return getAuthorMetaphone() + getTitleMetaphone();
            case 1:
                return getVenueMetaphone() + year;
            case 2:
                return getAffiliationMetaphone() + getLocationMetaphone();
            case 3:
                return year + getTitleMetaphone();
            case 4:
                return year + getAuthorMetaphone();
            case 5:
                return getVenueMetaphone() + getTitleMetaphone();
            default:
                return null;
        }
    }

    @Override
    public String[] getBlockingKeys(int bkId) {
        String[] keys = new String[2];
        switch (bkId) {
            case 0:
                keys[0] = "a1#@#" + getBlockingKey(4);
                keys[1] = "a2#@#" + getBlockingKey(5);
                return keys;
            case 1:
                keys[0] = "a1#@#" + getBlockingKey(4);
                keys[1] = "a2#@#" + getBlockingKey(5);
                return keys;
            default:
                return null;
        }
    }

    private String getLocationMetaphone() {
        String loMetaphone = doubleMetaphone.encode(location.replaceAll("[^\\w]", ""));
        if (loMetaphone != null && loMetaphone.length() > 3) {
            loMetaphone = loMetaphone.substring(0, 3);
        }
        return loMetaphone;
    }

    private String getTitleMetaphone() {
        String titleMetaphone = doubleMetaphone.encode(title.replaceAll("[^\\w]", ""));
        if (titleMetaphone != null && titleMetaphone.length() > 3) {
            titleMetaphone = titleMetaphone.substring(0, 3);
        }
        return titleMetaphone;
    }

    private String getVenueMetaphone() {
        String venueMetaphone = doubleMetaphone.encode(venue.replaceAll("[^\\w]", ""));
        if (venueMetaphone != null && venueMetaphone.length() > 3) {
            venueMetaphone = venueMetaphone.substring(0, 3);
        }
        return venueMetaphone;
    }
}
