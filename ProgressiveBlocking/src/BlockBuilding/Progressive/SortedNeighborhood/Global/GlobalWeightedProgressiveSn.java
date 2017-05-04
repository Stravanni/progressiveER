package BlockBuilding.Progressive.SortedNeighborhood.Global;

import BlockBuilding.Progressive.DataStructures.InverseMetablockingComparator;
import BlockBuilding.Progressive.DataStructures.WeightingSchemeSn;
import BlockBuilding.Progressive.DataStructures.MetablockingComparator;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import BlockBuilding.Progressive.DataStructures.PositionIndex.PositionIndex;
import BlockBuilding.Progressive.SortedNeighborhood.Local.NaiveProgressiveSn;
import DataStructures.SchemaBasedProfiles.ProfileType;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.*;

/*
* todo: DONE
* todo: sortedTopComparisons is not sorted, it comes from an HashSet of comparison. Contains topKedges for each entity, but unsorted.
* todo: need something similar to CepCnp
*
* */

/**
 * @author gap2
 * @author giovanni
 */
public class GlobalWeightedProgressiveSn extends NaiveProgressiveSn implements Iterator<Comparison> {

    //-
    protected double minimumWeight;
    protected int counter;
    protected static int maxCPE = 1000; /*max comparisons per entity*/

    //protected final int[] counters_neighbor_cooccurrence;
    protected final int[] flags;
    protected final Queue<Comparison> topKEdges;
    protected final Set<Integer> distinctNeighbors;
    protected final PositionIndex sPositionIndex;
    //protected Comparison[] sortedTopComparisons;
    protected MinMaxPriorityQueue<Comparison> sortedTopComparisons;

    /*protected final int max_cep = 1000;*/

    protected final WeightingSchemeSn weightingSchema;
    //--

    public GlobalWeightedProgressiveSn(int bk, ProfileType pt, List<EntityProfile>[] profiles, WeightingSchemeSn ws) {
        this(bk, pt, profiles, ws, 100);
    }

    public GlobalWeightedProgressiveSn(List<EntityProfile>[] profiles, WeightingSchemeSn ws) {
        this(profiles, ws, 100);
    }

    public GlobalWeightedProgressiveSn(List<EntityProfile>[] profiles, WeightingSchemeSn ws, int max_win) {
        super(profiles, max_win);

        weightingSchema = ws;

        counter = 0;
        /*maxCPE = max_cep;*/

        //counters_neighbor_cooccurrence = new int[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        System.out.println("Window\t:\t" + maxWindow);

        //-
        distinctNeighbors = new HashSet<>();
        topKEdges = new PriorityQueue<>(maxCPE, new MetablockingComparator());
        sortedTopComparisons = MinMaxPriorityQueue.orderedBy(new InverseMetablockingComparator())
                .maximumSize(maxCPE * noOfEntities)
                .create();
        System.out.println("max size heap: " + maxCPE * noOfEntities);
        sPositionIndex = new PositionIndex(noOfEntities, sortedEntities, weightingSchema, profiles);
        //--

        if (cleanCleanER) {
            getCleanCleanComparisons();
        } else {
            getDirtyComparisons();
        }
    }

    public GlobalWeightedProgressiveSn(List<EntityProfile>[] profiles, WeightingSchemeSn ws, int max_win, int max_cep) {
        super(profiles, max_win);

        if (max_cep > 0) {
            maxCPE = max_cep;
        }

        weightingSchema = ws;

        counter = 0;
        /*maxCPE = max_cep;*/

        //counters_neighbor_cooccurrence = new int[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        System.out.println("Window\t:\t" + maxWindow);

        //-
        distinctNeighbors = new HashSet<>();
        topKEdges = new PriorityQueue<>(maxCPE, new MetablockingComparator());
        sortedTopComparisons = MinMaxPriorityQueue.orderedBy(new InverseMetablockingComparator())
                .maximumSize(maxCPE * noOfEntities)
                .create();
        System.out.println("max size heap: " + maxCPE * noOfEntities);
        sPositionIndex = new PositionIndex(noOfEntities, sortedEntities, weightingSchema, profiles);
        //--

        if (cleanCleanER) {
            getCleanCleanComparisons();
        } else {
            getDirtyComparisons();
        }
    }

    public GlobalWeightedProgressiveSn(int bk, ProfileType pt, List<EntityProfile>[] profiles, WeightingSchemeSn ws, int max_win) {
        super(bk, pt, profiles, max_win);

        weightingSchema = ws;

        counter = 0;
        /*maxCPE = max_cep;*/

        //counters_neighbor_cooccurrence = new int[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        System.out.println("Window\t:\t" + maxWindow);

        //-
        distinctNeighbors = new HashSet<>();
        topKEdges = new PriorityQueue<>(maxCPE, new MetablockingComparator());
        sortedTopComparisons = MinMaxPriorityQueue.orderedBy(new InverseMetablockingComparator())
                .maximumSize(maxCPE * noOfEntities)
                .create();
        System.out.println("max size heap: " + maxCPE * noOfEntities);
        sPositionIndex = new PositionIndex(noOfEntities, sortedEntities, weightingSchema, profiles);
        //--

        if (cleanCleanER) {
            getCleanCleanComparisons();
        } else {
            getDirtyComparisons();
        }
    }

    private void getCleanCleanComparisons() {
        final Set<Comparison> topComparisons = new HashSet<>();

        for (int entityId = 0; entityId < datasetLimit; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (currentWindow = 1; currentWindow < maxWindow; currentWindow++) {
                for (int position : entityPositions) {
                    if (position + currentWindow < sortedEntities.length
                            && datasetLimit <= sortedEntities[position + currentWindow]) {
                        int neighborId = sortedEntities[position + currentWindow];
                        if (flags[neighborId] != entityId) {
                            sPositionIndex.getCounters()[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId);
                    }

                    if (0 <= position - currentWindow
                            && datasetLimit <= sortedEntities[position - currentWindow]) {
                        int neighborId = sortedEntities[position - currentWindow];
                        if (flags[neighborId] != entityId) {
                            sPositionIndex.getCounters()[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId);
                    }

                }
            }

            topKEdges.clear();
            minimumWeight = -1;
            for (Integer neighborId : distinctNeighbors) {
                double weight = getWeight(entityId, neighborId);
                if (weight < minimumWeight) {
                    continue;
                }

                Comparison c = new Comparison(cleanCleanER, entityId, neighborId - datasetLimit);
                c.setUtilityMeasure(weight);
                topKEdges.add(c);
                if (maxCPE < topKEdges.size()) {
                    Comparison lastComparison = topKEdges.poll();
                    minimumWeight = lastComparison.getUtilityMeasure();
                }
            }
            topComparisons.addAll(topKEdges);
        }
        /*sortedTopComparisons = topComparisons.toArray(new Comparison[topComparisons.size()]);*/
        for (Comparison c : topComparisons) {
            sortedTopComparisons.offer(c);
        }
        /*System.out.println("Total comparisons\t:\t" + sortedTopComparisons.length);*/
        System.out.println("Total comparisons:\t" + sortedTopComparisons.size());
    }

    private void getDirtyComparisons() {
        final Set<Comparison> topComparisons = new HashSet<>();

        for (int entityId = 0; entityId < noOfEntities; entityId++) {
            distinctNeighbors.clear();

            final int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (currentWindow = 1; currentWindow < maxWindow; currentWindow++) {
                for (int position : entityPositions) {
                    if (position + currentWindow < sortedEntities.length
                            && sortedEntities[position + currentWindow] < entityId) {
                        int neighborId = sortedEntities[position + currentWindow];
                        if (flags[neighborId] != entityId) {
                            sPositionIndex.getCounters()[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId);
                    }

                    if (0 <= position - currentWindow
                            && sortedEntities[position - currentWindow] < entityId) {
                        int neighborId = sortedEntities[position - currentWindow];
                        if (flags[neighborId] != entityId) {
                            sPositionIndex.getCounters()[neighborId] = 0;
                            flags[neighborId] = entityId;
                        }

                        updateLocalWeight(neighborId);
                    }
                }
            }

            topKEdges.clear();
            minimumWeight = -1;
            for (Integer neighborId : distinctNeighbors) {
                double weight = getWeight(entityId, neighborId);
                if (weight < minimumWeight) {
                    continue;
                }

                Comparison c = new Comparison(cleanCleanER, neighborId, entityId);
                c.setUtilityMeasure(weight);
                topKEdges.add(c);
                if (maxCPE < topKEdges.size()) {
                    Comparison lastComparison = topKEdges.poll();
                    minimumWeight = lastComparison.getUtilityMeasure();
                }
            }
            topComparisons.addAll(topKEdges);
        }
        /*sortedTopComparisons = topComparisons.toArray(new Comparison[topComparisons.size()]);*/
        for (Comparison c : topComparisons) {
            sortedTopComparisons.offer(c);
        }
        /*System.out.println("Total comparisons\t:\t" + sortedTopComparisons.length);*/
        System.out.println("Total comparisons:\t" + sortedTopComparisons.size());
    }

    protected double getWeight(int entityId, int neighborId) {
        return sPositionIndex.getWeight(entityId, neighborId, 0, 0);
//        switch (weightingSchema) {
//            case NCF:
//                double denominator = sPositionIndex.getEntityPositions(entityId).length + sPositionIndex.getEntityPositions(neighborId).length - counters_neighbor_cooccurrence[neighborId];
//                return counters_neighbor_cooccurrence[neighborId] / denominator;
//            default:
//                return counters_neighbor_cooccurrence[neighborId];
//        }
    }

    @Override
    public boolean hasNext() {
        /*return counter < sortedTopComparisons.length;*/
        return !sortedTopComparisons.isEmpty();
    }

    @Override
    public Comparison next() {
        if (hasNext()) {
            /*return sortedTopComparisons[counter++];*/
            return sortedTopComparisons.pollFirst();
        }
        return null;
    }


    protected void updateLocalWeight(int neighborId) {
        switch (weightingSchema) {
            case ID:
                sPositionIndex.getCounters()[neighborId] += 1.0 / currentWindow;
                distinctNeighbors.add(neighborId);
                break;
            default: // ACF
                sPositionIndex.getCounters()[neighborId]++;
                break;
        }
        distinctNeighbors.add(neighborId);
    }
}