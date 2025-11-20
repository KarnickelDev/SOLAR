package karnickeldev.solar.render.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import karnickeldev.solar.util.Logger;

import java.util.*;

/**
 * @author KarnickelDev
 * @since 28.09.2025
 **/
public class ShaderManager implements Disposable {

    private record ShaderNode(FileHandle vertexSource, FileHandle fragmentSource, ShaderProgram shaderProgram) {}

    private final Map<String, ShaderNode> shaders = new HashMap<>();

    public void registerFromInternalFile(String name, String vertPath, String fragPath) {
        register(name, Gdx.files.internal(vertPath), Gdx.files.internal(fragPath));
    }

    public void register(String name, FileHandle vertScr, FileHandle fragSrc) {

        // read & preprocess shader from file
        String processedVertShader;
        String processedFragShader;
        try {
            processedVertShader = ShaderUtils.preprocessShader(vertScr);
            processedFragShader = ShaderUtils.preprocessShader(fragSrc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(processedVertShader.isEmpty() || processedFragShader.isEmpty()) {
            throw new RuntimeException("Tried to register invalid shader");
        }

        ShaderProgram shader = new ShaderProgram(processedVertShader, processedFragShader);
        if(!shader.isCompiled()) {
            shader.dispose();
            Logger.error(Logger.SHADER, "Compile error: " + shader.getLog());
            return;
        }

        // dispose existing
        unload(name);

        ShaderNode node = new ShaderNode(vertScr, fragSrc, shader);
        shaders.put(name, node);
    }

    public boolean isRegistered(String name) {
        return shaders.containsKey(name);
    }

    public ShaderProgram get(String name) {
        // should never throw Exception unless name not registered
        return shaders.get(name).shaderProgram;
    }

    public void unload(String name) {
        ShaderNode s = shaders.remove(name);
        if(s != null) s.shaderProgram.dispose();
    }

    public void reload() {
        List<String> s = new ArrayList<>(shaders.keySet());
        for(String name: s) {
            ShaderNode node = shaders.get(name);
            FileHandle vertSrc = node.vertexSource;
            FileHandle fragSrc = node.fragmentSource;
            register(name, vertSrc, fragSrc);
        }

        Logger.log(Logger.SHADER, "Reloaded Shaders");
    }

    public void dispose() {
        for(String shader: shaders.keySet()) {
            unload(shader);
        }
        shaders.clear();
    }

}
