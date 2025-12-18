package pwr.not_mario;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameScene extends Scene {

    private Stage stage;
    private Scene mainMenuScene;

    // --- Game Objects ---
    private Group root;
    private Group gameWorld;
    private Group gameUI;
    private Group pauseMenu;
    private Group gameOverMenu; // NEW: Game Over Screen
    private Group backgroundLayer;

    private Rectangle player;
    private List<Rectangle> platforms = new ArrayList<>();
    private List<Node> coins = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();

    // --- Stats ---
    private int score = 0;
    private int lives = 3; // NEW: Lives counter

    // --- UI Elements ---
    private Label scoreLabel;
    private Label livesLabel;

    // --- Input State ---
    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    private boolean isSpacePressed = false;

    // --- Physics Constants ---
    private static final double GRAVITY = 0.8;
    private static final double JUMP_STRENGTH = 18.0;
    private static final double PLAYER_SPEED = 7.0;
    private static final int BLOCK_SIZE = 60;

    // --- Physics State ---
    private double playerVelocityY = 0;
    private boolean isGrounded = false;

    // We store spawn point to respawn player easily
    private double spawnX = 100;
    private double spawnY = 100;

    // --- Game State ---
    private boolean isPaused = false;
    private boolean isGameOver = false; // NEW: Game Over Flag
    // --- Invincibility (Nieśmiertelność) ---
    private boolean isInvincible = false;
    private double invincibilityTimer = 0; // Czas w sekundach
    private static final double INVINCIBILITY_DURATION = 2.0; // 2 sekundy ochrony

    private AnimationTimer timer;

    public GameScene(Stage stage, Scene mainMenuScene, int levelNumber) {
        super(new Group(), 800, 600, Color.LIGHTBLUE);
        this.root = (Group) getRoot();
        this.stage = stage;
        this.mainMenuScene = mainMenuScene;

        // Initialize Groups
        gameWorld = new Group();
        gameUI = new Group();
        pauseMenu = new Group();
        gameOverMenu = new Group(); // Initialize Game Over
        backgroundLayer = new Group();

        // Order: Background -> World -> UI -> Menus
        root.getChildren().addAll(backgroundLayer, gameWorld, gameUI, pauseMenu, gameOverMenu);

        createPlayer();
        createPlaceholderLevel(levelNumber);
        createUI();
        createPauseMenu();
        createGameOverMenu(); // Build the screen hidden
        setupInputs();
        createGameLoop();

        timer.start();
    }

    // --- 1. HUD / UI (Updated for Readability) ---
    private void createUI() {
        // We use an HBox (Horizontal Box) to hold Score and Lives side-by-side
        HBox uiContainer = new HBox(30); // 30px spacing between elements
        uiContainer.setPadding(new javafx.geometry.Insets(10, 20, 10, 20)); // Padding inside the box
        uiContainer.setAlignment(Pos.CENTER_LEFT);

        // STYLE: Semi-transparent black background with rounded corners
        uiContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 15;");

        // Position the HUD slightly off the top-left corner
        uiContainer.setTranslateX(20);
        uiContainer.setTranslateY(20);

        // Score Label
        scoreLabel = new Label("Coins: 0");
        scoreLabel.setTextFill(Color.GOLD);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Lives Label (Using Unicode Heart ❤)
        livesLabel = new Label("Lives: " + "❤".repeat(lives));
        livesLabel.setTextFill(Color.RED); // Red Hearts
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        uiContainer.getChildren().addAll(livesLabel, scoreLabel);

        gameUI.getChildren().add(uiContainer);
    }

    private void updateStatsUI() {
        scoreLabel.setText("Coins: " + score);
        livesLabel.setText("Lives: " + "❤".repeat(Math.max(0, lives)));
    }

    // --- 2. GAME OVER MENU ---
    private void createGameOverMenu() {
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.DARKRED); // Scary red tint
        overlay.setOpacity(0.6);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        Label title = new Label("GAME OVER");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 80));

        Label subTitle = new Label("Press ANY KEY to exit");
        subTitle.setTextFill(Color.WHITE);
        subTitle.setFont(new Font("Arial", 20));

        // Flash animation
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), subTitle);
        fade.setFromValue(1.0);
        fade.setToValue(0.2);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());
        layout.getChildren().addAll(title, subTitle);

        gameOverMenu.getChildren().addAll(overlay, layout);
        gameOverMenu.setVisible(false);
    }

    private void triggerGameOver() {
        isGameOver = true;
        timer.stop();
        gameOverMenu.setVisible(true);
        gameWorld.setEffect(new BoxBlur(10, 10, 3));
    }

    // ---  PHYSICS & LOGIC UPDATE =================================================================================
    private void update() {
        if (isPaused || isGameOver) return;

        // --- OBSŁUGA NIEŚMIERTELNOŚCI ------------------------------------------------------------------------
        if (isInvincible) {
            invincibilityTimer -= 0.016;

            // Efekt mrugania
            if (invincibilityTimer <= 0) {
                isInvincible = false;
                player.setOpacity(1.0);
            } else {
                // Szybka matematyka modulo, żeby mrugał co 0.1 sekundy
                if ((int)(invincibilityTimer * 10) % 2 == 0) {
                    player.setOpacity(0.4);
                } else {
                    player.setOpacity(1.0);
                }
            }
        }

        // Movement
        if (isLeftPressed) player.setX(player.getX() - PLAYER_SPEED);
        if (isRightPressed) player.setX(player.getX() + PLAYER_SPEED);

        // Wall Left
        if (player.getX() < 0) player.setX(0);

        // Jump
        if (isSpacePressed && isGrounded) {
            playerVelocityY = -JUMP_STRENGTH;
            isGrounded = false;
        }

        // Gravity
        playerVelocityY += GRAVITY;
        player.setY(player.getY() + playerVelocityY);

        // Collisions
        checkForCollisions();
        checkCoinCollection();

        // --- NEW: DEATH BY VOID (Falling off map) ---
        // If player is way below the screen height
        if (player.getY() > stage.getHeight() + 200) { // +200 buffer so we fall completely off screen
            handleDeath();
        }

        // Camera Logic
        double cameraX = -player.getX() + (getWidth() / 2) - (player.getWidth() / 2);
        if (cameraX > 0) cameraX = 0;
        gameWorld.setTranslateX(cameraX);



        // --- LOGIKA PRZECIWNIKÓW --------------------------------------------------
        enemies.removeIf(enemy -> {
            enemy.update(platforms); // Ruch wroga

            if (player.getBoundsInParent().intersects(enemy.getBoundsInParent())) {

                // 1. Sprawdzamy czy zabijamy wroga (skok na głowę)
                boolean isFalling = playerVelocityY > 0;
                double playerBottom = player.getY() + player.getHeight();
                double enemyTop = enemy.getY();

                // Jeśli spadamy na głowę I nie jesteśmy w trakcie "damage boosta"
                if (isFalling && playerBottom < enemyTop + 20) {
                    playerVelocityY = -10; // Odbicie
                    gameWorld.getChildren().remove(enemy);
                    return true; // Usuń wroga
                }
                // 2. Jeśli wróg dotyka nas (i NIE skaczemy na niego)
                else {
                    // Jeśli jesteśmy nieśmiertelni, ignorujemy kolizję
                    if (!isInvincible) {
                        playerTakeDamage(); // <--- Nowa metoda
                    }
                }
            }
            return false;
        });
    }

    private void handleDeath() {
        lives--;
        updateStatsUI();

        if (lives > 0) {
            respawnPlayer();
        } else {
            triggerGameOver();
        }
    }

    private void respawnPlayer() {
        // Reset Position
        player.setX(spawnX);
        player.setY(spawnY);
        playerVelocityY = 0;
        // Move camera back instantly
        gameWorld.setTranslateX(0);
    }

    // ---  LEVEL GENERATOR LOGIC ------------------------------------------------------------------
    private void createPlaceholderLevel(int levelNumber) {
        platforms.clear();
        coins.clear();
        gameWorld.getChildren().clear();
        gameWorld.getChildren().add(player);

        String[] currentLevel = LevelData.LEVEL_1;
        double levelHeight = currentLevel.length * BLOCK_SIZE;
        double startY = stage.getHeight() - levelHeight;

        for (int row = 0; row < currentLevel.length; row++) {
            String line = currentLevel[row];
            for (int col = 0; col < line.length(); col++) {
                char cell = line.charAt(col);
                double x = col * BLOCK_SIZE;
                double y = startY + (row * BLOCK_SIZE);

                if (cell == '1') {
                    Rectangle block = new Rectangle(x, y, BLOCK_SIZE, BLOCK_SIZE);
                    block.setFill(Color.FORESTGREEN);
                    block.setStroke(Color.BLACK);
                    gameWorld.getChildren().add(block);
                    platforms.add(block);
                }
                else if (cell == 'C') {

                    try {
                        Image coinImg = new Image(getClass().getResourceAsStream("/assets/coin.png"));
                        ImageView coinView = new ImageView(coinImg);


                        coinView.setFitWidth(40);
                        coinView.setFitHeight(40);


                        coinView.setX(x + (BLOCK_SIZE - 40) / 2);
                        coinView.setY(y + (BLOCK_SIZE - 40) / 2);

                        gameWorld.getChildren().add(coinView);
                        coins.add(coinView);
                    } catch (Exception e) {

                        System.out.println("Coin image not found, using Circle fallback.");
                        javafx.scene.shape.Circle coinFallback = new javafx.scene.shape.Circle(x + BLOCK_SIZE/2.0, y + BLOCK_SIZE/2.0, 15, Color.GOLD);
                        gameWorld.getChildren().add(coinFallback);
                        coins.add(coinFallback);
                    }
                }
                else if (cell == 'E') {
                    Enemy enemy = new Enemy(x + 5, y+ (BLOCK_SIZE - Enemy.ENEMY_SIZE) + 1, levelNumber);
                    gameWorld.getChildren().add(enemy);
                    enemies.add(enemy);
                }
            }
        }

        // Background loading (same as before)
        backgroundLayer.getChildren().clear();
        String bgPath = "/assets/background_level" + levelNumber + ".png";
        try {
            if (getClass().getResource(bgPath) != null) {
                Image bgImage = new Image(getClass().getResourceAsStream(bgPath));
                ImageView bgView = new ImageView(bgImage);
                bgView.setFitWidth(stage.getWidth());
                bgView.setFitHeight(stage.getHeight());
                backgroundLayer.getChildren().add(bgView);
            } else {
                backgroundLayer.getChildren().add(new Rectangle(stage.getWidth(), stage.getHeight(), Color.LIGHTBLUE));
            }
        } catch (Exception e) {
            backgroundLayer.getChildren().add(new Rectangle(stage.getWidth(), stage.getHeight(), Color.LIGHTBLUE));
        }

        player.toFront();
    }

    // --- INPUTS (Modified for Game Over) ---
    private void setupInputs() {
        this.setOnKeyPressed(event -> {

            // IF GAME OVER -> Any key exits
            if (isGameOver) {
                returnToMenu();
                return;
            }

            switch (event.getCode()) {
                case LEFT:  isLeftPressed = true; break;
                case RIGHT: isRightPressed = true; break;
                case UP:    isSpacePressed = true; break;
                case ESCAPE: togglePause(); break;
            }
        });

        this.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:  isLeftPressed = false; break;
                case RIGHT: isRightPressed = false; break;
                case UP:    isSpacePressed = false; break;
            }
        });
    }

    // --- Standard Methods (Keep these as they were) ---
    private void createPlayer() {
        player = new Rectangle(50, 50, Color.RED);
        player.setX(spawnX);
        player.setY(spawnY);
        gameWorld.getChildren().add(player);
    }

    private void createGameLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
    }

    private void checkForCollisions() {
        isGrounded = false;
        for (Rectangle platform : platforms) {
            if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                boolean isFalling = playerVelocityY > 0;
                double playerBottom = player.getY() + player.getHeight();
                double platformTop = platform.getY();
                if (isFalling && (playerBottom - playerVelocityY <= platformTop + 10)) {
                    isGrounded = true;
                    playerVelocityY = 0;
                    player.setY(platformTop - player.getHeight());
                }
            }
        }
    }

    private void checkCoinCollection() {
        coins.removeIf(coin -> {
            if (player.getBoundsInParent().intersects(coin.getBoundsInParent())) {
                gameWorld.getChildren().remove(coin);
                score++;
                updateStatsUI();
                return true;
            }
            return false;
        });
    }

    private void createPauseMenu() {
        // Reuse your existing Pause Menu code logic here
        // (Copied largely from previous step, ensure Group pauseMenu is initialized)
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.6);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        Label pauseLabel = new Label("PAUSED");
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setFont(new Font("Arial", 80));

        Label resumeLabel = new Label("Press ESC to Resume");
        resumeLabel.setTextFill(Color.WHITE);
        resumeLabel.setFont(new Font("Arial", 20));

        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), resumeLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.2);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        javafx.scene.control.Button quitBtn = new javafx.scene.control.Button("QUIT TO MENU");
        quitBtn.setStyle("-fx-font-size: 18px; -fx-base: #444444; -fx-text-fill: white;");
        quitBtn.setPrefWidth(200);
        quitBtn.setOnAction(e -> returnToMenu());

        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.prefWidthProperty().bind(stage.widthProperty());
        menuLayout.prefHeightProperty().bind(stage.heightProperty());
        menuLayout.getChildren().addAll(pauseLabel, resumeLabel, quitBtn);

        pauseMenu.getChildren().addAll(overlay, menuLayout);
        pauseMenu.setVisible(false);
    }

    private void togglePause() {
        if (isGameOver) return; // Cannot pause if dead
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            pauseMenu.setVisible(true);
            gameWorld.setEffect(new BoxBlur(10, 10, 3));
        } else {
            timer.start();
            pauseMenu.setVisible(false);
            gameWorld.setEffect(null);
        }
    }

    private void returnToMenu() {
        timer.stop();
        stage.setScene(mainMenuScene);
        stage.setWidth(stage.getWidth());
        stage.setHeight(stage.getHeight());
        gameWorld.setEffect(null);
    }
    private void playerTakeDamage() {
        lives--;
        updateStatsUI();

        if (lives <= 0) {
            player.setOpacity(1.0);
            triggerGameOver();
        } else {
            // Aktywuj nieśmiertelność
            isInvincible = true;
            invincibilityTimer = INVINCIBILITY_DURATION;


            // Wyrzucamy gracza lekko w górę
            playerVelocityY = -10;
            isGrounded = false;

            System.out.println("Oberwałeś! Pozostało żyć: " + lives);
        }
    }
}