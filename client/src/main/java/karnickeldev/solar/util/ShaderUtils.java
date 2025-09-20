package karnickeldev.solar.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashSet;
import java.util.Set;

/**
 * @author : KarnickelDev
 * @since : 24.09.2025
 **/
public class ShaderUtils {

    public static String preprocessShader(String path) {
        return preprocessShader(Gdx.files.internal(path), new HashSet<>());
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
            if(line.startsWith("#include")) {
                // extract filename and add
                String toIncludePath = line.split("\"")[1];
                FileHandle toInclude = file.parent().child(toIncludePath);
                result.append(preprocessShader(toInclude, includes)).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

}
