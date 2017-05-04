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
import java.io.Serializable;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractModel implements Serializable {
    
    private static final long serialVersionUID = 328759404L;

    protected final int nSize;
    protected double noOfDocuments;
    
    protected final RepresentationModel modelType;
    protected final String instanceName;
    
    public AbstractModel(int n, RepresentationModel md, String iName) {
        instanceName = iName;
        modelType = md;
        nSize = n;
        noOfDocuments = 0;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public RepresentationModel getModelType() {
        return modelType;
    }
    
    public double getNoOfDocuments() {
        return noOfDocuments;
    }
    
    public int getNSize() {
        return nSize;
    }
    
    public abstract double getSimilarity(AbstractModel oModel);
    public abstract void updateModel(String text);
}