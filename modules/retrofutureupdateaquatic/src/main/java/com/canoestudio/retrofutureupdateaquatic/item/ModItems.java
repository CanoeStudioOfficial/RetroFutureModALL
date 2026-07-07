package com.canoestudio.retrofutureupdateaquatic.item;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.entity.AquaticFishType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class ModItems {

    public static final Item DRIED_KELP = new ItemFood(1, 0.6F, false)
        .setRegistryName(RetroFutureUpdateAquatic.ID, "dried_kelp")
        .setTranslationKey(RetroFutureUpdateAquatic.ID + ".dried_kelp")
        .setCreativeTab(CreativeTabs.FOOD);
    public static final Item NAUTILUS_SHELL = simpleItem("nautilus_shell", CreativeTabs.MATERIALS);
    public static final Item HEART_OF_THE_SEA = simpleItem("heart_of_the_sea", CreativeTabs.MATERIALS);
    public static final Item TRIDENT = new ItemTrident();
    public static final Item COD = foodItem("cod", 2, 0.1F);
    public static final Item SALMON = foodItem("salmon", 2, 0.1F);
    public static final Item PUFFERFISH = foodItem("pufferfish", 1, 0.1F);
    public static final Item TROPICAL_FISH = foodItem("tropical_fish", 1, 0.1F);
    public static final Item COOKED_COD = foodItem("cooked_cod", 5, 0.6F);
    public static final Item COOKED_SALMON = foodItem("cooked_salmon", 6, 0.8F);
    public static final Item SCUTE = simpleItem("scute", CreativeTabs.MATERIALS);
    public static final Item TURTLE_HELMET = new ItemTurtleHelmet();
    public static final Item PHANTOM_MEMBRANE = simpleItem("phantom_membrane", CreativeTabs.MATERIALS);
    public static final Item COD_BUCKET = new ItemFishBucket(AquaticFishType.COD);
    public static final Item SALMON_BUCKET = new ItemFishBucket(AquaticFishType.SALMON);
    public static final Item PUFFERFISH_BUCKET = new ItemFishBucket(AquaticFishType.PUFFERFISH);
    public static final Item TROPICAL_FISH_BUCKET = new ItemFishBucket(AquaticFishType.TROPICAL_FISH);
    private static final Map<AquaticFishType, Item> FISH_BUCKETS = new EnumMap<AquaticFishType, Item>(AquaticFishType.class);

    static {
        FISH_BUCKETS.put(AquaticFishType.COD, COD_BUCKET);
        FISH_BUCKETS.put(AquaticFishType.SALMON, SALMON_BUCKET);
        FISH_BUCKETS.put(AquaticFishType.PUFFERFISH, PUFFERFISH_BUCKET);
        FISH_BUCKETS.put(AquaticFishType.TROPICAL_FISH, TROPICAL_FISH_BUCKET);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(DRIED_KELP, NAUTILUS_SHELL, HEART_OF_THE_SEA, TRIDENT,
            COD, SALMON, PUFFERFISH, TROPICAL_FISH, COOKED_COD, COOKED_SALMON, SCUTE, TURTLE_HELMET,
            PHANTOM_MEMBRANE,
            COD_BUCKET, SALMON_BUCKET, PUFFERFISH_BUCKET, TROPICAL_FISH_BUCKET);
    }

    public static Item getBucket(AquaticFishType fishType) {
        return FISH_BUCKETS.get(fishType);
    }

    private static Item simpleItem(String name, CreativeTabs tab) {
        return new Item()
            .setRegistryName(RetroFutureUpdateAquatic.ID, name)
            .setTranslationKey(RetroFutureUpdateAquatic.ID + "." + name)
            .setCreativeTab(tab);
    }

    private static Item foodItem(String name, int amount, float saturation) {
        return new ItemFood(amount, saturation, false)
            .setRegistryName(RetroFutureUpdateAquatic.ID, name)
            .setTranslationKey(RetroFutureUpdateAquatic.ID + "." + name)
            .setCreativeTab(CreativeTabs.FOOD);
    }

    private ModItems() {
    }
}
