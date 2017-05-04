package ProgressiveSortedNeighborhood;

import BlockBuilding.Progressive.SortedNeighborhood.ProgressiveSnBuilder;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;

import java.util.Iterator;
import java.util.List;

/**
 * @author gap2
 */

public class NaiveProgressiveSnIterator implements Iterator<Comparison> {

    protected final boolean cleanCleanER;

    protected int currentPosition;
    protected int currentWindow;

    protected final int datasetLimit;
    protected final int maxWindow;
    protected final int noOfEntities;

    protected final int[] sortedEntities;

    private int maxW = 10;

    public NaiveProgressiveSnIterator(List<EntityProfile>[] profiles) {
        /*final NaiveProgressiveSN npsn = new NaiveProgressiveSN(profiles);*/
        final ProgressiveSnBuilder npsn = new ProgressiveSnBuilder(profiles);
        npsn.buildBlocks();

        sortedEntities = npsn.getEntityList();

        cleanCleanER = npsn.isClean();
        datasetLimit = npsn.getDatasetLimit();
        noOfEntities = (int) npsn.getTotalNoOfEntities();

        if (cleanCleanER) {
            maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities) + 1)) + 1;
        } else {
            /*maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities))) + 1;*/
            maxWindow = maxW;
        }

        // initialize comparisons
        currentPosition = -1;
        currentWindow = 1;
    }

    public NaiveProgressiveSnIterator(int bk, ProfileType pt, List<EntityProfile>[] profiles) {
        /*final NaiveProgressiveSN npsn = new NaiveProgressiveSN(profiles);*/
        final ProgressiveSnBuilder npsn = new ProgressiveSnBuilder(bk, pt, profiles);
        npsn.buildBlocks();

        sortedEntities = npsn.getEntityList();

        cleanCleanER = npsn.isClean();
        datasetLimit = npsn.getDatasetLimit();
        noOfEntities = (int) npsn.getTotalNoOfEntities();

        if (cleanCleanER) {
            maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities) + 1)) + 1;
        } else {
            /*maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities))) + 1;*/
            maxWindow = maxW;
        }

        // initialize comparisons
        currentPosition = -1;
        currentWindow = 1;
    }

    @Override
    public boolean hasNext() {
        return currentWindow < maxWindow;
    }

    protected boolean isValidComparison() {
        if (cleanCleanER) { // the entity ids should belong to different entity collections
            return (sortedEntities[currentPosition] < datasetLimit && datasetLimit <= sortedEntities[currentPosition + currentWindow])
                    || (datasetLimit <= sortedEntities[currentPosition] && sortedEntities[currentPosition + currentWindow] < datasetLimit);
        } else { // the entity ids should be different
            return sortedEntities[currentPosition] != sortedEntities[currentPosition + currentWindow];
        }
    }

    @Override
    public Comparison next() {
        updateCounters();
        if (!hasNext()) {
            return null;
        }

        while (!isValidComparison()) {
            updateCounters();
            if (!hasNext()) {
                return null;
            }
        }

        int id1 = sortedEntities[currentPosition];
        int id2 = sortedEntities[currentPosition + currentWindow];
        if (cleanCleanER) {
            return (id1 < id2) ? new Comparison(cleanCleanER, id1, id2 - datasetLimit) : new Comparison(cleanCleanER, id2, id1 - datasetLimit);
        }

        return (id1 < id2) ? new Comparison(cleanCleanER, id1, id2) : new Comparison(cleanCleanER, id2, id1);
    }

    protected void updateCounters() {
        if (currentPosition + currentWindow == sortedEntities.length - 1) { // reached the end of sorted list
            currentPosition = 0; // restart start from the beginning
            currentWindow++; // increment window size
        } else {
            currentPosition++;
        }
    }
}
