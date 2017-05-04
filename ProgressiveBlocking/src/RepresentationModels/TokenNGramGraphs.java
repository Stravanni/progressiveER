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
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentWordGraph;

/**
 *
 * @author gap2
 */

public class TokenNGramGraphs extends GraphModel {
    
    public TokenNGramGraphs (int n, RepresentationModel model, String iName) {
        super(n, model, iName);

        graphModel = new DocumentWordGraph(nSize, nSize, nSize);
    }

    @Override
    public void updateModel(String text) {
        final DocumentWordGraph tempGraph = new DocumentWordGraph(nSize, nSize, nSize);
        tempGraph.setDataString(text);
        
        noOfDocuments++;
        getGraphModel().merge(tempGraph, 1 - (noOfDocuments-1)/noOfDocuments);
    }
}