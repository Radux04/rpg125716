package it.unicam.cs.mpgc.rpg125716;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.application.Application;
import javafx.stage.Stage;

public class FrontendApplication extends Application {
    private static final double WINDOW_WIDTH = 1600;
    private static final double WINDOW_HEIGHT = 900;

    @Override
    public void start(Stage stage) {
        stage.setTitle("The Forgotten Gate");
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMaxWidth(WINDOW_WIDTH);
        stage.setMaxHeight(WINDOW_HEIGHT);
        stage.setResizable(false);

        SceneNavigator sceneNavigator = new SceneNavigator(
                stage,
                new GameService(),
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );
        sceneNavigator.showMainMenu();
        stage.show();
        stage.centerOnScreen();
    }
}
