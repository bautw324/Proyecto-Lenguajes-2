import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public class PruebaJavaFX extends Application {

    private VBox card;
    private Label tempLabel, condLabel;
    private StackPane centerStack;

    @Override
    public void start(Stage primaryStage) {

        // 1. CREAMOS EL STACKPANE ROOT PRIMERO (Padre supremo)
        // Esto es clave para que el Dark Mode afecte tanto a la app como al modal
        StackPane rootStack = new StackPane();

        /* =====================================================
         * ROOT DEL CONTENIDO (La App en sí)
         * ===================================================== */
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        // Agregamos la app al stack principal
        rootStack.getChildren().add(root);

        /* =====================================================
         * SECCIÓN SUPERIOR (TOP BAR)
         * ===================================================== */
        HBox top = new HBox(10);
        top.getStyleClass().add("top-bar");
        top.setPadding(new Insets(16, 16, 6, 16));

        Label location = new Label("Salta, Argentina");
        location.getStyleClass().add("location-label");

        Button changeBtn = new Button("Cambiar");
        changeBtn.getStyleClass().add("change-city-btn");

        ToggleButton darkToggle = new ToggleButton("🌙");
        darkToggle.getStyleClass().add("change-city-btn");

        // ARREGLO DARK MODE: Le pasamos 'rootStack' para que aplique la clase al padre de todo
        darkToggle.setOnAction(e -> toggleDarkMode(rootStack, darkToggle.isSelected()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(location, spacer, changeBtn, darkToggle);

        /* =====================================================
         * TARJETA PRINCIPAL (Temperatura actual)
         * ===================================================== */
        centerStack = new StackPane();
        centerStack.setPadding(new Insets(0, 12, 0, 12));

        card = new VBox(6);
        card.getStyleClass().add("center-card");
        card.setMinHeight(380);
        card.setPrefHeight(380);
        card.setMaxHeight(380);
        card.setAlignment(Pos.CENTER);
        StackPane.setMargin(card, new Insets(0, 0, 12, 0));

        // Ajuste de ancho consistente
        card.maxWidthProperty().bind(root.widthProperty().subtract(24));
        card.prefWidthProperty().bind(root.widthProperty().subtract(24));

        // ICONO DEL SOL
        SVGPath sunSvg = new SVGPath();
        sunSvg.setScaleX(1.1);
        sunSvg.setScaleY(1.1);
        sunSvg.setTranslateY(-6);
        sunSvg.setContent(
                "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z " +
                        "M12 1v2 M12 21v2 M4.2 4.2l1.4 1.4 M18.4 18.4l1.4 1.4 " +
                        "M1 12h2 M21 12h2 M4.2 19.8l1.4-1.4 M18.4 5.6l1.4-1.4"
        );
        sunSvg.getStyleClass().add("sun-svg");

        tempLabel = new Label("27°C");
        tempLabel.getStyleClass().add("center-temp-large");

        condLabel = new Label("Soleado");
        condLabel.getStyleClass().add("center-condition-label");

        VBox textContainer = new VBox(4, tempLabel, condLabel);
        textContainer.setAlignment(Pos.CENTER);

        VBox sunAndText = new VBox(0, sunSvg, textContainer);
        sunAndText.setAlignment(Pos.CENTER);

        card.getChildren().add(sunAndText);
        centerStack.getChildren().add(card);


        /* =====================================================
         * PANEL INTERMEDIO (Máx, Mín, Humedad, Sensación)
         * ===================================================== */
        VBox infoPanel = new VBox();
        infoPanel.getStyleClass().add("info-panel");
        infoPanel.maxWidthProperty().bind(root.widthProperty().subtract(24));
        infoPanel.prefWidthProperty().bind(root.widthProperty().subtract(24));

        infoPanel.getChildren().addAll(
                createInfoRow("Máx: ", "29°"),
                createInfoRow("Mín: ", "18°"),
                createInfoRow("Humedad: ", "60%"),
                createInfoRow("Sensación: ", "28°")
        );


        /* =====================================================
         * LISTA DE LOS 8 DÍAS
         * ===================================================== */
        VBox dailyList = new VBox();
        dailyList.getStyleClass().add("daily-list");
        dailyList.setPadding(new Insets(10, 0, 10, 0));
        dailyList.maxWidthProperty().bind(root.widthProperty().subtract(24));
        dailyList.prefWidthProperty().bind(root.widthProperty().subtract(24));

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


        /* =====================================================
         * SCROLL GENERAL (oculta barra)
         * ===================================================== */
        VBox appContent = new VBox(16);
        appContent.setPadding(new Insets(0, 12, 16, 12));
        appContent.getChildren().addAll(top, centerStack, infoPanel, dailyList);

        ScrollPane mainScroll = new ScrollPane(appContent);
        mainScroll.setFitToWidth(true);
        mainScroll.setPannable(true);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.getStyleClass().add("no-scrollbar");

        root.setCenter(mainScroll);


        /* =====================================================
         * ESCENA FINAL
         * ===================================================== */
        // ARREGLO: Usamos rootStack en la escena, no root
        Scene scene = new Scene(rootStack, 360, 700);

        // Acción del botón cambiar
        changeBtn.setOnAction(e ->
                showCityOverlay(rootStack, city -> location.setText(city + ", Argentina"))
        );

        scene.getStylesheets().add("estilos.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("App Del Jodido CLIMAA ☠️☠️");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void toggleDarkMode(Region rootStack, boolean enable) {
        if (enable) {
            if (!rootStack.getStyleClass().contains("app-root-dark")) {
                rootStack.getStyleClass().add("app-root-dark");
            }
        } else {
            rootStack.getStyleClass().remove("app-root-dark");
        }
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox();
        row.getStyleClass().add("info-row");
        Label l = new Label(label); l.getStyleClass().add("info-label");
        Label v = new Label(value); v.getStyleClass().add("info-value");
        row.getChildren().addAll(l, v);
        return row;
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

    private void showCityOverlay(StackPane rootStack, Consumer<String> onCitySelected) {

        // BACKDROP
        Rectangle backdrop = new Rectangle();
        backdrop.setFill(Color.rgb(0, 0, 0, 0.45));
        backdrop.widthProperty().bind(rootStack.widthProperty());
        backdrop.heightProperty().bind(rootStack.heightProperty());

        // MODAL
        VBox modal = new VBox(12);
        modal.getStyleClass().add("city-modal");
        modal.setAlignment(Pos.TOP_CENTER);
        modal.setPadding(new Insets(16));
        modal.setMaxWidth(280);

        // 🔥🔥 ARREGLO CLAVE: Esto evita que el modal ocupe toda la altura
        modal.setMaxHeight(Region.USE_PREF_SIZE);
        // ------------------------------------------------------------

        // DETECTAR DARK MODE EN EL ROOTSTACK
        boolean isDark = rootStack.getStyleClass().contains("app-root-dark");
        if (isDark) modal.getStyleClass().add("dark");

        // Close Button
        Label closeBtn = new Label("✕");
        closeBtn.getStyleClass().add("modal-close");
        HBox closeBox = new HBox(closeBtn);
        closeBox.setAlignment(Pos.TOP_RIGHT);

        // Título e Input
        Label title = new Label("Cambiar ciudad");
        title.getStyleClass().add("modal-title");

        TextField input = new TextField();
        input.setPromptText("Ej: Salta");
        input.getStyleClass().add("city-input");
        input.setPrefWidth(200);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("modal-error");
        errorLabel.setVisible(false);

        Button ok = new Button("Aceptar");
        Button cancel = new Button("Cancelar");
        ok.getStyleClass().add("change-city-btn");
        cancel.getStyleClass().add("change-city-btn");

        HBox buttonRow = new HBox(10, ok, cancel);
        buttonRow.setAlignment(Pos.CENTER);

        modal.getChildren().addAll(closeBox, title, input, errorLabel, buttonRow);

        StackPane overlay = new StackPane(backdrop, modal);
        StackPane.setAlignment(modal, Pos.CENTER);

        rootStack.getChildren().add(overlay);

        // Eventos
        Runnable closeAction = () -> rootStack.getChildren().remove(overlay);
        closeBtn.setOnMouseClicked(e -> closeAction.run());
        cancel.setOnAction(e -> closeAction.run());

        ok.setOnAction(e -> {
            String city = input.getText().trim();
            if (city.isEmpty()) {
                errorLabel.setText("Escribí una ciudad.");
                errorLabel.setVisible(true);
                return;
            }
            List<String> valid = List.of("Salta", "Buenos Aires", "Córdoba", "Jujuy", "Rosario");
            if (!valid.contains(city)) {
                errorLabel.setText("Ciudad no encontrada.");
                errorLabel.setVisible(true);
                return;
            }
            onCitySelected.accept(city);
            closeAction.run();
        });

        // Animación Entrada
        modal.setOpacity(0);
        modal.setScaleX(0.92); modal.setScaleY(0.92);
        FadeTransition ft = new FadeTransition(Duration.millis(150), modal);
        ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(150), modal);
        st.setToX(1); st.setToY(1);
        new ParallelTransition(ft, st).play();
    }


    // --- ICONOS SVG ---
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