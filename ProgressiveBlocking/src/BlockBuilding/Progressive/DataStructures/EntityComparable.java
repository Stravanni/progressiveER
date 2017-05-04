package BlockBuilding.Progressive.DataStructures;

import DataStructures.Attribute;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author giovanni
 */

public class EntityComparable implements Comparable<EntityComparable> {

    private final int entitYId;
    private final double weight;

    public EntityComparable(int id, double w) {
        entitYId = id;
        weight = w;
    }

    public int getId() {
        return entitYId;
    }

    public double getWeight() {
        return weight;
    }
    @Override
    public int compareTo(EntityComparable o) {
        return Double.compare(weight, o.weight);
    }
}