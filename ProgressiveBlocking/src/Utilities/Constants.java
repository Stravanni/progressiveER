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

package Utilities;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author G.A.P. II
 */

public interface Constants {  
    int SUBJECT = 1;
    int PREDICATE = 2;
    int OBJECT = 3;
    
    int MAX_Q_GRAMS = 15;
    
    double MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD = 1E-11;
    
    NumberFormat twoDigitsDouble = new DecimalFormat("#0.00");
    NumberFormat fourDigitsDouble = new DecimalFormat("#0.0000");
    
    String BLANK_NODE_BEGINNING = "_:";
    String CLUSTER_PREFIX = "#$!cl";
    String CLUSTER_SUFFIX = "!$#";
    String DOC_ID = "docid";
    String DEPTH_ONE_INFIX_DELIMITER = "+";
    String INFIX_DELIMITER = "++++";
    String INFIX_FIELD_TITLE = "Infix";
    String INFIX_REG_EX_DELIMITER = "\\+\\+\\+\\+";
    String LITERAL_BEGINNING = "\"";
    String URI_FIELD_TITLE = "URI";
    String URL_LABEL = "entityUrl";
    String VALUE_LABEL = "value";
    
    //for supervised meta-blocking
    double SAMPLE_SIZE = 0.05;
    int DUPLICATE = 1;
    int NON_DUPLICATE = 0;
    String MATCH = "match";
    String NON_MATCH = "nonmatch";
}