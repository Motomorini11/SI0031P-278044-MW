package pwr.not_mario;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class FallingHazard extends Rectangle {

    private double speed;

    public FallingHazard(double x, double y, int levelNumber) {
        // Rozmiar 40x40
        super(x, y, 40, 40);


        if (levelNumber == 3) {

            this.speed = 3.0;
            loadGraphics("coconut.png", Color.SADDLEBROWN);
        } else if (levelNumber == 5) {

            this.speed = 5.0;
            loadGraphics("magma.png", Color.ORANGERED);
        } else {

            this.speed = 3.0;
            this.setFill(Color.BLACK);
        }
    }

    private void loadGraphics(String imageName, Color fallbackColor) {
        try {
            String path = "/assets/" + imageName;
            Image img = new Image(getClass().getResourceAsStream(path));
            this.setFill(new ImagePattern(img));
        } catch (Exception e) {

            this.setFill(fallbackColor);
            this.setArcWidth(40);
            this.setArcHeight(40);
        }
    }

    public void update() {

        this.setY(this.getY() + speed);
    }
}
