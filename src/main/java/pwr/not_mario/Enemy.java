package pwr.not_mario;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.util.List;

public class Enemy extends Rectangle {

    private double speed = 1.0;
    private int direction = 1; // 1 = prawo, -1 = lewo
    public static final int ENEMY_SIZE = 60;

    public Enemy(double x, double y, int levelNumber) {
        // Ustawiamy rozmiar przeciwnika na 40x40 (trochę mniejszy niż blok 60x60)
        super(x, y, ENEMY_SIZE, ENEMY_SIZE);

        // Ładowanie grafiki w zależności od poziomu
        // Szuka pliku np. "enemy1.png" dla poziomu 1
        String path = "/assets/enemy" + levelNumber + ".png";

        try {
            Image enemyImg = new Image(getClass().getResourceAsStream(path), ENEMY_SIZE, ENEMY_SIZE, true, false);
            // ImagePattern nakłada obrazek na prostokąt
            this.setFill(new ImagePattern(enemyImg));
        } catch (Exception e) {
            System.out.println("Nie znaleziono grafiki przeciwnika: " + path);
            this.setFill(Color.PURPLE);
            this.setStroke(Color.BLACK);
        }
    }

    public void update(List<Rectangle> platforms) {
        // 1. Ruch
        this.setX(this.getX() + (speed * direction));

        // 2. Sprawdzanie otoczenia (Czy zawrócić?)
        boolean shouldTurn = false;

        // Punkty sprawdzania:
        // checkX -> punkt tuż przed nosem przeciwnika
        double checkX = (direction == 1) ? getX() + getWidth() + 5 : getX() - 5;

        // checkY_Floor -> punkt pod nogami (sprawdza czy jest podłoga)
        double checkY_Floor = getY() + getHeight() + 5;

        // checkY_Wall -> punkt na wysokości klatki piersiowej (sprawdza czy jest ściana)
        double checkY_Wall = getY() + getHeight() / 2;

        boolean floorDetected = false;
        boolean wallDetected = false;

        for (Rectangle platform : platforms) {
            // Sprawdź czy pod nogami jest podłoga
            if (platform.contains(checkX, checkY_Floor)) {
                floorDetected = true;
            }
            // Sprawdź czy przed nosem jest ściana
            if (platform.contains(checkX, checkY_Wall)) {
                wallDetected = true;
            }
        }

        // Decyzja: Zawracamy jeśli:
        // A) Skończyła się podłoga (!floorDetected)
        // B) Uderzyliśmy w ścianę (wallDetected)
        if (!floorDetected || wallDetected) {
            turnAround();
        }
    }

    public void turnAround() {
        direction *= -1;
        // Opcjonalnie: Tutaj można by odwracać grafikę (ScaleX = -1),
        // ale ImagePattern tego nie obsługuje łatwo, więc na razie zostawmy.
    }
}