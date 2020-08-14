package goblinbob.mba.editor;

import goblinbob.mba.editor.gui.GuiCustomizeWindow;
import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.IAnimationEditor;

public class AddonEntry implements IAddon
{

    private static class Editor implements IAnimationEditor
    {
        @Override
        public EditorGuiBase createGui(GuiBendsMenu guiBendsMenu)
        {
            return new GuiCustomizeWindow();
        }
    }

    @Override
    public void registerContent(AddonAnimationRegistry registry)
    {
        registry.registerAnimationEditor(new Editor());
    }

    @Override
    public String getDisplayName()
    {
        return "Animation Editor";
    }

}
