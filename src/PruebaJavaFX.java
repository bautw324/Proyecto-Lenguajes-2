import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PruebaJavaFX extends Application {

    private VBox card;
    private Label tempLabel, condLabel, feelsLabel;
    private StackPane centerStack;
    private boolean darkMode = false;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        // TOP
        HBox top = new HBox(10);
        top.getStyleClass().add("top-bar");
        top.setPadding(new Insets(16, 16, 6, 16));
        Label location = new Label("Salta, Argentina");
        location.getStyleClass().add("location-label");
        Button changeBtn = new Button("Cambiar");
        changeBtn.getStyleClass().add("change-city-btn");

        // Dark mode toggle
        ToggleButton darkToggle = new ToggleButton("🌙");
        darkToggle.getStyleClass().add("change-city-btn");
        darkToggle.setOnAction(e -> toggleDarkMode(root, darkToggle.isSelected()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(location, spacer, changeBtn, darkToggle);
        root.setTop(top);

        // CENTER CARD (tarjeta amarilla)
        centerStack = new StackPane();

        // 💥 FIX MÁS IMPORTANTE: sin padding lateral
//        centerStack.setPadding(new Insets(0));
        centerStack.setPadding(new Insets(0, 12, 0, 12));


        card = new VBox(6);
        card.getStyleClass().add("center-card");
        card.setPrefHeight(260);
        card.setMaxHeight(260);
        card.setAlignment(Pos.CENTER);

        // 💥 SEGUNDO FIX: la tarjeta ocupa todo el ancho
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(Double.MAX_VALUE);
        StackPane.setAlignment(card, Pos.CENTER);

        // SVG Sol (vector) - Opción A
        SVGPath sunSvg = new SVGPath();
        sunSvg.setContent(
                "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z " +
                        "M12 1v2 M12 21v2 M4.2 4.2l1.4 1.4 M18.4 18.4l1.4 1.4 " +
                        "M1 12h2 M21 12h2 M4.2 19.8l1.4-1.4 M18.4 5.6l1.4-1.4"
        );
        sunSvg.getStyleClass().add("sun-svg");

        // Big temp and texts
        tempLabel = new Label("27°C");
        tempLabel.getStyleClass().add("center-temp-large");

        condLabel = new Label("Soleado");
        condLabel.getStyleClass().add("center-condition-label");

        feelsLabel = new Label("Sensación térmica 28°C");
        feelsLabel.getStyleClass().add("center-feels-like");

        VBox textContainer = new VBox(4, tempLabel, condLabel, feelsLabel);
        textContainer.setAlignment(Pos.CENTER);
        textContainer.getStyleClass().add("center-text-content");

        // put sun + texts (sun above)
        VBox sunAndText = new VBox(0, sunSvg, textContainer);
        sunAndText.setAlignment(Pos.CENTER);
        card.getChildren().add(sunAndText);

//        centerStack.getChildren().add(card);
        centerStack.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(0));

        card.maxWidthProperty().bind(centerStack.widthProperty());


        root.setCenter(centerStack);

        // BOTTOM -> lista con scroll
        VBox dailyList = new VBox();
        dailyList.getStyleClass().add("daily-list");
        dailyList.setPadding(new Insets(8));

        dailyList.getChildren().addAll(
                createDayRow("Miércoles", iconCloud(), "22° / 12°"),
                createDayRow("Jueves", iconRain(), "20° / 10°"),
                createDayRow("Viernes", iconSun(), "24° / 14°"),
                createDayRow("Sábado", iconTherm(), "28° / 18°"),
                createDayRow("Domingo", iconPartly(), "27° / 17°"),
                createDayRow("Lunes", iconRain(), "19° / 11°"),
                createDayRow("Martes", iconSun(), "23° / 12°"),
                createDayRow("Miércoles", iconCloud(), "21° / 13°")
        );



        ScrollPane scroll = new ScrollPane(dailyList);
        scroll.getStyleClass().add("bottom-scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setBottom(scroll);

        // Animación al presionar "Cambiar" (ejemplo visual)
        changeBtn.setOnAction(_ -> {
            // animación de salida
            TranslateTransition out = new TranslateTransition(Duration.millis(300), card);
            out.setByY(-30);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
            fadeOut.setToValue(0.0);

            out.setOnFinished(ev -> {
                // aquí iría la actualización de datos con la API antes de mostrar
                tempLabel.setText("19°C");
                condLabel.setText("Nublado");
                feelsLabel.setText("Sensación térmica 18°C");

                // animación de entrada
                TranslateTransition in = new TranslateTransition(Duration.millis(350), card);
                in.setFromY(30);
                in.setToY(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
                fadeIn.setToValue(1.0);

                ParallelTransition pIn = new ParallelTransition(in, fadeIn);
                pIn.play();
            });

            ParallelTransition pOut = new ParallelTransition(out, fadeOut);
            pOut.play();
        });

        Scene scene = new Scene(root, 360, 700);
        scene.getStylesheets().add("estilos.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("App Del Jodido CLIMAA ☠️☠️");
        primaryStage.setResizable(false);
        primaryStage.show();



    }


    // Toggle dark mode (solo cambia clase raíz)
    private void toggleDarkMode(Region root, boolean enable) {
        darkMode = enable;
        if (enable) {
            root.getStyleClass().remove("app-root");
            root.getStyleClass().add("app-root-dark");
        } else {
            root.getStyleClass().remove("app-root-dark");
            root.getStyleClass().add("app-root");
        }
    }

    private HBox createDayRow(String day, SVGPath icon, String temps) {
        HBox row = new HBox(12);
        row.getStyleClass().add("day-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));

        Label dayLabel = new Label(day);
        dayLabel.getStyleClass().add("day-label");

        icon.getStyleClass().add("day-icon");
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        Label tempLabel = new Label(temps);
        tempLabel.getStyleClass().add("temp-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(dayLabel, spacer, icon, tempLabel);
        return row;
    }

    // SVG icons (vectoriales). Podés reemplazarlos por paths más complejos si querés.
    private SVGPath iconSun() {
        SVGPath p = new SVGPath();
        p.setContent("M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z");
        return p;
    }

    private SVGPath iconCloud() {
        SVGPath p = new SVGPath();
        p.setContent("M20 17.5A4.5 4.5 0 0 0 15.5 13h-1A6 6 0 0 0 6 16");
        return p;
    }

    private SVGPath iconRain() {
        SVGPath p = new SVGPath();
        p.setContent("M20 16.5A4.5 4.5 0 0 0 15.5 12h-1A6 6 0 0 0 6 15 M8 19l1 2 M12 19l1 2 M16 19l1 2");
        return p;
    }

    private SVGPath iconTherm() {
        SVGPath p = new SVGPath();
        p.setContent("M13 3a2 2 0 0 0-4 0v7a3 3 0 1 0 4 0z");
        return p;
    }

    private SVGPath iconPartly() {
        SVGPath p = new SVGPath();
        p.setContent("M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z M18 8h.01");
        return p;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
