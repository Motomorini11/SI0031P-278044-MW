package pwr.not_mario;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainMenu menu = new MainMenu();
        menu.start(primaryStage);
    }

    public static void main(String[] args) {
        launch();
    }
}

