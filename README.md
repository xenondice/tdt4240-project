# Lyv Lyv, Tjuesyv
![](https://img.shields.io/badge/platform-android-green.svg)
![](https://img.shields.io/badge/Min%20SDK-16-green.svg)
[![Build Status](https://travis-ci.org/szeestraten/tdt4240-project.svg?branch=master)](https://travis-ci.org/szeestraten/tdt4240-project)

## Data structure in Firebase
Here is how the data is structured in Firebase.

* `games/`
    * `<game-id>`
        * `active` -> `true` - A boolean value indicating the state of the game.
        * `gameCode` -> `ABCD` - A string for the game code used for players to identify the game.
        * `gameHost` -> `<user-id>` - A string with the user ID of the game host.
        * `maxPlayers` -> `8` - An integer value of the max allowed players in this game.
        * `players` -> `[<player-id>, <player-id>, ...]` - A list of player IDs who are in this game.
        * `round` -> `1` - An integer value of the current round.
        * `started` -> `false` - A boolean value indicating if the game has started.
* `questions/`
    * `<question-number>` -> `Who is God?` - A string with a question 
* `scores/`
    * `<game-id>`
        * `gameId` -> `<game-id>` - A string with the ID of the game.
        * `playerId`-> `<user-id>` - A string with the ID of the player.
        * `score` - > `0` - An integer with the score of the player
* `users/`
    * `<user-id`>
        * `nickname` -> `Bob Kaare` - A string with the nickname of the user.
        * `games` -> `[<game-id>, <game-id>, ...]` - A list of games with ID that the user is in.


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
