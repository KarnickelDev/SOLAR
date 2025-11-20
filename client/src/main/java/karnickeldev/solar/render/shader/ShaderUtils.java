package karnickeldev.solar.render.shader;

import com.badlogic.gdx.files.FileHandle;

import java.util.HashSet;
import java.util.Set;

/**
 * @author KarnickelDev
 * @since 24.09.2025
 **/
public class ShaderUtils {

    public static String preprocessShader(FileHandle file) {
        return preprocessShader(file, new HashSet<>());
    }

    private static String preprocessShader(FileHandle file, Set<String> includes) {
        if(includes.contains(file.path())) {
            // prevent recursive include
            return "";
        }

        includes.add(file.path());

        StringBuilder result = new StringBuilder();
        String[] lines = file.readString().split("\\r?\\n");

        for(String line: lines) {
            if(line.trim().startsWith("#include")) {
                String toIncludePath = line.trim().split("\"")[1];
                FileHandle toInclude = normalizePath(file.parent(), toIncludePath);
                if(toInclude != null) result.append(preprocessShader(toInclude, includes)).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    private static FileHandle normalizePath(FileHandle parent, String relPath) {
        if(parent == null || relPath == null || relPath.isEmpty()) return null;

        String[] path = relPath.split("/");

        for(String s: path) {
            if(s.equals("..")) {
                parent = parent.parent();
            } else {
                parent = parent.child(s);
            }
        }

        return parent;
    }

}
