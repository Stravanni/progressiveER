package DataStructures;

import Utilities.ComparisonIterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author giovanni.simonini@unimore.it
 */

public abstract class AbstractHierarchicalBlock extends AbstractBlock implements Iterator<Comparison> {
    protected double comparisons;
    protected int blockIndex;
    protected double utilityMeasure;
    protected List<AbstractHierarchicalBlock> children;
    protected ComparisonIterator currentComparisonIterator;
    protected LinkedList<AbstractBlock> subBlocks;
    protected AbstractBlock currentBlock;

    protected String key;

    public AbstractHierarchicalBlock() {
        blockIndex = -1;
        utilityMeasure = -1;
        children = new LinkedList<>();
        subBlocks = new LinkedList<>();
        currentComparisonIterator = null;
    }

    @Override
    public boolean hasNext() {
        if (currentComparisonIterator == null) {
            if (subBlocks.size() < 1) {
                return false;
            } else {
                currentBlock = subBlocks.removeFirst();
                currentComparisonIterator = currentBlock.getComparisonIterator();
            }
        }
        if (currentComparisonIterator.hasNext()) {
            return true;
        } else if (subBlocks.isEmpty()) {
            return false;
        } else {
            currentBlock = subBlocks.removeFirst();
            currentComparisonIterator = currentBlock.getComparisonIterator();
            return currentComparisonIterator.hasNext();
        }
    }

    @Override
    public Comparison next() {
        if (currentComparisonIterator.hasNext()) {
            return currentComparisonIterator.next();
        } else {
            System.out.println("error in unilaterlaHierarchicalBlock");
            return null;
        }
    }

    public List<AbstractHierarchicalBlock> getChildren() {
        return children;
    }

    public void addChild(AbstractHierarchicalBlock child) {
        children.add(child);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public List<AbstractBlock> processSubBlocks() {
        return null;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public double getNoOfComparisons() {
        return comparisons;
    }

    public double getUtilityMeasure() {
        return utilityMeasure;
    }


    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public abstract double getTotalBlockAssignments();
    public abstract double getAggregateCardinality();
    public abstract void setUtilityMeasure();
}