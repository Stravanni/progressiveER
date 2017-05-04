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

package BlockBuilding.DiskBased.TotalDescription;

import DataStructures.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * created on 28.12.2009
 * by gap2
 */

public class ScalableUriSplit {

    private final List<String> uris;
    private List<Uri> normalizedUris;
    private Map<String, HashSet<String>> prefixesFreq;

    public ScalableUriSplit (ArrayList<String> uris) {
        this.uris = uris;
        
        processUris();
        setPrefixes();
        setSuffixes();
    }

    public List<Uri> getOutput() {
        return normalizedUris;
    }

    public void printYs() {
        normalizedUris.stream().forEach((currentUri) -> {
            System.out.println(currentUri.getNormalForm().getInfix());
        });
    }

    private void processUris() {
        System.out.println("\n\nProcessing URIs...");

        normalizedUris = new ArrayList<>();
        prefixesFreq = new HashMap<>();

        for (String currentUrl : uris) {
            Uri currentUri = new Uri(currentUrl);
            normalizedUris.add(currentUri);

            int counter = -1;
            for (String currentPrefix : currentUri.getSubUris()) {
                counter++;

                HashSet<String> currentFreq = prefixesFreq.get(currentPrefix);
                if (currentFreq == null) {
                    currentFreq = new HashSet<>();
                }

                if (0 < counter) {
                    currentFreq.add(currentUri.getSubUris().get(counter - 1));
                }

                prefixesFreq.put(currentPrefix, currentFreq);
            }
        }

        System.out.println(prefixesFreq.size() + " distinct prefixes!");
        System.out.println("URIs were processed!");
    }

    private void setPrefixes() {
        System.out.println("\n\nSetting prefixes...");

        int truePositives = 0;
        normalizedUris.stream().forEach((currentUri) -> {
            currentUri.setNormalForm(prefixesFreq);
        });

        System.out.println("True Positives\t:\t" + truePositives);
        System.out.println("Prefixes were set!");
    }

    private void setSuffixes() {
        System.out.println("\n\nSetting suffixes...");

        HashMap<String, ArrayList<Uri>> suffixInvestigator = new HashMap<>(2*normalizedUris.size());
        for (Uri currentUri : normalizedUris) {
            String possibleSuffix = SuffixIndentifier.getPossibleSuffix(currentUri);

            if (possibleSuffix.trim().length() == 0) {
                continue;
            }

            ArrayList<Uri> commonSuffixUris = suffixInvestigator.get(possibleSuffix);
            if (commonSuffixUris == null) {
                commonSuffixUris = new ArrayList<>();
            }
            commonSuffixUris.add(currentUri);
            suffixInvestigator.put(possibleSuffix, commonSuffixUris);
        }

        for(Entry<String, ArrayList<Uri>> e: suffixInvestigator.entrySet()) {
            if (e.getValue().size() < 10) {
                continue;
            }

            for (Uri currentUri : e.getValue()) {
                currentUri.setSuffix(e.getKey());
            }
        }

        System.out.println("Suffixes were set!");
    }

    public static void main (String[] args) {
        ArrayList<String> uris = new ArrayList<>(); // add here all URis
        ScalableUriSplit scalableUS = new ScalableUriSplit(uris);
        scalableUS.printYs();
    }
}