package ProgressiveSortedNeighborhood;

import DataStructures.Comparison;
import DataStructures.EntityProfile;
import ProgressiveSortedNeighborhood.DataStructures.SimplePositionIndex;

import java.util.*;

/**
 *
 * @author gap2
 */

public class LocalCpProgressiveSnIterator extends NaiveProgressiveSnIterator implements Iterator<Comparison> {

    protected final List<Comparison> windowComparisons;
    protected final Set<Integer> neighbors;
    protected final SimplePositionIndex sPositionIndex;

    public LocalCpProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);

        currentWindow = 0;
        neighbors = new HashSet<>();
        windowComparisons = new ArrayList<>();
        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);
    }

    protected void getCleanCleanWindowComparisons() {
        for (int entityId = 0; entityId < datasetLimit; entityId++) {
            neighbors.clear();

            int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (int position : entityPositions) {
                if (position + currentWindow < sortedEntities.length
                        && datasetLimit <= sortedEntities[position + currentWindow]) {
                    neighbors.add(sortedEntities[position + currentWindow]);
                }

                if (0 <= position - currentWindow
                        && datasetLimit <= sortedEntities[position - currentWindow]) {
                    neighbors.add(sortedEntities[position - currentWindow]);
                }

            }

            for (Integer neighborId : neighbors) {
                windowComparisons.add(new Comparison(cleanCleanER, entityId, neighborId - datasetLimit));
            }
        }
    }

    protected void getDirtyWindowComparisons() {
        for (int entityId = 0; entityId < noOfEntities; entityId++) {
            neighbors.clear();

            int[] entityPositions = sPositionIndex.getEntityPositions(entityId);
            for (int position : entityPositions) {
                if (position + currentWindow < sortedEntities.length
                        && sortedEntities[position + currentWindow] < entityId) {
                    neighbors.add(sortedEntities[position + currentWindow]);
                }

                if (0 <= position - currentWindow
                        && sortedEntities[position - currentWindow] < entityId) {
                    neighbors.add(sortedEntities[position - currentWindow]);
                }
            }

            for (Integer neighborId : neighbors) {
                windowComparisons.add(new Comparison(cleanCleanER, neighborId, entityId));
            }
        }
    }

    @Override
    public Comparison next() {
        if (windowComparisons.isEmpty()) {
            currentWindow++;
            if (hasNext()) {
                if (cleanCleanER) {
                    getCleanCleanWindowComparisons();
                } else {
                    getDirtyWindowComparisons();
                }
            } else {
                return null;
            }
        }

        return windowComparisons.remove(0);
    }
}
