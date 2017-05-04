package DataStructures;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */


//To be tested!!!
public class IncrementalEntityIndex {
    
    private static final long serialVersionUID = 13445743447L;
    
    private boolean cleanCleanER;
    private int datasetLimit;
    private int noOfEntities;
    private List<Integer>[] entityBlocks;
    
    public IncrementalEntityIndex(List<AbstractBlock> blocks) {
        if (blocks.isEmpty()) {
            System.err.println("Entity index received an empty block collection as input!");
            return;
        }
        
        if (blocks.get(0) instanceof DecomposedBlock) {
            System.err.println("The entity index is incompatible with a set of decomposed blocks!");
            System.err.println("Its functionalities can be carried out with same efficiency through a linear search of all comparisons!");
            return;
        }
        
        enumerateBlocks(blocks);
        setNoOfEntities(blocks);
        initializeIndex();
    }
    
    public void addBlockToIndex(AbstractBlock block) {
        if (cleanCleanER) {
            BilateralBlock bBlock = (BilateralBlock) block;
            for (int entityId : bBlock.getIndex1Entities()) {
                entityBlocks[entityId].add(block.getBlockIndex());
            }
            for (int entityId : bBlock.getIndex2Entities()) {
                entityBlocks[entityId+datasetLimit].add(block.getBlockIndex());
            }
        } else {
            UnilateralBlock uBlock = (UnilateralBlock) block;
            for (int entityId : uBlock.getEntities()) {
                entityBlocks[entityId].add(block.getBlockIndex());
            }
        }
    }
    
    private void enumerateBlocks(List<AbstractBlock> blocks) {
        int blockIndex = 0;
        for (AbstractBlock block : blocks) {
            block.setBlockIndex(blockIndex++);
        }
    }
    
    private void initializeIndex() {
        entityBlocks = new List[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            entityBlocks[i] = new ArrayList<Integer>();
        }
    }
    
    public boolean isRepeated(Comparison comparison) {
        int entity2 = comparison.getEntityId2();
        if (cleanCleanER) {
            entity2 += datasetLimit;
        }
        
        for (int blockId1 : entityBlocks[comparison.getEntityId1()]) {
            for (int blockId2 : entityBlocks[entity2]) {
                if (blockId2 < blockId1) {
                    continue;
                }

                if (blockId1 < blockId2) {
                    break;
                }

                if (blockId1 == blockId1) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void setNoOfEntities(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            cleanCleanER = true;
            setNoOfBilateralEntities(blocks);
        } else {
            cleanCleanER = false;
            setNoOfUnilateralEntities(blocks);
        }
    }
    
    private void setNoOfBilateralEntities(List<AbstractBlock> blocks) {
        noOfEntities = Integer.MIN_VALUE;
        datasetLimit = Integer.MIN_VALUE;
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                if (noOfEntities < id1+1) {
                    noOfEntities = id1+1;
                }
            }
            
            for (int id2 : bilBlock.getIndex2Entities()) {
                if (datasetLimit < id2+1) {
                    datasetLimit = id2+1;
                }
            }
        }
        
        int temp = noOfEntities;
        noOfEntities += datasetLimit;
        datasetLimit = temp;
    }
    
    private void setNoOfUnilateralEntities(List<AbstractBlock> blocks) {
        noOfEntities = Integer.MIN_VALUE;
        datasetLimit = 0;
        for (AbstractBlock block : blocks) {
            UnilateralBlock bilBlock = (UnilateralBlock) block;
            for (int id : bilBlock.getEntities()) {
                if (noOfEntities < id+1) {
                    noOfEntities = id+1;
                }
            }
        }
    }
}