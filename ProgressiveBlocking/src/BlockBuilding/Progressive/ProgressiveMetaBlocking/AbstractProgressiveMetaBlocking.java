package BlockBuilding.Progressive.ProgressiveMetaBlocking;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;

import java.util.Iterator;
import java.util.List;

/**
 * @author giovanni
 */
public interface AbstractProgressiveMetaBlocking extends Iterator<Comparison> {
    void applyProcessing(List<AbstractBlock> blocks);
    String getName();
    //boolean hasNext();
    //Comparison next();
}
