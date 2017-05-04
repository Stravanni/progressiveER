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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * created on 10.12.2009
 * by gap2
 */

public class Uri implements Serializable {

    static final long serialVersionUID = 4070409649129120458L;

    private List<String> subUris;
    private String uri;
    private UriNormalForm normalForm;

    public Uri (String uri) {
        this.uri = uri;

        setSubUris();
    }

    public UriNormalForm getNormalForm() {
        return normalForm;
    }

    private String getPrefixLastToken(String subUri) {
        if (subUri == null) {
            return null;
        }
        
        int lastUnderscoreIndex = subUri.lastIndexOf("_");
        return subUri.substring(lastUnderscoreIndex + 1, subUri.length());
    }

    public List<String> getSubUris() {
        return subUris;
    }
    
    public String getUri() {
        return uri;
    }

    public void setNormalForm(Map<String, HashSet<String>> prefixesFreq) {
        int maxFreq = 0;
        String mostFreqSubUri = null;

        for (String currentSubUri : subUris) {
            int currentFreq = ((HashSet<String>) prefixesFreq.get(currentSubUri)).size();
            if (maxFreq < currentFreq) {
                maxFreq = currentFreq;
                mostFreqSubUri = currentSubUri;
            }
        }

        normalForm = new UriNormalForm();
        String centralToken = getPrefixLastToken(mostFreqSubUri);
        if (centralToken == null|| centralToken.trim().length() == 0) {
            normalForm.setInfix(uri);
            return;
        }
        
        int actualCentralTokenIndex = -1;
        int bestDistance = Integer.MAX_VALUE;
        int currentIndex = 0;
        int currentIndexOfCentralToken = 0;
        while (true) {
            currentIndexOfCentralToken = uri.indexOf(centralToken, currentIndex);
            if (currentIndexOfCentralToken == -1) {
                break;
            }
            
            int currentDistance = Math.abs(mostFreqSubUri.length() - currentIndexOfCentralToken);
            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                actualCentralTokenIndex = currentIndexOfCentralToken;
            }

            currentIndex = currentIndexOfCentralToken + centralToken.length();
        } 
        
        if (actualCentralTokenIndex < 0) {
            normalForm.setInfix(uri);
            return;
        }
        actualCentralTokenIndex += centralToken.length();
        normalForm.setPrefix(uri.substring(0, actualCentralTokenIndex));

        String infix = "";
        if (actualCentralTokenIndex + 1 < uri.length()) {
            infix = uri.substring(actualCentralTokenIndex + 1);
        }
        normalForm.setInfix(infix);
    }

    public void setNormalForm(UriNormalForm normalForm) {
        this.normalForm = normalForm;
    }

    private void setSubUris() {
        subUris = new ArrayList<>();

        String[] tokens = uri.split("[#!@&'./:?]");

        int noOfTokens = tokens.length;
        for (int i = noOfTokens - 1; 3 < i; i--) {
            if (tokens[i].trim().length() == 0) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                if (tokens[j].trim().length() == 0) {
                    continue;
                }

                sb.append(tokens[j]).append("_");
            }
            String currentSubUri = sb.toString();
            subUris.add(currentSubUri.substring(0, currentSubUri.length() - 1));
        }
    }

    public void setSuffix(String suffix) {
        if (suffix == null || suffix.trim().length() == 0) {
            return;
        }
        
        String infix = normalForm.getInfix();
        int suffixIndex = infix.lastIndexOf(suffix);
        if (1 < suffixIndex) {
            infix = infix.substring(0, suffixIndex - 1);
            if (infix.endsWith("/")) {
                infix = infix.substring(0, infix.length() -1);
            }
        }
        
        normalForm.setInfix(infix);
        normalForm.setSuffix(suffix);
    }

    @Override
    public String toString() {
        return uri + " : " + normalForm.getPrefix() + "\t\t" + normalForm.getInfix() + "\t\t" + normalForm.getSuffix();
    }
}