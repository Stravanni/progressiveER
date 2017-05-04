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
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramHGraph;

/**
 *
 * @author gap2
 */

public class CharacterNGramGraphs extends GraphModel {
    
    private final static int SEGMENTS_UNIT = 100;
    
    public CharacterNGramGraphs (int n, RepresentationModel model, String iName) {
        super(n, model, iName);
        
        graphModel = new DocumentNGramHGraph(nSize, nSize, nSize, nSize*SEGMENTS_UNIT);
    }
    
    @Override
    public void updateModel(String text) {
        final DocumentNGramGraph tempGraph = new DocumentNGramGraph(nSize, nSize, nSize);
        tempGraph.setDataString(text);
        
        noOfDocuments++;
        graphModel.merge(tempGraph, 1 - (noOfDocuments-1)/noOfDocuments);
    }
}