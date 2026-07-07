package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class RenderMangroveSign extends TileEntitySpecialRenderer<TileEntitySign> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(RTWU.ID, "textures/blocks/mangrove_sign.png");
    private final ModelSign model = new ModelSign();

    @Override
    public void render(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage,
                       float alpha) {
        Block block = te.getBlockType();
        GlStateManager.pushMatrix();

        if (block == ModBlocks.MANGROVE_SIGN) {
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            int rotation = te.getWorld().getBlockState(te.getPos()).getValue(BlockStandingSign.ROTATION);
            GlStateManager.rotate(-((float) rotation * 360.0F / 16.0F), 0.0F, 1.0F, 0.0F);
            this.model.signStick.showModel = true;
        } else {
            int meta = te.getWorld().getBlockState(te.getPos()).getValue(BlockWallSign.FACING).getIndex();
            float yaw = 0.0F;
            if (meta == 2) {
                yaw = 180.0F;
            } else if (meta == 4) {
                yaw = 90.0F;
            } else if (meta == 5) {
                yaw = -90.0F;
            }
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            this.model.signStick.showModel = false;
        }

        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            this.bindTexture(TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
        this.model.renderSign();
        GlStateManager.popMatrix();
        renderText(te, destroyStage);
        GlStateManager.popMatrix();

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    private void renderText(TileEntitySign te, int destroyStage) {
        FontRenderer font = this.getFontRenderer();
        GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);

        if (destroyStage < 0) {
            for (int i = 0; i < te.signText.length; ++i) {
                if (te.signText[i] == null) {
                    continue;
                }
                ITextComponent component = te.signText[i];
                List<ITextComponent> list = GuiUtilRenderComponents.splitText(component, 90, font, false, true);
                String line = list != null && !list.isEmpty() ? list.get(0).getFormattedText() : "";
                if (i == te.lineBeingEdited) {
                    line = "> " + line + " <";
                }
                font.drawString(line, -font.getStringWidth(line) / 2, i * 10 - te.signText.length * 5, 0);
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
