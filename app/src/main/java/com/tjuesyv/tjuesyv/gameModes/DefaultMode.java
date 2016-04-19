package com.tjuesyv.tjuesyv.gameModes;

import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.gameHandlers.GameMode;
import com.tjuesyv.tjuesyv.states.ChooseState;
import com.tjuesyv.tjuesyv.states.CreateState;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;
import com.tjuesyv.tjuesyv.states.LobbyState;
import com.tjuesyv.tjuesyv.states.ScoreState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by RayTM on 08.04.2016.
 */
public class DefaultMode extends GameMode {

    private static final int NUMBER_OF_ROUNDS = 2;

    @Override
    public boolean isGameOver(Game game) {
        return game.getRound() >= NUMBER_OF_ROUNDS;
    }

    @Override
    public List<Class<? extends GameState>> getStates() {
        return new ArrayList<>(Arrays.asList(
                CreateState.class,
                ChooseState.class,
                ScoreState.class
        ));
    }

    @Override
    public Class<? extends GameState> getLobby() {
        return LobbyState.class;
    }
}
