package AutoDriveEditor.RoadNetwork;

//
// Used for storing all the marker groups and ID's in Route config files
//

@SuppressWarnings("rawtypes")
public class MarkerGroup implements Comparable{

    public int groupIndex;
    public String groupName;

    public MarkerGroup(int index, String name) {
        this.groupIndex = index;
        this.groupName = name;
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compareTo(Object o) {
        if (o instanceof MarkerGroup) {
            MarkerGroup other = (MarkerGroup) o;
            if (other.groupName.equals(groupName) && other.groupIndex == groupIndex) {
                return 0;
            }
        }
        return 1;
    }
}
