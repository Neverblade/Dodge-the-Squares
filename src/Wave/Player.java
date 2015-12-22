package Wave;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Player extends Circle {

    private static final int SIZE = 20;
    private static final Color COLOR = Color.GOLD;
    private static final double OPACITY = 1;

    private Duration speed;

    public Player(Scene scene) {
        super(0, 0, SIZE, COLOR);
        setTranslateX(scene.getWidth() / 2);
        setTranslateY(scene.getHeight() / 2);
        setOpacity(OPACITY);
    }

    /** Constructor for creating future states with. */
    public Player(double x, double y) {
        super(x, y, SIZE);
    }

    public double getX() {
        return getCenterX() + getTranslateX();
    }

    public double getY() {
        return getCenterY() + getTranslateY();
    }

    public Duration getSpeed() {
        return speed;
    }

    public void setSpeed(Duration duration) {
        speed = duration;
    }
}
