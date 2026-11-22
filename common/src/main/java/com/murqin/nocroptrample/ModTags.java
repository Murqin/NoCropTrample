package com.murqin.nocroptrample;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

/**
 * Datapack tags recognized by NoCropTrample.
 */
public final class ModTags {

    private ModTags() {
    }

    /**
     * Blocks on top of farmland that count as planted crops in addition to
     * vanilla crop/stem classes and {@code #minecraft:crops}.
     */
    public static final TagKey<Block> PROTECTED_CROPS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "protected_crops"));

    /**
     * Entity types that never trample farmland, overriding mob/empty/fall-distance checks
     * unless also matched by {@link #ALWAYS_TRAMPLE_ENTITIES}.
     */
    public static final TagKey<EntityType<?>> TRAMPLE_EXEMPT_ENTITIES = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "trample_exempt_entities"));

    /**
     * Entity types that always trample farmland regardless of protection rules.
     */
    public static final TagKey<EntityType<?>> ALWAYS_TRAMPLE_ENTITIES = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "always_trample_entities"));
}
