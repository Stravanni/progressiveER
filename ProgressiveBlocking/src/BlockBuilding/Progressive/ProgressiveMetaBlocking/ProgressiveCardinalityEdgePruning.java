package BlockBuilding.Progressive.ProgressiveMetaBlocking;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import MetaBlocking.WeightingScheme;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * ProgressiveCEP can run in 2 mode:
 * 1) single-step:
 * it's like std CEP, but returns the pair to compare progressivelly, ordered by their weights in the blocking graph;
 * its complexity is the same of CEP, since using a MinMax Heap (Guava implementation), polling the max is O(log(n)),
 * i.e. it does not sorts the array of pairs (~ it emit the pairs while it sort them)
 * - tested on DBpediaCC for scalability, it works good
 * // TODO: threshold should be set according to the available memory
 * // TODO: how to exploit found duplicates? (~look ahead)
 * <p>
 * 2) iterative: // commented in the "next()" method
 * it perform iteratively ProgressiveCEP as in 1, but each step consider only the pairs not yet considered;
 * here is the filter is implemented through HashSet, it doesn't scale well, of course;
 * I tried also a solution with bloom filters, but it's hard to scale too, for an exact solution;
 * I have something in mind for an approximate solution, but I'll try next
 * <p>
 * // TODO: ~ pair comparison propagation
 *
 * @author giovanni
 */
public class ProgressiveCardinalityEdgePruning implements Iterator<Comparison>, AbstractProgressiveMetaBlocking {
    protected List<AbstractBlock> blocks;
    protected WeightingScheme wScheme;
    private ProgressiveCardinalityEdgePruning_SingleStep cep;
    private boolean continua = true;
    private double max_pc;
    private double custom_threshold = 0;
    private boolean multiplePass = false;

    public ProgressiveCardinalityEdgePruning(WeightingScheme ws) {
        this.wScheme = ws;
    }

    //
    /*public ProgressiveCardinalityEdgePruning(WeightingScheme ws, double heap_size) {
        this.wScheme = ws;
        custom_threshold = heap_size;
    }*/

    public ProgressiveCardinalityEdgePruning(WeightingScheme ws, double heap_size) {
        this.wScheme = ws;
        this.custom_threshold = heap_size;
        this.multiplePass = true;
    }

    public void applyProcessing(List<AbstractBlock> blocks) {
        this.blocks = blocks;
        /*cep = new ProgressiveCardinalityEdgePruning_SingleStep(wScheme, new HashSet[1], true);*/
        cep = multiplePass ?
                new ProgressiveCardinalityEdgePruning_SingleStep(wScheme, new HashSet[1], true)
                : new ProgressiveCardinalityEdgePruning_SingleStep(wScheme);
        if (custom_threshold > 0) {
            cep.setThreshold(custom_threshold);
        }
        cep.applyProcessing(blocks);
    }
    @Override
    public String getName() {
        return "progressiveCep";
    }

    public void stopIteration() {
        this.continua = true;
    }

    @Override
    public boolean hasNext() {
        if (cep.hasNext()) {
            return true;
        } else if (!cep.final_iteration && continua) {
            HashSet[] bf_ = cep.getCandidatePairs();
            /*cep = new ProgressiveCardinalityEdgePruning_SingleStep(wScheme, bf_, false);
            cep.applyProcessing(blocks);*/
            if (cep.hasNext()) {
                return true;
            } else {
                return false;
            }
        }
        System.out.println("hesNext not verified");
        return false;
        //return continua && (!cep.final_iteration || cep.hasNext());
    }
    //@Override
    public Comparison next() {
        if (cep.hasNext()) {
            return (Comparison) cep.next();
        }
        /*else if (!cep.final_iteration && continua) {
            HashSet[] bf_ = cep.getCandidatePairs();
            cep = new ProgressiveCardinalityEdgePruning_SingleStep(wScheme, bf_, false);
            cep.applyProcessing(blocks);
            if (cep.hasNext()) {
                return cep.next();
            } else {
                System.out.println("error here in ProgressiveCEP");
                return null;
            }
        }*/
        System.out.println("error here in ProgressiveCEP 2");
        return null;
    }
}