package pwr.not_mario;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private List<Platform> platforms = new ArrayList<>();

    public LevelManager() {
        createLevel();
    }

    private void createLevel() {
        platforms.clear();
        // Tworzenie platform - logika przeniesiona tutaj
        platforms.add(new Platform(300, 800, 200, 30));
        platforms.add(new Platform(600, 650, 200, 30));
        platforms.add(new Platform(900, 500, 300, 30));
        platforms.add(new Platform(200, 400, 150, 30));
        platforms.add(new Platform(1300, 700, 400, 30));
    }

    public void render(GraphicsContext gc) {
        for (Platform p : platforms) {
            p.render(gc);
        }
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }
}
