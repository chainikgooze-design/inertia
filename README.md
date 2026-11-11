# Armor Weight: Equipment Encumbrance System

A production-grade, event-driven equipment encumbrance framework for Minecraft 1.21.1 (NeoForge). The mod provides a modular calculation model translating equipped armor defense, toughness, and custom item overrides into physical weight (Encumbrance). It dynamically applies mobility penalties and exposes a synchronized entity attribute designed as an authoritative foundation for client-side physical movement inertia.

---

## Technical Overview

* **Target Version:** Minecraft 1.21.1
* **Mod Loader:** NeoForge (v21.1.84+)
* **Java Toolchain:** Java 21 (LTS)
* **Design Philosophy:** Zero per-tick inventory polling, pure reactive event pipelines, standard entity attribute synchronization, and strict separation of concerns.

---

## Architecture

The mod is engineered around performance predictability and extensible physics integration:

```
[Equipment Change Event] ──> [WeightCalculator] ──> [ModAttributes.ENCUMBRANCE]
                                                          │
                    ┌─────────────────────────────────────┴─────────────────────────────────────┐
                    ▼                                                                           ▼
       [Attributes.MOVEMENT_SPEED]                                                 [Attributes.JUMP_STRENGTH]
       (AttributeModifier: -Speed)                                                 (AttributeModifier: -Jump)
                    │
                    ▼ (S2C Sync)
         [ClientboundUpdateAttributesPacket]
                    │
         ┌──────────┴──────────┐
         ▼                     ▼
   [HUD Overlay]     [Movement Inertia API]
```

### Encumbrance Attribute as an Inertia Physics API

Vanilla Minecraft recalculates inventory state inefficiently if polled every client or server tick. To overcome this limitation:

* A custom `Attribute` (`armor_weight:encumbrance`) is registered via `DeferredRegister` with `setSyncable(true)`.
* When a player equips or unequips armor, `LivingEquipmentChangeEvent` triggers recalculation **strictly on the logical server**.
* The computed weight is written to the entity's base attribute value (`AttributeInstance.setBaseValue`).
* Minecraft's networking engine automatically replicates the updated attribute value to tracking clients using standard `ClientboundUpdateAttributesPacket` payloads.
* **Inertia Foundation:** External movement physics systems, client-side camera/inertia tick loops, and the HUD overlay query `player.getAttributeValue(ModAttributes.ENCUMBRANCE)` with $O(1)$ constant-time complexity. No inventory iteration, NBT deserialization, or component reflection occurs during render or movement ticks.

---

## Mathematical Model

The total encumbrance of an entity ($W_{total}$) is defined as:

$$W_{total} = \sum_{s \in \text{ArmorSlots}} W(s)$$

Where the weight of an individual slot $W(s)$ is resolved via:

$$W(s) = \begin{cases} 
O(i), & \text{if item } i \text{ has explicit config override} \\
(A_i \times k_a) + (T_i \times k_t) + W_{base}, & \text{otherwise}
\end{cases}$$

* $A_i$: Armor Defense points from item attribute modifiers or components.
* $T_i$: Armor Toughness points from item attribute modifiers or components.
* $k_a$: `armorWeightMultiplier` (default: `1.5`)
* $k_t$: `toughnessWeightMultiplier` (default: `3.0`)
* $W_{base}$: `basePieceWeight` (default: `0.5`)
* $O(i)$: Manual override mapped from `itemOverrides` in TOML config.

### Mobility Penalty Scaling

Penalties are converted into negative additive base multipliers:

$$\text{Speed Penalty} = -\min(W_{total} \times \lambda_{speed}, P_{speed}^{max})$$

$$\text{Jump Penalty} = -\min(W_{total} \times \lambda_{jump}, P_{jump}^{max})$$

* $\lambda_{speed}$: `speedPenaltyPerUnit` (default: `0.005` or `0.5%` per weight unit)
* $P_{speed}^{max}$: `maxSpeedPenalty` (default: `0.70` or `70%` maximum penalty)
* $\lambda_{jump}$: `jumpPenaltyPerUnit` (default: `0.003` or `0.3%` per weight unit)
* $P_{jump}^{max}$: `maxJumpPenalty` (default: `0.50` or `50%` maximum penalty)

Modifiers are injected using `AttributeModifier.Operation.ADD_MULTIPLIED_BASE`. When encumbrance reaches zero, modifiers are cleanly purged to prevent lingering attribute clutter.

---

## Package Architecture

```
net.armorweight
├── ArmorWeightMod.java                  # Main entry point and lifecycle orchestration
├── api
│   ├── EncumbranceData.java             # Immutable record encapsulating weight and penalties
│   └── IEncumbranceProvider.java        # Extensible service interface for external integration
├── client
│   ├── ClientModEvents.java             # Client-side mod bus event subscriber
│   └── EncumbranceHudOverlay.java       # Hardware-accelerated text HUD layer
├── command
│   └── ArmorWeightCommand.java          # Brigadier command: /armorweight check [player]
├── config
│   ├── WeightClientConfig.java          # Client-specific display and layout preferences
│   └── WeightServerConfig.java          # Server-authoritative mechanics, rates, and overrides
├── event
│   ├── EquipmentChangeEventHandler.java # Reactive LivingEquipmentChangeEvent listener
│   ├── ModBusEventHandler.java          # Entity attribute injection and config lifecycle
│   └── PlayerLifecycleEventHandler.java # Connection, respawn, and dimension synchronization
├── registry
│   └── ModAttributes.java               # DeferredRegister for armor_weight:encumbrance
└── util
    └── WeightCalculator.java            # Stateless computational core and attribute modifier logic
```

---

## Configuration Reference

### Server Configuration (`config/armor_weight-server.toml`)

| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `calculation.armorWeightMultiplier` | Double | `1.5` | Weight contributed per unit of armor defense |
| `calculation.toughnessWeightMultiplier` | Double | `3.0` | Weight contributed per unit of armor toughness |
| `calculation.basePieceWeight` | Double | `0.5` | Flat weight applied per non-empty armor slot |
| `penalties.speedPenaltyPerUnit` | Double | `0.005` | Fraction of movement speed deducted per kg/unit |
| `penalties.maxSpeedPenalty` | Double | `0.70` | Upper bound for speed penalty deduction (70%) |
| `penalties.jumpPenaltyPerUnit` | Double | `0.003` | Fraction of jump strength deducted per kg/unit |
| `penalties.maxJumpPenalty` | Double | `0.50` | Upper bound for jump strength deduction (50%) |
| `overrides.itemOverrides` | List | (see TOML) | Custom overrides using format `"namespace:item=weight"` |

### Client Configuration (`config/armor_weight-client.toml`)

| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `hud.enableHud` | Boolean | `true` | Toggle hotbar encumbrance HUD overlay |
| `hud.hudXOffset` | Integer | `0` | Horizontal pixel offset from screen center |
| `hud.hudYOffset` | Integer | `-52` | Vertical pixel offset relative to screen bottom |
| `hud.displayUnit` | String | `"kg"` | Metric unit suffix rendered on the HUD |
| `hud.showPenaltyInHud` | Boolean | `true` | Appends speed reduction percentage to HUD text |
| `hud.showWhenZero` | Boolean | `false` | If true, renders HUD even when unencumbered |
| `hud.textColor` | Integer | `0xFFE0E0E0` | ARGB integer color for normal encumbrance |
| `hud.heavyTextColor` | Integer | `0xFFFF5555` | ARGB integer color for high encumbrance (≥25%) |

---

## Commands

### `/armorweight check`
Displays the executing player's current encumbrance, speed penalty percentage, jump penalty percentage, and total equipped armor defense and toughness.

### `/armorweight check <player>`
Inspects another player's encumbrance metrics. Requires operator permission level 2.

---

## Performance Characteristics

1. **Zero Per-Tick Polling:** Weight evaluation only triggers when armor slots change (`LivingEquipmentChangeEvent`), on login, respawn, or dimension switch.
2. **Synchronized Client Mirroring:** The client reads the synchronized vanilla `AttributeInstance` value directly. No network packets are dispatched other than vanilla attribute broadcasts.
3. **Optimized Override Parsing:** String parsing of `itemOverrides` happens once during config load/reload and is indexed into an unmodifiable hash table (`Map<ResourceLocation, Double>`) for instantaneous key lookups.
4. **Data-Component Aware:** Leverages Minecraft 1.21's `ItemAttributeModifiers` component pipeline, seamlessly supporting both vanilla and third-party custom armor without hardcoded item checks.
