package ServerApp;

import ServerApp.Controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {

    public static Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("View/UI.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();

        primaryStage.setTitle("Java Chat Application (Server)");
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("./View/style.css").toExternalForm());

        //scene.getStylesheets().clear();
        //设置场景到舞台
        primaryStage.setScene(scene);
        //设置舞台尺寸不可改变
        primaryStage.setResizable(false);
        //设置关闭请求
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        //设置是否隐式关闭
        Platform.setImplicitExit(true);

        //展现舞台
        primaryStage.show();
    }
}
