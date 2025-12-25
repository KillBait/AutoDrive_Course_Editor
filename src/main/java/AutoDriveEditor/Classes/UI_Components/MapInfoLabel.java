package AutoDriveEditor.Classes.UI_Components;

import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.Classes.UI_Components.MapInfoLabel.LabelStatus.NONE;
import static AutoDriveEditor.Classes.Util_Classes.ColourUtils.ColorToHexWithAlpha;

@SuppressWarnings("unused")
public class MapInfoLabel extends JLabel {

    LabelStatus labelStatus = NONE;
    String labelText = "";

    public MapInfoLabel() {
        super();
    }

    public MapInfoLabel(String labelText) {
        super(labelText);
        this.labelText = labelText;
    }

    @SuppressWarnings("MagicConstant")
    public MapInfoLabel(String labelText, int alignment) {
        super(labelText, alignment);
        this.labelText = labelText;
    }

    public void setStatus(LabelStatus status) {
        if (status != null) {
            this.labelStatus = status;
            String statusText = status.getWarnText();
            Color textColour = status.getColor();
            String labelText = this.labelText + " : <b><font color=" + ColorToHexWithAlpha(textColour,true) + ">" + statusText + "</b>";
            this.setText("<html>" + labelText + "</html>");
        }
    }

    public String getTooltipText() {
        if (this.labelStatus != null) {
            LabelStatus status = this.labelStatus;
            Color textColour = status.getColor();
            return this.labelText + " : <b><font color=" + ColorToHexWithAlpha(textColour,true) + ">" + status.getWarnText() + "</b>";
        }
        return "";
    }

    public LabelStatus getStatus() { return labelStatus; }

    public void updateColours() { setStatus(labelStatus); }

    public enum LabelStatus {
        NONE(Color.WHITE, Color.BLACK),
        LOADED(new Color(0,220,0), new Color(0,150,0)),
        IMPORTED(new Color(255,125,0), new Color(191, 56, 14)),
        MANUAL_LOAD(new Color(240,240,0), new Color(180,180,0)),
        NOT_FOUND(new Color(255,0,0), new Color(190,0,0));

        private final Color color;
        private final Color color2;

        LabelStatus(Color newColor, Color newColor2) {
            this.color = newColor;
            this.color2 = newColor2;
        }

        public Color getColor() { return (FlatLaf.isLafDark()) ? color : color2; }

        public int getWarnValue() { return ordinal(); }

        public String getWarnText() {
            String name = this.name().toLowerCase().replace("_", " ");
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }
}
