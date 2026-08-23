# RetroFuture Update Aquatic

The 1.13 aquatic-content module for the RetroFuture series, backported to
Minecraft 1.12.2 Forge. Fluidlogged API is optional: the shared core uses it
when installed, and otherwise uses the Farmers-Future-Delight waterlogged
state, variable water-height model and flowing-water implementation.

## Wiki coverage

The implementation is organized against the Update Aquatic feature groups:

| Wiki area | Current implementation |
| --- | --- |
| Water behavior | Fluidlogged-compatible aquatic blocks and water restoration |
| Seafloor life | Kelp, seagrass, tall seagrass, coral blocks, coral fans/plants, sea pickles, dead variants and growth behavior |
| Ocean climate | OE-style noise patches over the native OCEAN/BEACH biomes: warm sand/coral reefs, frozen seafloor, ice sheets and icebergs; optional Buffet climate variants remain available |
| Exploration | Shipwreck templates and loot, ocean ruins, buried treasure, treasure maps, icebergs and blue ice |
| Conduit | Water-only placement, prismarine frame activation, range scaling, conduit power, water breathing, night vision, haste and hostile-target damage |
| Equipment | Trident throwing/melee behavior, loyalty/impaling/riptide/channeling, turtle helmet breathing reserve and phantom-membrane Elytra repair |
| Mobs | Fish variants, dolphins and Dolphin's Grace, turtles/scute, drowned, zombie-to-drowned conversion and phantoms |
| Other parity hooks | Fishing nautilus shells, carved pumpkins, stripped wood, banner map markers, insomnia tracking, slow falling and undead underwater sinking |

## Code layout

Event handling is deliberately split by responsibility:

- `event/AquaticInteractionEvents.java` contains block, item and fishing
  interactions.
- `event/AquaticPlayerEvents.java` contains player ticks, insomnia, slow
  falling, anvil repair and player-only compatibility behavior.
- `event/AquaticEntityEvents.java` contains conduit-derived effects, undead
  conversion, underwater movement and phantom spawning.

World generation, structures, entities, items and blocks remain in their own
packages. The split keeps future Wiki parity work local to the relevant
feature group without changing the existing structure-generation pipeline.

## Reference implementations

- `参考/Oceanic-Expanse-master` was used for aquatic entity behavior, conduit
  effects, turtle-shell behavior, coral/plant behavior and the OCEAN/BEACH
  noise-based world-generation model. OE decorates the vanilla biome layer;
  it does not replace the final GenLayer with randomly selected ocean IDs.
- `参考/phantoms-main` was used for the three-stage phantom AI and standard
  spawn initialization. Its LGPL-2.1 license is retained in that reference
  directory.
- `参考/Future-MC-main` was consulted for 1.12.2 item/entity compatibility
  details. Its README states that the repository is All Rights Reserved, so
  this module does not redistribute its files or claim that code as licensed
  here.
- `参考/Fluidlogged-API-1.12.2-Latest` is an external runtime/compile
  dependency and is intentionally not copied into this module.

The reference projects are adaptation sources, not additional runtime
dependencies. Check their original notices before redistributing any directly
copied implementation.

## Build

Place a compatible Fluidlogged API jar in this module's `libs/` directory only
if you want to test the optional API path; the fallback path needs no extra jar.
Then run:

```text
gradlew.bat :retrofutureupdateaquatic:compileJava
gradlew.bat :retrofutureupdateaquatic:build
```
