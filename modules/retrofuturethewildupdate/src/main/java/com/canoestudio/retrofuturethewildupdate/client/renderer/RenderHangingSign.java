package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.BlockMangroveHangingSign;
import com.canoestudio.retrofuturethewildupdate.block.BlockMangroveWallHangingSign;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.block.TileEntityHangingSign;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class RenderHangingSign extends TileEntitySpecialRenderer<TileEntityHangingSign> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RTWU.ID, "textures/blocks/mangrove_hanging_sign.png");

    @Override
    public void render(TileEntityHangingSign te, double x, double y, double z, float partialTicks, int destroyStage,
                       float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GlStateManager.rotate(this.getYaw(te), 0.0F, 1.0F, 0.0F);

        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        } else {
            this.bindTexture(TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.disableCull();
        this.renderBoard();
        this.renderChains(te);
        GlStateManager.enableCull();
        this.renderText(te);

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        GlStateManager.popMatrix();
    }

    private float getYaw(TileEntityHangingSign te) {
        Block block = te.getBlockType();
        if (block == ModBlocks.MANGROVE_HANGING_SIGN) {
            int rotation = te.getBlockMetadata();
            return -((float) rotation * 360.0F / 16.0F);
        }
        if (block == ModBlocks.MANGROVE_WALL_HANGING_SIGN) {
            return -te.getWallFacing().getHorizontalAngle();
        }
        return 0.0F;
    }

    private void renderBoard() {
        drawBox(-0.4375F, -0.5F, -0.0625F, 0.4375F, 0.125F, 0.0625F,
            1.0F / 32.0F, 8.0F / 32.0F, 30.0F / 32.0F, 21.0F / 32.0F);
    }

    private void renderChains(TileEntityHangingSign te) {
        Block block = te.getBlockType();
        if (block == ModBlocks.MANGROVE_WALL_HANGING_SIGN) {
            drawBox(-0.5F, 0.375F, -0.125F, 0.5F, 0.5F, 0.125F,
                0.0F, 2.0F / 32.0F, 16.0F / 32.0F, 6.0F / 32.0F);
            drawSlantedChain(-0.3125F, 0.125F, -0.0625F, -0.5F, 0.375F, -0.0625F);
            drawSlantedChain(0.3125F, 0.125F, -0.0625F, 0.5F, 0.375F, -0.0625F);
            return;
        }
        if (te.isAttached()) {
            drawSlantedChain(-0.375F, 0.125F, 0.0F, 0.375F, 0.5F, 0.0F);
            drawSlantedChain(0.375F, 0.125F, 0.0F, -0.375F, 0.5F, 0.0F);
        } else {
            drawSlantedChain(-0.3125F, 0.125F, -0.25F, -0.3125F, 0.5F, -0.25F);
            drawSlantedChain(0.3125F, 0.125F, 0.25F, 0.3125F, 0.5F, 0.25F);
            drawSlantedChain(-0.3125F, 0.125F, 0.25F, -0.3125F, 0.5F, 0.25F);
            drawSlantedChain(0.3125F, 0.125F, -0.25F, 0.3125F, 0.5F, -0.25F);
        }
    }

    private void drawSlantedChain(float x1, float y1, float z1, float x2, float y2, float z2) {
        GlStateManager.glLineWidth(2.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x1, y1, z1).tex(11.0F / 32.0F, 4.0F / 32.0F).endVertex();
        buffer.pos(x2, y2, z2).tex(16.0F / 32.0F, 7.0F / 32.0F).endVertex();
        tessellator.draw();
    }

    private void renderText(TileEntitySign te) {
        this.renderTextSide(te, 0.064F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        this.renderTextSide(te, 0.064F);
        GlStateManager.popMatrix();
    }

    private void renderTextSide(TileEntitySign te, float zOffset) {
        FontRenderer font = this.getFontRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -0.1875F, zOffset);
        GlStateManager.scale(0.008F, -0.008F, 0.008F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.008F);
        GlStateManager.depthMask(false);

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

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static void drawBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                float minU, float minV, float maxU, float maxV) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        quad(buffer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minU, minV, maxU, maxV);
        quad(buffer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, minU, minV, maxU, maxV);
        quad(buffer, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minU, minV, maxU, maxV);
        quad(buffer, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minU, minV, maxU, maxV);
        quad(buffer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minU, minV, maxU, maxV);
        quad(buffer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minU, minV, maxU, maxV);
        tessellator.draw();
    }

    private static void quad(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float minU, float minV, float maxU, float maxV) {
        buffer.pos(x1, y1, z1).tex(minU, maxV).endVertex();
        buffer.pos(x2, y2, z2).tex(maxU, maxV).endVertex();
        buffer.pos(x3, y3, z3).tex(maxU, minV).endVertex();
        buffer.pos(x4, y4, z4).tex(minU, minV).endVertex();
    }
}
