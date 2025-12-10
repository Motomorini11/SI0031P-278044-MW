package pwr.not_mario;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Platform {
    private double x, y, w, h;

    public Platform(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Metoda do rysowania samej siebie
    public void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(x, y, w, h);
    }

    // Gettery potrzebne do kolizji
    public double getX() { return x; }
    public double getY() { return y; }
    public double getW() { return w; }
    public double getH() { return h; }
}
