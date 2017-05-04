package BlockBuilding.Progressive.ProgressiveMetaBlocking;

import BlockBuilding.Progressive.Hierarchy.SuffixHierarchies;
import BlockBuilding.Utilities;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class PrefixHierarchies extends SuffixHierarchies implements Iterator<Comparison> {

    private final static int MINIMUM_PREFIX_LENGTH = 1;
    
    public PrefixHierarchies(List<EntityProfile>[] profiles) {
        super(profiles);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> prefixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            prefixes.addAll(Utilities.getPrefixes(MINIMUM_PREFIX_LENGTH, token));
        }
        return prefixes;
    }
}
