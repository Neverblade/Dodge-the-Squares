package Wave;

import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    /* Static variables. */
    static final int PREFERRED_HEIGHT = 800, PREFERRED_WIDTH = 1200;
    private static final int HIGH_CAP = 200, LOW_CAP = 25;
    private static final int WALL_THICKNESS = 40;
    private static final int START_BASE = 1;
    private static final int START_RANGE = 5;
    private static final int BASE_CAP = 4;
    private static final int RANGE_CAP = 13;
    private static final int START_INTERVAL = 5;
    private static final int PAUSE_TEXT_SIZE = 70;
    private static final int SCORE_TEXT_SIZE = 30;
    private static final int SCORE_INSET = 6;

    /* Overview variables. */
    private Stage stage;
    private AnimationTimer loop;
    private Player player;
    private Scene gameScene, startScene;

    /* Game Scene variables. */
    private int playerSpeed = 8; //pixels per second
    private int interval, range, base;
    private long prevSeconds, startTime;
    private boolean paused;
    private boolean inGame;
    private List<Node> pausedItems;
    private int score;
    private int highScore;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setResizable(false);
        stage.setTitle("Dodge the Squares");
        stage.setWidth(PREFERRED_WIDTH);
        stage.setHeight(PREFERRED_HEIGHT);
        pausedItems = new ArrayList<Node>();

        playGame(stage);
    }

    public void playGame(Stage stage) {
        inGame = true;
        score = 0;

        /* Initialize Variables */
        Group root = new Group();
        gameScene = new Scene(root, Color.DIMGRAY);
        gameScene.setCursor(Cursor.CROSSHAIR);

        /* Set the scene and show it. */
        stage.setScene(gameScene);
        stage.show();

        /* Add the walls in. */
        Wall northWall = new Wall(0, 0, gameScene.getWidth(), WALL_THICKNESS);
        Wall southWall = new Wall(0, gameScene.getHeight() - WALL_THICKNESS, gameScene.getWidth(), WALL_THICKNESS);
        Wall westWall = new Wall(0, 0, WALL_THICKNESS, gameScene.getHeight());
        Wall eastWall = new Wall(gameScene.getWidth() - WALL_THICKNESS, 0, WALL_THICKNESS, gameScene.getHeight());
        root.getChildren().addAll(northWall, southWall, westWall, eastWall);

        /* Add the score and high score text. */
        Text scores = new Text(scoreString());
        scores.setFont(Font.font("Verdana", FontWeight.BOLD, SCORE_TEXT_SIZE));
        scores.setY(gameScene.getY() + SCORE_INSET);
        scores.setX(gameScene.getX() + gameScene.getWidth() / 2 - scores.getLayoutBounds().getWidth() / 2);
        scores.setFill(Color.SILVER);
        root.getChildren().add(scores);

        /* Add the player in the center of the scene. */
        player = new Player(gameScene);
        player.setSpeed(Duration.millis(HIGH_CAP - 0.1 * playerSpeed * (HIGH_CAP - LOW_CAP)));
        root.getChildren().add(player);

        /* Create the enemy list. */
        List<Enemy> enemies = new ArrayList<>();

        /* Start the Timer loop. */
        this.prevSeconds = -1; this.startTime = -1;
        this.interval = START_INTERVAL; this.base = START_BASE; this.range = START_RANGE;
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                /* Get the time. */
                long seconds = now / 1000000000;
                if (startTime == -1) {
                    startTime = seconds;
                }

                /* Update the score. */
                score = (int) (seconds - startTime);
                if (score > highScore) {
                    highScore = score;
                }
                scores.setText(scoreString());

                /* Add new enemies when the interval is up. */
                if (seconds - prevSeconds > interval || prevSeconds == -1) {
                    summonEnemy(gameScene, root, enemies);
                    interval++;
                    range = Math.min(range + 1, RANGE_CAP);
                    base = Math.min(base + 1, BASE_CAP);
                    prevSeconds = seconds;
                }
                /* Process the player. */
                processPlayer(player, gameScene, enemies);

                /* Update enemy movement. */
                for (Enemy e : enemies) {
                    if (e.getStatus() == Enemy.ON) {
                        fixEnemy(e, gameScene);
                        e.setX(e.getX() + e.getDX());
                        e.setY(e.getY() + e.getDY());
                    }
                }
            }
        };

        /* Create pausing. */
        gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode key = event.getCode();
                if (key == KeyCode.ESCAPE && inGame) {
                    if (paused) {
                        loop.start();
                        paused = false;
                        removePause();
                    } else {
                        loop.stop();
                        paused = true;
                        createPause();
                    }
                }
            }
        });

        /* Start the game! */
        loop.start();
        paused = false;
    }

    private String scoreString() {
        return "High-Score: " + String.format("%04d", highScore) + "  Score: " + String.format("%04d", score);
    }

    /** Performs all player related processing. */
    private void processPlayer(Player player, Scene scene, List<Enemy> enemies) {
        /* Fix positioning if player is out of bounds. */
        fixPlayer(player, scene);

        /* Check if player has collided with an enemy. */
        if (detectCollision(player, enemies)) {
            endGame();
        }

        /* Grab mouse position and calculate positioning/speed. */
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        double x = mouse.getX() - (stage.getX() + scene.getX());
        double y = mouse.getY() - (stage.getY() + scene.getY());

        /* Create the corresponding default transition. */
        TranslateTransition tt = new TranslateTransition(player.getSpeed(), player);
        tt.setToX(x);
        tt.setToY(y);

        /* Start the transition. */
        tt.play();
    }

    /** Ends the game. */
    private void endGame() {
        /* Stop the game first. */
        loop.stop();

        /* Animate the player dieing. */
        double growth = 1.5;
        ScaleTransition st1 = new ScaleTransition(Duration.millis(200), player);
        st1.setFromX(1); st1.setFromY(1); st1.setToX(growth); st1.setToY(growth);
        ScaleTransition st2 = new ScaleTransition(Duration.millis(1000), player);
        st2.setFromX(growth); st2.setFromY(growth); st2.setToX(0); st2.setToY(0);
        SequentialTransition st = new SequentialTransition(player, st1, st2);
        st.play();

        /* Start the game anew. */
        st.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playGame(stage);
            }
        });
    }

    /** Summon an enemy with a random starting point and random direction. */
    private void summonEnemy(Scene scene, Group root, List<Enemy> enemies) {
        double x = Math.random() * (scene.getWidth() - WALL_THICKNESS * 2 - Enemy.SIZE) + WALL_THICKNESS;
        double y = Math.random() * (scene.getHeight() - WALL_THICKNESS * 2 - Enemy.SIZE) + WALL_THICKNESS;
        double dX = Math.random() * range + base;
        if (Math.random() < 0.5) dX *= -1;
        double dY = Math.random() * range + base;
        if (Math.random() < 0.5) dY *= -1;
        Enemy e = new Enemy(x, y, dX, dY);
        enemies.add(e);
        root.getChildren().add(e);
        e.animateEntrance();
    }

    /** Checks if an enemy is moving out of bounds and reflects them if so. */
    private void fixEnemy(Enemy e, Scene scene) {
        if (e.getY() < WALL_THICKNESS || e.getY() + e.getHeight() > scene.getHeight() - WALL_THICKNESS) { //north or south
            e.setDY(-e.getDY());
        }
        if (e.getX() + e.getWidth() > scene.getWidth() - WALL_THICKNESS || e.getX() < WALL_THICKNESS) { //east or west
            e.setDX(-e.getDX());
        }
    }

    /** Initialize the pause screen for the game scene. */
    private void createPause() {
        /* Create overlay. */
        Rectangle overlay = new Rectangle(0, 0, gameScene.getWidth(), gameScene.getHeight());
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.5);

        /* Create text. */
        Text text = new Text("PRESS ESC TO RESUME");
        text.setFont(Font.font("Verdana", FontWeight.BOLD, PAUSE_TEXT_SIZE));
        text.setY(gameScene.getY() + gameScene.getHeight() / 2 - text.getLayoutBounds().getHeight() / 2);
        text.setX(gameScene.getX() + gameScene.getWidth() / 2  - text.getLayoutBounds().getWidth() / 2);
        text.setFill(Color.SILVER);

        /* Add components to scene graph. */
        Group root = (Group) gameScene.getRoot();
        pausedItems.add(overlay);
        root.getChildren().add(overlay);
        pausedItems.add(text);
        root.getChildren().add(text);
    }

    /** Remove the elements of the pause screen from the game scene. */
    private void removePause() {
        Group root = (Group) gameScene.getRoot();
        for (Node node : pausedItems) {
            root.getChildren().remove(node);
        }
    }

    /** Checks if target collides with any of the given objects.
     * @param target The object we're dealing with (usually the player).
     * @param enemies The list of objects we'll be comparing target to (probably walls).
     * @return If target collides with an object, return the INDEX of that object, -1 otherwise.
     */
    private boolean detectCollision(Shape target, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.getStatus() == Enemy.ON) {
                Shape intersect = Shape.intersect(target, e);
                if (intersect.getBoundsInLocal().getWidth() != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Fixes the player to make sure it's not out of bounds. */
    private void fixPlayer(Player player, Scene scene) {
        if (player.getY() - player.getRadius() < WALL_THICKNESS) { //northern side
            player.setTranslateY(WALL_THICKNESS + player.getRadius());
        }
        if (player.getX() + player.getRadius() > scene.getWidth() - WALL_THICKNESS) { //eastern side
            player.setTranslateX(scene.getWidth() - WALL_THICKNESS - player.getRadius());
        }
        if (player.getY() + player.getRadius() > scene.getHeight() - WALL_THICKNESS) { //southern side
            player.setTranslateY(scene.getHeight() - WALL_THICKNESS - player.getRadius());
        }
        if (player.getX() - player.getRadius() <  WALL_THICKNESS) { //western side
            player.setTranslateX(WALL_THICKNESS + player.getRadius());
        }
    }
}
