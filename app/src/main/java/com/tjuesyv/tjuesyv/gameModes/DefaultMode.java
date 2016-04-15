package com.tjuesyv.tjuesyv.gameModes;

import com.tjuesyv.tjuesyv.gameHandlers.GameMode;
import com.tjuesyv.tjuesyv.states.ChooseState;
import com.tjuesyv.tjuesyv.states.CreateState;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;
import com.tjuesyv.tjuesyv.states.LobbyState;
import com.tjuesyv.tjuesyv.states.ScoreState;

/**
 * Created by RayTM on 08.04.2016.
 */
public class DefaultMode extends GameMode {
    private GameState[] states = new GameState[]{
        new CreateState(),
        new ChooseState(),
        new ScoreState()
    };

    private GameState lobby = new LobbyState();

    @Override
    public int getNumberOfRounds() {
        return 2;
    }

    @Override
    public GameState[] getStates() {
        return states;
    }

    @Override
    public GameState getLobby() {
        return lobby;
    }
}
