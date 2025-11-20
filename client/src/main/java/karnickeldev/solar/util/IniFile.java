package karnickeldev.solar.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author KarnickelDev
 * @since 11.07.2025
 **/
public class IniFile {

    private final Map<String, Map<String, String>> sections = new LinkedHashMap<>();

    /**
     * Loads a File as a .ini file (UTF-8)
     * @param path Path of the File
     * @throws IOException on error
     */
    public void load(Path path) throws IOException, RuntimeException {
        Map<String, String> currentSection = null;

        for (String line : Files.readAllLines(path)) {
            line = line.trim();

            // ignore comments for parsing
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) continue;

            if (line.startsWith("[") && line.endsWith("]") /*new section*/) {
                String sectionName = line.substring(1, line.length() - 1).trim();
                currentSection = new LinkedHashMap<>();
                sections.put(sectionName, currentSection);
            } else if (line.contains("=") && currentSection != null /*value assignment*/) {
                String[] parts = line.split("=", 2);
                currentSection.put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    public void save(Path path) throws IOException, RuntimeException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Map.Entry<String, Map<String, String>> entry : sections.entrySet()) {
                writer.write("[" + entry.getKey() + "]"); // write section
                writer.newLine();
                for (Map.Entry<String, String> kv : entry.getValue().entrySet()) {
                    writer.write(kv.getKey() + "=" + kv.getValue()); // write value assignments
                    writer.newLine();
                }
                writer.newLine();
            }
            writer.flush();
        }
    }

    public void set(String section, String key, String value) {
        sections.computeIfAbsent(section, k -> new LinkedHashMap<>()).put(key, value);
    }

    public String get(String section, String key) {
        Map<String, String> sec = sections.get(section);
        return (sec != null) ? sec.get(key) : null;
    }

    public Set<String> getSections() {
        return sections.keySet();
    }

    public Map<String, String> getSection(String section) {
        return sections.getOrDefault(section, Collections.emptyMap());
    }

    public void remove(String section, String key) {
        Map<String, String> sec = sections.get(section);
        if (sec != null) sec.remove(key);
    }

    public void removeSection(String section) {
        sections.remove(section);
    }

    public void dispose() {
        sections.clear();
    }

}
