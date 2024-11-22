package com.example.assignment;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HandoffVisualizationApp extends Application {
    final double BASE_STATION_RADIUS = 80; // Coverage area of Base Station
    final double UE_RADIUS = 10; // Radius of the UE which moves
    // Defining the base stations positions in the frame
    double[] bsPosX = {150, 400, 650};
    double[] bsPosY = {150, 300, 450};

    // UE positions and speed
    double ueX = 100, ueY = 100;
    double ueSpeedX = 2, ueSpeedY = 2;
    // Signal-to-Interference-plus-Noise Ratio (SINR)
    double[] sinr = {0, 0, 0};

    // Current Serving Base Station
    int currentServingBS = 0;
    // Load Balancing
    int[] towerLoad = {0, 0, 0};
    // Obstacles with different shapes
    double[][] obstacles = {
            {300, 200, 50}, // Obstacle 1 (x, y, radius)
            {500, 350, 60}  // Obstacle 2 (x, y, radius)
    };
    // Dashboard Elements
    Label statusLabel;
    Label loadLabel;
    final double SINR_THRESHOLD = 0.2;
    final double HYSTERESIS_MARGIN = 0.1;

    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();
        Scene scene = new Scene(pane, 800, 600);

        // Base stations with coverage areas
        Circle[] baseStations = new Circle[bsPosX.length];
        Circle[] coverageCircles = new Circle[bsPosX.length];
        for (int i = 0; i < bsPosX.length; i++) {
            baseStations[i] = new Circle(bsPosX[i], bsPosY[i], 20, Color.GREEN); // Tower
            coverageCircles[i] = new Circle(bsPosX[i], bsPosY[i], BASE_STATION_RADIUS, Color.rgb(100, 255, 100, 0.3)); // Coverage
            pane.getChildren().addAll(coverageCircles[i], baseStations[i]);
        }

        // Obstacles with enhanced visuals (polygonal shapes for diversity)
        for (double[] obs : obstacles) {
            // Creating irregular obstacles using Polygon for better visualization
            Polygon obstacle = createObstacle(obs[0], obs[1], obs[2]);
            obstacle.setFill(Color.GRAY);
            obstacle.setOpacity(0.5); // Slight transparency for better visibility
            pane.getChildren().add(obstacle);
        }

        // UE
        Circle ue = new Circle(ueX, ueY, UE_RADIUS);
        ue.setFill(Color.BLUE);
        pane.getChildren().add(ue);

        // SINR bars
        Rectangle[] sinrBars = new Rectangle[bsPosX.length];
        for (int i = 0; i < sinrBars.length; i++) {
            sinrBars[i] = new Rectangle(bsPosX[i] - 40, 550, 20, 0);
            sinrBars[i].setFill(Color.BLUE);
            pane.getChildren().add(sinrBars[i]);
        }

        // Dashboard Labels
        statusLabel = new Label("Current BS: Base Station 1");
        loadLabel = new Label("Tower Loads: [0, 0, 0]");
        statusLabel.setLayoutX(10);
        statusLabel.setLayoutY(10);
        loadLabel.setLayoutX(10);
        loadLabel.setLayoutY(30);
        pane.getChildren().addAll(statusLabel, loadLabel);

        // Control Button
        Button pauseButton = new Button("Pause/Resume");
        pauseButton.setLayoutX(10);
        pauseButton.setLayoutY(50);
        pane.getChildren().add(pauseButton);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            // Update UE position
            ueX += ueSpeedX;
            ueY += ueSpeedY;
            if (ueX > 800 || ueX < 0) ueSpeedX = -ueSpeedX;
            if (ueY > 600 || ueY < 0) ueSpeedY = -ueSpeedY;
            ue.setCenterX(ueX);
            ue.setCenterY(ueY);

            // Calculate SINR with obstacle effects
            double totalInterference = 0;
            for (int i = 0; i < bsPosX.length; i++) {
                double distance = calculateDistance(ueX, ueY, bsPosX[i], bsPosY[i]);
                double obstacleEffect = getObstacleEffect(ueX, ueY, bsPosX[i], bsPosY[i]);
                sinr[i] = 1 / (distance * distance * obstacleEffect); // Adjust SINR with obstacle effect
                totalInterference += sinr[i];
            }
            for (int i = 0; i < sinr.length; i++) {
                sinr[i] /= totalInterference; // Normalize SINR
                sinrBars[i].setHeight(sinr[i] * 1000);
                sinrBars[i].setY(550 - sinrBars[i].getHeight());
            }

            // Handoff
            int newServingBS = getBestBaseStation();
            if (newServingBS != currentServingBS &&
                    sinr[newServingBS] > sinr[currentServingBS] + HYSTERESIS_MARGIN) {
                // Update visuals and state
                baseStations[currentServingBS].setFill(Color.GREEN);
                baseStations[newServingBS].setFill(Color.RED);
                currentServingBS = newServingBS;
                towerLoad[newServingBS]++;
                statusLabel.setText("Current BS: Base Station " + (newServingBS + 1));
                loadLabel.setText("Tower Loads: " + java.util.Arrays.toString(towerLoad));
            }

            // Show obstacle effect
            for (double[] obs : obstacles) {
                double obsX = obs[0], obsY = obs[1], obsR = obs[2];
                if (calculateDistance(ueX, ueY, obsX, obsY) < obsR) {
                    // Signal degradation effect when near an obstacle
                    for (int i = 0; i < sinr.length; i++) {
                        // Reduce SINR when UE passes near an obstacle
                        sinr[i] *= 0.5; // Signal weakened by 50%
                        sinrBars[i].setHeight(sinr[i] * 1000);
                        sinrBars[i].setY(550 - sinrBars[i].getHeight());
                    }
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Pause/Resume button functionality
        pauseButton.setOnAction(e -> {
            if (timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.pause();
            } else {
                timeline.play();
            }
        });

        stage.setTitle("Handoff Visualization");
        stage.setScene(scene);
        stage.show();
    }

    private int getBestBaseStation() {
        double maxSinr = -Double.MAX_VALUE;
        int bestBS = 0;
        for (int i = 0; i < sinr.length; i++) {
            if (sinr[i] > maxSinr) {
                maxSinr = sinr[i];
                bestBS = i;
            }
        }
        return bestBS;
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private double getObstacleEffect(double ueX, double ueY, double bsX, double bsY) {
        // Calculate the effect of obstacles on the signal
        for (double[] obs : obstacles) {
            double obsX = obs[0], obsY = obs[1], obsR = obs[2];
            if (calculateDistance(ueX, ueY, obsX, obsY) < obsR) {
                return 0.5; // Signal degraded by 50% near obstacle
            }
        }
        return 1.0; // No effect if no obstacle nearby
    }

    private Polygon createObstacle(double x, double y, double radius) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI * i / 6;
            polygon.getPoints().addAll(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
        }
        return polygon;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
