package goblinbob.mba.editor.gui;

import goblinbob.mba.editor.project.BendsPackProject;
import goblinbob.mba.editor.project.NodeProject;
import goblinbob.mobends.core.client.gui.elements.GuiPanel;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiStateEditor extends GuiPanel
{
    private final BendsPackProject bendsPackProject;

    public GuiStateEditor(BendsPackProject bendsPackProject)
    {
        super(null, 400, 400, 300, 300, GuiPanel.Direction.RIGHT);
        this.bendsPackProject = bendsPackProject;
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
        final int nodeWidth = 20;
        final int nodeHeight = 20;

        for (NodeProject node : this.bendsPackProject.packStateProject.nodes)
        {
            Draw.rectangle(node.x - nodeWidth/2, node.y - nodeHeight/2, nodeWidth, nodeHeight, 0xff666666);
        }
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        boolean eventHandled = super.handleMouseClicked(mouseX, mouseY, button);

        NodeProject node = new NodeProject();
        node.x = mouseX;
        node.y = mouseY;
        bendsPackProject.packStateProject.nodes.add(node);

        return mouseX >= x && mouseX <= x + getWidth() && mouseY >= y && mouseY <= y + getHeight();
    }

    public void mouseReleased(int mouseX, int mouseY, int event)
    {
    }
}
