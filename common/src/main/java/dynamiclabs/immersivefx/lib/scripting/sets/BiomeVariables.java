/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.lib.scripting.sets;

import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.biomes.BiomeDictionary;
import dynamiclabs.immersivefx.lib.biomes.BiomeUtilities;
import dynamiclabs.immersivefx.lib.scripting.VariableSet;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class BiomeVariables extends VariableSet<IBiomeVariables> implements IBiomeVariables {

    private static final String DEFAULT_ID = "minecraft:plains";

    @Nullable
    private Holder<Biome> biome;
    private final LazyVariable<Set<BiomeDictionary.Type>> biomeTraits = new LazyVariable<>(() -> this.biome != null ? BiomeDictionary.getTypes(this.biome) : Collections.singleton(BiomeDictionary.Type.PLAINS));
    private final LazyVariable<Set<String>> biomeTraitNames = new LazyVariable<>(() -> this.biomeTraits.get().stream().map(BiomeDictionary.Type::getName).collect(Collectors.toSet()));
    private final LazyVariable<String> traits = new LazyVariable<>(() -> String.join(" ", this.biomeTraitNames.get()));
    private final LazyVariable<String> name = new LazyVariable<>(() -> this.biome != null ? BiomeUtilities.getBiomeName(this.biome) : "Plains");
    private final LazyVariable<String> modid = new LazyVariable<>(() -> getIdString().substring(0, getIdString().indexOf(':')));
    private final LazyVariable<String> id = new LazyVariable<>(this::getIdString);
    private final LazyVariable<String> category = new LazyVariable<>(() -> BiomeDictionary.getCategory(this.biomeTraits.get()));
    private final LazyVariable<String> rainType = new LazyVariable<>(() -> this.biome != null ? BiomeUtilities.getPrecipitationType(this.biome.value()).getSerializedName() : "rain");

    public BiomeVariables() {
        super("biome");
    }

    @Nonnull
    private String getIdString() {
        if (this.biome == null)
            return DEFAULT_ID;
        return this.biome.unwrapKey().map(k -> k.identifier().toString()).orElse(DEFAULT_ID);
    }

    public void setBiome(@Nullable final Holder<Biome> biome) {
        if (this.biome != biome) {
            this.biome = biome;
            reset();
        }
    }

    @Nonnull
    @Override
    public IBiomeVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        Holder<Biome> newBiome = null;
        if (GameUtils.isInGame()) {
            newBiome = BiomeUtilities.getClientBiomeHolder(GameUtils.getPlayer().blockPosition());
        }

        if (newBiome != this.biome) {
            this.biome = newBiome;
            reset();
        }
    }

    private void reset() {
        this.name.reset();
        this.modid.reset();
        this.id.reset();
        this.category.reset();
        this.rainType.reset();
        this.traits.reset();
        this.biomeTraits.reset();
        this.biomeTraitNames.reset();
    }

    @Override
    public String getModId() {
        return this.modid.get();
    }

    @Override
    public String getId() {
        return this.id.get();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public float getRainfall() {
        return this.biome != null ? BiomeUtilities.getRainfall(this.biome.value()) : 0.4F;
    }

    @Override
    public float getTemperature() {
        return this.biome != null ? BiomeUtilities.getTemperature(this.biome.value()) : 0.8F;
    }

    @Override
    public String getCategory() {
        return this.category.get();
    }

    @Override
    public String getRainType() {
        return this.rainType.get();
    }

    @Override
    public String getTraits() {
        return this.traits.get();
    }

    @Override
    public boolean is(@Nonnull final String traitName) {
        return this.biomeTraitNames.get().contains(traitName.toUpperCase(Locale.ROOT));
    }

    public boolean is(@Nonnull final BiomeDictionary.Type type) {
        return this.biomeTraits.get().contains(type);
    }
}
