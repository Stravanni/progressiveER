package BlockBuilding.Progressive.MemoryBased;

import BlockBuilding.Progressive.AbstractProgressiveSortedNeighbor_heap;
import BlockBuilding.Progressive.DataStructures.PIWeightingScheme;
import DataStructures.EntityProfile;

import java.util.List;

/**
 * Created by gio
 * on 14/04/16.
 */
public class ProgressiveSortedNeighbor_heap extends AbstractProgressiveSortedNeighbor_heap {

    public ProgressiveSortedNeighbor_heap(List<EntityProfile>[] profiles, boolean removeRepetedComparisons) {
        super(removeRepetedComparisons);
        name = "psn" + (removeRepetedComparisons ? "_norep" : "_rep");
        snb = new SortedNeighborhoodBlocking_builder(profiles);
    }

    public ProgressiveSortedNeighbor_heap(List<EntityProfile>[] profiles, PIWeightingScheme wScheme, boolean removeRepetedComparisons) {
        super(wScheme, removeRepetedComparisons);
        name = "psn_mh_" + (removeRepetedComparisons ? "_norep" : "_rep");
        snb = new SortedNeighborhoodBlocking_builder(profiles);
    }

}
