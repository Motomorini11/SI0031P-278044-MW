package pwr.not_mario;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.util.List;

public class Enemy extends Rectangle {

    private double speed = 1.0;
    private int direction = 1;
    public static final int ENEMY_SIZE = 60;

    public Enemy(double x, double y, int levelNumber) {
        super(x, y, ENEMY_SIZE, ENEMY_SIZE);

        String path = "/assets/enemy" + levelNumber + ".png";

        try {
            Image enemyImg = new Image(getClass().getResourceAsStream(path), ENEMY_SIZE, ENEMY_SIZE, true, false);

            this.setFill(new ImagePattern(enemyImg));
        } catch (Exception e) {
            System.out.println("Nie znaleziono grafiki przeciwnika: " + path);
            this.setFill(Color.PURPLE);
            this.setStroke(Color.BLACK);
        }
    }

    public void update(List<Rectangle> platforms) {

        this.setX(this.getX() + (speed * direction));


        double checkX = (direction == 1) ? getX() + getWidth() + 5 : getX() - 5;


        double checkY_Floor = getY() + getHeight() + 5;


        double checkY_Wall = getY() + getHeight() / 2;

        boolean floorDetected = false;
        boolean wallDetected = false;

        for (Rectangle platform : platforms) {

            if (platform.contains(checkX, checkY_Floor)) {
                floorDetected = true;
            }

            if (platform.contains(checkX, checkY_Wall)) {
                wallDetected = true;
            }
        }

        if (!floorDetected || wallDetected) {
            turnAround();
        }
    }

    public void turnAround() {
        direction *= -1;

    }
}