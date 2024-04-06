package com.buuz135.functionalstorage.recipe;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CustomCompactingRecipe implements Recipe<Container> {
    public static final Codec<CustomCompactingRecipe> CODEC = RecordCodecBuilder.create(in -> in.group(
            ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("lower_input").forGetter(CustomCompactingRecipe::getLower_input),
            ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("higher_input").forGetter(CustomCompactingRecipe::getHigher_input)
    ).apply(in, CustomCompactingRecipe::new));

    public static List<CustomCompactingRecipe> RECIPES = new ArrayList<>();

    public ItemStack lower_input = ItemStack.EMPTY;
    public ItemStack higher_input = ItemStack.EMPTY;

    public CustomCompactingRecipe() {
    }

    public CustomCompactingRecipe(ItemStack lower_input, ItemStack higher_input) {
        this.lower_input = lower_input;
        this.higher_input = higher_input;
        RECIPES.add(this);
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FunctionalStorage.CUSTOM_COMPACTING_RECIPE_SERIALIZER.value();
    }

    @Override
    public RecipeType<?> getType() {
        return FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.value();
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, this, null);
    }

    public void save(RecipeOutput output) {
        save(output, BuiltInRegistries.ITEM.getKey(higher_input.getItem()));
    }


    public ItemStack getLower_input() {
        return lower_input;
    }

    public ItemStack getHigher_input() {
        return higher_input;
    }
}
