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

package BlockBuilding;

import DataStructures.AbstractBlock;
import DataStructures.Attribute;
import DataStructures.BilateralBlock;
import DataStructures.EntityIndex;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.Constants;
import Utilities.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Blocking method presented in 
 * "TYPiMatch: type-specific unsupervised learning of keys and key values for heterogeneous web data integration" 
 * by Yongtao Ma, Thanh Tran, ACM WSDM 2013.
 * 
 * @author gap2
 */

public abstract class AbstractTYPiMatch extends AbstractTokenBlocking implements Constants {
    
    private final static int MINIMUM_CLIQUE_SIZE = 1;
    private final static int MINIMUM_FEATURE_LENGTH = 1;

    private boolean firstPass;
    
    private final double epsilon;
    private final double theta;

    private int datasetLimit;
    private int entityCounter;
    private int totalEntities;
    private int[][] cliqueEntities;
    
    private Integer[][] arrayOfBlocks;
    private final Set<String> stopWords;
    private int[] entityTypes;
    
    public AbstractTYPiMatch(double ep, double th, Set<String> sWords, List<EntityProfile>[] profiles) {
        super("Memory-based TYPiMatch", profiles);
        
        entityCounter = 0;
        epsilon = ep;
        firstPass = true;
        theta = th;
        stopWords = sWords;
    }
    
    public AbstractTYPiMatch(double ep, double th, Set<String> sWords, String[] entityPaths, String[] indexPaths) {
        super("Disk-based TYPiMatch", entityPaths, indexPaths);
        
        entityCounter = 0;
        epsilon = ep;
        firstPass = true;
        theta = th;
        stopWords = sWords;
    }
    
    @Override
    public List<AbstractBlock> buildBlocks() {
        // extract features
        // equivalent to applying Token Blocking, while excluding tokens in stopWords or shorter than MINIMUM_FEATURE_LENGTH
        blocks = super.buildBlocks();
        
        // convert blocks to a more suitable format
        getArrayOfBlocks();
        
        // extract pseudo-schema features
        // group them according to the number of entities they have in common
        final Set<Integer>[] featureCliques = extractPseudoSchemaFeatures();
        
        // cluster cliques to get types
        List<Set<Integer>> connectedComponents = getConnectedComponents(featureCliques);

        // get type per entity
        getEntityTypes(connectedComponents);
        
        entityCounter = 0;
        firstPass = false;
        return super.buildBlocks();
    }

    private Set<Integer>[] extractPseudoSchemaFeatures() {
        int noOfFeatures = arrayOfBlocks.length;
        final UndirectedGraph<Integer, DefaultEdge> featuresGraph = getGraph(noOfFeatures);
        for (int i = 0; i < noOfFeatures; i++) {
            for (int j = i + 1; j < noOfFeatures; j++) {
                double noOfCommonEntities = Converter.getSortedListsOverlap(arrayOfBlocks[i], arrayOfBlocks[j]);
                if (0 < noOfCommonEntities) {
                    double probFiFj = noOfCommonEntities / arrayOfBlocks[j].length;
                    double probFjFi = noOfCommonEntities / arrayOfBlocks[i].length;
                    if (probFiFj > theta && probFjFi > theta) {
                        featuresGraph.addEdge(i, j);
                    }
                }
            }
        }

        BronKerboschCliqueFinder cliqueFinder = new BronKerboschCliqueFinder(featuresGraph);
        Collection<Set<Integer>> cliquesList = cliqueFinder.getAllMaximalCliques();
        return getCleanCliques(cliquesList);
    }
    
    protected void getArrayOfBlocks () {
        // gather auxiliary information
        EntityIndex eIndex = new EntityIndex(blocks);
        datasetLimit = eIndex.getDatasetLimit();
        totalEntities = eIndex.getNoOfEntities();
        
        arrayOfBlocks = new Integer[blocks.size()][];
        if (0 < datasetLimit) { // Clean-Clean ER
            int counter = 0;
            for (AbstractBlock block : blocks) {
                // sort entity ids in block
                List<Integer> entityIds = new ArrayList<>();
                for (int id : ((BilateralBlock) block).getIndex1Entities()) {
                    entityIds.add(id);
                }
                for (int id : ((BilateralBlock) block).getIndex2Entities()) {
                    entityIds.add(id+datasetLimit);
                }
                Collections.sort(entityIds);
                arrayOfBlocks[counter++] = entityIds.toArray(new Integer[entityIds.size()]);
            }
            
        } else { // Dirty ER
            int counter = 0;
            for (AbstractBlock block : blocks) {
                // sort entity ids in block
                List<Integer> entityIds = new ArrayList<>();
                for (int id : ((UnilateralBlock) block).getEntities()) {
                    entityIds.add(id);
                }
                Collections.sort(entityIds);
                arrayOfBlocks[counter++] = entityIds.toArray(new Integer[entityIds.size()]);
            }
        }
        
    }

    private Set<Integer>[] getCleanCliques(Collection<Set<Integer>> cliquesList) {
        Iterator iterator = cliquesList.iterator();
        while (iterator.hasNext()) {
            Set<Integer> currentClique = (Set<Integer>) iterator.next();
            if (currentClique.size() <= MINIMUM_CLIQUE_SIZE) {
                iterator.remove();
            } 
        }
        return cliquesList.toArray(new Set[cliquesList.size()]);
    }
    
    private List<Set<Integer>> getConnectedComponents(Set<Integer>[] cliques) {
        int noOfCliques = cliques.length;
        cliqueEntities = new int[noOfCliques][];
        for (int i = 0; i < noOfCliques; i++) {
            // get distinct entities per clique
            Set<Integer> currentEntities = new TreeSet<>();
            for (int featureId : cliques[i]) {
                currentEntities.addAll(Arrays.asList(arrayOfBlocks[featureId]));
            }
            
            // sort entities per clique
            List<Integer> sortedEntities = new ArrayList<>(currentEntities);
            Collections.sort(sortedEntities);
            
            cliqueEntities[i] = Converter.convertCollectionToArray(sortedEntities);
        }
        
        // build cliques graph
        // nodes -> cliques, edges -> overlapping cliques
        final UndirectedGraph<Integer, DefaultEdge> graph = getGraph(noOfCliques);
        for (int i = 0; i < noOfCliques; i++) {
            for (int j = i+1; j < noOfCliques; j++) {
                double overlap = Converter.getSortedListsOverlap(cliqueEntities[i], cliqueEntities[j]);
                if (0 < overlap) {
                    double probFiFj = overlap/cliqueEntities[i].length;
                    double probFjFi = overlap/cliqueEntities[j].length;
                    if (probFiFj > epsilon && probFjFi > epsilon) {
                        graph.addEdge(i, j);
                    }
                }
            }
        }
        
        // get connected components
        ConnectivityInspector ci = new ConnectivityInspector(graph);
        return ci.connectedSets();
    }
    
    private UndirectedGraph<Integer, DefaultEdge> getGraph(int noOfEdges) {
        final UndirectedGraph<Integer, DefaultEdge> featuresGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfEdges; i++) {
            featuresGraph.addVertex(i);
        }
        return featuresGraph;
    }
    
    private void getEntityTypes(List<Set<Integer>> connectedComponents) {
        boolean[] entityInType = new boolean[totalEntities];
        Arrays.fill(entityInType, Boolean.FALSE);
        
        // get entities per type
        int index = 0;
        entityTypes = new int[totalEntities];
        for (Set<Integer> component : connectedComponents) {
            for (Integer clique : component) {
                for (int j = 0; j < cliqueEntities[clique].length; j++) {
                    entityTypes[cliqueEntities[clique][j]] = index;
                    entityInType[cliqueEntities[clique][j]] = true;
                }
            }
            index++;
        }
        
        // add remaining entities to a glue type
        int noOfConComs = connectedComponents.size();
        for (int i = 0; i < totalEntities; i++) {
            if (!entityInType[i]) {
                entityTypes[i] = noOfConComs;
            }
        }
    }
        
    @Override
    protected String[] getTokens (String attributeValue) {
        attributeValue = attributeValue.toLowerCase();
        Pattern p = Pattern.compile("[.,\"\\?!:';()_/-<>]");
        Matcher m = p.matcher(attributeValue);
        String cleanValue = m.replaceAll(" ");
        cleanValue = cleanValue.replaceAll("-", "  ").replaceAll("/", " ").replaceAll("\\\\", " ");
        
        String[] originalTokens = cleanValue.split(" ");
        List<String> validTokens = new ArrayList<>();
        for (String token : originalTokens) {
            if (MINIMUM_FEATURE_LENGTH < token.length() && 
                    !stopWords.contains(token)) {
                validTokens.add(token);
            }
        }
        return validTokens.toArray(new String[validTokens.size()]);
    }
    
    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                
                String entitySuffix = "";
                if (!firstPass) {
                    entitySuffix = CLUSTER_PREFIX + entityTypes[entityCounter++] + CLUSTER_SUFFIX;
                }
                
                for (Attribute attribute : profile.getAttributes()) {
                    for (String token : getTokens(attribute.getValue())) {
                        if (0 < token.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, token.trim() + entitySuffix, Field.Store.YES));
                        }
                    }
                }

                index.addDocument(doc);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}