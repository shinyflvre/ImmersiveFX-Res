/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.specialfx;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class SpecialFxConfig {
    public static Options OPTIONS;

    private SpecialFxConfig() {
    }

    public static void build(@Nonnull final ModConfigSpec.Builder builder) {
        OPTIONS = new Options(builder);
    }

    private static String key(final String path) {
        return "immersivefx.configuration." + path;
    }

    public static final class Options {
        public final BooleanValue enabled;
        public final Effect bloodSplatter;
        public final BooleanValue bloodParticles;
        public final IntValue bloodParticleAmount;
        public final Effect crafting;
        public final Effect smelting;
        public final Effect itemDrop;
        public final Effect workstationClicks;
        public final Effect mobSpawn;
        public final BooleanValue mobSpawnSmoke;
        public final Effect hunger;
        public final Effect bed;
        public final Effect chat;
        public final Effect oreBreak;
        public final BooleanValue oreBreakReplace;
        public final Effect stoneBreak;
        public final BooleanValue stoneBreakReplace;
        public final Effect typing;
        public final Effect inventoryOpen;

        Options(@Nonnull final ModConfigSpec.Builder builder) {
            this.enabled = builder
                    .comment("Master switch of all Special FX sounds and particles")
                    .translation(key("enabled"))
                    .define("Enabled", true);

            this.bloodSplatter = new Effect(builder, "bloodSplatter", "Blood Splatter", "Splatter sound when hitting with a sword, axe or pickaxe");
            builder.comment("Blood particles when entities are hurt").translation(key("bloodParticles")).push("Blood Particles");
            this.bloodParticles = builder.translation(key("bloodParticles.enabled")).define("Enabled", true);
            this.bloodParticleAmount = builder
                    .comment("Amount of blood particles in percent")
                    .translation(key("bloodParticles.amount"))
                    .defineInRange("Amount", 100, 0, 200);
            builder.pop();
            this.crafting = new Effect(builder, "crafting", "Crafting", "Sound when an item is crafted");
            this.smelting = new Effect(builder, "smelting", "Smelting", "Sound when an item is taken out of a furnace");
            this.itemDrop = new Effect(builder, "itemDrop", "Item Drop", "Sound when an item is thrown away");
            this.workstationClicks = new Effect(builder, "workstationClicks", "Workstation Clicks",
                    "Sound when using a crafting table, furnace, anvil, loom, stonecutter, smithing table ...");
            final BooleanValue[] smoke = new BooleanValue[1];
            this.mobSpawn = new Effect(builder, "mobSpawn", "Mob Spawn", "Sound when a monster or animal appears",
                    b -> smoke[0] = b.comment("Smoke puff when a monster or animal appears")
                            .translation(key("mobSpawn.smoke"))
                            .define("Smoke", true));
            this.mobSpawnSmoke = smoke[0];
            this.hunger = new Effect(builder, "hunger", "Hunger", "Stomach growl when the food bar is low");
            this.bed = new Effect(builder, "bed", "Bed", "Sound when lying down in a bed");
            this.chat = new Effect(builder, "chat", "Chat", "Sound when a chat message is sent");
            final BooleanValue[] oreReplace = new BooleanValue[1];
            this.oreBreak = new Effect(builder, "oreBreak", "Ore Break", "Sound when an ore block is mined",
                    b -> oreReplace[0] = b.comment("Play it instead of the vanilla break sound")
                            .translation(key("oreBreak.replace"))
                            .define("Replace Vanilla Sound", true));
            this.oreBreakReplace = oreReplace[0];
            final BooleanValue[] stoneReplace = new BooleanValue[1];
            this.stoneBreak = new Effect(builder, "stoneBreak", "Stone Break", "Sound when a stone block is mined",
                    b -> stoneReplace[0] = b.comment("Play it instead of the vanilla break sound")
                            .translation(key("stoneBreak.replace"))
                            .define("Replace Vanilla Sound", true));
            this.stoneBreakReplace = stoneReplace[0];
            this.typing = new Effect(builder, "typing", "Typing", "Click when typing in the chat or any text field");
            this.inventoryOpen = new Effect(builder, "inventoryOpen", "Inventory Open", "Sound when opening your inventory");
        }
    }

    public static final class Effect {
        public final BooleanValue enabled;
        public final IntValue volume;

        Effect(@Nonnull final ModConfigSpec.Builder builder, final String id, final String section, final String comment) {
            this(builder, id, section, comment, b -> {
            });
        }

        Effect(@Nonnull final ModConfigSpec.Builder builder, final String id, final String section, final String comment,
               @Nonnull final Consumer<ModConfigSpec.Builder> extra) {
            builder.comment(comment).translation(key(id)).push(section);
            this.enabled = builder.translation(key(id + ".enabled")).define("Enabled", true);
            this.volume = builder
                    .comment("Volume in percent")
                    .translation(key(id + ".volume"))
                    .defineInRange("Volume", 100, 0, 100);
            extra.accept(builder);
            builder.pop();
        }

        public float factor() {
            if (!OPTIONS.enabled.get() || !this.enabled.get())
                return 0F;
            return this.volume.get() / 100F;
        }
    }
}
