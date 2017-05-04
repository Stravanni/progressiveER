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
import java.util.HashSet;
import java.util.Map.Entry;

/**
 *
 * @author G.A.P. II
 */

public class TokenNGrams extends AbstractModel {

    private double noOfTotalTerms;
    private final HashMap<String, Integer> documentFrequency;
    private final HashMap<String, Integer> itemsFrequency;
    
    public TokenNGrams(int n, RepresentationModel model, String iName) {
        super(n, model, iName);
        
        noOfTotalTerms = 0;
        documentFrequency = new HashMap<String, Integer>();
        itemsFrequency = new HashMap<String, Integer>();
    }
    
    public HashMap<String, Integer> getDocumentFrequency() {
        return documentFrequency;
    }

    public HashMap<String, Integer> getItemsFrequency() {
        return itemsFrequency;
    }

    public double getNoOfTotalTerms() {
        return noOfTotalTerms;
    }
    
    @Override
    public void updateModel(String text) {
        noOfDocuments++;
             
        String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(text);
        
        int noOfTokens = tokens.length;
        noOfTotalTerms += noOfTokens;
        final HashSet<String> features = new HashSet<String>();
        for (int j = 0; j < noOfTokens-nSize; j++) {
            final StringBuilder sb = new StringBuilder();
            for (int k = 0; k < nSize; k++) {
                sb.append(tokens[j+k]).append(" ");
            }
            String feature = sb.toString().trim();
            features.add(feature);
            
            Integer frequency = itemsFrequency.get(feature);
            if (frequency == null) {
                frequency = new Integer(0);
            }
            frequency++;
            itemsFrequency.put(feature, frequency);
        }

        for (String item : features) {
            Integer frequency = documentFrequency.get(item);
            if (frequency == null) {
                frequency = new Integer(0);
            }
            frequency++;
            documentFrequency.put(item, frequency);
        }
    }

    @Override
    public double getSimilarity(AbstractModel oModel) {
        final TokenNGrams otherModel = (TokenNGrams) oModel;
        final HashMap<String, Integer> oItemVector = otherModel.getItemsFrequency();

        double numerator = 0.0;
        for (Entry<String, Integer> entry : itemsFrequency.entrySet()) {
            Integer frequency2 = oItemVector.get(entry.getKey());
            if (frequency2 != null) {
                double inverseDocFreq1 = Math.log(this.getNoOfDocuments()/(1.0+this.getDocumentFrequency().get(entry.getKey())));
                double inverseDocFreq2 = Math.log(otherModel.getNoOfDocuments()/(1.0+otherModel.getDocumentFrequency().get(entry.getKey())));
                numerator += ((double)entry.getValue()/this.getNoOfTotalTerms())*((double)frequency2/otherModel.getNoOfTotalTerms())*inverseDocFreq1*inverseDocFreq2;
            }
        }

        double denominator = getVectorMagnitude(this)*getVectorMagnitude(otherModel); 
        return numerator / denominator;
    }

    private double getVectorMagnitude(TokenNGrams model) {
        double magnitude = 0.0;
        for (Entry<String, Integer> entry : model.getItemsFrequency().entrySet()) {
            double inverseDocFreq = Math.log(model.getNoOfDocuments()/(1.0+model.getDocumentFrequency().get(entry.getKey())));
            double weight = ((double)entry.getValue())/model.getNoOfTotalTerms()*inverseDocFreq;
            magnitude += Math.pow(weight, 2.0);
        }

        return Math.sqrt(magnitude);
    }
}