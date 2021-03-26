package goblinbob.mba.editor.gui;

import goblinbob.mba.editor.store.CustomizeStore;
import goblinbob.mba.editor.viewport.AlterEntryRig;
import goblinbob.mobends.core.client.gui.elements.GuiPanel;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiPartProperties extends GuiPanel
{

    public GuiPartProperties()
    {
        super(null, 0, 400, 100, 300, GuiPanel.Direction.RIGHT);
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    public void cleanUp()
    {

    }

    @Override
    protected void drawBackground(float partialTicks)
    {
        Draw.rectangle(0, 0, this.getWidth(), this.getHeight() - 2, 0xff00528a);
        Draw.rectangle(0, this.getHeight() - 2, this.getWidth(), 2, 0xff00406b);
    }

    @Override
    protected void drawForeground(float partialTicks)
    {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        AlterEntryRig.Bone selectedPart = CustomizeStore.getSelectedPart();
        if (selectedPart != null)
            fontRenderer.drawString(selectedPart.getName(), 0, 0, 0xffffffff);
    }

}
