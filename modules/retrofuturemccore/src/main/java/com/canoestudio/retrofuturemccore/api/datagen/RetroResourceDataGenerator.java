package com.canoestudio.retrofuturemccore.api.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.ResourceLocation;

public final class RetroResourceDataGenerator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RetroResourceDataGenerator() {
    }

    public static Path writeRecipe(Path resourcesRoot, ResourceLocation id, JsonElement json) throws IOException {
        return writeJson(assetPath(resourcesRoot, id, "recipes", ".json"), json);
    }

    public static Path writeLootTable(Path resourcesRoot, ResourceLocation id, JsonElement json) throws IOException {
        return writeJson(assetPath(resourcesRoot, id, "loot_tables", ".json"), json);
    }

    public static Path writeBlockModel(Path resourcesRoot, ResourceLocation id, JsonElement json) throws IOException {
        return writeJson(assetPath(resourcesRoot, id, "models/block", ".json"), json);
    }

    public static Path writeItemModel(Path resourcesRoot, ResourceLocation id, JsonElement json) throws IOException {
        return writeJson(assetPath(resourcesRoot, id, "models/item", ".json"), json);
    }

    public static Path writeBlockstate(Path resourcesRoot, ResourceLocation id, JsonElement json) throws IOException {
        return writeJson(assetPath(resourcesRoot, id, "blockstates", ".json"), json);
    }

    public static Path writeJson(Path file, JsonElement json) throws IOException {
        return writeString(file, GSON.toJson(json) + System.lineSeparator());
    }

    public static Path writeString(Path file, String contents) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    public static Path assetPath(Path resourcesRoot, ResourceLocation id, String folder, String suffix) {
        String relative = id.getPath();
        if (suffix != null && !relative.endsWith(suffix)) {
            relative = relative + suffix;
        }
        return resourcesRoot.resolve("assets")
                .resolve(id.getNamespace())
                .resolve(folder)
                .resolve(relative);
    }
}
