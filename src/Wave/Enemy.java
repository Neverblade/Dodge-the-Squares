package Wave;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Enemy extends Rectangle {

    static final String OFF = "OFF";
    static final String ON = "ON";
    static final int SIZE = 40;
    static final Color COLOR = Color.RED;
    private static final int ENTRANCE_DURATION = 2000;
    private static final double OPACITY = 1;
    private static final double ARC = SIZE / 4;

    private String status;
    private double dX;
    private double dY;

    public Enemy(double x, double y, double dX, double dY) {
        super(x, y, SIZE, SIZE);
        setFill(COLOR);
        setArcHeight(ARC);
        setArcWidth(ARC);
        status = OFF;
        this.dX = dX;
        this.dY = dY;
    }

    void animateEntrance() {
        Duration duration = Duration.millis(ENTRANCE_DURATION);
        FadeTransition ft = new FadeTransition(duration, this);
        ft.setFromValue(0);
        ft.setToValue(OPACITY);
        RotateTransition rt = new RotateTransition(duration, this);
        rt.setToAngle(720);
        ScaleTransition st = new ScaleTransition(duration, this);
        st.setFromX(0);
        st.setFromY(0);
        st.setToX(1);
        st.setToY(1);
        ParallelTransition pt = new ParallelTransition(this, ft, rt, st);
        pt.setCycleCount(1);
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                status = ON;
            }
        });
        pt.play();
    }

    public String getStatus() {
        return status;
    }

    public double getDX() {
        return dX;
    }
    public double getDY() {
        return dY;
    }
    public void setDX(double dX) {
        this.dX = dX;
    }
    public void setDY(double dY) {
        this.dY = dY;
    }

}
