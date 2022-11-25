package AutoDriveEditor.GUI.Buttons;

public abstract class OptionsBaseButton extends BaseButton{

    @Override
    public Boolean ignoreDeselect() { return true; }

    @Override
    public void setSelected(boolean selected) {}
}
