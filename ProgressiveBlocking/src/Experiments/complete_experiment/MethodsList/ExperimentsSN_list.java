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
import BlockBuilding.Progressive.SortedNeighborhood.Local.LocalGlobalProgressiveSn;
import BlockBuilding.Progressive.SortedNeighborhood.Local.LocalWeightedProgressiveSn;
import BlockBuilding.Progressive.SortedNeighborhood.Local.NaiveProgressiveSn;
import BlockBuilding.prova.NaiveProgressiveEntropySn;
import BlockBuilding.prova.ProgressiveEntropySnBuilder;
import DataStructures.EntityProfile;
import DataStructures.SchemaBasedProfiles.ProfileType;
import ProgressiveSortedNeighborhood.LocalAcfWeightedProgressiveSnIterator;
import ProgressiveSortedNeighborhood.LocalNcfWeightedProgressiveSnIterator;

import java.util.*;

/**
 * @author giovanni
 */
public class ExperimentsSN_list {

    private static boolean REMOVE_REPETED = false;
    private int MAX_WINDOW = 1;
    private int MAX_LOCAL_WINDOW = 10;

    private int MAX_CPE = 1000; /* Comparison Per Entity, used in GlobalPSN */

    public String[] descriptions;

    private List<EntityProfile>[] profiles;

    public ExperimentsSN_list(List<EntityProfile>[] profiles, int max_win, int max_local_win, int max_cpe) {
        this(profiles, max_win, max_cpe);
        this.MAX_LOCAL_WINDOW = max_local_win;
    }

    public ExperimentsSN_list(List<EntityProfile>[] profiles, int max_win, int max_cep) {
        this(profiles, max_win);
        this.MAX_CPE = max_cep;
    }

    public ExperimentsSN_list(List<EntityProfile>[] profiles, int max_win) {
        this.MAX_WINDOW = max_win;

        this.profiles = profiles;

        descriptions = new String[]{
                "naivePSN",
                "naivePSN_schema",
                "naivePSN_norep",
                "naivePSN_norep_schema",
                "globalACF",
                "globalNCF",
                "globalID",
                /*"heap_ACF",*/
                /*"heap_minhash",*/
                /*"globalMINHASH",*/
                "heap_minhash",
                "heap_minhash_norep",
                /*"heap_no_weight",*/
                "localACF",//slow
                "localACF_norep",
                "localNCF",
                /*"localMINHASH",//slow
                "local_no_rep"*/
        };
    }

    public Iterator naivePSN(int bk, ProfileType pt) {
        return new NaiveProgressiveSn(profiles, MAX_WINDOW);
    }

    public Iterator naivePSN_schema(int bk, ProfileType pt) {
        System.out.println("Schema based");
        return new NaiveProgressiveSn(1, pt, profiles, this.MAX_WINDOW);
    }
    /*public Iterator naivePSN() {

        return new NaiveProgressiveSn(profiles, false);
    }*/
        /*return new NaiveProgressiveSn_schema(profiles, false,
                (EntityProfile p) -> getKey(p)
        );*/
        /*
    }

    *//*private List<String> getKey(EntityProfile p) {
        Map<String, Integer> key_attributes = new HashMap<>();
        key_attributes.put("Attr1", 0);
        key_attributes.put("Attr2", 1);
        key_attributes.put("name", 0);
        key_attributes.put("title", 1);
        List<String> keys = new ArrayList<>();
        String key = "";
        DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
        for (Attribute a : p.getAttributes()) {
            if (key_attributes.get(a.getName()) != null) {
                String partialKey = doubleMetaphone.encode(a.getValue().replaceAll("[^\\w]", ""));
                if (partialKey.length() > 3) {
                    partialKey = partialKey.substring(0, 3);
                }
                if (key_attributes.get(a.getName()) == 0) {
                    key = key + partialKey;
                } else {
                    key = partialKey + key;
                }
            }
        }
        keys.add(key);
        return keys;
    }*/

    public Iterator naivePSN_norep(int bk, ProfileType pt) {
        return new GlobalNaiveProgressiveSnIterator(profiles, this.MAX_WINDOW);
    }


    public Iterator naivePSN_norep_schema(int bk, ProfileType pt) {
        return new GlobalNaiveProgressiveSnIterator(1, pt, profiles, this.MAX_WINDOW);
    }

    public Iterator globalACF(int bk, ProfileType pt) {
        if (bk < 0) {
            return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.ACF, this.MAX_WINDOW, this.MAX_CPE);
        } else {
            return new GlobalWeightedProgressiveSn(bk, pt, profiles, WeightingSchemeSn.ACF, this.MAX_WINDOW);
        }
    }
    public Iterator globalNCF(int bk, ProfileType pt) {
        if (bk < 0) {
            return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.NCF, this.MAX_WINDOW, this.MAX_CPE);
        } else {
            return new GlobalWeightedProgressiveSn(bk, pt, profiles, WeightingSchemeSn.NCF, this.MAX_WINDOW);
        }
    }
    public Iterator globalID(int bk, ProfileType pt) {
        if (bk < 0) {
            return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.ID, this.MAX_WINDOW, this.MAX_CPE);
        } else {
            return new GlobalWeightedProgressiveSn(bk, pt, profiles, WeightingSchemeSn.ID, this.MAX_WINDOW);
        }
    }
    public Iterator globalMINHASH(int bk, ProfileType pt) {
        if (bk < 0) {
            return new GlobalWeightedProgressiveSn(profiles, WeightingSchemeSn.MINHASH, this.MAX_WINDOW, this.MAX_CPE);
        } else {
            return new GlobalWeightedProgressiveSn(bk, pt, profiles, WeightingSchemeSn.MINHASH, this.MAX_WINDOW);
        }
    }

    public Iterator localGlobalACF(int bk, ProfileType pt) {
        return new LocalGlobalProgressiveSn(profiles, WeightingSchemeSn.ACF, this.MAX_WINDOW, this.MAX_LOCAL_WINDOW, this.MAX_CPE);
    }

    public Iterator localGlobalNCF(int bk, ProfileType pt) {
        return new LocalGlobalProgressiveSn(profiles, WeightingSchemeSn.NCF, this.MAX_WINDOW, this.MAX_LOCAL_WINDOW, this.MAX_CPE);
    }

    public Iterator heap_no_weight(int bk, ProfileType pt) {
        return new ProgressiveSnHeap(profiles, REMOVE_REPETED, this.MAX_WINDOW);
    }

    public Iterator heap_minhash(int bk, ProfileType pt) {
        return new ProgressiveSnHeap(profiles, WeightingSchemeSn.MINHASH, REMOVE_REPETED, this.MAX_WINDOW);
    }

    public Iterator heapACF(int bk, ProfileType pt) {
        System.out.println("heapACF");
        return new ProgressiveSnHeap(profiles, WeightingSchemeSn.ACF, REMOVE_REPETED, this.MAX_WINDOW);
    }

    public Iterator heap_minhash_norep(int bk, ProfileType pt) {
        return new ProgressiveSnHeap(profiles, WeightingSchemeSn.MINHASH, true, this.MAX_WINDOW);
    }

    public Iterator heap_ACF(int bk, ProfileType pt) {
        return new ProgressiveSnHeap(profiles, WeightingSchemeSn.ACF, false, this.MAX_WINDOW);
    }

    public Iterator localACF(int bk, ProfileType pt) {
        System.out.println("local");
        return new LocalWeightedProgressiveSn(profiles, WeightingSchemeSnLocal.ACF, false, this.MAX_WINDOW);
        /*return new LocalAcfWeightedProgressiveSnIterator(profiles, WeightingSchemeSnLocal.ACF, false, this.MAX_WINDOW);*/
    }

    public Iterator localACF_norep(int bk, ProfileType pt) {
        System.out.println("local");
        return new LocalWeightedProgressiveSn(profiles, WeightingSchemeSnLocal.ACF, true, this.MAX_WINDOW);
    }

    public Iterator localNCF(int bk, ProfileType pt) {
        System.out.println("local");
        return new LocalWeightedProgressiveSn(profiles, WeightingSchemeSnLocal.NCF, false, this.MAX_WINDOW);
    }
    public Iterator localMINHASH(int bk, ProfileType pt) {
        System.out.println("local");
        return new LocalWeightedProgressiveSn(profiles, WeightingSchemeSnLocal.MINHASH, false, this.MAX_WINDOW);
    }
    public Iterator local_no_rep(int bk, ProfileType pt) {
        System.out.println("local");
        return new LocalWeightedProgressiveSn(profiles, null, false, this.MAX_WINDOW);
    }
}