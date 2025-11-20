package karnickeldev.solar.context;

/**
 * @author KarnickelDev
 * @since 01.07.2025
 **/
public final class GameContext {

    private static volatile GameContextContainer currentGameContext;

    // prevent initialization
    private GameContext() {}

    public static void setContext(GameContextContainer context) {
        currentGameContext = context;
    }

    public static GameContextContainer get() {
        if(currentGameContext == null) throw new NullPointerException("Game Context should never be null");
        return currentGameContext;
    }

    public static boolean isSet() {
        return currentGameContext != null;
    }

    public static void clear() {
        currentGameContext = null;
    }
}
