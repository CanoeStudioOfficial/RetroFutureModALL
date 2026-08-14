package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 1.13 debug stick behaviour adapted to the 1.12.2 property API.
 *
 * <p>Only creative players may use it.  Sneak-right-click selects the next
 * property; normal right-click cycles the selected property's values.  The
 * selected property is stored on the stack so it survives inventory moves
 * and world saves.</p>
 */
public class ItemDebugStick extends Item {

    private static final String SELECTED_PROPERTY = "SelectedProperty";

    public ItemDebugStick() {
        this.setRegistryName(RetroFutureUpdateAquatic.ID, "debug_stick");
        this.setTranslationKey(RetroFutureUpdateAquatic.ID + ".debug_stick");
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.capabilities.isCreativeMode) {
            return EnumActionResult.FAIL;
        }

        IBlockState state = world.getBlockState(pos);
        List<IProperty<?>> properties = getProperties(state);
        if (properties.isEmpty()) {
            return EnumActionResult.FAIL;
        }

        String selected = getSelectedProperty(stack);
        IProperty<?> property = findProperty(properties, selected);
        if (player.isSneaking()) {
            int index = property == null ? -1 : properties.indexOf(property);
            property = properties.get((index + 1 + properties.size()) % properties.size());
            setSelectedProperty(stack, property.getName());
            if (!world.isRemote) {
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                    "Debug Stick: " + property.getName()), true);
            }
            return EnumActionResult.SUCCESS;
        }

        if (property == null) {
            property = properties.get(0);
            setSelectedProperty(stack, property.getName());
        }

        IBlockState next = cycleValue(state, property);
        if (next == state) {
            return EnumActionResult.SUCCESS;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, next, 11);
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                "Debug Stick: " + property.getName() + "=" + next.getValue(property)), true);
        }
        return EnumActionResult.SUCCESS;
    }

    private static List<IProperty<?>> getProperties(IBlockState state) {
        List<IProperty<?>> properties = new ArrayList<IProperty<?>>(state.getPropertyKeys());
        Collections.sort(properties, new Comparator<IProperty<?>>() {
            @Override
            public int compare(IProperty<?> first, IProperty<?> second) {
                return first.getName().compareTo(second.getName());
            }
        });
        return properties;
    }

    private static IProperty<?> findProperty(List<IProperty<?>> properties, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (IProperty<?> property : properties) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState cycleValue(IBlockState state, IProperty<?> property) {
        List<Comparable> values = new ArrayList<Comparable>((java.util.Collection)property.getAllowedValues());
        if (values.size() < 2) {
            return state;
        }
        Collections.sort(values, new Comparator<Comparable>() {
            @Override
            public int compare(Comparable first, Comparable second) {
                return String.valueOf(first).compareTo(String.valueOf(second));
            }
        });
        Comparable current = (Comparable)state.getValue(property);
        int index = values.indexOf(current);
        Comparable next = values.get((index + 1 + values.size()) % values.size());
        return state.withProperty((IProperty)property, next);
    }

    private static String getSelectedProperty(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null || !tag.hasKey(SELECTED_PROPERTY, 8) ? "" : tag.getString(SELECTED_PROPERTY);
    }

    private static void setSelectedProperty(ItemStack stack, String property) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setString(SELECTED_PROPERTY, property);
    }
}
