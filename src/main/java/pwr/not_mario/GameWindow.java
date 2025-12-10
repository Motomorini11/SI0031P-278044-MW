package pwr.not_mario;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameWindow {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int PLAYER_SIZE = 40;

    private GraphicsContext gc;
    private LevelManager levelManager;
    private VBox pauseMenu;

    // --- ZMIENNE GRACZA ---
    private double playerX = 0;
    private double playerY = 1000;
    private double playerVelocityY = 0;

    // --- FIZYKA ---
    private static final double GRAVITY = 1;
    private static final double JUMP_STRENGTH = -30;
    private static final double SPEED = 9;

    // --- STEROWANIE ---
    private boolean goLeft, goRight, isJumping;
    private boolean isPaused = false;

    public void start(Stage stage) {
        levelManager = new LevelManager();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // Tworzymy menu pauzy
        createPauseMenu(stage);

        StackPane root = new StackPane();
        root.getChildren().addAll(canvas, pauseMenu);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root);

        // --- OBSŁUGA KLAWISZY ---
        scene.setOnKeyPressed(e -> {
            KeyCode key = e.getCode();

            if (key == KeyCode.ESCAPE) {
                togglePause();
            }

            if (isPaused) return;

            if (key == KeyCode.A || key == KeyCode.LEFT) goLeft = true;
            if (key == KeyCode.D || key == KeyCode.RIGHT) goRight = true;
            if (key == KeyCode.SPACE || key == KeyCode.W || key == KeyCode.UP) isJumping = true;
        });

        scene.setOnKeyReleased(e -> {
            KeyCode key = e.getCode();
            if (key == KeyCode.A || key == KeyCode.LEFT) goLeft = false;
            if (key == KeyCode.D || key == KeyCode.RIGHT) goRight = false;
            if (key == KeyCode.SPACE || key == KeyCode.W || key == KeyCode.UP) isJumping = false;
        });

        stage.setScene(scene);

        // --- WINDOWED zamiast fullscreen ---
        stage.setFullScreen(false);
        stage.setResizable(false);
        stage.setMaximized(true);
        stage.setTitle("Not Mario");
        stage.show();

        root.requestFocus();
        startGameLoop();
    }

    private void createPauseMenu(Stage stage) {
        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        String btnStyle = "-fx-font-size: 20px; -fx-padding: 10 20; -fx-background-color: #444; -fx-text-fill: white;";

        Button btnMainMenu = new Button("Quit to Main Menu");
        btnMainMenu.setStyle(btnStyle);
        btnMainMenu.setOnAction(e -> {
            MainMenu menu = new MainMenu();
            menu.start(stage);   // zamiast zamykania okna wracamy do menu
        });

        Button btnQuit = new Button("Quit Game");
        btnQuit.setStyle(btnStyle);
        btnQuit.setOnAction(e -> System.exit(0));

        pauseMenu.getChildren().addAll(btnMainMenu, btnQuit);
        pauseMenu.setVisible(false);
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseMenu.setVisible(isPaused);
    }

    private void startGameLoop() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) {
                    if (!isPaused) {
                        update();
                    }
                    render();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private void update() {
        if (goLeft) playerX -= SPEED;
        if (goRight) playerX += SPEED;

        playerVelocityY += GRAVITY;
        playerY += playerVelocityY;

        boolean onGround = false;
        if (playerY >= HEIGHT - PLAYER_SIZE - 50) {
            playerY = HEIGHT - PLAYER_SIZE - 50;
            playerVelocityY = 0;
            onGround = true;
        }

        for (Platform p : levelManager.getPlatforms()) {
            if (playerVelocityY > 0) {
                if (playerX + PLAYER_SIZE > p.getX() && playerX < p.getX() + p.getW()) {
                    if (playerY + PLAYER_SIZE >= p.getY() && playerY + PLAYER_SIZE <= p.getY() + 20) {
                        playerY = p.getY() - PLAYER_SIZE;
                        playerVelocityY = 0;
                        onGround = true;
                    }
                }
            }
        }

        if (isJumping && onGround) {
            playerVelocityY = JUMP_STRENGTH;
        }
    }

    private void render() {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.GREEN);
        gc.fillRect(0, HEIGHT - 50, WIDTH, 50);

        levelManager.render(gc);

        gc.setFill(Color.RED);
        gc.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        if (isPaused) {
            // Tytuł PAUSED
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 80));
            gc.fillText("PAUSED", WIDTH / 2 - 150, HEIGHT / 2 - 150);

            // Migający napis ESC
            double alpha = Math.abs(Math.sin(System.currentTimeMillis() / 300.0));
            gc.setFill(Color.color(1, 1, 1, alpha)); // biała barwa, ale migająca
            gc.setFont(new Font("Arial", 30));
            gc.fillText("Press ESC to resume", WIDTH / 2 - 160, HEIGHT / 2 - 100);
        }
    }
}
