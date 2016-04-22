![](https://www.dropbox.com/s/4dx0xv2r9ub6gdn/logoinverted.png?dl=1)

![](https://img.shields.io/badge/platform-android-green.svg)
![](https://img.shields.io/badge/Min%20SDK-16-green.svg)
[![Build Status](https://travis-ci.org/szeestraten/tdt4240-project.svg?branch=master)](https://travis-ci.org/szeestraten/tdt4240-project)

## Data structure in Firebase

* `games/`
    * `<game-id>`
        * `active` - `true` - A boolean value indicating the state of the game.
        * `gameCode` - `"ABCD"` - A string for the game code used for players to identify the game.
        * `gameHost` - `"<user-id>"` - A string with the user ID of the game host.
        * `maxPlayers` - `8` - An integer value of the max allowed players in this game.
        * `players` - A list of players who are in this game.
            * `<user-id>` - `True` - A boolean value indicating if the player is the game master or not  
        * `round` - `1` - An integer value of the current round.
        * `currentQuestion` - `<currentQuestion-id>` - An integer value of the id of the current currentQuestion.
        * `answers`
            * `<user-id>` - `Bob is your uncle` - A string value of the answer for this player.
        * `started` - `false` - A boolean value indicating if the game has started.
        * `stateId` - `0` - A integer describing which state in the active game mode the server is in (0 = lobby, 1+ = index for state list).
        * `gameModeId` - `0` - A id for the current active game mode (0 = DefaultMode).
        * `gameMaster` - `<gameHost>` - A string with the user ID for the game master for the current round (changes each round).
* `questions/`
    * `<currentQuestion-id>`
         * `currentQuestion` - `"The name of the dog that won the 2012 World's Ugliest Dog Competition."` - A string with a currentQuestion
         * `answer` - `"Mugly"` - A string with a currentQuestion
* `scores/`
    * `<game-id>`
        * `<user-id>`
            * `score` -  `0` - An integer with the score of the player
* `users/`
    * `<user-id`>
        * `nickname` - `"Bob Kaare"` - A string with the nickname of the user.
        * `games` - A list of games that the user is in.
            * `<game-id>` -  `True` - A boolean value indicating that the player is currently in the game.


## Coding conventions
#### Branch naming
```
feat_       Features
bugfix_     Bug fixes
exp_        Experimental
```

#### Examples
```
feat_add-flask-#33          A new feature branch for adding Flask in issue #33
bugfix_typo-in-header-#21   A bug fix branch to fix a typo in issue #21
exp_testing-mysql           An experimental branch for testing my-sql
```

#### Pull requests
When creating pull requests, use the keyword ```closes``` to group with issue in waffle.
```
feat_add-flask closes #33
```
