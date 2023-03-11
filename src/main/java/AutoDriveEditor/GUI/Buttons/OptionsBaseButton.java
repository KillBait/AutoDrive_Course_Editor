package AutoDriveEditor.GUI.Buttons;

public abstract class OptionsBaseButton extends BaseButton{

    @Override
    public String getButtonAction() { return "OptionButton"; }

    @Override
    public String getButtonPanel() { return "Options"; }

    @Override
    public String getInfoText() { return null; }

    @Override
    public Boolean ignoreDeselect() { return true; }

    @Override
    public void setSelected(boolean selected) {}
}
