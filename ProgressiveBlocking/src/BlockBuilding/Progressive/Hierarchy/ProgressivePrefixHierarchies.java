package BlockBuilding.Progressive.Hierarchy;

import BlockBuilding.Utilities;
import DataStructures.*;

import java.util.*;

/**
 * @author giovanni.simonini@unimore.it
 */
public class ProgressivePrefixHierarchies extends ProgressiveSuffixHierarchies {

    private static boolean STRICT_MINIMUM;

    public ProgressivePrefixHierarchies(List<EntityProfile>[] profiles, boolean removeRepeated) {
        super(profiles, removeRepeated);
    }

    public ProgressivePrefixHierarchies(List<EntityProfile>[] profiles, int min_suffix_len) {
        super(profiles, min_suffix_len);
    }

    public ProgressivePrefixHierarchies(List<EntityProfile>[] profiles, int min_suffix_len, boolean strict_minimum, boolean removeRepeated) {
        super(profiles, min_suffix_len, strict_minimum, removeRepeated);
        this.STRICT_MINIMUM = strict_minimum;
    }

    public ProgressivePrefixHierarchies(List<EntityProfile>[] profiles) {
        super(profiles);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(Utilities.getPrefixes(minimumSuffixLength, token, STRICT_MINIMUM));
        }
        return suffixes;
    }

    @Override
    protected void buildTree() {
        for (int i = 0; i < sortedKeys.length; i++) {
            String key = sortedKeys[i];
            if (key.length() > minimumSuffixLength + 1) {
                String parentKey = key.substring(0, key.length() - 1);
                BlocksMapping.get(parentKey).addChild(BlocksMapping.get(key)); // no need to check for the key
            }
        }
    }
}