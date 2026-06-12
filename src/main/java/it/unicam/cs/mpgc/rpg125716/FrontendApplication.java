package it.unicam.cs.mpgc.rpg125716;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class FrontendApplication extends Application {
    @Override
    public void start(Stage stage) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
        double windowWidth = primaryScreenBounds.getWidth();
        double windowHeight = primaryScreenBounds.getHeight();

        stage.setTitle("The Forgotten Gate");
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.setMaxWidth(windowWidth);
        stage.setMaxHeight(windowHeight);
        stage.setResizable(false);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));

        SceneNavigator sceneNavigator = new SceneNavigator(
                stage,
                new GameService(),
                windowWidth,
                windowHeight
        );
        sceneNavigator.showMainMenu();
        stage.show();
        stage.setFullScreen(true);
    }
}
