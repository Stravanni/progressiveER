package BlockBuilding.Progressive.DataStructures;

/**
 *
 * @author G.A.P. II
 */

public class PositionInfo implements Comparable<PositionInfo> {
    
    private final int entityId;
    private final int positionId;
    
    public PositionInfo(int eId, int pId) {
        entityId = eId;
        positionId = pId;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getPositionId() {
        return positionId;
    }

    @Override
    public int compareTo(PositionInfo t) {
        // orders objects from lowest position to largest position
        return new Integer(positionId).compareTo(t.getPositionId());
    }
    
    @Override
    public String toString() {
        return "Entity : " + entityId + ", position : " + positionId;
    }
}