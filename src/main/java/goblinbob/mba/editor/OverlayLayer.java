package goblinbob.mba.editor;

import goblinbob.mba.editor.gui.GuiCustomizeWindow;
import goblinbob.mba.editor.gui.GuiPartHierarchy;
import goblinbob.mba.editor.gui.GuiPartProperties;
import goblinbob.mba.editor.gui.GuiStateEditor;
import goblinbob.mba.editor.project.BendsPackProject;
import goblinbob.mba.editor.store.CustomizeStore;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.IChangeListener;
import goblinbob.mobends.core.client.gui.IObservable;
import goblinbob.mobends.core.client.gui.elements.GuiDropDownList;
import goblinbob.mobends.core.client.gui.elements.GuiToggleButton;
import goblinbob.mobends.core.flux.ISubscriber;
import goblinbob.mobends.core.flux.Subscription;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;
import java.util.List;

import static goblinbob.mba.editor.store.CustomizeMutations.SHOW_ANIMATED_ENTITY;

public class OverlayLayer implements IGuiLayer, IChangeListener, ISubscriber
{

	private int screenWidth;
	private int screenHeight;
	private final GuiDropDownList<EntityBender<?>> targetList;
	private final GuiToggleButton toggleButton;
	private final GuiPartHierarchy hierarchy;
	private final GuiPartProperties properties;
	private final GuiStateEditor stateEditor;

	public OverlayLayer(GuiCustomizeWindow customizeWindow)
	{
		this.screenWidth = customizeWindow.width;
		this.screenHeight = customizeWindow.height;
		this.targetList = new GuiDropDownList().forbidNoValue();
		this.toggleButton = new GuiToggleButton("Animated", 64);
		this.hierarchy = new GuiPartHierarchy();
		this.properties = new GuiPartProperties();
		this.stateEditor = new GuiStateEditor(new BendsPackProject());

		for (EntityBender animatedEntity : customizeWindow.animatedEntities)
		{
			this.targetList.addEntry(animatedEntity.getLocalizedName(), animatedEntity);
		}

		this.targetList.addListener(this);
		this.targetList.selectValue(CustomizeStore.getCurrentAnimatedEntity());

		this.trackSubscription(CustomizeStore.observeAnimatedEntity((EntityBender<?> animatedEntity) ->
		{
			this.targetList.selectValue(animatedEntity);
			this.toggleButton.setToggleState(animatedEntity.isAnimated());
		}));
	}

	private List<Subscription<?>> subscriptions = new LinkedList<>();

	@Override
	public List<Subscription<?>> getSubscriptions() { return this.subscriptions; }

	@Override
	public void cleanUp()
	{
		this.hierarchy.cleanUp();
		this.properties.cleanUp();
		this.removeSubscriptions();
	}

	public void initGui()
	{
		this.targetList.setPosition(2, 2);
		this.toggleButton.initGui(10, 30);
		this.hierarchy.initGui();
		this.properties.initGui();
		this.stateEditor.initGui();

		EntityBender<?> animatedEntity = CustomizeStore.getCurrentAnimatedEntity();
		if (animatedEntity != null)
			this.toggleButton.setToggleState(animatedEntity.isAnimated());
	}

	@Override
	public void handleResize(int width, int height)
	{
		this.screenWidth = width;
		this.screenHeight = height;
	}
	
	@Override
	public void update(int mouseX, int mouseY)
	{
		this.targetList.update(mouseX, mouseY);
		this.hierarchy.update(mouseX, mouseY);
		this.properties.update(mouseX, mouseY);
		this.stateEditor.update(mouseX, mouseY);
		this.toggleButton.update(mouseX, mouseY);
	}
	
	@Override
	public void draw(float partialTicks)
	{
		GlStateManager.disableTexture2D();
		Draw.rectangle(0, 0, this.screenWidth, 20, 0xff00528a);
		Draw.rectangle(0, 20, this.screenWidth, 2, 0xff00406b);

		GlStateManager.enableTexture2D();
		this.toggleButton.draw();
		this.hierarchy.draw(partialTicks);
		this.properties.draw(partialTicks);
		this.stateEditor.draw(partialTicks);
		this.targetList.display();
	}
	
	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;

		eventHandled |= this.targetList.mouseClicked(mouseX, mouseY, button);

		if (!eventHandled && this.stateEditor.handleMouseClicked(mouseX, mouseY, button))
		{
			eventHandled = true;
		}

		if (!eventHandled && this.toggleButton.mouseClicked(mouseX, mouseY, button))
		{
			EntityBender<?> animatedEntity = CustomizeStore.getCurrentAnimatedEntity();
			if (animatedEntity != null)
				animatedEntity.setAnimate(this.toggleButton.getToggleState());
			
			eventHandled = true;
		}

		if (!eventHandled && this.hierarchy.handleMouseClicked(mouseX, mouseY, button))
		{
			eventHandled = true;
		}
		
		return eventHandled;
	}

	@Override
	public boolean handleMouseReleased(int mouseX, int mouseY, int button)
	{
		this.targetList.mouseReleased(mouseX, mouseY, button);
		this.hierarchy.handleMouseReleased(mouseX, mouseY, button);

		return false;
	}

	@Override
	public boolean handleMouseInput()
	{
		boolean eventHandled = false;

		eventHandled |= this.targetList.handleMouseInput();
		if (!eventHandled)
		{
			eventHandled |= this.hierarchy.handleMouseInput();
		}

		return eventHandled;
	}

	@Override
	public void handleChange(IObservable objectChanged)
	{
		if (objectChanged == this.targetList)
		{
			CustomizeStore.instance.commit(SHOW_ANIMATED_ENTITY, this.targetList.getSelectedValue());
		}
	}
	
}
