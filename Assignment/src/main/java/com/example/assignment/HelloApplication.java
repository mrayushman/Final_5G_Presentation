//Ayushman Singh
// IIT Gandhinagar
// End Term Project

package com.example.assignment;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        Button startHandoffButton = new Button("Start Handoff Simulation");

        // Set up the button action to start the Handoff Visualization
        startHandoffButton.setOnAction(e -> {
            // Creating a new instance of HandoffVisualizationApp and passing the current stage
            HandoffVisualizationApp handoffVisualizationApp = new HandoffVisualizationApp();
            Stage handoffStage = new Stage();
            if (!handoffStage.isShowing()) {
                handoffVisualizationApp.start(handoffStage); // Start the handoff simulation in a new stage
            }
        });

        // Add some additional text for better UX
        Text instructionsText = new Text("Click the button to start the Handoff Simulation!");

        // Use a VBox for layout
        VBox root = new VBox(10, instructionsText, startHandoffButton);
        root.setStyle("-fx-padding: 20;");

        // Apply CSS stylesheet
        Scene scene = new Scene(root, 300, 250);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Handoff Simulation Control");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
