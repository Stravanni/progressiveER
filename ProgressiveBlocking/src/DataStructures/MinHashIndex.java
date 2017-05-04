package DataStructures;

import info.debatty.java.lsh.MinHash;

import java.util.*;

/**
 * Created by giovanni
 */
public class MinHashIndex {

    protected final List<AbstractBlock> blocks;

    protected final int signature_size; // the size of the minhash
    protected final List<EntityProfile>[] profiles; // entity profiles
    protected final int noProfiles;
    protected int sourceId = 0;

    protected MinHash minhash;

    protected int[][] signatures;

    protected final boolean cleanCleanER;

    protected HashMap<String, Integer> shinglesMapping;
    protected int all_shingle_counter;

    protected final int maxNum_per_block;

    protected boolean exteranl_shinglesMap = false;

    public MinHashIndex(List<EntityProfile>[] profiles, int signature_size) {
        blocks = new ArrayList<>();

        this.signature_size = signature_size;
        this.profiles = profiles;
        shinglesMapping = new HashMap<>();
        all_shingle_counter = 0;
        maxNum_per_block = Integer.MAX_VALUE;

        cleanCleanER = (profiles.length == 2) ? true : false;

        noProfiles = cleanCleanER ? (profiles[0].size() + profiles[1].size()) : profiles[0].size();
        signatures = new int[noProfiles][signature_size];
    }

    public MinHashIndex(List<EntityProfile>[] profiles, int signature_size, HashMap<String, Integer> shinglesMapping) {
        blocks = new ArrayList<>();

        this.signature_size = signature_size;
        this.profiles = profiles;
        this.shinglesMapping = shinglesMapping;
        all_shingle_counter = this.shinglesMapping.size();
        maxNum_per_block = Integer.MAX_VALUE;

        cleanCleanER = (profiles.length == 2) ? true : false;

        noProfiles = cleanCleanER ? (profiles[0].size() + profiles[1].size()) : profiles[0].size();
        signatures = new int[noProfiles][signature_size];

        exteranl_shinglesMap = true;
    }

    public void buildIndex() {
        if (!exteranl_shinglesMap) {
            buildShingles();
            if (cleanCleanER) {
                sourceId = 1;
                buildShingles();
            }
        }

        System.out.println("map size: " + shinglesMapping.size());

        minhash = new MinHash(signature_size, all_shingle_counter);

        int id = 0;
        sourceId = 0;
        for (EntityProfile entity : profiles[sourceId]) {
            signatures[id++] = minhash.signature(buildSignature(entity));
        }
        if (cleanCleanER) {
            sourceId = 1;
            for (EntityProfile entity : profiles[sourceId]) {
                signatures[id++] = minhash.signature(buildSignature(entity));
            }
        }

        if (id != noProfiles) {
            System.out.println(id);
            System.out.println(noProfiles);
        }
    }

    protected void buildShingles() {
        for (EntityProfile entity : profiles[sourceId]) {
            for (Attribute attribute : entity.getAttributes()) {
                String values = attribute.getValue();

                String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(values);

                int noOfTokens = tokens.length;
                for (int j = 0; j < noOfTokens; j++) {
                    String feature = tokens[j].trim();

                    if (!(shinglesMapping.containsKey(feature))) {
                        shinglesMapping.put(feature, all_shingle_counter++);
                    }
                }
            }
        }
    }

    public HashMap<String, Integer> getAllTokens() {
        return shinglesMapping;
    }

    public int[][] getSignatures() {
        return signatures;
    }

    protected Set<Integer> buildSignature(EntityProfile entity) {
        HashSet<Integer> shingleSet = new HashSet<>();
        for (Attribute attribute : entity.getAttributes()) {
            String values = attribute.getValue();
            String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(values);
            int noOfTokens = tokens.length;
            for (int j = 0; j < noOfTokens; j++) {
                String feature = tokens[j].trim();
                if (!shinglesMapping.containsKey(feature)) {
                    continue;
                    //System.out.println("shingle not contained");
                } else {
                    shingleSet.add(shinglesMapping.get(feature));
                }
            }
        }
        return shingleSet;
    }

    public static int[] convertCollectionToArray(Collection<Integer> ids) {
        int index = 0;
        int[] array = new int[ids.size()];
        for (Integer id : ids) {
            array[index++] = id;
        }
        return array;
    }

    public double getApproximateSimilarity(int id1, int id2) {
        return minhash.similarity(signatures[id1], signatures[id2]);
    }
}