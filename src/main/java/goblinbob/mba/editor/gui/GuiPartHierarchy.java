package goblinbob.mba.editor.gui;

import goblinbob.mobends.core.client.gui.elements.GuiPanel;
import goblinbob.mobends.core.util.Draw;

public class GuiPartHierarchy extends GuiPanel
{

    private GuiPartList partList;

    public GuiPartHierarchy()
    {
        super(null, 0, 60, 100, 300, Direction.RIGHT);
        this.partList = new GuiPartList(this, 2, 2, 96, 90);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.addElement(this.partList);
    }

    public void cleanUp()
    {
        this.partList.cleanUp();
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int event)
    {
        return this.partList.handleMouseClicked(mouseX - x, mouseY - y, event);
    }

    @Override
    public boolean handleMouseReleased(int mouseX, int mouseY, int event)
    {
        this.partList.handleMouseReleased(mouseX - x, mouseY - y, event);

        return false;
    }

    public boolean handleMouseInput()
    {
        return this.partList.handleMouseInput();
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

    }

}
