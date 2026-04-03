package karnickeldev.solar.ui.components.mainmenu;

import karnickeldev.solar.Metadata;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.util.IniFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 11.07.2025
 **/
public class ServerListIni {

    public static final String fileName = "server-list.ini";

    private final IniFile iniFile = new IniFile();

    public ServerListIni() {

    }

    public void addServer(String name, String ip, String password) {
        iniFile.set(name, "ip", ip);
        iniFile.set(name, "password", password);
    }

    public void removeServer(String name) {
        iniFile.removeSection(name);
    }

    public List<String[]> getServerList() {
        List<String[]> serverList = new ArrayList<>();

        for(String section: iniFile.getSections()) {
            String[] sec = {
                section,
                iniFile.get(section, "ip"),
                iniFile.get(section, "password")
            };
            serverList.add(sec);
        }

        return serverList;
    }

    public boolean fileExists() {
        return new File(Metadata.ROOT_DIR, fileName).exists();
    }

    public boolean createFile() {
        File file = new File(Metadata.ROOT_DIR, fileName);
        if(file.exists()) return true;

        try {
            return file.createNewFile();
        } catch (IOException e) {
            Logger.get(LogTag.ASSETS).error("Failed to create File: " + file.getName(), e);
        }
        return false;
    }

    public boolean load() {
        if(!createFile()) return false;

        Path path = new File(Metadata.ROOT_DIR, fileName).toPath();

        try {
            iniFile.load(path);
            return true;
        } catch (IOException e) {
            Logger.get(LogTag.ASSETS).error("Failed to load File: " + fileName, e);
        }
        return false;
    }

    public boolean save() {
        if(!createFile()) return false;

        Path path = new File(Metadata.ROOT_DIR, fileName).toPath();
        try {
            iniFile.save(path);
            return true;
        } catch (IOException e) {
            Logger.get(LogTag.ASSETS).error("Failed to save File: " + fileName, e);
        }
        return false;
    }

}
