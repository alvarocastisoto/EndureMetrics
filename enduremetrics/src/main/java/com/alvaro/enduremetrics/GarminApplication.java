package com.alvaro.enduremetrics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import atlantafx.base.theme.PrimerLight;

@SpringBootApplication
public class GarminApplication extends Application {

	private ConfigurableApplicationContext springContext;

	public static void main(String[] args) {
		Application.launch(GarminApplication.class, args);
	}

	@Override
	public void init() {
		springContext = SpringApplication.run(GarminApplication.class);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
		fxmlLoader.setControllerFactory(springContext::getBean);
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		stage.setTitle("EndureMetrics");
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() {
		springContext.close();
		Platform.exit();
	}
}