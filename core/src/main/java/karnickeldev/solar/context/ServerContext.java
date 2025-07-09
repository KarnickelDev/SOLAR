package karnickeldev.solar.context;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class ServerContext {

    private static volatile ServerContextContainer currentServerContext;

    private ServerContext() {}

    public static void setContext(ServerContextContainer context) {
        currentServerContext = context;
    }

    public static ServerContextContainer get() {
        if(currentServerContext == null) throw new NullPointerException("Server Context should never be null");
        return currentServerContext;
    }

    public static boolean isSet() {
        return currentServerContext != null;
    }

    public static void clear() {
        currentServerContext = null;
    }

}
