package com.tjuesyv.tjuesyv.gameHandlers;

public abstract class GameState {

    protected GameObserver observer;

    /**
     * Called once the state is entered
     * @param observer
     */
    public GameState(GameObserver observer) {
        this.observer = observer;
    }

    /**
     * Return the layout id from the activity game xml file,
     * that is, what number the view has where 0 is top, and counting upwards
     * the further down in views you go
     * Rememer to differentiate the views for the game master and player
     * @return
     */
    public abstract int getViewId();

    /**
     * Continue the game to the nest state
     */
    public void nextState() {
        observer.progressServer();
    }
}
