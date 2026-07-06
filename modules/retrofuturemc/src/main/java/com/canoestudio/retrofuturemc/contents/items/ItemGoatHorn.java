package com.canoestudio.retrofuturemc.contents.items;

import com.canoestudio.retrofuturemc.retrofuturemc.Tags;
import com.canoestudio.retrofuturemc.sounds.ModSoundHandler;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvents;
import com.canoestudio.retrofuturemccore.api.item.RetroCooldowns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Locale;

import static com.canoestudio.retrofuturemc.contents.tab.CreativeTab.CREATIVE_TABS;

public class ItemGoatHorn extends Item {
    public static final int USE_DURATION = 140;
    public static final int COOLDOWN_TICKS = 140;
    private static final String VARIANT_KEY = "HornVariant";
    private static final String[] VARIANT_NAMES = new String[] {
            "ponder", "sing", "seek", "feel", "admire", "call", "yearn", "dream"
    };
    private static final SoundEvent[] VARIANT_SOUNDS = new SoundEvent[] {
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_0,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_1,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_2,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_3,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_4,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_5,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_6,
            ModSoundHandler.ITEM_GOAT_HORN_SOUND_7
    };

    public ItemGoatHorn() {
        setTranslationKey(Tags.MOD_ID + ".goat_horn");
        setRegistryName("goat_horn");
        setCreativeTab(CREATIVE_TABS);
        setMaxStackSize(1);
        ModItems.ITEMS.add(this);
    }

    public static ItemStack createRandomHorn(World world, boolean screaming) {
        ItemStack stack = new ItemStack(ModItems.GOAT_HORN);
        int base = screaming ? 4 : 0;
        int variant = base + world.rand.nextInt(4);
        setVariant(stack, variant);
        return stack;
    }

    public static void setVariant(ItemStack stack, int variant) {
        stack.setTagInfo(VARIANT_KEY, new net.minecraft.nbt.NBTTagInt(MathHelper.clamp(variant, 0, VARIANT_NAMES.length - 1)));
    }

    public static int getVariant(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(VARIANT_KEY)) {
            return 0;
        }
        return MathHelper.clamp(tag.getInteger(VARIANT_KEY), 0, VARIANT_NAMES.length - 1);
    }

    public static String getVariantName(ItemStack stack) {
        return VARIANT_NAMES[getVariant(stack)];
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        return useHorn(worldIn, playerIn, handIn, stack);
    }

    public static ActionResult<ItemStack> useHorn(World worldIn, EntityPlayer playerIn, EnumHand handIn, ItemStack stack) {
        if (RetroCooldowns.hasCooldown(playerIn, stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        int variant = getVariant(stack);
        SoundEvent sound = VARIANT_SOUNDS[variant];
        if (!worldIn.isRemote) {
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, sound, SoundCategory.RECORDS, 16.0F, 1.0F);
            RetroGameEvents.emit(worldIn, RetroGameEvent.INSTRUMENT_PLAY, playerIn);
        }
        playerIn.setActiveHand(handIn);

        if (!playerIn.capabilities.isCreativeMode) {
            RetroCooldowns.setCooldown(playerIn, stack, COOLDOWN_TICKS);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String variant = getVariantName(stack).replace('_', ' ');
        return super.getItemStackDisplayName(stack) + " (" + variant.substring(0, 1).toUpperCase(Locale.ROOT) + variant.substring(1) + ")";
    }
}
