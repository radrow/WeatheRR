package weatherr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.Call;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/weatherr.fxml"));
		primaryStage.setTitle("WeatheRR");

		final Scene scene = new Scene(root, 640, 480);
		primaryStage.setScene(scene);

		primaryStage.setHeight(480);
		primaryStage.setWidth(640);
		primaryStage.setMinHeight(480);
		primaryStage.setMinWidth(640);
		primaryStage.sizeToScene();
		primaryStage.show();
		primaryStage.setResizable(false);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
