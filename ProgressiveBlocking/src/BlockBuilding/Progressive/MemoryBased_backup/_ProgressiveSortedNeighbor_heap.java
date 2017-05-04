//package BlockBuilding.Progressive.SortedNeighborhood.Hybrid.MemoryBased;
//
//import BlockBuilding.Progressive.SortedNeighborhood.AbstractProgressiveSortedNeighbor_heap;
//import BlockBuilding.Progressive.DataStructures.PositionIndexWeightingScheme;
//import BlockBuilding.Progressive.SortedNeighborhood.ProgressiveSnBuilder;
//import DataStructures.EntityProfile;
//
//import java.util.List;
//
///**
// * Created by gio
// * on 14/04/16.
// */
//public class ProgressiveSortedNeighbor_heap extends AbstractProgressiveSortedNeighbor_heap {
//
//    public ProgressiveSortedNeighbor_heap(List<EntityProfile>[] profiles, boolean removeRepetedComparisons) {
//        super(removeRepetedComparisons);
//        name = "psn" + (removeRepetedComparisons ? "_norep" : "_rep");
//        snb = new ProgressiveSnBuilder(profiles);
//    }
//
//    public ProgressiveSortedNeighbor_heap(List<EntityProfile>[] profiles, PositionIndexWeightingScheme wScheme, boolean removeRepetedComparisons) {
//        super(wScheme, removeRepetedComparisons);
//        name = "psn_mh_" + (removeRepetedComparisons ? "_norep" : "_rep");
//        snb = new ProgressiveSnBuilder(profiles);
//    }
//
//    public ProgressiveSortedNeighbor_heap(List<EntityProfile>[] profiles, PositionIndexWeightingScheme wScheme, boolean removeRepetedComparisons, int max_win) {
//        super(wScheme, removeRepetedComparisons);
//        name = "psn_mh_" + (removeRepetedComparisons ? "_norep" : "_rep");
//        snb = new ProgressiveSnBuilder(profiles);
//        this.buildEntityList(max_win);
//    }
//
//}
