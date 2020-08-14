package goblinbob.mba.editor.gui;

import goblinbob.mba.editor.IGuiLayer;
import goblinbob.mba.editor.store.CustomizeStore;
import goblinbob.mba.editor.viewport.ViewportLayer;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.IAnimationEditor;
import goblinbob.mobends.core.flux.ISubscriber;
import goblinbob.mba.editor.IEditorAction;
import goblinbob.mba.editor.OverlayLayer;
import goblinbob.mobends.core.flux.Subscription;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static goblinbob.mba.editor.store.CustomizeMutations.SHOW_ANIMATED_ENTITY;
import static goblinbob.mba.editor.store.CustomizeMutations.TRACK_EDITOR_ACTION;

public class GuiCustomizeWindow extends IAnimationEditor.EditorGuiBase implements ISubscriber
{

    /*
     * Used to remember which AnimatedEntity was used last, so
     * the GUI can show that as default the next time it
     * opens up.
     */
    protected static EntityBender lastAnimatedEntityViewed = null;

    public final List<EntityBender<?>> animatedEntities = new ArrayList<>();

    private final ViewportLayer viewportLayer;
    private final OverlayLayer overlayLayer;
    private final LinkedList<IGuiLayer> layers = new LinkedList<>();

    public GuiCustomizeWindow()
    {
        this.animatedEntities.addAll(EntityBenderRegistry.instance.getRegistered());

        this.viewportLayer = new ViewportLayer(this);
        this.overlayLayer = new OverlayLayer(this);
        this.layers.add(this.viewportLayer);
        this.layers.add(this.overlayLayer);

        this.trackSubscription(CustomizeStore.observeAnimatedEntity((EntityBender<?> animatedEntity) -> {
            lastAnimatedEntityViewed = animatedEntity;
        }));

        // Showing the AnimatedEntity viewed the last time
        // this gui was open.
        CustomizeStore.instance.commit(SHOW_ANIMATED_ENTITY, lastAnimatedEntityViewed != null ? lastAnimatedEntityViewed : this.animatedEntities.get(0));
    }

    /**
     * Subscriber implementation
     */
    private final List<Subscription<?>> subscriptions = new LinkedList<>();

    @Override
    public List<Subscription<?>> getSubscriptions() { return this.subscriptions; }

    @Override
    public void onGuiClosed()
    {
        for (IGuiLayer layer : this.layers)
        {
            layer.cleanUp();
        }
        this.removeSubscriptions();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.overlayLayer.initGui();
        this.viewportLayer.initGui();

        for (IGuiLayer layer : this.layers)
        {
            layer.handleResize(this.width, this.height);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        for (IGuiLayer layer : this.layers)
        {
            layer.draw(partialTicks);
        }

		/*if (!PackManager.isCurrentPackLocal())
		{
			this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.chooseapacktoedit"),
					this.x + WIDTH / 2, this.y + 135, 0xffffff);
			
			return;
		}*/
    }

    @Override
    public void updateScreen()
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        for (IGuiLayer layer : this.layers)
        {
            layer.update(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button)
    {
        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseClicked(mouseX, mouseY, button))
                break;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseReleased(mouseX, mouseY, button))
                break;
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseInput())
                break;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        boolean eventHandled = false;

        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleKeyTyped(typedChar, keyCode))
            {
                eventHandled |= true;
                break;
            }
        }

        if (!eventHandled && keyCode == Keyboard.KEY_ESCAPE)
        {
            goBack();
            eventHandled |= true;
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void goBack()
    {
        this.mc.displayGuiScreen(new GuiBendsMenu());
    }

    public void performAction(IEditorAction<GuiCustomizeWindow> action)
    {
        action.perform(this);
        CustomizeStore.instance.commit(TRACK_EDITOR_ACTION, action);
    }

}
