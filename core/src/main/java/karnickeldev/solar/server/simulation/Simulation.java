package karnickeldev.solar.server.simulation;

import karnickeldev.solar.gamestate.GameState;
import karnickeldev.solar.physics.OrbitalObject;
import karnickeldev.solar.gamestate.StarSystemTree;

class Simulation implements Runnable {

    private static final int SPEED = 3600*24*30;

    private final GameState gameState;

    private final int timeStep;

    private double time = 0d;

    protected Simulation(GameState gameState, int timeStep) {
        this.gameState = gameState;
        this.timeStep = timeStep;
    }

    @Override
    public void run() {
        updateStaticObjects();
    }


    private void updateStaticObjects() {
        time += SPEED / 60d;
        StarSystemTree.Iterator iterator = new StarSystemTree.Iterator(gameState.getStarSystem());
        while(iterator.hasNext()) {
            OrbitalObject object = iterator.next();

            object.update(time);
        }
    }

    public GameState getGameState() {
        return gameState;
    }
}
