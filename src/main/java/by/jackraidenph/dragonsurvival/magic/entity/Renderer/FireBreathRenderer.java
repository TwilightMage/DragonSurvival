package by.jackraidenph.dragonsurvival.magic.entity.Renderer;

import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import by.jackraidenph.dragonsurvival.magic.entity.FireBreathEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

public class FireBreathRenderer extends EntityRenderer<FireBreathEntity>
{
	public static final ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/entity/fire_breath.png");
	private static RenderType RENDER_TYPE = RenderType.entityTranslucent(texture);
	
	
	public FireBreathRenderer(EntityRendererManager p_i46179_1_)
	{
		super(p_i46179_1_);
	}
	
	@Override
	public ResourceLocation getTextureLocation(FireBreathEntity p_110775_1_)
	{
		return texture;
	}
	
	public void render(FireBreathEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
		p_225623_4_.pushPose();
		GL11.glPushMatrix();
		//p_225623_4_.scale(2.0F, 2.0F, 2.0F);
		p_225623_4_.mulPose(this.entityRenderDispatcher.cameraOrientation());
		p_225623_4_.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		MatrixStack.Entry matrixstack$entry = p_225623_4_.last();
		Matrix4f matrix4f = matrixstack$entry.pose();
		Matrix3f matrix3f = matrixstack$entry.normal();
		
		IVertexBuilder ivertexbuilder = p_225623_5_.getBuffer(RENDER_TYPE);
		vertex(ivertexbuilder, matrix4f, matrix3f, p_225623_6_, 0.0F, 0, 0, 1);
		vertex(ivertexbuilder, matrix4f, matrix3f, p_225623_6_, 1.0F, 0, 1, 1);
		vertex(ivertexbuilder, matrix4f, matrix3f, p_225623_6_, 1.0F, 1, 1, 0);
		vertex(ivertexbuilder, matrix4f, matrix3f, p_225623_6_, 0.0F, 1, 0, 0);
		p_225623_4_.popPose();
		super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
		GL11.glPopMatrix();
	}
	
	private static void vertex(IVertexBuilder p_229045_0_, Matrix4f p_229045_1_, Matrix3f p_229045_2_, int p_229045_3_, float p_229045_4_, int p_229045_5_, int p_229045_6_, int p_229045_7_) {
		p_229045_0_.vertex(p_229045_1_, p_229045_4_ - 0.5F, (float)p_229045_5_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)p_229045_6_, (float)p_229045_7_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_229045_3_).normal(p_229045_2_, 0.0F, 1.0F, 0.0F).endVertex();
	}
	
}
