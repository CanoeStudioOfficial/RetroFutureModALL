package com.canoestudio.retrofuturemccore.api.event;

import com.canoestudio.retrofuturemccore.api.tag.RetroTagKey;
import com.canoestudio.retrofuturemccore.api.tag.RetroTagRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class RetroInteractions {

    private RetroInteractions() {
    }

    public static BlockBuilder block() {
        return new BlockBuilder();
    }

    public static EntityBuilder entity() {
        return new EntityBuilder();
    }

    public static final class BlockBuilder {
        private final List<Predicate<RetroBlockInteraction>> predicates = new ArrayList<Predicate<RetroBlockInteraction>>();
        private BlockInteractionAction action;

        private BlockBuilder() {
        }

        public BlockBuilder block(final Block block) {
            return when(new Predicate<RetroBlockInteraction>() {
                @Override
                public boolean test(RetroBlockInteraction interaction) {
                    return interaction.getBlock() == block;
                }
            });
        }

        public BlockBuilder blockTag(final RetroTagKey<Block> tag) {
            return when(new Predicate<RetroBlockInteraction>() {
                @Override
                public boolean test(RetroBlockInteraction interaction) {
                    return RetroTagRegistry.containsBlock(tag, interaction.getBlock());
                }
            });
        }

        public BlockBuilder item(final Item item) {
            return when(new Predicate<RetroBlockInteraction>() {
                @Override
                public boolean test(RetroBlockInteraction interaction) {
                    return !interaction.getStack().isEmpty() && interaction.getStack().getItem() == item;
                }
            });
        }

        public BlockBuilder itemTag(final RetroTagKey<Item> tag) {
            return when(new Predicate<RetroBlockInteraction>() {
                @Override
                public boolean test(RetroBlockInteraction interaction) {
                    return !interaction.getStack().isEmpty()
                            && RetroTagRegistry.containsItem(tag, interaction.getStack().getItem());
                }
            });
        }

        public BlockBuilder when(Predicate<RetroBlockInteraction> predicate) {
            if (predicate != null) {
                this.predicates.add(predicate);
            }
            return this;
        }

        public BlockBuilder onUse(BlockInteractionAction action) {
            this.action = action;
            return this;
        }

        public RetroBlockInteractionHandler register() {
            final List<Predicate<RetroBlockInteraction>> tests =
                    new ArrayList<Predicate<RetroBlockInteraction>>(this.predicates);
            final BlockInteractionAction registeredAction = this.action;
            RetroBlockInteractionHandler handler = new RetroBlockInteractionHandler() {
                @Override
                public RetroEventResult onRightClickBlock(World world, BlockPos pos, IBlockState state,
                        EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing face, Vec3d hitVec) {
                    if (registeredAction == null) {
                        return RetroEventResult.PASS;
                    }
                    RetroBlockInteraction interaction =
                            new RetroBlockInteraction(world, pos, state, player, hand, stack, face, hitVec);
                    for (Predicate<RetroBlockInteraction> predicate : tests) {
                        if (!predicate.test(interaction)) {
                            return RetroEventResult.PASS;
                        }
                    }
                    return registeredAction.apply(interaction);
                }
            };
            RetroEventRegistry.registerBlockInteraction(handler);
            return handler;
        }
    }

    public static final class EntityBuilder {
        private final List<Predicate<RetroEntityInteraction>> predicates =
                new ArrayList<Predicate<RetroEntityInteraction>>();
        private EntityInteractionAction action;
        private EntityInteractionAction actionAt;

        private EntityBuilder() {
        }

        public EntityBuilder entityClass(final Class<? extends Entity> entityClass) {
            return when(new Predicate<RetroEntityInteraction>() {
                @Override
                public boolean test(RetroEntityInteraction interaction) {
                    return entityClass.isInstance(interaction.getTarget());
                }
            });
        }

        public EntityBuilder entityTag(final RetroTagKey<Class<? extends Entity>> tag) {
            return when(new Predicate<RetroEntityInteraction>() {
                @Override
                public boolean test(RetroEntityInteraction interaction) {
                    return RetroTagRegistry.containsEntity(tag, interaction.getTarget());
                }
            });
        }

        public EntityBuilder item(final Item item) {
            return when(new Predicate<RetroEntityInteraction>() {
                @Override
                public boolean test(RetroEntityInteraction interaction) {
                    return !interaction.getStack().isEmpty() && interaction.getStack().getItem() == item;
                }
            });
        }

        public EntityBuilder itemTag(final RetroTagKey<Item> tag) {
            return when(new Predicate<RetroEntityInteraction>() {
                @Override
                public boolean test(RetroEntityInteraction interaction) {
                    return !interaction.getStack().isEmpty()
                            && RetroTagRegistry.containsItem(tag, interaction.getStack().getItem());
                }
            });
        }

        public EntityBuilder when(Predicate<RetroEntityInteraction> predicate) {
            if (predicate != null) {
                this.predicates.add(predicate);
            }
            return this;
        }

        public EntityBuilder onUse(EntityInteractionAction action) {
            this.action = action;
            return this;
        }

        public EntityBuilder onUseAt(EntityInteractionAction action) {
            this.actionAt = action;
            return this;
        }

        public RetroEntityInteractionHandler register() {
            final List<Predicate<RetroEntityInteraction>> tests =
                    new ArrayList<Predicate<RetroEntityInteraction>>(this.predicates);
            final EntityInteractionAction registeredAction = this.action;
            final EntityInteractionAction registeredActionAt = this.actionAt;
            RetroEntityInteractionHandler handler = new RetroEntityInteractionHandler() {
                @Override
                public RetroEventResult onRightClickEntity(World world, Entity target, EntityPlayer player,
                        EnumHand hand, ItemStack stack) {
                    return apply(world, target, player, hand, stack, null, registeredAction);
                }

                @Override
                public RetroEventResult onRightClickEntityAt(World world, Entity target, EntityPlayer player,
                        EnumHand hand, ItemStack stack, Vec3d localPos) {
                    return apply(world, target, player, hand, stack, localPos, registeredActionAt);
                }

                private RetroEventResult apply(World world, Entity target, EntityPlayer player, EnumHand hand,
                        ItemStack stack, Vec3d localPos, EntityInteractionAction selectedAction) {
                    if (selectedAction == null) {
                        return RetroEventResult.PASS;
                    }
                    RetroEntityInteraction interaction =
                            new RetroEntityInteraction(world, target, player, hand, stack, localPos);
                    for (Predicate<RetroEntityInteraction> predicate : tests) {
                        if (!predicate.test(interaction)) {
                            return RetroEventResult.PASS;
                        }
                    }
                    return selectedAction.apply(interaction);
                }
            };
            RetroEventRegistry.registerEntityInteraction(handler);
            return handler;
        }
    }
}
