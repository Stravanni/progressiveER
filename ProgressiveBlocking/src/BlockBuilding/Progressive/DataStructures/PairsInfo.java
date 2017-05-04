package BlockBuilding.Progressive.DataStructures;

/**
 * @author giovanni
 */
public class PairsInfo implements Comparable<PairsInfo> {

    private final int position1;
    private final int position2;

    public PairsInfo(int pi1, int pi2) {
        position1 = pi1;
        position2 = pi2;
    }

    @Override
    public int compareTo(PairsInfo t) {
        // orders objects from lowest distance to largest distance
        int distance = (int) (this.getDistance() - t.getDistance());
        if (distance != 0) {
            return distance;
        }

        int minPosition = Math.min(position1, position2);
        int otherMinPosition = Math.min(t.getPosition1(), t.getPosition2());
        return minPosition - otherMinPosition;
    }

    public int getPosition1() {
        return position1;
    }

    public int getPosition2() {
        return position2;
    }

    public double getDistance() {
        return Math.abs(position1 - position2);
    }
}
