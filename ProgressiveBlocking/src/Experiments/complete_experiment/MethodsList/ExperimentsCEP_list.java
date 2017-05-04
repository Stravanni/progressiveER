/**
 * - NaiveProgressiveSn:                    naivePSN
 * - LocalWeightedProgressiveSn(ws=null):   naivePSN w/o duplicate comparisons within W
 * - LocalWeightedProgressiveSn(ws):        PSN local ordering w/o duplicate comparisons within W
 * - ProgressiveSnHeap:                     PSN hybrid ordering w/ duplicate comparison (they could be removed)
 * - GlobalNaiveProgressiveSnIterator:      naivePSN w/o duplicate comparisons (ALL duplicates comparisons are removed)
 * - GlobalWeightedProgressiveSn(ws=null):  For each entity retain a fixed num of comparisons
 * - GlobalWeightedProgressiveSn(ws):       For each entity retain a fixed num of comparisons + WEIGHTS
 */
package Experiments.complete_experiment.MethodsList;

import BlockBuilding.Progressive.DataStructures.WeightingSchemeSn;
import BlockBuilding.Progressive.DataStructures.WeightingSchemeSnLocal;
import BlockBuilding.Progressive.SortedNeighborhood.Global.GlobalNaiveProgressiveSnIterator;
import BlockBuilding.Progressive.SortedNeighborhood.Global.GlobalWeightedProgressiveSn;
import BlockBuilding.Progressive.SortedNeighborhood.Hybrid.ProgressiveSnHeap;
import BlockBuilding.Progressive.SortedNeighborhood.Local.LocalWeightedProgressiveSn;
import BlockBuilding.Progressive.SortedNeighborhood.Local.NaiveProgressiveSn;
import DataStructures.EntityProfile;

import java.util.Iterator;
import java.util.List;

/**
 * @author giovanni
 */
public class ExperimentsCEP_list {

    private static int MAX_WINDOW = 30;
    private static boolean REMOVE_REPETED = false;

    public String[] descriptions;

    private List<EntityProfile>[] profiles;

    public ExperimentsCEP_list(List<EntityProfile>[] profiles) {

        this.profiles = profiles;

        descriptions = new String[]{
                "progressiveCep",
                "progressiveCepCnp",
                "CepBlocking",
                "CepEntity",
        };
    }

    public Iterator naivePSN() {
        return new NaiveProgressiveSn(profiles, false);
    }
    public Iterator progressiveCep() {
        return new GlobalNaiveProgressiveSnIterator(profiles);
    }
    public Iterator progressiveCepCnp() {
        return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.ACF);
    }
    public Iterator CepBlocking() {
        return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.NCF);
    }
    public Iterator CepEntity() {
        return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.ID);
    }
}