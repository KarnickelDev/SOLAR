package karnickeldev.solar.launch;

import karnickeldev.solar.Metadata;

public class TestLauncher {

    public static void main(String[] args) {

        Metadata.loadVersionData();

        System.out.println(Metadata.APP_NAME + " v" + Metadata.VERSION);

    }

}
