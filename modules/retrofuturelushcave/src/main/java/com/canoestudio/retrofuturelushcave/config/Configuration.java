package com.canoestudio.retrofuturelushcave.config;

import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
public final class Configuration {
    private Configuration() {
    }

    @Config.Name("Cave Generation")
    @Config.Comment("Settings for underground noise caves, aquifers, and canyons.")
    @Config.LangKey("config.retrofuturelushcave.cave_generation")
    public static CaveGeneration CAVE_GENERATION = new CaveGeneration();

    @Config.Name("Lush Caves")
    @Config.Comment("Settings for lush cave climate selection and vegetation features.")
    @Config.LangKey("config.retrofuturelushcave.lush_caves")
    public static LushCaves LUSH_CAVES = new LushCaves();

    @Config.Name("Underground Features")
    @Config.Comment("Settings for dripstone, glow lichen, and underwater magma features.")
    @Config.LangKey("config.retrofuturelushcave.underground_features")
    public static UndergroundFeatures UNDERGROUND_FEATURES = new UndergroundFeatures();

    @Config.Name("Debug")
    @Config.Comment("Print debug information in the log, used for troubleshooting world generation issues.\n")
    @Config.LangKey("config.retrofuturelushcave.debug")
    public static Debug DEBUG = new Debug();

    public static final class CaveGeneration {
        @Config.Name("Surface Entrance Depth")
        @Config.Comment("Keeps large caves deeper underground. Only narrow natural entrances may generate in this upper layer.")
        @Config.LangKey("config.retrofuturelushcave.caves.surface_entrance_depth")
        @Config.RangeInt(min = 1, max = 96)
        @Config.RequiresMcRestart
        public int surfaceEntranceDepth = 48;

        @Config.Name("Surface Entrance Density Threshold")
        @Config.Comment("Lower values make natural cave entrances rarer and smaller.")
        @Config.LangKey("config.retrofuturelushcave.caves.entrance_density_threshold")
        @Config.RangeDouble(min = -1.0D, max = 0.0D)
        @Config.RequiresMcRestart
        public double entranceDensityThreshold = -0.02D;

        @Config.Name("Aquifer Minimum Depth")
        @Config.Comment("Prevents underground water from filling shallow cave entrances. Higher values keep shallow caves dry.")
        @Config.LangKey("config.retrofuturelushcave.caves.aquifer_minimum_depth")
        @Config.RangeInt(min = 0, max = 96)
        @Config.RequiresMcRestart
        public int aquiferMinimumDepth = 16;

        @Config.Name("Surface Fluid Protection Depth")
        @Config.Comment("Keeps extra rock below riverbeds, lakes, and oceans so caves do not break into surface water.")
        @Config.LangKey("config.retrofuturelushcave.caves.surface_fluid_protection_depth")
        @Config.RangeInt(min = 0, max = 96)
        @Config.RequiresMcRestart
        public int surfaceFluidProtectionDepth = 24;

        @Config.Name("Canyon Generation Probability")
        @Config.Comment("Chance for an official-style canyon source to start in each sampled chunk. Large values create many ravines.")
        @Config.LangKey("config.retrofuturelushcave.canyon.probability")
        @Config.RangeDouble(min = 0.0D, max = 0.05D)
        @Config.RequiresMcRestart
        public double canyonProbability = 0.001D;

        @Config.Name("Canyon Path Length")
        @Config.Comment("Maximum number of steps an official-style canyon path can travel from its source.")
        @Config.LangKey("config.retrofuturelushcave.canyon.path_length")
        @Config.RangeInt(min = 8, max = 256)
        @Config.RequiresMcRestart
        public int canyonPathLength = 80;

        @Config.Name("Canyon Vertical Scale")
        @Config.Comment("Controls the vertical size of canyon cuts. Higher values make ravines taller and deeper.")
        @Config.LangKey("config.retrofuturelushcave.canyon.vertical_scale")
        @Config.RangeDouble(min = 0.25D, max = 8.0D)
        @Config.RequiresMcRestart
        public double canyonVerticalScale = 3.0D;
    }

    public static final class LushCaves {
        @Config.Name("Lush Cave Humidity Threshold")
        @Config.Comment("Minimum underground humidity required for a location to belong to a lush cave. Default: 0.70.")
        @Config.LangKey("config.retrofuturelushcave.lush_caves.vanilla_humidity_threshold")
        @Config.RangeDouble(min = -1.0D, max = 1.0D)
        @Config.RequiresMcRestart
        public double vanillaHumidityThreshold = 0.70D;

        @Config.Name("Minimum Underground Depth Parameter")
        @Config.Comment("Lower bound of the underground climate depth interval that can contain lush or dripstone cave regions.")
        @Config.LangKey("config.retrofuturelushcave.lush_caves.minimum_depth")
        @Config.RangeDouble(min = -2.0D, max = 2.0D)
        @Config.RequiresMcRestart
        public double minimumDepth = 0.20D;

        @Config.Name("Maximum Underground Depth Parameter")
        @Config.Comment("Upper bound of the underground climate depth interval that can contain lush or dripstone cave regions.")
        @Config.LangKey("config.retrofuturelushcave.lush_caves.maximum_depth")
        @Config.RangeDouble(min = -2.0D, max = 2.0D)
        @Config.RequiresMcRestart
        public double maximumDepth = 0.90D;

        @Config.Name("Ceiling Vegetation Attempts")
        @Config.Comment("Number of placed-feature candidates per chunk for moss and vegetation on lush-cave ceilings.")
        @Config.LangKey("config.retrofuturelushcave.lush_vegetation.ceiling_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int ceilingVegetationAttempts = 125;

        @Config.Name("Cave Vine Attempts")
        @Config.Comment("Number of placed-feature candidates per chunk for cave-vine columns in lush caves.")
        @Config.LangKey("config.retrofuturelushcave.lush_vegetation.cave_vine_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int caveVineAttempts = 188;

        @Config.Name("Classic Vine Attempts")
        @Config.Comment("Number of placed-feature candidates per chunk for ordinary vines in lush caves.")
        @Config.LangKey("config.retrofuturelushcave.lush_vegetation.classic_vine_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int classicVineAttempts = 256;

        @Config.Name("Clay Feature Attempts")
        @Config.Comment("Number of placed-feature candidates per chunk for clay patches and leaf-covered clay pools in lush caves.")
        @Config.LangKey("config.retrofuturelushcave.lush_vegetation.clay_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int clayAttempts = 62;

        @Config.Name("Floor Vegetation Attempts")
        @Config.Comment("Number of placed-feature candidates per chunk for moss and vegetation on lush-cave floors.")
        @Config.LangKey("config.retrofuturelushcave.lush_vegetation.floor_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int floorVegetationAttempts = 125;

        @Config.Name("Spore Blossom Attempts")
        @Config.Comment("Number of placed-feature candidates per chunk for spore blossoms in lush caves.")
        @Config.LangKey("config.retrofuturelushcave.lush_vegetation.spore_blossom_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int sporeBlossomAttempts = 25;

        @Config.Name("Minimum Azalea Tree Attempts")
        @Config.Comment("Minimum rooted azalea tree-system candidates per chunk. Trees only grow when a valid surface and lush cave connection exist.")
        @Config.LangKey("config.retrofuturelushcave.root_system.minimum_tree_attempts")
        @Config.RangeInt(min = 0, max = 64)
        @Config.RequiresMcRestart
        public int minimumTreeAttempts = 1;

        @Config.Name("Additional Azalea Tree Attempts")
        @Config.Comment("Additional random rooted azalea tree-system candidates per chunk, added to the minimum attempts.")
        @Config.LangKey("config.retrofuturelushcave.root_system.additional_tree_attempts")
        @Config.RangeInt(min = 0, max = 64)
        @Config.RequiresMcRestart
        public int additionalTreeAttempts = 1;

        @Config.Name("Maximum Root Column Height")
        @Config.Comment("Maximum vertical distance that rooted dirt may connect a successful azalea tree to a lush-cave ceiling.")
        @Config.LangKey("config.retrofuturelushcave.root_system.maximum_column_height")
        @Config.RangeInt(min = 1, max = 256)
        @Config.RequiresMcRestart
        public int maximumRootColumnHeight = 100;

        @Config.Name("Hanging Root Attempts")
        @Config.Comment("Number of hanging-root placement candidates below a successful azalea root column.")
        @Config.LangKey("config.retrofuturelushcave.root_system.hanging_root_attempts")
        @Config.RangeInt(min = 0, max = 128)
        @Config.RequiresMcRestart
        public int hangingRootAttempts = 20;
    }

    public static final class UndergroundFeatures {
        @Config.Name("Small Dripstone Cluster Minimum Attempts")
        @Config.Comment("Minimum small dripstone-cluster candidates per chunk before the additional random amount is applied.")
        @Config.LangKey("config.retrofuturelushcave.dripstone.small_cluster_minimum_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int smallDripstoneMinimumAttempts = 48;

        @Config.Name("Small Dripstone Cluster Additional Attempts")
        @Config.Comment("Additional random small dripstone-cluster candidates per chunk.")
        @Config.LangKey("config.retrofuturelushcave.dripstone.small_cluster_additional_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int smallDripstoneAdditionalAttempts = 48;

        @Config.Name("Large Dripstone Minimum Attempts")
        @Config.Comment("Minimum large dripstone candidates per chunk before the additional random amount is applied.")
        @Config.LangKey("config.retrofuturelushcave.dripstone.large_minimum_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int largeDripstoneMinimumAttempts = 10;

        @Config.Name("Large Dripstone Additional Attempts")
        @Config.Comment("Additional random large dripstone candidates per chunk.")
        @Config.LangKey("config.retrofuturelushcave.dripstone.large_additional_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int largeDripstoneAdditionalAttempts = 38;

        @Config.Name("Dripstone Continentalness Threshold")
        @Config.Comment("Minimum underground continentalness required for a location to belong to a dripstone cave region.")
        @Config.LangKey("config.retrofuturelushcave.dripstone.continentalness_threshold")
        @Config.RangeDouble(min = -1.0D, max = 1.0D)
        @Config.RequiresMcRestart
        public double dripstoneContinentalnessThreshold = 0.18D;

        @Config.Name("Glow Lichen Minimum Attempts")
        @Config.Comment("Minimum glow-lichen placed-feature candidates per chunk before the additional random amount is applied.")
        @Config.LangKey("config.retrofuturelushcave.glow_lichen.minimum_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int glowLichenMinimumAttempts = 104;

        @Config.Name("Glow Lichen Additional Attempts")
        @Config.Comment("Additional random glow-lichen placed-feature candidates per chunk.")
        @Config.LangKey("config.retrofuturelushcave.glow_lichen.additional_attempts")
        @Config.RangeInt(min = 0, max = 512)
        @Config.RequiresMcRestart
        public int glowLichenAdditionalAttempts = 53;

        @Config.Name("Glow Lichen Minimum Ocean Floor Depth")
        @Config.Comment("Minimum depth below the ocean floor required before glow lichen may generate.")
        @Config.LangKey("config.retrofuturelushcave.glow_lichen.minimum_ocean_floor_depth")
        @Config.RangeInt(min = 0, max = 96)
        @Config.RequiresMcRestart
        public int glowLichenMinimumOceanFloorDepth = 13;

        @Config.Name("Underwater Magma Minimum Attempts")
        @Config.Comment("Minimum underwater magma candidates per chunk before the additional random amount is applied.")
        @Config.LangKey("config.retrofuturelushcave.underwater_magma.minimum_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int underwaterMagmaMinimumAttempts = 44;

        @Config.Name("Underwater Magma Additional Attempts")
        @Config.Comment("Additional random underwater magma candidates per chunk.")
        @Config.LangKey("config.retrofuturelushcave.underwater_magma.additional_attempts")
        @Config.RangeInt(min = 0, max = 256)
        @Config.RequiresMcRestart
        public int underwaterMagmaAdditionalAttempts = 8;

        @Config.Name("Underwater Magma Placement Probability")
        @Config.Comment("Chance for each valid block in an underwater magma candidate to become a magma block.")
        @Config.LangKey("config.retrofuturelushcave.underwater_magma.placement_probability")
        @Config.RangeDouble(min = 0.0D, max = 1.0D)
        @Config.RequiresMcRestart
        public double underwaterMagmaPlacementProbability = 0.50D;
    }

    public static final class Debug {
        @Config.Name("Enable World Generation Debug")
        @Config.Comment("Writes one aggregate world-generation report after every 128 new chunks. Keep disabled during normal play.")
        @Config.LangKey("config.retrofuturelushcave.debug.enable_worldgen")
        public boolean enableWorldgenDebug = false;
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static final class SyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Tags.MOD_ID.equals(event.getModID())) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
