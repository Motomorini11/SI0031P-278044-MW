package pwr.not_mario;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu {

    public void start(Stage stage) {

        // Tworzymy przyciski
        Button startBtn = new Button("Start Game");
        Button settingsBtn = new Button("Settings");
        Button quitBtn = new Button("Quit");

        // Trochę stylu, żeby było je widać na czarnym tle
        String buttonStyle = "-fx-font-size: 20px; -fx-min-width: 200px; -fx-background-color: white; -fx-text-fill: black;";
        startBtn.setStyle(buttonStyle);
        settingsBtn.setStyle(buttonStyle);
        quitBtn.setStyle(buttonStyle);

        // Akcje przycisków
        startBtn.setOnAction(e -> {
            GameWindow game = new GameWindow();
            game.start(stage);
        });

        quitBtn.setOnAction(e -> stage.close());

        // Układ (Layout)
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(startBtn, settingsBtn, quitBtn);

        // Ustawiamy czarne tło za pomocą CSS
        layout.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Not Mario - Menu");

        // PEŁNY EKRAN
        stage.setFullScreen(false);
        // Opcjonalnie: usuwa komunikat "Press ESC to exit full screen"
        stage.setFullScreenExitHint("");

        stage.show();
    }
}