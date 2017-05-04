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

import RepresentationModels.AbstractModel;
import RepresentationModels.CharacterNGramGraphs;
import RepresentationModels.CharacterNGrams;
import RepresentationModels.TokenNGramGraphs;
import RepresentationModels.TokenNGrams;

/**
 *
 * @author G.A.P. II
 */

public enum RepresentationModel {
    CHARACTER_BIGRAMS,
    CHARACTER_BIGRAM_GRAPHS,
    CHARACTER_TRIGRAMS,
    CHARACTER_TRIGRAM_GRAPHS,
    CHARACTER_FOURGRAMS,
    CHARACTER_FOURGRAM_GRAPHS,
    TOKEN_UNIGRAMS, 
    TOKEN_UNIGRAM_GRAPHS, 
    TOKEN_BIGRAMS,
    TOKEN_BIGRAM_GRAPHS, 
    TOKEN_TRIGRAMS,
    TOKEN_TRIGRAM_GRAPHS;
    
    public static AbstractModel getModel (RepresentationModel model, String instanceName) {
        switch (model) {
            case CHARACTER_BIGRAMS:
                return new CharacterNGrams(2, model, instanceName);
            case CHARACTER_BIGRAM_GRAPHS:
                return new CharacterNGramGraphs(2, model, instanceName);
            case CHARACTER_FOURGRAMS:
                return new CharacterNGrams(4, model, instanceName);
            case CHARACTER_FOURGRAM_GRAPHS:
                return new CharacterNGramGraphs(4, model, instanceName);
            case CHARACTER_TRIGRAMS:
                return new CharacterNGrams(3, model, instanceName);
            case CHARACTER_TRIGRAM_GRAPHS:
                return new CharacterNGramGraphs(3, model, instanceName);
            case TOKEN_BIGRAMS:
                return new TokenNGrams(2, model, instanceName);
            case TOKEN_BIGRAM_GRAPHS:
                return new TokenNGramGraphs(2, model, instanceName);
            case TOKEN_TRIGRAMS:
                return new TokenNGrams(3, model, instanceName);
            case TOKEN_TRIGRAM_GRAPHS:
                return new TokenNGramGraphs(3, model, instanceName);
            case TOKEN_UNIGRAMS:
                return new TokenNGrams(1, model, instanceName);
            case TOKEN_UNIGRAM_GRAPHS:
                return new TokenNGramGraphs(1, model, instanceName);
            default:
                return null;    
        }
    }
}