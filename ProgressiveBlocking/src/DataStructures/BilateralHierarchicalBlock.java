package DataStructures;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @giovanni.simonini@unimore.it
 */
public class BilateralHierarchicalBlock extends AbstractHierarchicalBlock {
    protected final Set<Integer> entitySet1;
    protected final Set<Integer> entitySet2;

    public BilateralHierarchicalBlock(int[] entities1, int[] entities2) {
        super();

        entitySet1 = Arrays.stream(entities1).boxed().collect(Collectors.toCollection(HashSet::new));
        entitySet2 = Arrays.stream(entities2).boxed().collect(Collectors.toCollection(HashSet::new));

        children.stream().forEach(child -> comparisons -= child.getNoOfComparisons());

        currentComparisonIterator = null;
    }

    @Override
    public List<AbstractBlock> processSubBlocks() {
        BilateralHierarchicalBlock[] childrenArray = new BilateralHierarchicalBlock[children.size()];
        childrenArray = children.toArray(childrenArray);

        Set<Integer>[] entitiesRemaining = new HashSet[2];
        Set<Integer>[] entitiesChildren = new HashSet[2];

        entitiesRemaining[0] = new HashSet<>(entitySet1);
        entitiesRemaining[1] = new HashSet<>(entitySet2);
        entitiesChildren[0] = new HashSet<>();
        entitiesChildren[1] = new HashSet<>();

        // child-child
        if (this.getChildren().size() > 1) {
            for (int i = 0; i < children.size() - 1; i++) {
                entitiesChildren[0].addAll(childrenArray[i].getEntitySet1());
                entitiesChildren[1].addAll(childrenArray[i].getEntitySet2());

                for (int j = i + 1; j < children.size(); j++) {
                    Set<Integer> childrenEntities11 = new HashSet<>(childrenArray[i].getEntitySet1());
                    Set<Integer> childrenEntities12 = new HashSet<>(childrenArray[i].getEntitySet2());
                    Set<Integer> childrenEntities21 = new HashSet<>(childrenArray[j].getEntitySet1());
                    Set<Integer> childrenEntities22 = new HashSet<>(childrenArray[j].getEntitySet2());

                    childrenEntities11.removeAll(childrenArray[j].getEntitySet1());
                    childrenEntities12.removeAll(childrenArray[j].getEntitySet2());
                    childrenEntities21.removeAll(childrenArray[i].getEntitySet1());
                    childrenEntities22.removeAll(childrenArray[i].getEntitySet2());

                    if (childrenEntities11.size() > 0 && childrenEntities22.size() > 0) {
                        int[] entities1 = childrenEntities11.stream().mapToInt(Number::intValue).toArray();
                        int[] entities2 = childrenEntities22.stream().mapToInt(Number::intValue).toArray();
                        BilateralBlock block = new BilateralBlock(entities1, entities2);
                        subBlocks.add(block);
                    }

                    if (childrenEntities21.size() > 0 && childrenEntities12.size() > 0) {
                        int[] entities1 = childrenEntities21.stream().mapToInt(Number::intValue).toArray();
                        int[] entities2 = childrenEntities12.stream().mapToInt(Number::intValue).toArray();
                        BilateralBlock block = new BilateralBlock(entities1, entities2);
                        subBlocks.add(block);
                    }
                }
            }
            entitiesChildren[0].addAll(childrenArray[children.size() - 1].getEntitySet1());
            entitiesChildren[1].addAll(childrenArray[children.size() - 1].getEntitySet2());
            entitiesRemaining[0].removeAll(entitiesChildren[0]);
            entitiesRemaining[1].removeAll(entitiesChildren[1]);
        }

        // parent-child
        if (entitiesRemaining[0].size() > 0 && entitiesChildren[1].size() > 0) {
            int[] entities1 = entitiesRemaining[0].stream().mapToInt(Number::intValue).toArray();
            int[] entities2 = entitiesChildren[1].stream().mapToInt(Number::intValue).toArray();
            BilateralBlock block = new BilateralBlock(entities1, entities2);
            subBlocks.add(block);
        }
        if (entitiesChildren[0].size() > 0 && entitiesRemaining[1].size() > 0) {
            int[] entities1 = entitiesChildren[0].stream().mapToInt(Number::intValue).toArray();
            int[] entities2 = entitiesRemaining[1].stream().mapToInt(Number::intValue).toArray();
            BilateralBlock block = new BilateralBlock(entities1, entities2);
            subBlocks.add(block);
        }


        // parent-parent
        if (entitiesRemaining[0].size() > 0 && entitiesRemaining[1].size() > 0) {
            int[] entities1 = entitiesRemaining[0].stream().mapToInt(Number::intValue).toArray();
            int[] entities2 = entitiesRemaining[1].stream().mapToInt(Number::intValue).toArray();
            BilateralBlock block = new BilateralBlock(entities1, entities2);
            //if (entities.length > 1) {
            subBlocks.add(block);
            //}
        }

        return subBlocks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BilateralHierarchicalBlock other = (BilateralHierarchicalBlock) obj;
        if (!entitySet1.equals(other.entitySet1)) {
            return false;
        }
        if (!entitySet1.equals(other.entitySet1)) {
            return false;
        }
        return true;
    }

    public Set<Integer> getEntitySet1() {
        return entitySet1;
    }
    public Set<Integer> getEntitySet2() {
        return entitySet2;
    }

    @Override
    public double getTotalBlockAssignments() {
        return entitySet1.size() + entitySet2.size();
    }

    @Override
    public double getAggregateCardinality() {
        return entitySet1.size() * entitySet2.size();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + entitySet1.toArray().hashCode();
        hash = 53 * hash + entitySet2.toArray().hashCode();
        return hash;
    }

    @Override
    public void setUtilityMeasure() {
        utilityMeasure = 1.0 / Math.max(entitySet1.size(), entitySet2.size());
    }
}