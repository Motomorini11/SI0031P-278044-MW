package pwr.not_mario;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainMenu {

    private Stage stage;
    private Scene mainScene;
    private Scene levelSelectScene;
    private Scene settingsScene;

    public void start(Stage primaryStage) {
        this.stage = primaryStage;


        createMainScene();
        createLevelSelectScene();
        createSettingsScene();


        primaryStage.setTitle("Not Mario");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.setResizable(false);

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private Scene createStandardMenuScene(VBox contentBox, int width, int height) {

        contentBox.setAlignment(Pos.CENTER);
        contentBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 20; -fx-padding: 30;");


        contentBox.setMinWidth(width);
        contentBox.setMaxWidth(width);

        contentBox.setMinHeight(height);
        contentBox.setMaxHeight(height);

        StackPane root = new StackPane();

        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/assets/MainMenu_background.png"));
            ImageView bgView = new ImageView(bgImage);


            bgView.fitWidthProperty().bind(stage.widthProperty());
            bgView.fitHeightProperty().bind(stage.heightProperty());

            root.getChildren().add(bgView);

        } catch (Exception e) {
            root.setStyle("-fx-background-color: gray;");
        }

        root.getChildren().add(contentBox);

        return new Scene(root, 800, 600);
    }

    // --- SCENE 1: MAIN MENU ---
    private void createMainScene() {


        Button btnStart = createStyledButton("START GAME");
        Button btnSettings = createStyledButton("SETTINGS");
        Button btnQuit = createStyledButton("QUIT");

        btnStart.setOnAction(e -> {
            createLevelSelectScene();
            stage.setScene(levelSelectScene);
        });
        btnSettings.setOnAction(e -> stage.setScene(settingsScene));
        btnQuit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        VBox layout = new VBox(20);
        layout.getChildren().addAll(btnStart, btnSettings, btnQuit);


        mainScene = createStandardMenuScene(layout, 350, 300);
    }

    // --- SCENE 2: LEVEL SELECT  ---
    private void createLevelSelectScene() {
        Label titleLabel = new Label("SELECT LEVEL");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(new Font("Arial", 40));

        HBox levelContainer = new HBox(15);
        levelContainer.setAlignment(Pos.CENTER);


        int maxUnlockedLevel = SaveSystem.loadProgress();

        for (int i = 1; i <= 5; i++) {
            int levelNum = i;
            Button lvlBtn = createStyledButton("Level " + i);
            lvlBtn.setPrefWidth(100);


            if (i > maxUnlockedLevel) {

                lvlBtn.setDisable(true);
                lvlBtn.setStyle("-fx-base: #222222; -fx-text-fill: gray; -fx-font-size: 18px;");
                lvlBtn.setText("LOCKED"); // Opcjonalnie: zmiana tekstu
            } else {

                lvlBtn.setOnAction(e -> {
                    System.out.println("Loading Level " + levelNum + "...");
                    GameScene game = new GameScene(stage, mainScene, levelNum);
                    stage.setScene(game);
                });
            }

            levelContainer.getChildren().add(lvlBtn);
        }

        Button btnBack = createStyledButton("BACK");
        btnBack.setOnAction(e -> stage.setScene(mainScene));

        VBox layout = new VBox(40);
        layout.getChildren().addAll(titleLabel, levelContainer, btnBack);

        levelSelectScene = createStandardMenuScene(layout, 800, 400);
    }

    // --- SCENE 3: SETTINGS ---
    private void createSettingsScene() {
        Label titleLabel = new Label("SETTINGS");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(new Font("Arial", 40));

        CheckBox musicCheck = new CheckBox("Music On");
        musicCheck.setTextFill(Color.WHITE);
        musicCheck.setFont(new Font("Arial", 20));

        musicCheck.setSelected(true);

        musicCheck.setOnAction(e -> {
            boolean isOn = musicCheck.isSelected();
            SoundManager.setMusicEnabled(isOn);
        });

        Button btnBack = createStyledButton("BACK");
        btnBack.setOnAction(e -> stage.setScene(mainScene));

        VBox layout = new VBox(30);
        layout.getChildren().addAll(titleLabel, musicCheck, btnBack);

        settingsScene = createStandardMenuScene(layout, 400, 350);
    }

    // --- HELPER METHODS ---
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-font-size: 18px; -fx-base: #444444; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }
}