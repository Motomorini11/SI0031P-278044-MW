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
    private Group gameOverMenu;
    private Group backgroundLayer;

    private Rectangle player;
    private List<Rectangle> platforms = new ArrayList<>();
    private List<Node> coins = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<FallingHazard> hazards = new ArrayList<>();
    private double hazardSpawnTimer = 0;
    private List<Rectangle> finishLines = new ArrayList<>();
    private Group levelCompletedMenu;

    // --- Stats ---
    private int score = 0;
    private int lives = 3;
    private int maxCoins = 0;

    // --- UI Elements ---
    private Label scoreLabel;
    private Label livesLabel;
    private Label statsLabel;

    // --- Input State ---
    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    private boolean isSpacePressed = false;

    // --- Physics Constants ---
    private static final double GRAVITY = 0.8;
    private static final double JUMP_STRENGTH = 18.0;
    private static final double MAX_SPEED = 7.0;
    private double ACCELERATION = 1.0;
    private double friction = 0.9;
    private static final int BLOCK_SIZE = 60;

    // --- Physics State ---
    private double playerVelocityY = 0;
    private double playerVelocityX = 0; // NOWE: Prędkość pozioma
    private boolean isGrounded = false;

    // ------ Spawn Point ---------
    private double spawnX = 100;
    private double spawnY = 100;

    // --- Game State ---
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isInvincible = false;
    private double invincibilityTimer = 0;
    private static final double INVINCIBILITY_DURATION = 2.0;
    private int currentLevelNumber;
    private boolean isLevelCompleted = false;

    private AnimationTimer timer;

    public GameScene(Stage stage, Scene mainMenuScene, int levelNumber) {
        super(new Group(), 800, 600, Color.LIGHTBLUE);
        this.currentLevelNumber = levelNumber;
        this.root = (Group) getRoot();
        this.stage = stage;
        this.mainMenuScene = mainMenuScene;

        // Initialize Groups
        gameWorld = new Group();
        gameUI = new Group();
        pauseMenu = new Group();
        gameOverMenu = new Group();
        levelCompletedMenu = new Group();
        backgroundLayer = new Group();

        // Order: Background -> World -> UI -> Menus
        root.getChildren().addAll(backgroundLayer, gameWorld, gameUI, pauseMenu, gameOverMenu, levelCompletedMenu);

        createPlayer();
        createPlaceholderLevel(levelNumber);
        createUI();
        createPauseMenu();
        createGameOverMenu();
        createLevelCompletedMenu();
        setupInputs();
        createGameLoop();

        timer.start();
    }

    // --- 1. HUD / UI (Updated for Readability) ---
    private void createUI() {

        HBox uiContainer = new HBox(30);
        uiContainer.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
        uiContainer.setAlignment(Pos.CENTER_LEFT);

        uiContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 15;");

        uiContainer.setTranslateX(20);
        uiContainer.setTranslateY(20);

        scoreLabel = new Label("Coins: 0");
        scoreLabel.setTextFill(Color.GOLD);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

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

    // ---  GAME OVER MENU ---
    private void createGameOverMenu() {
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.DARKRED); // Scary red tint
        overlay.setOpacity(0.6);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        Label title = new Label("GAME OVER");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 80));

        Label subTitle = new Label("Press SPACEBAR to exit");
        subTitle.setTextFill(Color.WHITE);
        subTitle.setFont(new Font("Arial", 20));


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

    private void createLevelCompletedMenu() {


        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.LIGHTGREEN);
        overlay.setOpacity(0.6);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());


        Label title = new Label("LEVEL COMPLETED!");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        statsLabel = new Label("Coins collected: 0 / 0");
        statsLabel.setTextFill(Color.GOLD);
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        statsLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");


        Label subTitle = new Label("Press SPACEBAR to exit");
        subTitle.setTextFill(Color.WHITE);
        subTitle.setFont(new Font("Arial", 24));
        subTitle.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");


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
        layout.getChildren().addAll(title, statsLabel, subTitle);

        levelCompletedMenu.getChildren().addAll(overlay, layout);
        levelCompletedMenu.setVisible(false);
    }
    private void triggerLevelCompleted() {
        isLevelCompleted = true;
        timer.stop();
        statsLabel.setText("Coins collected: " + score + " / " + maxCoins);
        levelCompletedMenu.setVisible(true);
        gameWorld.setEffect(new BoxBlur(10, 10, 3));

        // Tutaj w przyszłości logika odblokowywania Levelu + 1 ???????????????????????????????????????????????????????????
    }

    // ---  PHYSICS & LOGIC UPDATE =================================================================================
    private void update() {
        if (isPaused || isGameOver) return;

        // --- OBSŁUGA NIEŚMIERTELNOŚCI ------------------------------------------------------------------------
        if (isInvincible) {
            invincibilityTimer -= 0.016;


            if (invincibilityTimer <= 0) {
                isInvincible = false;
                player.setOpacity(1.0);
            } else {

                if ((int)(invincibilityTimer * 10) % 2 == 0) {
                    player.setOpacity(0.4);
                } else {
                    player.setOpacity(1.0);
                }
            }
        }

        // Movement
        if (isLeftPressed) {
            playerVelocityX -= ACCELERATION;
        }
        if (isRightPressed) {
            playerVelocityX += ACCELERATION;
        }

        // Tarcie
        playerVelocityX *= friction;

        // Ograniczenie prędkości
        if (playerVelocityX > MAX_SPEED) playerVelocityX = MAX_SPEED;
        if (playerVelocityX < -MAX_SPEED) playerVelocityX = -MAX_SPEED;

        if (Math.abs(playerVelocityX) < 0.1) playerVelocityX = 0;

        // Grawitacja
        playerVelocityY += GRAVITY;

        // Skok
        if (isSpacePressed && isGrounded) {
            playerVelocityY = -JUMP_STRENGTH;
            isGrounded = false;
        }

        // --- 2. RUCH I KOLIZJE (AABB) ---

        movePlayerX();
        movePlayerY();

        // Wall Left
        if (player.getX() < 0) player.setX(0);


        checkCoinCollection();

        // ---  DEATH BY VOID (Falling off map) ---
        if (player.getY() > stage.getHeight() + 200) { // +200 buffer so we fall completely off screen
            handleDeath();
        }

        // Camera Logic
        double cameraX = -player.getX() + (getWidth() / 2) - (player.getWidth() / 2);
        if (cameraX > 0) cameraX = 0;
        gameWorld.setTranslateX(cameraX);



        // --- LOGIKA PRZECIWNIKÓW ----------------------------------------------------------------------------------------------
        enemies.removeIf(enemy -> {
            enemy.update(platforms);

            if (player.getBoundsInParent().intersects(enemy.getBoundsInParent())) {


                boolean isFalling = playerVelocityY > 0;
                double playerBottom = player.getY() + player.getHeight();
                double enemyTop = enemy.getY();


                if (isFalling && playerBottom < enemyTop + 20) {
                    playerVelocityY = -10;
                    gameWorld.getChildren().remove(enemy);
                    return true;
                }

                else {

                    if (!isInvincible) {
                        playerTakeDamage();
                    }
                }
            }
            return false;
        });

        if (!isLevelCompleted) {
            for (Rectangle finish : finishLines) {
                if (player.getBoundsInParent().intersects(finish.getBoundsInParent())) {
                    triggerLevelCompleted();
                    break;
                }
            }
        }

        if (currentLevelNumber == 3 || currentLevelNumber == 5) {
            spawnAndMoveHazards();
        }
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
        gameWorld.setTranslateX(0);
    }

    // ---  LEVEL GENERATOR LOGIC ------------------------------------------------------------------
    private void createPlaceholderLevel(int levelNumber) {
        platforms.clear();
        coins.clear();
        enemies.clear();
        hazards.clear();
        finishLines.clear();
        maxCoins = 0;
        gameWorld.getChildren().clear();
        gameWorld.getChildren().add(player);

        //  Wybieramy mapę
        int levelIndex = levelNumber - 1;
        if (levelIndex >= LevelData.LEVELS.length || levelIndex < 0) {
            levelIndex = 0;
        }
        String[] currentLevel = LevelData.LEVELS[levelIndex];


        Color themeColor;
        Color strokeColor = Color.BLACK;

        switch (levelNumber) {
            case 2:
                themeColor = Color.WHITE;      // Śnieg (Level 2)
                break;
            case 3:
                themeColor = Color.SANDYBROWN; // Piasek/Plaża (Level 3)
                break;
            case 4:
                themeColor = Color.DIMGRAY;    // Kamień/Jaskinia (Level 4)
                break;
            case 5:
                themeColor = Color.DARKRED;    // Wulkan/Magma (Level 5)
                break;
            default:
                themeColor = Color.FORESTGREEN; // Trawa
                break;
        }
        // ------------------------------------------------

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


                    block.setFill(themeColor);
                    block.setStroke(strokeColor);

                    gameWorld.getChildren().add(block);
                    platforms.add(block);
                }
                else if (cell == 'I') {
                    // BLOK LODU
                    Rectangle iceBlock = new Rectangle(x, y, BLOCK_SIZE, BLOCK_SIZE);


                    iceBlock.setFill(Color.DEEPSKYBLUE);
                    iceBlock.setStroke(Color.BLUE);

                    iceBlock.setUserData("ICE"); // Ważne dla fizyki!

                    gameWorld.getChildren().add(iceBlock);
                    platforms.add(iceBlock);
                }
                else if (cell == 'C') {
                    // MONETA
                    try {
                        Image coinImg = new Image(getClass().getResourceAsStream("/assets/coin.png"));
                        ImageView coinView = new ImageView(coinImg);
                        coinView.setFitWidth(40);
                        coinView.setFitHeight(40);
                        coinView.setX(x + (BLOCK_SIZE - 40) / 2);
                        coinView.setY(y + (BLOCK_SIZE - 40) / 2);
                        gameWorld.getChildren().add(coinView);
                        coins.add(coinView);
                        maxCoins++;
                    } catch (Exception e) {
                        javafx.scene.shape.Circle coinFallback = new javafx.scene.shape.Circle(x + BLOCK_SIZE/2.0, y + BLOCK_SIZE/2.0, 15, Color.GOLD);
                        gameWorld.getChildren().add(coinFallback);
                        coins.add(coinFallback);
                    }
                }
                else if (cell == 'E') {
                    // PRZECIWNIK
                    Enemy enemy = new Enemy(x + 5, y+ (BLOCK_SIZE - Enemy.ENEMY_SIZE) + 1, levelNumber);
                    gameWorld.getChildren().add(enemy);
                    enemies.add(enemy);
                }
                else if (cell == 'F') {
                    Rectangle finishRect = new Rectangle(x, y, BLOCK_SIZE, BLOCK_SIZE);

                    finishRect.setFill(Color.TRANSPARENT);
                    finishRect.setStroke(Color.RED); // TESTY METY

                    gameWorld.getChildren().add(finishRect);
                    finishLines.add(finishRect);
                }
            }
        }


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

                Color bgColor = Color.LIGHTBLUE;
                if (levelNumber == 4) bgColor = Color.BLACK;
                if (levelNumber == 5) bgColor = Color.ORANGERED.desaturate();

                backgroundLayer.getChildren().add(new Rectangle(stage.getWidth(), stage.getHeight(), bgColor));
            }
        } catch (Exception e) {
            backgroundLayer.getChildren().add(new Rectangle(stage.getWidth(), stage.getHeight(), Color.LIGHTBLUE));
        }

        player.toFront();
    }


    private void setupInputs() {
        this.setOnKeyPressed(event -> {


            if (isGameOver) {
                if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    returnToMenu();
                }
                return;
            }

            if (isLevelCompleted) {
                if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    returnToMenu();
                }
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

    // --- Standard Methods  ---
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

    private void movePlayerX() {
        player.setX(player.getX() + playerVelocityX);

        for (Rectangle platform : platforms) {
            if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {

                double playerBottom = player.getY() + player.getHeight();
                double platformTop = platform.getY();
                double platformBottom = platform.getY() + platform.getHeight();

                double overlapY = Math.min(playerBottom, platformBottom) - Math.max(player.getY(), platformTop);

                if (overlapY < 10) {
                    continue;
                }

                if (playerVelocityX > 0) {
                    player.setX(platform.getX() - player.getWidth());
                } else if (playerVelocityX < 0) {
                    player.setX(platform.getX() + platform.getWidth());
                }

                playerVelocityX = 0;
            }
        }

        if (player.getX() < 0) {
            player.setX(0);
            playerVelocityX = 0;
        }
    }

    private void movePlayerY() {
        player.setY(player.getY() + playerVelocityY);

        isGrounded = false;

        for (Rectangle platform : platforms) {
            if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {

                double playerRight = player.getX() + player.getWidth();
                double platformRight = platform.getX() + platform.getWidth();

                double overlapX = Math.min(playerRight, platformRight) - Math.max(player.getX(), platform.getX());


                if (overlapX < 2.0) {
                    continue;
                }

                if (playerVelocityY > 0) {
                    // Lądowanie
                    player.setY(platform.getY() - player.getHeight());
                    isGrounded = true;
                    playerVelocityY = 0;

                    Object type = platform.getUserData();
                    if (type != null && type.equals("ICE")) {
                        friction = 0.98;
                        ACCELERATION = 0.2;
                    } else {
                        friction = 0.90;
                        ACCELERATION = 1.5;
                    }

                } else if (playerVelocityY < 0) {

                    player.setY(platform.getY() + platform.getHeight());
                    playerVelocityY = 0;
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
        if (isGameOver) return;
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

            isInvincible = true;
            invincibilityTimer = INVINCIBILITY_DURATION;
        }
    }
    private void spawnAndMoveHazards() {

        hazardSpawnTimer -= 0.016;

        if (hazardSpawnTimer <= 0) {

            double randomX = Math.random() * (stage.getWidth() - 40);

            FallingHazard hazard = new FallingHazard(randomX, -50, currentLevelNumber);

            gameWorld.getChildren().add(hazard);
            hazards.add(hazard);

            if (currentLevelNumber == 5) {
                hazardSpawnTimer = 0.3 + Math.random() * 1.0;
            } else {
                hazardSpawnTimer = 1.0 + Math.random() * 1.5;
            }
        }

        hazards.removeIf(hazard -> {
            hazard.update();

            if (player.getBoundsInParent().intersects(hazard.getBoundsInParent())) {
                if (!isInvincible) {
                    playerTakeDamage();
                    playerVelocityY = -10;
                    isGrounded = false;
                    gameWorld.getChildren().remove(hazard);
                    return true;
                }
            }

            if (hazard.getY() > stage.getHeight()) {
                gameWorld.getChildren().remove(hazard);
                return true;
            }

            return false;
        });
    }
}