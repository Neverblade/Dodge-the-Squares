package Wave;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static javafx.scene.paint.Color.*;

/**
 * Created by James on 12/20/2015.
 */
public class Wall extends Rectangle {

    private static final Color COLOR = Color.BLACK;

    public Wall(double x, double y, double width, double height) {
        super(x, y, width, height);
        setFill(COLOR);
    }

    /** Checks which edge of its boundaries player collides with.
     * @param player
     * @return 0 for North, 1 for East, 2 for South, 3 for West
     */
    public int detectEdgeCollision(Player player) {
        double[] deltas = new double[4];
        deltas[0] = (player.getY() + player.getRadius()) - getY();
        deltas[1] = (getX() + getWidth()) - (player.getX() - player.getRadius());
        deltas[2] = (getY() + getHeight()) - (player.getY() - player.getRadius());
        deltas[3] = (player.getX() + player.getRadius()) - getX();
        //System.out.print(deltas[0] + " " + deltas[1] + " " + deltas[2] + " " + deltas[3] + " ");
        int edge = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < deltas.length; i++) {
            if (deltas[i] > 0 && deltas[i] < min) {
                edge = i;
                min = deltas[i];
            }
        }
        return edge;
    }
}
