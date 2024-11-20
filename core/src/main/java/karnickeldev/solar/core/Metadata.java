package karnickeldev.solar.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 11.10.2024
 */
public class Metadata {

    public static String APP_NAME;
    public static String VERSION;
    public static final String ROOT_DIR_NAME = "SOLAR";

    private static final String user_dir = System.getProperty("os.name").startsWith("Windows") ? System.getenv("APPDATA") : System.getProperty("user.dir");
    public static final File ROOT_DIR = new File(user_dir, ROOT_DIR_NAME);

    public static boolean loadVersionData() {
        boolean success = true;

        Properties properties = new Properties();
        try (InputStream input = SolarMain.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find version.properties");
                success = false;
            }
            // Load properties file
            properties.load(input);

            // Get the property values
            APP_NAME = properties.getProperty("appName");
            VERSION = properties.getProperty("version");
            if(APP_NAME == null || VERSION == null) {
                System.out.println("Error getting name or version from version.properties");
                success = false;
            }

        } catch (IOException ex) {
            success = false;
            System.out.println("Error loading version.properties:\n" + ex.getMessage());
        }

        return success;
    }

}
