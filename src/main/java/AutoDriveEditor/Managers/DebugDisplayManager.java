package AutoDriveEditor.Managers;

import java.awt.*;
import java.util.*;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class DebugDisplayManager {

    public static final int PRIORITY_NONE = 0;
    public static final int PRIORITY_FIRST = 1;
    public static final int PRIORITY_LAST = 2;

    private final LinkedHashMap<String, DebugGroup> debugGroups;
    private final Font debugFont;
    private final int padding = 5;

    public DebugDisplayManager() {
        // Initialize the debug display manager
        LOG.info("  Initializing DebugDisplayManager");
        this.debugGroups = new LinkedHashMap<>();
        this.debugFont = new Font("Dialog", Font.PLAIN, 10);
    }

    public DebugGroup addDebugGroup(String groupName) {
        DebugGroup group = new DebugGroup(groupName/*, PRIORITY_NONE*/);
        debugGroups.putIfAbsent(groupName, group);
        return group;
    }

    public void removeDebugGroup(DebugGroup group) {
        // use an Iterator to safely remove the group from the map, a For() loop will throw a ConcurrentModificationException
        Iterator<Map.Entry<String, DebugGroup>> iterator = debugGroups.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DebugGroup> entry = iterator.next();
            if (entry.getValue() == group) {
                iterator.remove();
                break;
            }
        }
    }

    public void drawDebug(Graphics2D graphics2D) {

        int startY = 5;
        graphics2D.setFont(debugFont);

        for (DebugGroup group : debugGroups.values()) {
            if (group.isVisible) {
                int groupHeight = (group.getDirectEntries().size() + 2) * (debugFont.getSize() + (padding / 2 )) + (padding * 2);
                //int groupWidth = 0;
                if (getStringLength(graphics2D, group.getGroupName()) + (padding * 2) > group.getGroupMaxWidth()) {
                    group.updateGroupMaxWidth(getStringLength(graphics2D, group.getGroupName()) + (padding * 2));
                }

                for (DebugDirectEntry entry : group.getDirectEntries()) {
                    String replaceText = entry.getDescription().replace("<center>", "").replace("<centre>", "");
                    String entryText = replaceText + ": " + entry.getInformation();
                    int length = getStringLength(graphics2D, entryText) + (padding * 3);
                    if (length > group.getGroupMaxWidth()) {
                        group.updateGroupMaxWidth(length);
                    }
                }

                int startX = getMapPanel().getWidth() - group.getGroupMaxWidth() - padding;

                // Draw the background rectangle with transparency
                graphics2D.setColor(new Color(32,32,32,192));
                graphics2D.fillRoundRect(startX, startY, group.getGroupMaxWidth(), groupHeight, 15, 15);

                // Draw the group name
                int entryY = startY + padding + debugFont.getSize();
                graphics2D.setColor(Color.WHITE);
                int groupNameLength = getStringLength(graphics2D, group.getGroupName());
                int groupNameHeight = getStringHeight(graphics2D, group.getGroupName()) / 2;
                graphics2D.drawLine(startX + padding, entryY - (groupNameHeight / 2), startX + ((group.getGroupMaxWidth() - groupNameLength) / 2) - padding, entryY - (groupNameHeight / 2));
                graphics2D.drawLine(startX + (group.getGroupMaxWidth() / 2) + (groupNameLength / 2) + padding, entryY - (groupNameHeight / 2), startX + (group.getGroupMaxWidth() - padding), entryY - (groupNameHeight / 2));
                graphics2D.drawString(group.getGroupName(), startX + ((group.getGroupMaxWidth() - groupNameLength) / 2), entryY);
                entryY += padding;

//                graphics2D.drawLine(startX + padding, entryY + (int)(padding * 1.5), startX + (group.getGroupMaxWidth() - padding), entryY + (int)(padding * 1.5));
//                entryY += debugFont.getSize();

                // Draw each entry
                String entryText;
                for (DebugDirectEntry entry : group.getDirectEntries()) {
                    entryY += debugFont.getSize() + (padding / 2 );
                    if (entry.getDescription().equals("<blank>") || entry.getDescription().equals("<empty>") || entry.getDescription().equals("<skip>")) {
                        continue;
                    } else if (entry.getDescription().equals("<line>")) {
                        graphics2D.drawLine(startX + padding, entryY - padding, startX + (group.getGroupMaxWidth() - padding), entryY - padding);
                        continue;
                    } else if (entry.getDescription().contains("<center>") || entry.getDescription().contains("<centre")) {
                        entryText = entry.getDescription().replace("<center>", "").replace("<centre>", "");
                        //if (entry.getInformation() != null) entryText += ": " + entry.getInformation();
                        int textLength = getStringLength(graphics2D, entryText);
                        int textHeight = getStringHeight(graphics2D, entryText) / 2;
                        if (Boolean.parseBoolean(entry.getInformation())) {
                            graphics2D.drawLine(startX + (padding * 5), entryY - (textHeight / 2), startX + ((group.getGroupMaxWidth() - textLength) / 2) - (padding * 2), entryY - (textHeight / 2));
                            graphics2D.drawLine(startX + (group.getGroupMaxWidth() / 2) + (textLength / 2) + (padding * 2), entryY - (textHeight / 2), startX + (group.getGroupMaxWidth() - (padding * 5)), entryY - (textHeight / 2));
                        }
                        graphics2D.drawString(entryText, startX + ((group.getGroupMaxWidth() - textLength) / 2), entryY);
                        entryY += padding / 2;
                        continue;
                    } else if (entry.getInformation() == null) {
                        entryText = entry.getDescription();
                    } else {
                        entryText = entry.getDescription() + ": " + entry.getInformation();
                    }
                    graphics2D.drawString(entryText, startX + padding, entryY);
                }

                // Move draw position ready for the next group
                startY += groupHeight + (padding * 2);
            }
        }
    }

    public DebugGroup getDebugGroup(String groupName) {
        return this.debugGroups.get(groupName);
    }

    private int getStringLength(Graphics2D g2, String text) {
        return g2.getFontMetrics().stringWidth(text);
    }

    private int getStringHeight(Graphics2D g2, String text) { return g2.getFontMetrics().getHeight(); }


    public static class DebugGroup/*  implements Comparable<DebugGroup>*/{
        private final String groupName;
        private int groupWidth;
        private final ArrayList<DebugDirectEntry> directEntries = new ArrayList<>();
        private boolean isVisible = false;
//        private int priority = 0;

        public DebugGroup(String groupName/*, int priority*/) {
            this.groupName = groupName;
//            this.priority = priority;
        }

        public void addText(String description, String information) {
            DebugDirectEntry entry = new DebugDirectEntry(description, information);
            directEntries.add(entry);
        }

        public void addEmptyLine() {
            DebugDirectEntry entry = new DebugDirectEntry("<blank>", null);
            directEntries.add(entry);
        }

        public void addCenteredText(String description, boolean hasCentreLine) {
            DebugDirectEntry entry = new DebugDirectEntry("<center>" + description, Boolean.toString(hasCentreLine));
            directEntries.add(entry);
        }

        public void addLine() {
            DebugDirectEntry entry = new DebugDirectEntry("<line>", null);
            directEntries.add(entry);
        }

        public void reset() { directEntries.clear();}

        private void updateGroupMaxWidth(int width) {
            groupWidth = width;
        }

        private int getGroupMaxWidth() {
            return groupWidth;
        }

        public String getGroupName() { return groupName; }

        public ArrayList<DebugDirectEntry> getDirectEntries() { return directEntries; }

        public void setVisible(boolean isVisible) {
            this.isVisible = isVisible;
        }

//        @Override
//        public int compareTo(DebugGroup other) { return Integer.compare(this.priority, other.priority); }
    }

    public static class DebugDirectEntry {
        private final String description;
        private final String information;

        public DebugDirectEntry(String description, String information) {
            this.description = description;
            this.information = information;
        }

        public String getDescription() {
            return description;
        }

        public String getInformation() { return information; }
    }
}
