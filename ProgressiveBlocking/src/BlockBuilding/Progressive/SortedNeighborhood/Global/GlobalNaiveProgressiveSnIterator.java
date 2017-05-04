package BlockBuilding.Progressive.SortedNeighborhood.Global;

import BlockBuilding.Progressive.SortedNeighborhood.Local.NaiveProgressiveSn;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import ProgressiveSortedNeighborhood.DataStructures.SimplePositionIndex;
import ProgressiveSortedNeighborhood.NaiveProgressiveSnIterator;

import java.util.List;

/**
 * @author gap2
 */

public class GlobalNaiveProgressiveSnIterator extends NaiveProgressiveSn {

    protected final SimplePositionIndex sPositionIndex;

    public GlobalNaiveProgressiveSnIterator(List<EntityProfile>[] profiles) {
        super(profiles);
        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);
    }

    public GlobalNaiveProgressiveSnIterator(List<EntityProfile>[] profiles, int max_win) {
        super(profiles, max_win);
        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);
    }

    public GlobalNaiveProgressiveSnIterator(int bk, ProfileType pt, List<EntityProfile>[] profiles) {
        super(bk, pt, profiles);
        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);
    }

    public GlobalNaiveProgressiveSnIterator(int bk, ProfileType pt, List<EntityProfile>[] profiles, int max_win) {
        super(bk, pt, profiles, max_win);
        sPositionIndex = new SimplePositionIndex(noOfEntities, sortedEntities);
    }

    @Override
    public Comparison next() {
        Comparison newComp;
        do {
            /*it is like the naive_psn, but it checks for redundancy in the while loop before emitting the comparison*/
            newComp = super.next();
        } while (isRedundant());
        return newComp;
    }

    protected boolean isRedundant() {
        int[] positions1 = sPositionIndex.getEntityPositions(sortedEntities[currentPosition]);
        int[] positions2 = sPositionIndex.getEntityPositions(sortedEntities[currentPosition + currentWindow]);
        if (positions2.length < positions1.length) {
            int[] temp = positions1;
            positions1 = positions2;
            positions2 = temp;
        }

        for (int p1 : positions1) {
            for (int p2 : positions2) {
                if (Math.abs(p1 - p2) < currentWindow) {
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
                    return Math.min(p1, p2) < currentPosition;
                }

                if (p1 < p2) {
                    break;
                }
            }
        }

        return false;
    }
}