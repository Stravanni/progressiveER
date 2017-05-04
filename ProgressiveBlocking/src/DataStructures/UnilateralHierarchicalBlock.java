package DataStructures;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author giovanni
 */

public class UnilateralHierarchicalBlock extends AbstractHierarchicalBlock {

    protected final Set<Integer> entitySet;

    public UnilateralHierarchicalBlock(int[] entities) {
        super();

        entitySet = Arrays.stream(entities).boxed().collect(Collectors.toCollection(HashSet::new));

        children.stream().forEach(child -> comparisons -= child.getNoOfComparisons());

        currentComparisonIterator = null;
    }

    @Override
    public List<AbstractBlock> processSubBlocks() {
        //UnilateralHirearchicalBlock[] childrenArray = new UnilateralHirearchicalBlock[((UnilateralHirearchicalBlock) block).getChildrens().size()];
        //List<AbstractHierarchicalBlocking> childrenList = new ArrayList<>(children);
        UnilateralHierarchicalBlock[] childrenArray = new UnilateralHierarchicalBlock[children.size()];
        childrenArray = children.toArray(childrenArray);

        Set<Integer> entitiesRemaining = new HashSet<>(entitySet);
        Set<Integer> entitiesChildren = new HashSet<>();

        // child-child
        if (this.getChildren().size() > 1) {
            for (int i = 0; i < children.size() - 1; i++) {
                entitiesChildren.addAll(childrenArray[i].getEntities());
                for (int j = i + 1; j < children.size(); j++) {
                    Set<Integer> childrenEntities1 = new HashSet<>(childrenArray[i].getEntities());
                    Set<Integer> childrenEntities2 = new HashSet<>(childrenArray[j].getEntities());

                    childrenEntities1.removeAll(childrenArray[j].getEntities());
                    childrenEntities2.removeAll(childrenArray[i].getEntities());

                    if (childrenEntities1.size() > 0 && childrenEntities2.size() > 0) {
                        int[] entities1 = childrenEntities1.stream().mapToInt(Number::intValue).toArray();
                        int[] entities2 = childrenEntities2.stream().mapToInt(Number::intValue).toArray();
                        BilateralBlock block = new BilateralBlock(entities1, entities2);
                        subBlocks.add(block);
                    }
                }
            }
            entitiesChildren.addAll(childrenArray[children.size() - 1].getEntities());
            entitiesRemaining.removeAll(entitiesChildren);
        }

        // parent-child
        if (entitiesRemaining.size() > 0 && entitiesChildren.size() > 0) {
            int[] entities1 = entitiesRemaining.stream().mapToInt(Number::intValue).toArray();
            int[] entities2 = entitiesChildren.stream().mapToInt(Number::intValue).toArray();
            BilateralBlock block = new BilateralBlock(entities1, entities2);
            subBlocks.add(block);
        }


        // parent-parent
        if (entitiesRemaining.size() > 1) {
            int[] entities = entitiesRemaining.stream().mapToInt(Number::intValue).toArray();
            UnilateralBlock block = new UnilateralBlock(entities);
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
        final UnilateralHierarchicalBlock other = (UnilateralHierarchicalBlock) obj;
        if (!entitySet.equals(other.entitySet)) {
            return false;
        }
        return true;
    }

    public Set getEntities() {
        return entitySet;
    }

    @Override
    public double getTotalBlockAssignments() {
        return entitySet.size();
    }

    @Override
    public double getAggregateCardinality() {
        return entitySet.size() * (entitySet.size() - 1);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.entitySet.toArray().hashCode();
        return hash;
    }

    @Override
    public void setUtilityMeasure() {
        utilityMeasure = 1.0 / entitySet.size();
    }
}