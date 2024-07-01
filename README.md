# Board game

The graphical interface uses the [libGDX](https://libgdx.com/) library.\
All images were downloaded from [Game-icons.net](https://game-icons.net/).

## Requirements

- Java JDK version 17 or higher
- Gradle version 7.2 or higher

## Usage

### From a terminal

First, run the Gradle wrapper (once is enough):

```bash
gradle wrapper
```
#### Starting the application

- on macOS/Linux:

```bash
./gradlew run
```

- on Windows:

```bash
gradlew.bat run
```

#### Running the unit tests

- on macOS/Linux:

```bash
./gradlew test
```

- on Windows:

```bash
gradlew.bat test
```


### With an IDE
Open this repository as a Gradle project.

Then to start the application, run the method [DesktopLauncher.main](desktop/src/it/unibz/inf/pp/clash/DesktopLauncher.java)
  (in your running configuration, you may need to specify `assets` as the Java working directory).


## Design

### Game snapshot

The project is designed around the notion of game _snapshot_.\
A snapshot is an object that intuitively stores all the information needed to resume an interrupted game (state of the board, remaining health, active player, etc.).\
In other words, you can think of a snapshot as a save state.

### Model-view-controller

In order to decouple the graphical interface from the mechanics of the game, the project (loosely) follows the [model-view-controller](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) (MVC) pattern.\
This means that the code is partitioned into three components called model, view and controller:

- The _controller_ registers the user actions (click, hovering, etc.), and notifies the model of each action.
- The _model_ is the core of the application.
  It keeps track of the state of the game and updates it after each action.
  The model takes its input from the controller, and outputs drawing instructions to the view.
- The _view_ is in charge of drawing the game on screen. It takes its input from the model.

