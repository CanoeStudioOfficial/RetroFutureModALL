package com.canoestudio.retrofuturemccore.api.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public final class RetroSignSet {
    private final ResourceLocation id;
    private final Block standingSign;
    private final Block wallSign;
    private final Block hangingSign;
    private final Block wallHangingSign;
    private final Item signItem;
    private final Item hangingSignItem;
    private final Class<? extends TileEntity> signTileEntityClass;
    private final Class<? extends TileEntity> hangingSignTileEntityClass;
    private final ResourceLocation texture;

    private RetroSignSet(Builder builder) {
        this.id = builder.id;
        this.standingSign = builder.standingSign;
        this.wallSign = builder.wallSign;
        this.hangingSign = builder.hangingSign;
        this.wallHangingSign = builder.wallHangingSign;
        this.signItem = builder.signItem;
        this.hangingSignItem = builder.hangingSignItem;
        this.signTileEntityClass = builder.signTileEntityClass;
        this.hangingSignTileEntityClass = builder.hangingSignTileEntityClass;
        this.texture = builder.texture;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Block getStandingSign() {
        return this.standingSign;
    }

    public Block getWallSign() {
        return this.wallSign;
    }

    public Block getHangingSign() {
        return this.hangingSign;
    }

    public Block getWallHangingSign() {
        return this.wallHangingSign;
    }

    public Item getSignItem() {
        return this.signItem;
    }

    public Item getHangingSignItem() {
        return this.hangingSignItem;
    }

    public Class<? extends TileEntity> getSignTileEntityClass() {
        return this.signTileEntityClass;
    }

    public Class<? extends TileEntity> getHangingSignTileEntityClass() {
        return this.hangingSignTileEntityClass;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public boolean hasNormalSign() {
        return this.standingSign != null && this.wallSign != null;
    }

    public boolean hasHangingSign() {
        return this.hangingSign != null && this.wallHangingSign != null;
    }

    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<Block>();
        addBlock(blocks, this.standingSign);
        addBlock(blocks, this.wallSign);
        addBlock(blocks, this.hangingSign);
        addBlock(blocks, this.wallHangingSign);
        return Collections.unmodifiableList(blocks);
    }

    private static void addBlock(List<Block> blocks, Block block) {
        if (block != null) {
            blocks.add(block);
        }
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Block standingSign;
        private Block wallSign;
        private Block hangingSign;
        private Block wallHangingSign;
        private Item signItem;
        private Item hangingSignItem;
        private Class<? extends TileEntity> signTileEntityClass;
        private Class<? extends TileEntity> hangingSignTileEntityClass;
        private ResourceLocation texture;

        private Builder(ResourceLocation id) {
            if (id == null) {
                throw new IllegalArgumentException("Sign set id cannot be null");
            }
            this.id = id;
        }

        public Builder sign(Block standingSign, Block wallSign, Item signItem) {
            this.standingSign = standingSign;
            this.wallSign = wallSign;
            this.signItem = signItem;
            return this;
        }

        public Builder hangingSign(Block hangingSign, Block wallHangingSign, Item hangingSignItem) {
            this.hangingSign = hangingSign;
            this.wallHangingSign = wallHangingSign;
            this.hangingSignItem = hangingSignItem;
            return this;
        }

        public Builder signTile(Class<? extends TileEntity> tileEntityClass) {
            this.signTileEntityClass = tileEntityClass;
            return this;
        }

        public Builder hangingSignTile(Class<? extends TileEntity> tileEntityClass) {
            this.hangingSignTileEntityClass = tileEntityClass;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public RetroSignSet build() {
            return new RetroSignSet(this);
        }

        public RetroSignSet register() {
            return RetroSignRegistry.register(this.build());
        }
    }
}
