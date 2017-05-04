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

public class CddbProfile extends AbstractProfile {

    private String artist;
    private String category;
    private String genre;
    private String title;
    private String year;

    public CddbProfile(EntityProfile profile) {
        super(profile.getEntityUrl());

        artist = "";
        title = "";
        genre = "";
        year = "";
        category = "";
        for (Attribute attribute : profile.getAttributes()) {
            String an = attribute.getName().toString().trim();
            if (an.equals("artist")) {
                artist = attribute.getValue();
            } else if (an.equals("category")) {
                category = attribute.getValue().toString().trim();
            } else if (an.equals("genre")) {
                genre = attribute.getValue();
            } else if (an.equals("title")) {
                title = attribute.getValue();
            } else if (an.equals("year")) {
                year = attribute.getValue().toString().trim();
            }
        }
    }

    private String getArtistMetaphone() {
        String artistMetaphone = doubleMetaphone.encode(artist.replaceAll("[^\\w]", ""));
        if (artistMetaphone != null && artistMetaphone.length() > 3) {
            artistMetaphone = artistMetaphone.substring(0, 3);
        }
        return artistMetaphone;
    }

    @Override
    public String getBlockingKey(int bkId) {
        switch (bkId) {
            case 0:
                return getArtistMetaphone() + getTitleMetaphone() + getGenreMetaphone();
            case 1:
                return getArtistMetaphone() + getTitleMetaphone() + getYearMetaphone();
            case 2:
                return getArtistMetaphone() + getCategoryMetaphone() + getGenreMetaphone();
            case 3:
                return getArtistMetaphone() + getCategoryMetaphone() + getYearMetaphone();
            case 4:
                return getTitleMetaphone() + getCategoryMetaphone() + getGenreMetaphone();
            case 5:
                return getTitleMetaphone() + getCategoryMetaphone() + getYearMetaphone();
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

    private String getCategoryMetaphone() {
        String catMetaphone = doubleMetaphone.encode(category.replaceAll("[^\\w]", ""));
        if (catMetaphone != null && catMetaphone.length() > 3) {
            catMetaphone = catMetaphone.substring(0, 3);
        }
        return catMetaphone;
    }

    private String getGenreMetaphone() {
        String genreMetaphone = doubleMetaphone.encode(genre.replaceAll("[^\\w]", ""));
        if (genreMetaphone != null && genreMetaphone.length() > 3) {
            genreMetaphone = genreMetaphone.substring(0, 3);
        }
        return genreMetaphone;
    }

    private String getTitleMetaphone() {
        String titleMetaphone = doubleMetaphone.encode(title.replaceAll("[^\\w]", ""));
        if (titleMetaphone != null && titleMetaphone.length() > 3) {
            titleMetaphone = titleMetaphone.substring(0, 3);
        }
        return titleMetaphone;
    }

    private String getYearMetaphone() {
        String yearMetaphone = year;
        if (yearMetaphone != null && yearMetaphone.length() > 4) {
            yearMetaphone = yearMetaphone.substring(0, 4);
        }
        return yearMetaphone;
    }
}
