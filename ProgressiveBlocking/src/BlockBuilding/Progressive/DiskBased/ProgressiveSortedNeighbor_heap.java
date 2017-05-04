package BlockBuilding.Progressive.DiskBased;

import BlockBuilding.Progressive.AbstractProgressiveSortedNeighbor_heap;
import BlockBuilding.Progressive.DataStructures.PIWeightingScheme;

/**
 * Created by gio
 * on 14/04/16.
 */
public class ProgressiveSortedNeighbor_heap extends AbstractProgressiveSortedNeighbor_heap {

    public ProgressiveSortedNeighbor_heap(String[] entities, String[] profiles, boolean removeRepetedComparisons) {
        super(removeRepetedComparisons);
        snb = new SortedNeighborhoodBlocking_builder(entities, profiles);
    }

    public ProgressiveSortedNeighbor_heap(String[] entities, String[] profiles, PIWeightingScheme wScheme, boolean removeRepetedComparisons) {
        super(wScheme, removeRepetedComparisons);
        snb = new SortedNeighborhoodBlocking_builder(entities, profiles);
    }

}
