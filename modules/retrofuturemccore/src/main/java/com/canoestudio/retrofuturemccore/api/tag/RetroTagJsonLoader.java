package com.canoestudio.retrofuturemccore.api.tag;

import com.canoestudio.retrofuturemccore.Tags;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RetroTagJsonLoader {
    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME + " Tags");
    private static final JsonParser PARSER = new JsonParser();
    private static boolean loadedActiveModTags;

    private RetroTagJsonLoader() {
    }

    public static synchronized int loadAllActiveModTags() {
        int loaded = 0;
        for (ModContainer container : Loader.instance().getActiveModList()) {
            loaded += loadFromMod(container);
        }
        loadedActiveModTags = true;
        if (loaded > 0) {
            LOGGER.info("Loaded {} RetroFuture tag json file(s).", loaded);
        }
        return loaded;
    }

    public static synchronized boolean hasLoadedActiveModTags() {
        return loadedActiveModTags;
    }

    public static int loadFromMod(ModContainer container) {
        File source = container.getSource();
        if (source == null || !source.exists()) {
            return 0;
        }
        try {
            return source.isDirectory() ? loadFromDirectory(source) : loadFromJar(source);
        } catch (IOException e) {
            LOGGER.warn("Failed to load RetroFuture tags from {}", source, e);
            return 0;
        }
    }

    public static int loadFromDirectory(File source) throws IOException {
        return loadDirectory(source, source);
    }

    private static int loadDirectory(File root, File file) throws IOException {
        if (file.isDirectory()) {
            int loaded = 0;
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    loaded += loadDirectory(root, child);
                }
            }
            return loaded;
        }

        String path = root.toURI().relativize(file.toURI()).getPath();
        TagFile tagFile = parseTagPath(path);
        if (tagFile == null) {
            return 0;
        }

        InputStream stream = new FileInputStream(file);
        try {
            loadTagJson(tagFile, stream);
            return 1;
        } finally {
            stream.close();
        }
    }

    public static int loadFromJar(File source) throws IOException {
        ZipFile zip = new ZipFile(source);
        try {
            int loaded = 0;
            java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                TagFile tagFile = parseTagPath(entry.getName());
                if (tagFile == null) {
                    continue;
                }
                InputStream stream = zip.getInputStream(entry);
                try {
                    loadTagJson(tagFile, stream);
                    loaded++;
                } finally {
                    stream.close();
                }
            }
            return loaded;
        } finally {
            zip.close();
        }
    }

    private static TagFile parseTagPath(String rawPath) {
        String path = rawPath.replace('\\', '/');
        if (!path.endsWith(".json")) {
            return null;
        }

        if (path.startsWith("data/")) {
            return parseDataTagPath(path);
        }
        if (path.startsWith("assets/")) {
            return parseAssetTagPath(path);
        }
        return null;
    }

    private static TagFile parseDataTagPath(String path) {
        String[] parts = path.split("/");
        if (parts.length < 5 || !"tags".equals(parts[2])) {
            return null;
        }

        RetroTagDomain domain = parseDomain(parts[3]);
        if (domain == null) {
            return null;
        }

        String tagPath = removeJson(join(parts, 4));
        return new TagFile(domain, new ResourceLocation(parts[1], tagPath));
    }

    private static TagFile parseAssetTagPath(String path) {
        String[] parts = path.split("/");
        if (parts.length < 6 || !"retrofuturemccore".equals(parts[2]) || !"tags".equals(parts[3])) {
            return null;
        }

        RetroTagDomain domain = parseDomain(parts[4]);
        if (domain == null) {
            return null;
        }

        String tagPath = removeJson(join(parts, 5));
        return new TagFile(domain, new ResourceLocation(parts[1], tagPath));
    }

    private static RetroTagDomain parseDomain(String folder) {
        if ("items".equals(folder) || "item".equals(folder)) {
            return RetroTagDomain.ITEM;
        }
        if ("blocks".equals(folder) || "block".equals(folder)) {
            return RetroTagDomain.BLOCK;
        }
        if ("entity_types".equals(folder) || "entities".equals(folder) || "entity".equals(folder)) {
            return RetroTagDomain.ENTITY;
        }
        if ("game_events".equals(folder) || "game_event".equals(folder)) {
            return RetroTagDomain.GAME_EVENT;
        }
        return null;
    }

    private static void loadTagJson(TagFile tagFile, InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        JsonObject json = PARSER.parse(reader).getAsJsonObject();
        RetroTagKey<?> key = createKey(tagFile.domain, tagFile.id);

        if (json.has("replace") && json.get("replace").getAsBoolean()) {
            clear(key);
        }

        if (!json.has("values") || !json.get("values").isJsonArray()) {
            return;
        }

        JsonArray values = json.getAsJsonArray("values");
        for (JsonElement value : values) {
            String id = parseValueId(value);
            if (id == null || id.isEmpty()) {
                continue;
            }
            addTagValue(key, tagFile.domain, id);
        }
    }

    private static String parseValueId(JsonElement value) {
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if (object.has("id")) {
                return object.get("id").getAsString();
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void clear(RetroTagKey<?> key) {
        RetroTagRegistry.clear((RetroTagKey) key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addTagValue(RetroTagKey<?> key, RetroTagDomain domain, String id) {
        boolean reference = id.charAt(0) == '#';
        ResourceLocation valueId = new ResourceLocation(reference ? id.substring(1) : id);
        if (reference) {
            RetroTagRegistry.addReference((RetroTagKey) key, createKey(domain, valueId));
        } else {
            RetroTagRegistry.addId((RetroTagKey) key, valueId);
        }
    }

    private static RetroTagKey<?> createKey(RetroTagDomain domain, ResourceLocation id) {
        if (domain == RetroTagDomain.ITEM) {
            return RetroTagKey.<Item>of(domain, id);
        }
        if (domain == RetroTagDomain.BLOCK) {
            return RetroTagKey.<Block>of(domain, id);
        }
        if (domain == RetroTagDomain.ENTITY) {
            return RetroTagKey.<Class<? extends Entity>>of(domain, id);
        }
        if (domain == RetroTagDomain.GAME_EVENT) {
            return RetroTagKey.<RetroGameEvent>of(domain, id);
        }
        return RetroTagKey.of(domain, id);
    }

    private static String join(String[] parts, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(parts[i]);
        }
        return builder.toString();
    }

    private static String removeJson(String path) {
        return path.substring(0, path.length() - ".json".length());
    }

    private static final class TagFile {
        private final RetroTagDomain domain;
        private final ResourceLocation id;

        private TagFile(RetroTagDomain domain, ResourceLocation id) {
            this.domain = domain;
            this.id = id;
        }
    }
}
