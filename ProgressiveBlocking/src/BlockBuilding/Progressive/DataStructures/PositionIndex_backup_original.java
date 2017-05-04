///*
// *    This program is free software; you can redistribute it and/or modify
// *    it under the terms of the GNU General Public License as published by
// *    the Free Software Foundation; either version 2 of the License, or
// *    (at your option) any later version.
// *
// *    This program is distributed in the hope that it will be useful,
// *    but WITHOUT ANY WARRANTY; without even the implied warranty of
// *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *    GNU General Public License for more details.
// *
// *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
// */
//package BlockBuilding.Progressive.DataStructures;
//
//import DataStructures.EntityProfile;
//import Utilities.ProfileComparison;
//import com.google.common.collect.MinMaxPriorityQueue;
//import com.google.common.collect.Ordering;
//
//import java.io.Serializable;
//import java.util.*;
//
///**
// * @author gap2
// */
//public class PositionIndex_backup_original implements Serializable {
//
//    private static final long serialVersionUID = 13483254243447435L;
//
//    private final int noOfEntities;
//    private final PositionInfo[][] entityPositions;
//    public LinkedHashSet<Integer>[] entityPositionsExtended;
//    private final PriorityQueue<PositionInfo> entitiesQueue;
//    private final Queue<PairsInfo> pairsQueue;
//    private final PIWeightingScheme weightingScheme;
//    private final List<EntityProfile>[] profiles;
//    public final int[] windowSize;
//    public final HashMap<Integer, Integer>[] windowSizeMap;
//    private final int datasetLimits;
//
//    public PositionIndex_backup_original(int entities, Integer[] sortedEntities, List<EntityProfile>[] profiles, PIWeightingScheme wScheme) {
//        noOfEntities = entities;
//        entitiesQueue = new PriorityQueue<>();
//
//        weightingScheme = wScheme;
//
//        //pairsQueue = new PriorityQueue<>();
//
//        //Comparator<PairsInfo> compMinMax = Ordering.natural().reverse();
//        Comparator<PairsInfo> compMinMax = Ordering.natural();
//        int sizePairsQueue = (weightingScheme == PIWeightingScheme.JS) ? 1 : noOfEntities;
//        pairsQueue = MinMaxPriorityQueue.orderedBy(compMinMax)
//                .maximumSize(sizePairsQueue)
//                .create();
//
//        this.profiles = profiles;
//        datasetLimits = profiles[0].size();
//
//        int[] counters = new int[noOfEntities];
//        for (int entityId : sortedEntities) {
//            counters[entityId]++;
//        }
//
//        //initialize inverted index
//        entityPositions = new PositionInfo[noOfEntities][];
//        entityPositionsExtended = new LinkedHashSet[noOfEntities];
//        windowSize = new int[noOfEntities];
//        windowSizeMap = new HashMap[noOfEntities];
//        for (int i = 0; i < noOfEntities; i++) {
//            windowSizeMap[i] = new HashMap<>();
//            windowSize[0] = 0;
//            entityPositionsExtended[i] = new LinkedHashSet<>();
//            entityPositions[i] = new PositionInfo[counters[i]];
//            counters[i] = 0;
//        }
//
//        //build inverted index
//        for (int i = 0; i < sortedEntities.length; i++) {
//            int entityId = sortedEntities[i];
//            entityPositions[entityId][counters[entityId]++] = new PositionInfo(entityId, i);
//        }
//    }
//
//    public void setWindows(int entityId, int w) {
//        windowSize[entityId] = w;
//    }
//
//    public void setWindowSizeMap(int entityId, int position, int w) {
//        windowSizeMap[entityId].put(position, w);
//    }
//
//    private double getAggregateInverseDinstance(int entity1, int entity2, int position1, int position2) {
//        pairsQueue.clear();
//        for (PositionInfo pi1 : entityPositions[entity1]) {
//            for (PositionInfo pi2 : entityPositions[entity2]) {
//                pairsQueue.add(new PairsInfo(pi1, pi2));
//            }
//        }
//
//        PairsInfo head = pairsQueue.poll();
//        double inputWindow = Math.abs(position1 - position2);
//        double minWindow = Math.abs(head.getPositionInfo1().getPositionId() - head.getPositionInfo2().getPositionId());
//        if (minWindow < inputWindow) {
//            return -1;
//        }
//
//        double totalWeight = 1.0 / minWindow;
//        Set<Integer> excludedPositions = new HashSet<Integer>();
//        excludedPositions.add(head.getPositionInfo1().getPositionId());
//        excludedPositions.add(head.getPositionInfo2().getPositionId());
//
//        while (!pairsQueue.isEmpty()) {
//            PairsInfo currentPair = pairsQueue.poll();
//            if (excludedPositions.contains(currentPair.getPositionInfo1().getPositionId())
//                    || excludedPositions.contains(currentPair.getPositionInfo2().getPositionId())) {
//                continue;
//            }
//
//            double currentWindow = Math.abs(currentPair.getPositionInfo1().getPositionId() - currentPair.getPositionInfo2().getPositionId());
//            totalWeight += 1.0 / currentWindow;
//            excludedPositions.add(currentPair.getPositionInfo1().getPositionId());
//            excludedPositions.add(currentPair.getPositionInfo2().getPositionId());
//        }
//        return totalWeight;
//    }
//
//    private double getExhaustiveAggregateInverseDistance(int entity1, int entity2, int position1, int position2) {
//        pairsQueue.clear();
//        for (PositionInfo pi1 : entityPositions[entity1]) {
//            for (PositionInfo pi2 : entityPositions[entity2]) {
//                pairsQueue.add(new PairsInfo(pi1, pi2));
//            }
//        }
//
//        PairsInfo head = pairsQueue.poll();
//        double inputWindow = Math.abs(position1 - position2);
//        double minWindow = Math.abs(head.getPositionInfo1().getPositionId() - head.getPositionInfo2().getPositionId());
//        if (minWindow < inputWindow) {
//            return -1;
//        }
//
//        double totalWeight = 1.0 / minWindow;
//        while (!pairsQueue.isEmpty()) {
//            PairsInfo currentPair = pairsQueue.poll();
//            double currentWindow = Math.abs(currentPair.getPositionInfo1().getPositionId() - currentPair.getPositionInfo2().getPositionId());
//            totalWeight += 1.0 / currentWindow;
//        }
//        return totalWeight;
//    }
//
//    private double getExhaustiveNormalizedWindowOverlap(int entity1, int entity2, int position1, int position2) {
//        double overlapWeight = getExhaustiveWindowOverlap(entity1, entity2, position1, position2);
//        return overlapWeight / (entityPositions[entity1].length + entityPositions[entity2].length);
//    }
//
//    private double getExhaustiveWindowOverlap(int entity1, int entity2, int position1, int position2) {
//        pairsQueue.clear();
//        for (PositionInfo pi1 : entityPositions[entity1]) {
//            for (PositionInfo pi2 : entityPositions[entity2]) {
//                pairsQueue.add(new PairsInfo(pi1, pi2));
//            }
//        }
//
//        PairsInfo head = pairsQueue.poll();
//        double inputWindow = Math.abs(position1 - position2);
//        double minWindow = Math.abs(head.getPositionInfo1().getPositionId() - head.getPositionInfo2().getPositionId());
//        if (minWindow < inputWindow) {
//            return -1;
//        }
//
//        double totalWeight = 1;
//        while (!pairsQueue.isEmpty()) {
//            PairsInfo currentPair = pairsQueue.poll();
//            double currentWindow = Math.abs(currentPair.getPositionInfo1().getPositionId() - currentPair.getPositionInfo2().getPositionId());
//            if (inputWindow < currentWindow) {
//                return totalWeight;
//            }
//            totalWeight++;
//        }
//        return totalWeight;
//    }
//
//    private double getNormalizedWindowOverlap(int entity1, int entity2, int position1, int position2) {
//        double overlapWeight = getWindowOverlap(entity1, entity2, position1, position2);
//        return overlapWeight / (entityPositions[entity1].length + entityPositions[entity2].length);
//    }
//
//    private double getWindowOverlap(int entity1, int entity2, int position1, int position2) {
//        pairsQueue.clear();
//        for (PositionInfo pi1 : entityPositions[entity1]) {
//            for (PositionInfo pi2 : entityPositions[entity2]) {
//                pairsQueue.add(new PairsInfo(pi1, pi2));
//            }
//        }
//
//        PairsInfo head = pairsQueue.poll();
//        double inputWindow = Math.abs(position1 - position2);
//        double minWindow = Math.abs(head.getPositionInfo1().getPositionId() - head.getPositionInfo2().getPositionId());
//        if (minWindow < inputWindow) {
//            return -1;
//        }
//
//        double totalWeight = 1;
//        Set<Integer> excludedPositions = new HashSet<Integer>();
//        excludedPositions.add(head.getPositionInfo1().getPositionId());
//        excludedPositions.add(head.getPositionInfo2().getPositionId());
//
//        while (!pairsQueue.isEmpty()) {
//            PairsInfo currentPair = pairsQueue.poll();
//            if (excludedPositions.contains(currentPair.getPositionInfo1().getPositionId())
//                    || excludedPositions.contains(currentPair.getPositionInfo2().getPositionId())) {
//                continue;
//            }
//
//            double currentWindow = Math.abs(currentPair.getPositionInfo1().getPositionId() - currentPair.getPositionInfo2().getPositionId());
//            if (inputWindow < currentWindow) {
//                return totalWeight;
//            }
//            totalWeight++;
//            excludedPositions.add(currentPair.getPositionInfo1().getPositionId());
//            excludedPositions.add(currentPair.getPositionInfo2().getPositionId());
//        }
//        return totalWeight;
//    }
//
//    public double getWeight(int entity1, int entity2, int position1, int position2) {
//        switch (weightingScheme) {
//            case AGGREGATE_INVERSE_DISTANCE:
//                return getAggregateInverseDinstance(entity1, entity2, position1, position2);
//            case EXHAUSTIVE_AGGREGATE_INVERSE_DISTANCE:
//                return getExhaustiveAggregateInverseDistance(entity1, entity2, position1, position2);
//            case EXHAUSTIVE_NORMALIZED_WINDOW_OVERLAP:
//                return getExhaustiveNormalizedWindowOverlap(entity1, entity2, position1, position2);
//            case EXHAUSTIVE_WINDOW_OVERLAP:
//                return getExhaustiveWindowOverlap(entity1, entity2, position1, position2);
//            case NORMALIZED_WINDOW_OVERLAP:
//                return getNormalizedWindowOverlap(entity1, entity2, position1, position2);
//            case WINDOW_OVERLAP:
//                return getWindowOverlap(entity1, entity2, position1, position2);
//            case JS:
//                return ProfileComparison.getJaccardSimilarity(profiles[(entity1 < datasetLimits) ? 0 : 1].get(entity1).getAttributes(), profiles[(entity2 < datasetLimits) ? 0 : 1].get(entity2).getAttributes());
//        }
//        return -1;
//    }
//
//    public boolean isRepeatedComparison(int entity1, int entity2, int position1, int position2) {
//        pairsQueue.clear();
//        for (PositionInfo pi1 : entityPositions[entity1]) {
//            for (PositionInfo pi2 : entityPositions[entity2]) {
//                pairsQueue.add(new PairsInfo(pi1, pi2));
//            }
//        }
//
//        PairsInfo head = pairsQueue.poll();
//        double inputWindow = Math.abs(position1 - position2);
//        double minWindow = Math.abs(head.getPositionInfo1().getPositionId() - head.getPositionInfo2().getPositionId());
//        if (minWindow < inputWindow) {
//            return true;
//        }
//
//        int maxPosition = Math.max(head.getPositionInfo1().getPositionId(), head.getPositionInfo2().getPositionId());
//        int minPosition = Math.min(head.getPositionInfo1().getPositionId(), head.getPositionInfo2().getPositionId());
//
//        int inputMaxPosition = Math.max(position1, position2);
//        int inputMinPosition = Math.min(position1, position2);
//        return maxPosition != inputMaxPosition || minPosition != inputMinPosition;
//    }
//
//    public boolean isRepeatedComparison2(int entity1, int entity2, int position1, int position2) {
//        pairsQueue.clear();
//        for (PositionInfo pi1 : entityPositions[entity1]) {
//            for (PositionInfo pi2 : entityPositions[entity2]) {
//                pairsQueue.add((pi1.getPositionId() < pi2.getPositionId()) ? new PairsInfo(pi1, pi2) : new PairsInfo(pi2, pi1));
//            }
//        }
//
////        for (PositionInfo pi1 : entityPositions[entity1]) {
////            for (PositionInfo pi2 : entityPositions[entity2]) {
////                PairsInfo pp = new PairsInfo(pi1, pi2);
////                PairsInfo tt = pairsQueue.peek();
////                if (tt.getDistance() == pp.getDistance() && Math.min(tt.getPositionInfo1().getPositionId(), tt.getPositionInfo2().getPositionId()) > Math.min(pi1.getPositionId(), pi2.getPositionId())) {
////                    System.out.println("not the top");
////                }
////                if (tt.getDistance() > pp.getDistance()) {
////                    System.out.println("not the top two");
////                }
////            }
////        }
//
//        PairsInfo head = pairsQueue.poll();
//        //double inputWindow = Math.abs(position1 - position2);
//        double inputWindow = position2 - position1;
//        double minWindow = Math.abs(head.getPositionInfo1().getPositionId() - head.getPositionInfo2().getPositionId());
//        if (minWindow < inputWindow) {
//            return true;
//        } else if (minWindow > inputWindow) {
//            System.out.println("problem with minWindow pindex");
//            return false;
//        }
//
//        int maxPosition = Math.max(head.getPositionInfo1().getPositionId(), head.getPositionInfo2().getPositionId());
//        int minPosition = Math.min(head.getPositionInfo1().getPositionId(), head.getPositionInfo2().getPositionId());
////
////        if (maxPosition != head.getPositionInfo2().getPositionId()) {
////            System.out.println("head max position not max");
////        }
//
//        int inputMaxPosition = Math.max(position1, position2);
//        int inputMinPosition = Math.min(position1, position2);
////        if (inputMinPosition != position1) {
////            System.out.println("\n\norder problem\n\n");
////        }
//
//        return maxPosition != inputMaxPosition || minPosition != inputMinPosition;
////        boolean sameMin = (head.getPositionInfo1().getPositionId() == position1);// && (entity1 == head.getPositionInfo1().getEntityId());
////        boolean sameMax = (head.getPositionInfo2().getPositionId() == position2);// && (entity2 == head.getPositionInfo2().getEntityId());
////
////        return !(sameMin && sameMax);
//    }
//
//    public void addPosition(int entityId, int visitedPosition) {
//        entityPositionsExtended[entityId].add(visitedPosition);
//    }
//
//    //public boolean isRepeatedComparisonExtended(int entity1, int entity2, int position1, int position2) {
//
//    public boolean isRepeatedComparisonExtended(int entity1, int entity2, int position1, int position2) {
//
//        if (entityPositionsExtended[entity1].isEmpty() || entityPositionsExtended[entity2].isEmpty()) {
//            //System.out.println("new entity");
//            return false;
//        }
//
//        Iterator it1 = entityPositionsExtended[entity1].iterator();
//        Iterator it2 = entityPositionsExtended[entity2].iterator();
//
//        while (it1.hasNext()) {
//            int p1 = (int) it1.next();
//            it2 = entityPositionsExtended[entity2].iterator();
//            while (it2.hasNext()) {
//                int p2 = (int) it2.next();
//
//                if (p1 < p2) {
//                    if (windowSizeMap[entity1].getOrDefault(p1, 0) == 0) {
//                        continue;
//                    }
//                    if (p2 - p1 <= windowSizeMap[entity1].get(p1)) {
//                        return true;
//                    }
//                }
//
//                if (p2 < p1) {
//                    if (windowSizeMap[entity2].getOrDefault(p2, 0) == 0) {
//                        continue;
//                    }
//                    if (p1 - p2 <= windowSizeMap[entity2].get(p2)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//
//    public boolean isRepeatedComparisonExtendedPrint(int entity1, int entity2, int position1, int position2) {
//
//        if (entityPositionsExtended[entity1].isEmpty() || entityPositionsExtended[entity2].isEmpty()) {
//            return false;
//        }
//
//        Iterator it1 = entityPositionsExtended[entity1].iterator();
//        Iterator it2 = entityPositionsExtended[entity2].iterator();
//
//        while (it1.hasNext()) {
//            int p1 = (int) it1.next();
//            it2 = entityPositionsExtended[entity2].iterator();
//            while (it2.hasNext()) {
//                int p2 = (int) it2.next();
//
//                if (p1 < p2) {
//                    if (windowSizeMap[entity1].getOrDefault(p1, 0) == 0) {
//                        continue;
//                    }
//                    if (p2 - p1 <= windowSizeMap[entity1].get(p1)) {
//                        System.out.println("print: " + p1 + " - " + p2 + " - " + windowSizeMap[entity1].get(p1));
//                        return true;
//                    }
//                }
//
//                if (p2 < p1) {
//                    if (windowSizeMap[entity2].getOrDefault(p2, 0) == 0) {
//                        continue;
//                    }
//                    if (p1 - p2 <= windowSizeMap[entity2].get(p2)) {
//                        System.out.println("print: " + p2 + " - " + p1 + " - " + windowSizeMap[entity2].get(p2));
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//}
