package Experiments;

import BlockBuilding.AbstractBlockingMethod;
import BlockBuilding.AbstractTokenBlocking;
import BlockBuilding.MemoryBased.TokenBlocking;
import BlockBuilding.MinHashBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.ComparisonPropagation;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.MinHashIndex;
import MetaBlocking.EnhancedMetaBlocking.FastImplementations.ReciprocalCardinalityNodePruning;
import MetaBlocking.FastImplementations.WeightedNodePruning;
import MetaBlocking.WeightingScheme;
import Utilities.RepresentationModel;
import Utilities.BlockStatistics;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author giovanni
 */
public class MinHashBlocking_exp {

    private static int DATASET = 4;
    private static boolean CLEAN = true;
    private static String BASEPATH = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    public static void main(String[] args) {

        Instant start = Instant.now();

        List<EntityProfile>[] profiles;
        AbstractDuplicatePropagation adp;

        double SMOOTHING_FACTOR = 1.005;
        double FILTERING_RATIO = 0.8;

        if (args.length > 0) {
            BASEPATH = args[0] + "/";
            //profiles = Utilities.getEntities(BASEPATH + "profiles/", DATASET, CLEAN);
            profiles = Utilities.getEntities(BASEPATH, DATASET, CLEAN);
            adp = Utilities.getGroundTruth(BASEPATH, DATASET, CLEAN);
        } else {
            profiles = Utilities.getEntities(DATASET, CLEAN);
            adp = Utilities.getGroundTruth(DATASET, CLEAN);
        }

        AbstractBlockingMethod mhb = null;
        List<AbstractBlock> blocks = new ArrayList<>();

        if (profiles.length > 1) {
            mhb = new TokenBlocking(profiles);
        } else {
            mhb = new TokenBlocking(new List[]{profiles[0]});
        }

        // lowest level hint
        //mhb = new MinHashBlocking(profiles, 256, 8);
        //blocks.addAll(mhb.buildBlocks());

        //mhb = new MinHashBlocking(profiles, 256, 4);
        //blocks.addAll(mhb.buildBlocks());

        //mhb = new MinHashBlocking(profiles, 256, 2);
        //blocks.addAll(mhb.buildBlocks());

        // highest level hint
        //mhb = new MinHashBlocking(profiles, 256, 1);
        //mhb = new MinHashBlocking(profiles, 256, 1, ((MinHashBlocking) mhb).getAllTokens(), ((MinHashBlocking) mhb).getSignatures());


        MinHashIndex mhi = new MinHashIndex(profiles, 80);
        mhi.buildIndex();
        //mhb = new MinHashBlocking(profiles, 80, 1);
        mhb = new MinHashBlocking(profiles, 1, mhi);
        blocks.addAll(mhb.buildBlocks());


        Instant start_purging = Instant.now();

        System.out.println("blocking time: " + Duration.between(start, start_purging));

        ComparisonPropagation cp = new ComparisonPropagation();
        ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTHING_FACTOR);
        BlockFiltering bf = new BlockFiltering(FILTERING_RATIO);

        //cbbp.applyProcessing(blocks);

        // block filtering
        //bf.applyProcessing(blocks);

        /** //TODO Comparison propagation
         * If used here is very slow. We may try to use it *during* progressive blocking and not *before*
         */
        //cp.applyProcessing(blocks);

        //List<AbstractBlock> blocks = ac.buildBlocks();

        BlockStatistics bStats1 = new BlockStatistics(blocks, adp);
        double[] values = bStats1.applyProcessing();

        Instant end = Instant.now();

        System.out.println("Total time: " + Duration.between(start, end).toString());
    }
}