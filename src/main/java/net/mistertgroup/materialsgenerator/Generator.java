package net.mistertgroup.materialsgenerator;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author misterT2525
 */
public class Generator extends JavaPlugin {

    @Override
    @SneakyThrows
    public void onEnable() {
        getDataFolder().mkdirs();
        generate(new File(getDataFolder(), "Materials.java"), "Materials", 4);
    }

    public void generate(File outputFile, String className, int indent) throws IOException {
        String indentString = Strings.repeat(" ", indent);

        Map<Material, String> mapping = findMapping();
        int maxFieldNameLength = mapping.values().stream().max(Comparator.comparing(String::length)).get().length();

        try (PrintWriter writer = new PrintWriter(outputFile)) {

            writer.println("import org.bukkit.Material;");
            writer.println();
            writer.println("public final class " + className + " {");
            writer.println();
            writer.println(indentString + "private " + className + "() {}");
            writer.println();

            findMapping().entrySet().forEach(entry -> {
                String fieldName = entry.getValue().toUpperCase();
                String arrangeFieldName = Strings.repeat(" ", maxFieldNameLength - fieldName.length());
                writer.println(indentString + "public static Material " + fieldName + arrangeFieldName + " = Material." + entry.getKey().name() + ";");
            });

            writer.println("}");
        }
    }

    public Map<Material, String> findMapping() {
        Map<Material, String> map = new TreeMap<>((a, b) -> a.getId() - b.getId());

        for (Material material : Material.values()) {
            String internalName = findInternalName(material);
            if (internalName == null) {
                getLogger().warning("Cannot find internal name for " + material.name() + "(" + material.getId() + ")");
            }

            map.put(material, internalName);
        }

        return Collections.unmodifiableMap(map);
    }

    private String findInternalName(@NonNull Material material) {
        int id = material.getId();

        Item internalItem = Item.getById(id);
        if (internalItem != null) {
            return Item.REGISTRY.c(internalItem).a();
        }

        Block internalBlock = Block.getById(id);
        if (internalBlock != null) {
            return Block.REGISTRY.c(internalBlock).a() + "_block";
        }

        return null;
    }
}
