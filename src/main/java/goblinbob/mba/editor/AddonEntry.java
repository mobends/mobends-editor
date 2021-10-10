package goblinbob.mba.editor;

import goblinbob.mobends.forge.addon.IAddon;
import goblinbob.mobends.core.client.gui.IAnimationEditor;
import goblinbob.mobends.forge.addon.IAddonContentRegistry;

public class AddonEntry implements IAddon
{
    private static class Editor implements IAnimationEditor
    {
        @Override
        public void openEditorGui()
        {
            //TODO Open the editor someday.
        }
    }

    @Override
    public void registerContent(IAddonContentRegistry registry)
    {
        registry.registerAnimationEditor(new Editor());
    }

    @Override
    public String getDisplayName()
    {
        return "Animation Editor";
    }
}
