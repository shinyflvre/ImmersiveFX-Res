/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.mobeffects.library;

import com.google.gson.reflect.TypeToken;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.resource.IResourceAccessor;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.service.IModuleService;
import dynamiclabs.immersivefx.lib.service.ModuleServiceManager;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.sndctrl.library.Primitives;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import dynamiclabs.immersivefx.platform.event.LevelEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemLibrary {

    // For things that don't make an equip sound - like AIR
    public static final ItemData EMPTY = new ItemData("EMPTY", Constants.NONE);
    public static final ItemData NONE = new ItemData("NONE", Constants.NONE, Constants.NONE, Constants.UTILITY_EQUIP);
    public static final ItemData LEATHER = new ItemData.ArmorItemData("LEATHER", Constants.LEATHER_ARMOR_EQUIP, Constants.LIGHT_ARMOR, Constants.LIGHT_FOOT_ARMOR, 0);
    public static final ItemData CHAIN = new ItemData.ArmorItemData("CHAIN", Constants.CHAIN_ARMOR_EQUIP, Constants.MEDIUM_ARMOR, Constants.MEDIUM_FOOT_ARMOR, 1);
    public static final ItemData CRYSTAL = new ItemData.ArmorItemData("CRYSTAL", Constants.CRYSTAL_ARMOR_EQUIP, Constants.CRYSTAL_ARMOR, Constants.CRYSTAL_FOOT_ARMOR, 2);
    public static final ItemData PLATE = new ItemData.ArmorItemData("PLATE", Constants.PLATE_ARMOR_EQUIP, Constants.HEAVY_ARMOR, Constants.HEAVY_FOOT_ARMOR, 3);
    public static final ItemData SHIELD = new ItemData("SHIELD", Constants.TOOL_SWING, Constants.SHIELD_USE, Constants.SHIELD_EQUIP);
    public static final ItemData SWORD = new ItemData("SWORD", Constants.SWORD_SWING, Constants.NONE, Constants.SWORD_EQUIP);
    public static final ItemData AXE = new ItemData("AXE", Constants.AXE_SWING, Constants.NONE, Constants.AXE_EQUIP);
    public static final ItemData BOW = new ItemData("BOW", Constants.TOOL_SWING, Constants.BOW_PULL, Constants.BOW_EQUIP);
    public static final ItemData CROSSBOW = new ItemData("CROSSBOW", Constants.TOOL_SWING, Constants.BOW_PULL, Constants.BOW_EQUIP);
    public static final ItemData TOOL = new ItemData("TOOL", Constants.TOOL_SWING, Constants.NONE, Constants.TOOL_EQUIP);
    public static final ItemData BOOK = new ItemData("BOOK", Constants.BOOK_EQUIP, Constants.BOOK_EQUIP, Constants.BOOK_EQUIP);
    public static final ItemData POTION = new ItemData("POTION", Constants.POTION_EQUIP, Constants.POTION_EQUIP, Constants.POTION_EQUIP);
    public static final ItemData NETHERITE = new ItemData("NETHERITE", Constants.NETHERITE_ARMOR_EQUIP, Constants.NETHERITE_ARMOR_EQUIP, Constants.NETHERITE_ARMOR_EQUIP);

    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(ItemLibrary.class);
    private static final Object2ReferenceAVLTreeMap<String, ItemData> CACHE = new Object2ReferenceAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
    // Pattern for matching Java class names
    private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    // https://www.regexplanet.com/advanced/java/index.html
    private static final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");
    // Pattern for matching ItemStack names
    private static final Pattern ITEM_PATTERN = Pattern.compile("([\\w\\-]+:[\\w\\.\\-/]+)[:]?(\\d+|\\*)?(\\{.*\\})?");
    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\w\\-]+:[\\w\\.\\-/]+)");
    private static final int SET_CAPACITY = 64;
    private static final int MAP_CAPACITY = 256;
    private static final Map<ItemData, Set<Class<?>>> classMap = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ItemData, List<TagKey<Item>>> tagMap = new Reference2ReferenceOpenHashMap<>();
    private static final Map<Item, ItemData> items = new IdentityHashMap<>(MAP_CAPACITY);
    private static final Map<Item, ItemData> resolved = new IdentityHashMap<>(MAP_CAPACITY);

    private static final Map<String, List<String>> LEGACY_CLASSES = new Object2ObjectOpenHashMap<>();

    static {
        CACHE.put(EMPTY.getName(), EMPTY);
        CACHE.put(NONE.getName(), NONE);
        CACHE.put(LEATHER.getName(), LEATHER);
        CACHE.put(CHAIN.getName(), CHAIN);
        CACHE.put(CRYSTAL.getName(), CRYSTAL);
        CACHE.put(PLATE.getName(), PLATE);
        CACHE.put(SHIELD.getName(), SHIELD);
        CACHE.put(SWORD.getName(), SWORD);
        CACHE.put(AXE.getName(), AXE);
        CACHE.put(BOW.getName(), BOW);
        CACHE.put(CROSSBOW.getName(), CROSSBOW);
        CACHE.put(TOOL.getName(), TOOL);
        CACHE.put(BOOK.getName(), BOOK);
        CACHE.put(POTION.getName(), POTION);

        LEGACY_CLASSES.put("ShieldItem", List.of("net.minecraft.world.item.ShieldItem"));
        LEGACY_CLASSES.put("AxeItem", List.of("#minecraft:axes"));
        LEGACY_CLASSES.put("PickaxeItem", List.of("#minecraft:pickaxes"));
        LEGACY_CLASSES.put("SwordItem", List.of("#minecraft:swords"));
        LEGACY_CLASSES.put("ShovelItem", List.of("#minecraft:shovels"));
        LEGACY_CLASSES.put("HoeItem", List.of("#minecraft:hoes"));
        LEGACY_CLASSES.put("BowItem", List.of("net.minecraft.world.item.BowItem"));
        LEGACY_CLASSES.put("CrossbowItem", List.of("net.minecraft.world.item.CrossbowItem"));
        LEGACY_CLASSES.put("FishingRodItem", List.of("net.minecraft.world.item.FishingRodItem"));
        LEGACY_CLASSES.put("BookItem", List.of("minecraft:book"));
        LEGACY_CLASSES.put("WritableBookItem", List.of("net.minecraft.world.item.WritableBookItem"));
        LEGACY_CLASSES.put("WrittenBookItem", List.of("net.minecraft.world.item.WrittenBookItem"));
        LEGACY_CLASSES.put("EnchantedBookItem", List.of("minecraft:enchanted_book"));
        LEGACY_CLASSES.put("KnowledgeBookItem", List.of("minecraft:knowledge_book"));
        LEGACY_CLASSES.put("PotionItem", List.of("net.minecraft.world.item.PotionItem"));
    }

    private ItemLibrary() {
    }

    static void initialize() {
        ModuleServiceManager.instance().add(new ItemLibraryService());
        LevelEvents.TAGS_UPDATED.register(event -> resolved.clear());
    }

    static void initFromConfig(@Nonnull final Map<String, List<String>> cfg) {
        for (final Map.Entry<String, List<String>> entry : cfg.entrySet()) {
            process(entry.getValue(), entry.getKey());
        }
    }

    @Nullable
    public static Equippable getArmorEquippable(@Nonnull final Item item) {
        final Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
            return null;
        final ItemAttributeModifiers modifiers = item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null)
            return null;
        for (final ItemAttributeModifiers.Entry e : modifiers.modifiers()) {
            if (e.attribute().is(Attributes.ARMOR))
                return equippable;
        }
        return null;
    }

    private static ItemData resolveClass(@Nonnull final Item item) {
        for (final ItemData ic : CACHE.values()) {
            final Set<Class<?>> itemSet = classMap.get(ic);
            if (itemSet != null && doesBelong(itemSet, item))
                return ic;
            final List<TagKey<Item>> tags = tagMap.get(ic);
            if (tags != null && isTagged(tags, item))
                return ic;
        }

        // See if it is an armor item.  We can use the equip sound from the material entry.
        final Equippable equippable = getArmorEquippable(item);
        if (equippable != null) {
            // Make sure the primitives get created before assigning to an item
            Primitives.getArmorToolbarAcoustic(equippable);
            Primitives.getArmorAccentAcoustic(equippable);
            final Identifier loc = Primitives.createArmorToolbarResource(equippable);
            final Identifier armorLoc = Primitives.createArmorAccentResource(equippable);
            return new ItemData.ArmorItemData("adhoc", loc, armorLoc, armorLoc, 0);
        }

        return NONE;
    }

    private static boolean doesBelong(@Nonnull final Set<Class<?>> itemSet, @Nonnull final Item item) {

        final Class<?> itemClass = item.getClass();

        // If the item is in the collection already, return
        if (itemSet.contains(itemClass))
            return true;

        // Need to iterate to see if an item is a sub-class of an existing
        // item in the list.
        final Optional<Class<?>> result = itemSet.stream().filter(c -> c.isAssignableFrom(itemClass)).findFirst();
        if (result.isPresent()) {
            itemSet.add(itemClass);
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean isTagged(@Nonnull final List<TagKey<Item>> tags, @Nonnull final Item item) {
        for (final TagKey<Item> tag : tags)
            if (item.builtInRegistryHolder().is(tag))
                return true;
        return false;
    }

    private static void process(@Nullable final List<String> itemList, @Nonnull final String itemClass) {
        if (itemList == null || itemList.isEmpty())
            return;

        final ItemData ic = CACHE.get(itemClass);

        for (final String c : itemList)
            processEntry(c, ic, true);
    }

    private static void processEntry(@Nonnull final String c, @Nullable final ItemData ic, final boolean allowLegacy) {
        final Set<Class<?>> theList = ic == null ? null : classMap.get(ic);

        // If its not a like match it has to be a concrete item
        Matcher match = ITEM_PATTERN.matcher(c);
        if (match.matches()) {
            final String itemName = match.group(1);
            final Identifier id = Identifier.tryParse(itemName);
            if (id != null) {
                final Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
                if (item.isPresent()) {
                    items.put(item.get(), ic);
                } else {
                    LOGGER.warn("Cannot locate item [%s] for ItemRegistry", c);
                }
            } else {
                LOGGER.warn("Item specification [%s] is not valid", itemName);
            }
            return;
        }

        match = TAG_PATTERN.matcher(c);
        if (match.matches()) {
            final Identifier id = Identifier.tryParse(match.group(1));
            if (id != null && ic != null) {
                tagMap.computeIfAbsent(ic, k -> new ArrayList<>()).add(TagKey.create(Registries.ITEM, id));
            } else {
                LOGGER.warn("Item tag specification [%s] is not valid", c);
            }
            return;
        }

        match = FQCN.matcher(c);
        if (match.matches()) {
            try {
                // If we don't have an Item assume its a class name. If it is an item
                // we want that class.
                final Class<?> clazz = Class.forName(c, false, ItemLibrary.class.getClassLoader());
                if (theList != null)
                    theList.add(clazz);
            } catch (@Nonnull final ClassNotFoundException e) {
                final List<String> replacement = allowLegacy ? legacyReplacement(c) : null;
                if (replacement != null) {
                    LOGGER.debug("Item class '%s' does not exist any more; using %s", c, replacement);
                    for (final String r : replacement)
                        processEntry(r, ic, false);
                } else {
                    LOGGER.warn("Cannot locate class '%s' for ItemRegistry", c);
                }
            }
        } else {
            LOGGER.warn("Unrecognized pattern '%s' for ItemRegistry", c);
        }
    }

    @Nullable
    private static List<String> legacyReplacement(@Nonnull final String className) {
        if (!className.startsWith("net.minecraft."))
            return null;
        final String simpleName = className.substring(className.lastIndexOf('.') + 1);
        final List<String> result = LEGACY_CLASSES.get(simpleName);
        if (result != null)
            return result;
        final String moved = "net.minecraft.world.item." + simpleName;
        try {
            Class.forName(moved, false, ItemLibrary.class.getClassLoader());
            return List.of(moved);
        } catch (@Nonnull final ClassNotFoundException ignore) {
            return null;
        }
    }

    @Nonnull
    public static ItemData getItemData(@Nonnull final ItemStack stack) {
        if (stack.isEmpty())
            return NONE;

        final Item item = stack.getItem();
        ItemData data = items.get(item);
        if (data == null) {
            data = resolved.get(item);
            if (data == null)
                resolved.put(item, data = resolveClass(item));
        }
        return data;
    }

    private static class ItemLibraryService implements IModuleService {

        private static final Type itemConfigType = TypeToken.getParameterized(List.class, String.class).getType();
        private static final Type entityConfigType = TypeToken.getParameterized(Map.class, String.class, itemConfigType).getType();

        static {
            // TODO: How do we do the validation?
        }

        @Override
        public String name() {
            return "ItemLibrary";
        }

        @Override
        public void start() {

            for (final ItemData ic : CACHE.values())
                classMap.put(ic, new ReferenceOpenHashSet<>(SET_CAPACITY));

            final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "armor_accents.json");

            IResourceAccessor.process(configs, accessor -> initFromConfig(accessor.as(entityConfigType)));

            resolved.clear();
        }

        @Override
        public void stop() {
            items.clear();
            resolved.clear();
            tagMap.clear();
        }
    }
}
