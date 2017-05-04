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

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import RepresentationModels.AbstractModel;
import Utilities.Constants;
import Utilities.RepresentationModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author gap2
 */
public abstract class AbstractAttributeClusteringBlocking extends AbstractTokenBlocking implements Constants {

    private int latestEntities;
    private final Map<String, Integer>[] attributeClusters;
    private final RepresentationModel model;

    public AbstractAttributeClusteringBlocking(RepresentationModel md, List<EntityProfile>[] profiles) {
        super("Memory-based Attribute Clustering Blocking", profiles);

        model = md;
        attributeClusters = new HashMap[2];
        sourceId = 0;
        AbstractModel[] attributeModels1 = buildAttributeModels();
        noOfEntities[0] = latestEntities;
        if (cleanCleanER) {
            sourceId = 1;
            AbstractModel[] attributeModels2 = buildAttributeModels();
            noOfEntities[1] = latestEntities;
            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }
    
    public AbstractAttributeClusteringBlocking(RepresentationModel md, String[] entities, String[] index) {
        super("Disk-based Attribute Clustering Blocking", entities, index);

        model = md;
        attributeClusters = new HashMap[2];
        sourceId = 0;
        AbstractModel[] attributeModels1 = buildAttributeModels();
        noOfEntities[0] = latestEntities;
        if (cleanCleanER) { 
            sourceId = 1;
            AbstractModel[] attributeModels2 = buildAttributeModels();
            noOfEntities[1] = latestEntities;
            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }

    private AbstractModel[] buildAttributeModels() {
        List<EntityProfile> profiles = getProfiles();
        latestEntities = profiles.size();
        
        final HashMap<String, List<String>> attributeProfiles = new HashMap<>();
        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            }
        }
        
        if (entitiesPath != null) {
            profiles.clear();
        }

        int index = 0;
        AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];
        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = RepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                attributeModels[index].updateModel(value);
            }
            index++;
        }
        return attributeModels;
    }

    private void clusterAttributes(AbstractModel[] attributeModels, SimpleGraph graph) {
        int noOfAttributes = attributeModels.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * noOfAttributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }
            
            for (int attributeId : cluster) {
                attributeClusters[0].put(attributeModels[attributeId].getInstanceName(), clusterId);
            }
        }
        attributeClusters[1] = null;
    }

    private void clusterAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, SimpleGraph graph) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;
        
        attributeClusters[0] = new HashMap<>(2 * d1Attributes);
        attributeClusters[1] = new HashMap<>(2 * d2Attributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }
            
            for (int attributeId : cluster) {
                if (attributeId < d1Attributes) {
                    attributeClusters[0].put(attributeModels1[attributeId].getInstanceName(), clusterId);
                } else {
                    attributeClusters[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), clusterId);
                }
            }
        }
    }

    private SimpleGraph compareAttributes(AbstractModel[] attributeModels) {
        int noOfAttributes = attributeModels.length;
        int[] mostSimilarName = new int[noOfAttributes];
        double[] maxSimillarity = new double[noOfAttributes];
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < noOfAttributes; i++) {
            for (int j = i + 1; j < noOfAttributes; j++) {
                double simValue = attributeModels[i].getSimilarity(attributeModels[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j;
                }

                if (maxSimillarity[j] < simValue) {
                    maxSimillarity[j] = simValue;
                    mostSimilarName[j] = i;
                }
            }
        }

        for (int i = 0; i < noOfAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    private SimpleGraph compareAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes + d2Attributes;
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);

        int[] mostSimilarName = new int[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
                double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j + d1Attributes;
                }

                if (maxSimillarity[j + d1Attributes] < simValue) {
                    maxSimillarity[j + d1Attributes] = simValue;
                    mostSimilarName[j + d1Attributes] = i;
                }
            }
        }

        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                for (Attribute attribute : profile.getAttributes()) {
                    Integer clusterId = attributeClusters[sourceId].get(attribute.getName());
                    if (clusterId == null) {
                        System.err.println(attribute.getName() + "\t\t" + attribute.getValue());
                        continue;
                    }
                    String clusterSuffix = CLUSTER_PREFIX + clusterId + CLUSTER_SUFFIX;
                    for (String token : getTokens(attribute.getValue())) {
                        if (0 < token.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, token.trim() + clusterSuffix, Field.Store.YES));
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
