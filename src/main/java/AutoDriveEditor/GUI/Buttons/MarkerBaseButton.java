package AutoDriveEditor.GUI.Buttons;

public abstract class MarkerBaseButton extends BaseButton {

    public static class markerDestinationInfo {
        private final String name;
        private final String group;
        public markerDestinationInfo(String destName, String groupName){
            name = destName;
            group = groupName;
        }

        //
        // getter setters
        //

        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }

    }
}
