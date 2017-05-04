package BlockBuilding;

import DataStructures.*;

import java.util.*;

/**
 * Created by giovanni
 */
public class MinHashBlocking extends AbstractBlockingMethod {

    private final List<AbstractBlock> blocks;

    private final List<EntityProfile>[] profiles; // entity profiles
    private final int noProfiles;
    private final boolean cleanCleanER;

    private final int signature_size; // the size of the minhash
    private final int row_number; // for the banding technique: the number of row per band
    private MinHashIndex minHashIndex;
    protected HashMap<String, Integer> shinglesMapping;
    protected final int maxNum_per_block;
    protected boolean external_signatures = false;

    public MinHashBlocking(List<EntityProfile>[] profiles, int signature_size, int row_number) {
        super("MinHashBlocking");

        this.profiles = profiles;
        cleanCleanER = (profiles.length == 2) ? true : false;
        noProfiles = cleanCleanER ? (profiles[0].size() + profiles[1].size()) : profiles[0].size();

        blocks = new ArrayList<>();

        this.signature_size = signature_size;
        this.row_number = row_number;

        minHashIndex = new MinHashIndex(profiles, signature_size);

        maxNum_per_block = Integer.MAX_VALUE;
    }

    public MinHashBlocking(List<EntityProfile>[] profiles, int signature_size, int row_number, HashMap<String, Integer> shinglesMapping) {
        super("MinHashBlocking");

        this.profiles = profiles;
        cleanCleanER = (profiles.length == 2) ? true : false;
        noProfiles = cleanCleanER ? (profiles[0].size() + profiles[1].size()) : profiles[0].size();

        blocks = new ArrayList<>();

        this.signature_size = signature_size;
        this.row_number = row_number;

        this.shinglesMapping = shinglesMapping;
        minHashIndex = new MinHashIndex(profiles, signature_size, shinglesMapping);


        maxNum_per_block = Integer.MAX_VALUE;
    }

    public MinHashBlocking(List<EntityProfile>[] profiles, int row_number, MinHashIndex mhi) {
        super("MinHashBlocking");

        blocks = new ArrayList<>();

        this.profiles = profiles;
        cleanCleanER = (profiles.length == 2) ? true : false;
        noProfiles = cleanCleanER ? (profiles[0].size() + profiles[1].size()) : profiles[0].size();

        this.minHashIndex = mhi;

        this.signature_size = mhi.getSignatures()[0].length;
        this.row_number = row_number;
        maxNum_per_block = Integer.MAX_VALUE;

        external_signatures = true;
    }

    @Override
    public List<AbstractBlock> buildBlocks() {

        if (!external_signatures) {
            minHashIndex.buildIndex();
        }

        /**
         * Here I'm using a-sort-of banding technique.
         *
         * That means:
         * - I'm not using an approximate solution with the buckets, but HashMap that yields a more precise blocking.
         * - This could be more memory consuming, so we can use the other technique (not implemented yet) if necessary.
         *
         */
        for (int i = 0; i < signature_size; i += row_number) { // band
            final HashMap<String, List<Integer>> band = new HashMap<>();
            final List<Integer> entityIds1 = new ArrayList<>();
            //if (cleanCleanER) {
            final List<Integer> entityIds2 = new ArrayList<>();
            //}
            String sig = "";
            for (int profileId = 0; profileId < noProfiles; profileId++) {
                if (i + row_number < signature_size) {
                    final StringBuilder bandSignature = new StringBuilder();
                    for (int j = i; j < i + row_number; j++) {
                        //bandSignature.append(Integer.toString(signatures[profileId][j]) + "-");
                        //bandSignature.append(Integer.toString(signatures[profileId][j]) + "-");
                        bandSignature.append(minHashIndex.getSignatures()[profileId][j]);
                    }
                    sig = bandSignature.toString();
                    final List<Integer> ids = band.getOrDefault(sig, new ArrayList<>());
                    ids.add(profileId);
                    band.put(sig, ids);
                }
            }
            if (band.getOrDefault(sig, new ArrayList<>()).size() > maxNum_per_block) {
                //System.out.println("clean this block");
                band.put(sig, new ArrayList<>());
            }

            Iterator it = band.entrySet().iterator();
            while (it.hasNext()) {
                entityIds1.clear();
                entityIds2.clear();
                Map.Entry pair = (Map.Entry) it.next();
                final List<Integer> ids = (List<Integer>) pair.getValue();

                for (int profileId : ids) {
                    if (cleanCleanER) {
                        if (profileId < profiles[0].size()) {
                            entityIds1.add(profileId);
                        } else {
                            entityIds2.add(profileId - profiles[0].size());
                        }
                    } else {
                        entityIds1.add(profileId);
                    }
                }
                //it.remove(); // avoids a ConcurrentModificationException
                if (cleanCleanER) {
                    if (entityIds1.size() > 0 && entityIds2.size() > 0) {
                        //if (entityIds1.size() > 0 && entityIds2.size() > 0 && entityIds1.size() * entityIds2.size() < 100000) {
                        int[] idsArray1 = convertCollectionToArray(entityIds1);
                        int[] idsArray2 = convertCollectionToArray(entityIds2);
                        blocks.add(new BilateralBlock(idsArray1, idsArray2));
                    }
                } else {
                    if (entityIds1.size() > 1) {
                        int[] idsArray1 = convertCollectionToArray(entityIds1);
                        blocks.add(new UnilateralBlock(idsArray1));
                    }
                }
            }
        }
        return blocks;
    }

    public static int[] convertCollectionToArray(Collection<Integer> ids) {
        int index = 0;
        int[] array = new int[ids.size()];
        for (Integer id : ids) {
            array[index++] = id;
        }

        return array;
    }
}