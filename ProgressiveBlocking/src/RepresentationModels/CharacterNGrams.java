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

package RepresentationModels;

import Utilities.RepresentationModel;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author G.A.P. II
 */

public class CharacterNGrams extends AbstractModel {

    private final HashMap<String, Integer> nGrams;
    
    public CharacterNGrams(int n, RepresentationModel model, String iName) {
        super(n, model, iName);
        nGrams = new HashMap<String, Integer>();
    }

    private HashMap<String, Integer> getNGrams() {
        return nGrams;
    }
    
    @Override
    public double getSimilarity(AbstractModel oModel) {//Jaccard similarity
        final CharacterNGrams otherModel = (CharacterNGrams) oModel;
        
        double numerator = 0.0;
        for (Entry<String, Integer> entry : nGrams.entrySet()) {
            Integer frequency2 = otherModel.getNGrams().get(entry.getKey());
            if (frequency2 != null) {
                numerator += Math.min(entry.getValue(), frequency2);
            }
        }

        double denominator = getTotalFrequency(this.getNGrams())+getTotalFrequency(otherModel.getNGrams())-numerator;
        return numerator/denominator;
    }
    
    private double getTotalFrequency(HashMap<String, Integer> nGramsFrequency) {
        double totalFrequency = 0;
        for (Integer frequency : nGramsFrequency.values()) {
            totalFrequency += frequency;
        }
        return totalFrequency;
    }
    
    @Override
    public void updateModel(String text) {
        int currentPosition = 0;
        final int length = text.length() - (nSize-1);
        while (currentPosition < length) {
            final String term = text.substring(currentPosition, currentPosition + nSize);
            Integer frequency = nGrams.get(term);
            if (frequency == null) {
                frequency = new Integer(0);
            } 
            frequency++;
            nGrams.put(term, frequency);
            currentPosition++;
        }
    }
}