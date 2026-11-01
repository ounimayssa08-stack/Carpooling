gipackage com.carpooling;

import com.carpooling.model.User;
import com.carpooling.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("CarPooling — Desktop");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // Initialise DB & schema
        DatabaseManager.initialize();

        showLogin();
        primaryStage.show();
    }

    public static void showLogin() throws IOException {
        loadScene("Login.fxml", 480, 560);
    }

    public static void showRegister() throws IOException {
        loadScene("Register.fxml", 500, 600);
    }

    public static void showDashboard(User user) throws IOException {
        loadScene("Dashboard.fxml", 1100, 700);
    }

    private static void loadScene(String fxmlFile, double width, double height)
            throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/carpooling/fxml/" + fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
                MainApp.class.getResource("/com/carpooling/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    @Override
    public void stop() {
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
