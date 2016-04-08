package com.tjuesyv.tjuesyv.gameHandlers;

import butterknife.ButterKnife;

/**
 * Created by RayTM on 08.04.2016.
 */
public abstract class GameState {

    /**
     * Use this to progress in the game
     * this is set before the game starts
     */
    protected GameHandler handler;

    /**
     * Return the layout id from the activity game xml file,
     * that is, what number the view has where 0 is top, and counting upwards
     * the further down in views you go
     * Rememer to differentiate the views for the game master and player
     * @return
     */
    public abstract int getViewId();

    /**
     * This is called once the state is entered
     */
    public abstract void onEnter();

    /**
     * Called internally by GameMode before starting the game
     * @param handler
     */
    void bindHandler(GameHandler handler) {
        System.out.println(this.getViewId());
        this.handler = handler;
    }

    /**
     * Continue the game to the nest state
     */
    public void nextState() {
        handler.nextState();
    }
}
