package goblinbob.mba.editor.viewport;

import goblinbob.mba.editor.IGuiLayer;
import goblinbob.mba.editor.gui.GuiCustomizeWindow;
import goblinbob.mba.editor.store.CustomizeStore;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.Mesh;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.GuiHelper;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.flux.ISubscriber;
import goblinbob.mobends.core.flux.Subscription;
import goblinbob.mobends.core.math.TransformUtils;
import goblinbob.mobends.core.math.matrix.Mat4x4d;
import goblinbob.mobends.core.math.physics.*;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.math.vector.VectorUtils;
import goblinbob.mobends.core.util.*;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

import static goblinbob.mba.editor.store.CustomizeMutations.HOVER_OVER_BONE;
import static goblinbob.mba.editor.store.CustomizeMutations.SELECT_PART;

public class ViewportLayer extends Gui implements IGuiLayer, ISubscriber
{
	private final ResourceLocation STAND_BLOCK_TEXTURE = new ResourceLocation(ModStatics.MODID, "textures/stand_block.png");
	private final GuiCustomizeWindow customizeWindow;
	private final Minecraft mc;
	private final ViewportCamera camera;
	private int x, y;
	private int width, height;

	private Mesh standBlockMesh;
	private Plane groundPlane;
	private OBBox obBox;
	private Vec3f contactPoint;
	
	public ViewportLayer(GuiCustomizeWindow customizeWindow)
	{
		this.customizeWindow = customizeWindow;
		this.mc = Minecraft.getMinecraft();
		this.camera = new ViewportCamera(0, 0, 0, -45F / 180.0F * GUtil.PI, 45F / 180.0F * GUtil.PI);
		this.camera.anchorTo(0, 0, 0, 1);

		this.standBlockMesh = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 24);
		this.standBlockMesh.beginDrawing(GL11.GL_QUADS);
		MeshBuilder.texturedSimpleCube(this.standBlockMesh, -0.5, -1, -0.5, 0.5, 0, 0.5, Color.WHITE, new int[] {16, 0, 16, 0, 16, 0, 16, 0, 0, 0, 32, 0}, 64, 16, 16);
		this.standBlockMesh.finishDrawing();
		
		this.groundPlane = new Plane(0, 0, 0, 0, 1, 0);
		Mat4x4d mat = new Mat4x4d(Mat4x4d.IDENTITY);
		TransformUtils.translate(mat, 0, 0, -4, mat);
		TransformUtils.rotate(mat, Math.PI/4, 0, 1, 0, mat);
		TransformUtils.scale(mat, 2F, 2F, 2F);
		this.obBox = new OBBox(-0.2F, -0.2F, -0.2F, 0.2F, 0.2F, 0.2F, mat);
		this.contactPoint = new Vec3f();

		this.trackSubscription(CustomizeStore.observeAnimatedEntity(alterEntry ->
		{
			IVec3fRead anchorPoint = Vec3f.ZERO;
			IPreviewer previewer = alterEntry.getPreviewer();
			if (previewer != null)
			{
				anchorPoint = previewer.getAnchorPoint();
			}
			this.camera.anchorTo(anchorPoint.getX(), anchorPoint.getY(), anchorPoint.getZ(), 5);
		}));
	}

	/**
	 * Subscriber implementation
	 */
	private final List<Subscription<?>> subscriptions = new LinkedList<>();

	@Override
	public List<Subscription<?>> getSubscriptions() { return this.subscriptions; }

	@Override
	public void cleanUp()
	{
		this.removeSubscriptions();
	}

	public void initGui()
	{
	}

	@Override
	public void handleResize(int width, int height)
	{
		this.width = width;
		this.height = height;
		float ratio = (float) mc.displayWidth / (float) mc.displayHeight;
		this.camera.setupProjection(60.0F, ratio, 0.05F, 1000);
	}

	@Override
	public void update(int mouseX, int mouseY)
	{
		final float moveSpeed = 0.5F;

		this.camera.update();

		if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
			this.camera.moveForward(moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			this.camera.moveForward(-moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			this.camera.moveSideways(moveSpeed);
		if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			this.camera.moveSideways(-moveSpeed);

		AlterEntryRig.Bone boneAtMouse = this.getBoneAtScreenCoords(mouseX, mouseY);
		CustomizeStore.instance.commit(HOVER_OVER_BONE, boneAtMouse);
	}

	/**
	 * Returns the closest bone to the camera that is shown at the specified screen coordinates.
	 * If there is no bone at those coordinates, it returns null.
	 * @param screenX
	 * @param screenY
	 * @return The bone
	 */
	@Nullable
	public AlterEntryRig.Bone getBoneAtScreenCoords(int screenX, int screenY)
	{
		AlterEntryRig.Bone closestBone = null;
		Ray ray = this.camera.getRayFromScreen(screenX, screenY, this.width, this.height);
		AlterEntryRig rig = CustomizeStore.getRig();
		if (rig != null)
		{
			float smallestDistanceSq = 0;
			for (AlterEntryRig.Bone bone : rig.nameToBoneMap.values())
			{
				RayHitInfo hit = Physics.intersect(ray, bone.collider);
				if (hit != null)
				{
					float distanceSq = VectorUtils.distanceSq(hit.hitPoint, camera.getPosition());
					if (closestBone == null || distanceSq < smallestDistanceSq)
					{
						closestBone = bone;
						smallestDistanceSq = distanceSq;
					}
				}
			}
		}

		return closestBone;
	}

	@Override
	public boolean handleKeyTyped(char typedChar, int keyCode)
	{
		// Assuming that it will be handled.
		boolean eventHandled = true;

		final float moveSpeed = 1;

        switch (keyCode)
        {
			default:
                // No case met, event was actually not handled.
                eventHandled = false;
                break;
        }

		return eventHandled;
	}

	@Override
	public boolean handleMouseInput()
	{
		boolean eventHandled = false;

		// Camera rotation

		float dx = Mouse.getEventDX();
		float dy = Mouse.getEventDY();

		if (Mouse.isButtonDown(2) || Mouse.isButtonDown(1))
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				final float speed = 0.02F;
				this.camera.moveSideways(-dx * speed);
				this.camera.moveUp(-dy * speed);
			}
			else
			{
				final float speed = 1 / 180.0F * GUtil.PI;
				this.camera.rotateYaw(dx * speed);
				this.camera.rotatePitch(-dy * speed);
			}

			eventHandled |= true;
		}

		// Mouse wheel

		int mouseWheelRoll = Mouse.getEventDWheel();

        if (mouseWheelRoll != 0)
        {
            mouseWheelRoll = mouseWheelRoll > 0 ? 1 : -1;

            this.camera.zoomInOrOut(-mouseWheelRoll);
            eventHandled |= true;
        }

		return eventHandled;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int button)
	{
		boolean eventHandled = false;

		if (button == 0)
		{
			AlterEntryRig.Bone bone = this.getBoneAtScreenCoords(mouseX, mouseY);
			CustomizeStore.instance.commit(SELECT_PART, bone);
			eventHandled = true;
		}

		return eventHandled;
	}

	public void drawBackground()
	{
		GlStateManager.clearColor(0F, 0.58F, 0.89F, 1F);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GlStateManager.color(1, 0, 0, 1);
		GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
		Draw.rectangleVerticalGradient(0, 0, width, height, 0xff0096e3, 0xff135e8b);
        GlStateManager.enableDepth();
	}

	@Override
	public void draw(float partialTicks)
	{
		int[] position = GuiHelper.getDeScaledCoords(x, y + height + 1);
		int[] size = GuiHelper.getDeScaledVector(width, height + 2);
		//GL11.glEnable(GL11.GL_SCISSOR_TEST);
		//GL11.glScissor(position[0], position[1], size[0], size[1]);

        drawBackground();

		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		this.camera.applyProjection();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		EntityBender<?> animatedEntity = CustomizeStore.getCurrentAnimatedEntity();
		AlterEntryRig rig = CustomizeStore.getRig();
		if (animatedEntity != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			this.camera.applyViewTransform(DataUpdateHandler.partialTicks);

			RenderHelper.enableStandardItemLighting();
			GlStateManager.color(1, 1, 1);
			mc.getTextureManager().bindTexture(STAND_BLOCK_TEXTURE);
			this.standBlockMesh.display();

			GlStateManager.disableTexture2D();

			/*if (this.ray != null)
			{
				final float scale = 5;
				IVec3fRead pos = this.ray.getPosition();
				IVec3fRead dir = this.ray.getDirection();

				Draw.line(pos.getX(), pos.getY(), pos.getZ(),
						  pos.getX() + dir.getX() * scale, pos.getY() + dir.getY() * scale, pos.getZ() + dir.getZ() * scale,
						  Color.RED);
			}*/

			/*if (this.contactPoint != null)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.contactPoint.x, this.contactPoint.y, this.contactPoint.z);
				float s = 0.03F;
				Draw.cube(-s, -s, -s, s, s, s, Color.RED);
				GlStateManager.popMatrix();
			}*/

			GlStateManager.enableTexture2D();

			renderLivingEntity(animatedEntity);

			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderHelper.disableStandardItemLighting();

			if (rig != null && animatedEntity.isAnimated())
			{
				rig.updateTransform();
				rig.nameToBoneMap.forEach((key, bone) ->
				{
					if (bone.collider != null)
					{
						if (rig.isBoneHoveredOver(bone) || rig.isBoneSelected(bone))
						{
							Color color = rig.isBoneHoveredOver(bone) ? new Color(1, 1, 1, 0.6F) : new Color(1, 1, 0.9F, 0.7F);

							GlStateManager.pushMatrix();
							GlHelper.transform(bone.collider.transform);
							final double p = 0.03;
							Draw.cube(bone.collider.min.getX() - p, bone.collider.min.getY() - p, bone.collider.min.getZ() - p,
									bone.collider.max.getX() + p, bone.collider.max.getY() + p, bone.collider.max.getZ() + p, color);
							GlStateManager.popMatrix();
						}
					}
				});
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		//GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	private static void renderLivingEntity(EntityBender<?> animatedEntity)
	{
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();

		float lightAngle = 45.0F;
		GL11.glRotatef(lightAngle, 0.0F, 1.0F, 0.0F);
		GL11.glColor3f(1, 1, 1);

		GL11.glRotatef(-lightAngle, 0.0F, 1.0F, 0.0F);

		LivingEntityData<?> data = animatedEntity.getDataForPreview();
		EntityLivingBase living = data.getEntity();

		float f2 = living.renderYawOffset;
		float f3 = living.rotationYaw;
		float f4 = living.rotationPitch;
		float f5 = living.prevRotationYawHead;
		float f6 = living.rotationYawHead;
		living.renderYawOffset = 0;
		living.rotationYaw = 0;
		living.rotationPitch = 0;
		living.rotationYawHead = living.rotationYaw;
		living.prevRotationYawHead = living.rotationYaw;

		Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;

		@SuppressWarnings("unchecked")
		IPreviewer<LivingEntityData<?>> previewer = (IPreviewer<LivingEntityData<?>>) animatedEntity.getPreviewer();

		if (previewer != null)
			previewer.prePreview(data, "walk");

		Minecraft.getMinecraft().getRenderManager().renderEntity(living, 0.0D, 0.0D, 0.0D, 0.0F, 1.0f,
				false);

		if (previewer != null)
			previewer.postPreview(data, "walk");

		living.renderYawOffset = f2;
		living.rotationYaw = f3;
		living.rotationPitch = f4;
		living.prevRotationYawHead = f5;
		living.rotationYawHead = f6;
		GL11.glPopMatrix();

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

}
