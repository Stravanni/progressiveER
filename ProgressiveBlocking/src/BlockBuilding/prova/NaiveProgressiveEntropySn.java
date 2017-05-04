package BlockBuilding.prova;

import BlockBuilding.Progressive.DataStructures.PositionIndex.PositionIndex;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;

import java.util.Iterator;
import java.util.List;

/**
 * @author gap2
 * @author giovanni
 */

public class NaiveProgressiveEntropySn implements Iterator<Comparison> {

    protected final boolean cleanCleanER;

    protected int currentPosition;
    protected int currentWindow;

    protected final int datasetLimit;
    protected final int maxWindow;
    protected final int noOfEntities;

    protected int repId1;
    protected int repId2;

    protected final int[] sortedEntities;

    protected final PositionIndex sPositionIndex;

    protected boolean removeRepeatedComparisons;

    public int countRedundant = 0;

    private int maxW = 1000;

    public NaiveProgressiveEntropySn(List<EntityProfile>[] profiles) {
        this(profiles, false);
    }


    public NaiveProgressiveEntropySn(int bk, ProfileType pt, List<EntityProfile>[] profiles) {
        this(profiles, false, false, bk, pt);
    }

    public NaiveProgressiveEntropySn(List<EntityProfile>[] profiles, boolean removeRep) {
        this(profiles, removeRep, removeRep); // when removeRep, it always needs also PositionIndex
    }

    public NaiveProgressiveEntropySn(List<EntityProfile>[] profiles, boolean removeRep, boolean buildPositionIndex) {
        final ProgressiveEntropySnBuilder npsn = new ProgressiveEntropySnBuilder(profiles);
        npsn.buildBlocks();

        sortedEntities = npsn.getEntityList();

        cleanCleanER = npsn.isClean();
        datasetLimit = npsn.getDatasetLimit();
        noOfEntities = (int) npsn.getTotalNoOfEntities();

        if (cleanCleanER) {
            /*maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities) + 1));*/
            maxWindow = maxW;
            System.out.println("win max: " + maxWindow);
        } else {
            /*maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities)));*/
            maxWindow = maxW;
        }

        // initialize comparisons
        currentPosition = -1;
        currentWindow = 1;

        this.removeRepeatedComparisons = removeRep;
        if (buildPositionIndex) {
            System.out.println("position index building");
            sPositionIndex = new PositionIndex(noOfEntities, sortedEntities);
        } else {
            sPositionIndex = null;
        }
    }

    public NaiveProgressiveEntropySn(List<EntityProfile>[] profiles, boolean removeRep, boolean buildPositionIndex, int bk, ProfileType pt) {
        final ProgressiveEntropySnBuilder npsn = new ProgressiveEntropySnBuilder(bk, pt, profiles);
        npsn.buildBlocks();

        sortedEntities = npsn.getEntityList();

        cleanCleanER = npsn.isClean();
        datasetLimit = npsn.getDatasetLimit();
        noOfEntities = (int) npsn.getTotalNoOfEntities();

        if (cleanCleanER) {
            /*maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities) + 1));*/
            maxWindow = maxW;
            System.out.println("win max: " + maxWindow);
        } else {
            /*maxWindow = noOfEntities <= 100 ? 2 : (int) Math.round(Math.pow(2, Math.log10(noOfEntities)));*/
            maxWindow = maxW;
        }

        // initialize comparisons
        currentPosition = -1;
        currentWindow = 1;

        this.removeRepeatedComparisons = removeRep;
        if (buildPositionIndex) {
            System.out.println("position index building");
            sPositionIndex = new PositionIndex(noOfEntities, sortedEntities);
        } else {
            sPositionIndex = null;
        }
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
        Comparison newComp;
        if (removeRepeatedComparisons) {
            do {
                newComp = nextComparison();
            } while (isRedundant(newComp));
        } else {
            newComp = nextComparison();
        }
        return newComp;
    }

    public Comparison nextComparison() {
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
        repId1 = currentPosition;
        repId2 = currentPosition + currentWindow;
        Comparison cc;
        if (cleanCleanER) {
            if (id1 < id2) {
                cc = new Comparison(cleanCleanER, id1, id2 - datasetLimit);
            } else {
                cc = new Comparison(cleanCleanER, id2, id1 - datasetLimit);
            }
        } else {
            cc = new Comparison(cleanCleanER, id1, id2);
        }
        cc.set_sn_positions(repId1, repId2);
        return cc;
        //return (id1 < id2) ? new Comparison(cleanCleanER, id1, id2) : new Comparison(cleanCleanER, id2, id1);
    }

    protected void updateCounters() {
        if (currentPosition + currentWindow == sortedEntities.length - 1) { // reached the end of sorted list
            currentPosition = 0; // restart start from the beginning
            currentWindow++; // increment window size
        } else {
            currentPosition++;
        }
    }

    protected boolean isRedundant(Comparison c) {
        if (c == null) {
            return false;
        }
//        int[] positions1 = sPositionIndex.getEntityPositions(sortedEntities[currentPosition]);
//        int[] positions2 = sPositionIndex.getEntityPositions(sortedEntities[currentPosition + currentWindow]);

        int comparison_e1_position = sortedEntities[c.get_sn_positions()[0]];
        int comparison_e2_position = sortedEntities[c.get_sn_positions()[1]];

        if (currentPosition != (Math.min(c.get_sn_positions()[0], c.get_sn_positions()[1]))) {
            System.out.println("current window error");
        }

        int[] positions1 = sPositionIndex.getEntityPositions(comparison_e1_position);
        int[] positions2 = sPositionIndex.getEntityPositions(comparison_e2_position);

        if (positions2.length < positions1.length) {
            int[] temp = positions1;
            positions1 = positions2;
            positions2 = temp;
        }

        for (int p1 : positions1) {
            for (int p2 : positions2) {
                if (Math.abs(p1 - p2) < currentWindow) {
                    countRedundant++;
                    return true;
                }
                if (p1 < p2) {
                    break;
                }
            }
        }

        for (int p1 : positions1) {
            for (int p2 : positions2) {
                if (Math.abs(p1 - p2) == currentWindow) {
                    boolean isRed = Math.min(p1, p2) < currentPosition;
                    if (isRed) {
                        countRedundant++;
                    }
                    return isRed;
                }
                if (p1 < p2) {
                    break;
                }
            }
        }
        return false;
    }
}