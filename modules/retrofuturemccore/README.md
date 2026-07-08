# RetroFutureMC Core

RetroFutureMC Core is a small 1.12.2 compatibility/API layer for RetroFuture-series mods. Its goal is to provide modern-version-style hooks without requiring every content mod to use ASM or Mixins.

## Dependency

In another module:

```groovy
dependencies {
    implementation project(':retrofuturemccore')
}
```

In the mod annotation:

```java
@Mod(
    modid = "examplemod",
    dependencies = "required-after:retrofuturemccore@[1.0.0,)"
)
```

## Data Tags

Use modern-style tag JSON files:

```text
src/main/resources/data/examplemod/tags/items/zoom_items.json
src/main/resources/data/examplemod/tags/blocks/copper_blocks.json
src/main/resources/data/examplemod/tags/entity_types/axolotl_hunt_targets.json
src/main/resources/data/examplemod/tags/game_events/vibrations.json
```

Example:

```json
{
  "replace": false,
  "values": [
    "examplemod:spyglass",
    "#retrofuturemccore:zoom_items"
  ]
}
```

Core loads tag JSON during `postInit`, after registries are ready. You can also register tags in code:

```java
RetroTagRegistry.addValue(RetroTags.ZOOM_ITEMS, ModItems.SPYGLASS);
RetroTagRegistry.addId(RetroTags.COPPER_BLOCKS, new ResourceLocation("examplemod", "copper_block"));
```

Check tags:

```java
boolean zoom = RetroTagRegistry.containsItem(RetroTags.ZOOM_ITEMS, stack.getItem());
boolean target = RetroTagRegistry.containsEntity(RetroTags.AXOLOTL_HUNT_TARGETS, entity);
```

## Item Use, Cooldown, Zoom

Register item right-click behavior without editing the item class:

```java
RetroUseItemRegistry.register(ModItems.GOAT_HORN, new AbstractRetroUseItem() {
    @Override
    public boolean matches(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (RetroCooldowns.hasCooldown(player, stack)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        RetroCooldowns.setCooldown(player, stack, 140);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }
});
```

Client zoom:

```java
RetroZoomRegistry.register(ModItems.SPYGLASS, new AbstractRetroZoomHandler() {
    @Override
    public boolean isZooming(EntityPlayer player, ItemStack stack) {
        return player.isHandActive() && player.getActiveItemStack() == stack;
    }
});
```

Register zoom handlers only on the client side if they reference client-only classes.

## Fluid And Water-Compatible Blocks

Use `RetroWaterlogging` for waterlogged blocks and for custom blocks that deliberately report `Material.WATER`.
Forge 1.12.2's vanilla water movement code reads `BlockLiquid.LEVEL` from every `Material.WATER` state, so water-like plant blocks must include that property even when their own metadata does not store it:

```java
public MyWaterPlantBlock() {
    super(Material.WATER);
    this.setDefaultState(RetroWaterlogging.withStillWaterLevel(this.blockState.getBaseState()
        .withProperty(TYPE, 0)));
}

@Override
protected BlockStateContainer createBlockState() {
    return RetroWaterlogging.createWaterMaterialStateContainer(this, TYPE);
}
```

On the client, hide the synthetic liquid level from blockstate model lookup:

```java
@Override
public void preInit() {
    super.preInit();
    RetroModelRegistry.ignoreLiquidLevel(ModBlocks.SEAGRASS, ModBlocks.KELP);
}
```

Waterlogged blocks should keep their normal block material and use `RetroWaterloggedBlock` plus `RetroWaterlogging` helpers for placement, restore, and Fluidlogged API bridging. Do not return `Material.WATER` from `getMaterial` unless the state includes the still-water level property.

## Interactions

Low-level event bridge:

```java
RetroEventRegistry.registerBlockInteraction(new AbstractRetroBlockInteractionHandler() {
    @Override
    public RetroEventResult onRightClickBlock(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing face, Vec3d hitVec) {
        return RetroEventResult.PASS;
    }
});
```

DSL style:

```java
RetroInteractions.block()
    .blockTag(RetroTags.COPPER_BLOCKS)
    .item(Items.HONEYCOMB)
    .onUse(interaction -> {
        // wax copper here
        return RetroEventResult.SUCCESS;
    })
    .register();
```

Entity interaction:

```java
RetroInteractions.entity()
    .entityTag(RetroTags.AXOLOTL_HUNT_TARGETS)
    .item(Items.WATER_BUCKET)
    .onUse(interaction -> RetroEventResult.SUCCESS)
    .register();
```

## Entity Lifecycle And Drops

```java
RetroEventRegistry.registerEntityLifecycle(new AbstractRetroEntityLifecycleHandler() {
    @Override
    public void onLivingUpdate(World world, EntityLivingBase entity) {
    }

    @Override
    public boolean onLivingHurt(World world, EntityLivingBase entity, DamageSource source, MutableFloat amount) {
        amount.set(amount.get() * 0.5F);
        return false;
    }
});
```

Drops:

```java
RetroEventRegistry.registerDrops(new AbstractRetroDropHandler() {
    @Override
    public boolean onLivingDrops(World world, EntityLivingBase entity, DamageSource source,
            List<EntityItem> drops, int lootingLevel, boolean recentlyHit) {
        return false;
    }
});
```

Return `true` from hurt/death/drop handlers to cancel the original Forge event.

## Block Families, Wood Sets, Signs And Boats

Core includes 26.2-inspired family metadata for keeping wood, stone, sign, hanging sign, boat, and chest boat registration consistent across RetroFuture modules:

```java
RetroBlockFamily mangroveFamily = RetroBlockFamilies.register(
    RetroBlockFamily.builder(ModBlocks.MANGROVE_PLANKS)
        .log(ModBlocks.MANGROVE_LOG)
        .strippedLog(ModBlocks.STRIPPED_MANGROVE_LOG)
        .stairs(ModBlocks.MANGROVE_STAIRS)
        .slab(ModBlocks.MANGROVE_SLAB)
        .fence(ModBlocks.MANGROVE_FENCE)
        .fenceGate(ModBlocks.MANGROVE_FENCE_GATE)
        .door(ModBlocks.MANGROVE_DOOR)
        .trapdoor(ModBlocks.MANGROVE_TRAPDOOR)
        .button(ModBlocks.MANGROVE_BUTTON)
        .pressurePlate(ModBlocks.MANGROVE_PRESSURE_PLATE)
        .hangingSign(ModBlocks.MANGROVE_HANGING_SIGN, ModBlocks.MANGROVE_WALL_HANGING_SIGN)
        .build()
);
```

For full wood sets:

```java
RetroWoodSet.builder(new ResourceLocation("examplemod", "mangrove"), ModBlocks.MANGROVE_PLANKS)
    .log(ModBlocks.MANGROVE_LOG)
    .strippedLog(ModBlocks.STRIPPED_MANGROVE_LOG)
    .signs(RetroSignSet.builder(new ResourceLocation("examplemod", "mangrove"))
        .hangingSign(ModBlocks.MANGROVE_HANGING_SIGN, ModBlocks.MANGROVE_WALL_HANGING_SIGN,
            ModItems.MANGROVE_HANGING_SIGN)
        .hangingSignTile(TileEntityHangingSign.class)
        .texture(new ResourceLocation("examplemod", "textures/blocks/mangrove_hanging_sign.png"))
        .build())
    .boats(RetroBoatSet.builder(new ResourceLocation("examplemod", "mangrove"))
        .boat(ModItems.MANGROVE_BOAT, EntityMangroveBoat.class)
        .chestBoat(ModItems.MANGROVE_CHEST_BOAT, EntityMangroveChestBoat.class)
        .texture(new ResourceLocation("examplemod", "textures/entity/boat/mangrove.png"))
        .build())
    .register();
```

Use `RetroBlockRegistration.registerBlocks(...)` and `registerSimpleBlockItems(...)` inside Forge registry events when your blocks are normal `ItemBlock`s. Doors, signs, and custom hanging signs usually still need module-specific item classes.

Tile entity registration can stay behind the sign API:

```java
RetroSignRegistry.registerTileEntities(ModWoodSets.MANGROVE_SIGNS);
```

Boat entity registration can use the same entity helper defaults:

```java
RetroBoatRegistry.registerBoatEntity(event.getRegistry(), EntityMangroveBoat.class,
    new ResourceLocation("examplemod", "mangrove_boat"), nextEntityId++, EntityMangroveBoat::new);
```

## Recipes, Loot And Resource Data

The datagen helpers create 1.12.2 JSON resources from modern-style recipes. They are meant for build tools or small module-side generators, not for changing recipes at runtime.

```java
JsonObject recipe = RetroRecipeJsonBuilder.hangingSign(
    ModBlocks.STRIPPED_MANGROVE_LOG,
    Items.IRON_BARS,
    ModItems.MANGROVE_HANGING_SIGN
);

RetroResourceDataGenerator.writeRecipe(resourcesRoot,
    new ResourceLocation("examplemod", "mangrove_hanging_sign"), recipe);
```

Common helpers cover modern wood recipes such as button, door, fence, fence gate, sign, hanging sign, slab, stairs, trapdoor, boat, and chest boat. `RetroLootTableBuilder.selfDrop(block)` is available for simple block loot tables.

## Structures And Biome Generation

`RetroStructurePlacement` gives 1.12 `IWorldGenerator` code a modern spacing/separation/salt placement check:

```java
private static final RetroStructurePlacement ANCIENT_CITY = RetroAncientCityPlacement
    .builder(new ResourceLocation("examplemod", "ancient_city"))
    .biome(biome -> RetroWorldgenRegistry.hasAnyType(biome, BiomeDictionary.Type.SPOOKY))
    .build();

if (ANCIENT_CITY.shouldStartAt(world, chunkX, chunkZ)) {
    Random cityRandom = ANCIENT_CITY.createChunkRandom(world, chunkX, chunkZ);
    BlockPos start = RetroAncientCityPlacement.findDeepStart(world, cityRandom, chunkX << 4, chunkZ << 4,
        18, state -> state.getBlock() == ModBlocks.DEEPSLATE);
}
```

This does not clone modern jigsaw structure generation. It provides stable high-version-like placement inputs for content modules that build equivalent 1.12 structures.

Biome and spawn helpers wrap common Forge 1.12 calls:

```java
RetroBiomeSpawnRegistry.registerBiome(ModBiomes.MANGROVE_SWAMP, BiomeManager.BiomeType.WARM, 7, true,
    BiomeDictionary.Type.SWAMP,
    BiomeDictionary.Type.WET,
    BiomeDictionary.Type.HOT,
    BiomeDictionary.Type.DENSE);

RetroBiomeSpawnRegistry.addSpawnToTypes(EntityFrog.class, EnumCreatureType.CREATURE, 12, 2, 5,
    BiomeDictionary.Type.SWAMP,
    BiomeDictionary.Type.WET);
```

## Entity Helpers

Entity registration:

```java
RetroEntityRegistry.builder(EntityGoat.class, new ResourceLocation("examplemod", "goat"), 1)
    .name("goat")
    .tracker(96, 3, true)
    .egg(0xa58d74, 0xd7c69a)
    .register(event.getRegistry());
```

Spawn registration:

```java
RetroEntityRegistry.addSpawn(EntityGoat.class, EnumCreatureType.CREATURE, 8, 2, 4,
    Biomes.EXTREME_HILLS,
    Biomes.EXTREME_HILLS_WITH_TREES
);
```

Safe attribute assignment:

```java
RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.ATTACK_DAMAGE, 2.0D);
RetroEntityAttributes.setBaseValue(this, SharedMonsterAttributes.MAX_HEALTH, 14.0D);
```

`RetroEntityAttributes` registers missing attributes when needed, useful for 1.12 entities where newer behavior expects attributes such as attack damage.

## Client Models And Renderers

Use `RetroModelRegistry` from your client proxy or `ModelRegistryEvent` subscriber. It wraps Forge 1.12.2's `ModelLoader` and `RenderingRegistry` APIs behind a smaller modern-style facade:

```java
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = "examplemod")
public final class ClientModelEvents {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        RetroModelRegistry.registerItems(ModItems.GOAT_HORN, ModItems.SPYGLASS);
        RetroModelRegistry.registerBlockItems(ModBlocks.COPPER_BLOCK, ModBlocks.LIGHTNING_ROD);
    }
}
```

Custom metadata or model paths are supported:

```java
RetroModelRegistry.registerItem(ModItems.GOAT_HORN, 1,
    new ResourceLocation("examplemod", "goat_horn_sing"), "inventory");
```

Register complex renderers through the same facade:

```java
@Override
public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    RetroModelRegistry.registerEntityRenderer(EntityGoat.class, RenderGoat::new);
}
```

Tile entity renderers can also stay behind the core facade:

```java
RetroModelRegistry.registerTileEntityRenderer(TileEntityHangingSign.class, new RenderHangingSign());
```

For simple living entities, core can create a 1.12 `RenderLiving` for you:

```java
RetroModelRegistry.registerLivingRenderer(
    EntityTadpole.class,
    ModelTadpole::new,
    new ResourceLocation("examplemod", "textures/entity/tadpole/tadpole.png"),
    0.14F
);
```

Variant textures and small pre-render transforms can stay inline:

```java
RetroModelRegistry.registerLivingRenderer(
    EntityFrog.class,
    ModelFrog::new,
    frog -> new ResourceLocation("examplemod", "textures/entity/frog/frog_" + frog.getVariantName() + ".png"),
    0.3F,
    context -> {
        if (context.getEntity().isChild()) {
            GlStateManager.scale(0.55F, 0.55F, 0.55F);
        }
    }
);
```

This is not a direct port of modern `ModelLayer` / baked model internals. It is a 1.12.2-safe compatibility layer that keeps client-only rendering code in client entry points while removing repeated boilerplate from content mods.

## Components

Register and attach components:

```java
RetroComponentType<MyComponent> TYPE = RetroComponentRegistry.register(
    new ResourceLocation("examplemod", "my_component"),
    MyComponent.class,
    MyComponent::new
);

RetroComponentRegistry.attachToEntity(EntityPlayer.class, TYPE);
```

Use `AbstractSyncedComponent` for entity data that should sync to clients. Core handles capability attachment and network sync.

## Brain Lite

Core includes a small behavior/memory/sensor layer inspired by modern Minecraft Brain AI:

```java
Brain<EntityLivingBase> brain = BrainProviderRegistry.getOrCreate(entity);
brain.setMemory(MyMemories.TARGET, target, 100);
```

Use `EntityAIBrainTask` to tick a `Brain` from a vanilla 1.12 `EntityAIBase`.

## Game Events And Vibration

Emit game events:

```java
RetroGameEvents.emit(world, RetroGameEvent.INSTRUMENT_PLAY, player);
```

Listen with vibration helpers:

```java
VibrationListener listener = new VibrationListener(user);
RetroGameEvents.registerListener(world, listener);
```

This is intended for sculk-like systems, vibration listeners, and future mob sensors.

## Notes

- Core APIs are Java 8 compatible.
- Client-only APIs such as zoom overlays should be registered from a client proxy.
- Data-driven tags are loaded in `postInit`; code that depends on JSON tag contents should run after that point.
