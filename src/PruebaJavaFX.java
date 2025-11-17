import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Clase principal de la aplicación JavaFX.
 * Maneja la construcción de la interfaz gráfica, eventos y actualizaciones de UI.
 */
public class PruebaJavaFX extends Application {

    // Componentes globales para permitir su actualización dinámica
    private VBox card;
    private Label locationLabel, tempLabel, condLabel;
    private VBox infoPanel, dailyList;
    private StackPane rootStack;

    @Override
    public void start(Stage primaryStage) {
        // 1. Configuración del Contenedor Raíz (StackPane)
        // Se utiliza para superponer elementos como el Modal de búsqueda sobre la aplicación.
        rootStack = new StackPane();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        rootStack.getChildren().add(root);

        /* ================= BARRA SUPERIOR ================= */
        HBox top = new HBox(10);
        top.getStyleClass().add("top-bar");
        top.setPadding(new Insets(16, 16, 6, 16));
        top.setAlignment(Pos.CENTER_LEFT);

        locationLabel = new Label("Cargando...");
        locationLabel.getStyleClass().add("location-label");
        locationLabel.setWrapText(true);
        locationLabel.setMaxWidth(200);

        Button changeBtn = new Button("Cambiar");
        changeBtn.getStyleClass().add("change-city-btn");
        changeBtn.setMinWidth(Region.USE_PREF_SIZE); // Evita que el botón se deforme

        ToggleButton darkToggle = new ToggleButton("🌙");
        darkToggle.getStyleClass().add("change-city-btn");
        darkToggle.setOnAction(e -> toggleDarkMode(rootStack, darkToggle.isSelected()));
        darkToggle.setMinWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(locationLabel, spacer, changeBtn, darkToggle);

        /* ================= TARJETA CENTRAL (CLIMA ACTUAL) ================= */
        StackPane centerStack = new StackPane();
        centerStack.setPadding(new Insets(0, 12, 0, 12));

        card = new VBox(6);
        card.getStyleClass().add("center-card");
        card.getStyleClass().add("card-default"); // Estado inicial transparente
        card.setMinHeight(380);
        card.setPrefHeight(380);
        card.setMaxHeight(380);
        card.setAlignment(Pos.CENTER);
        StackPane.setMargin(card, new Insets(0, 0, 12, 0));
        card.maxWidthProperty().bind(root.widthProperty().subtract(24));

        // Configuración del icono principal (SVG)
        SVGPath mainIcon = iconSun();
        mainIcon.setId("main-icon");
        mainIcon.setScaleX(3.5);
        mainIcon.setScaleY(3.5);
        mainIcon.setTranslateY(-20);

        tempLabel = new Label("--");
        tempLabel.getStyleClass().add("center-temp-large");

        condLabel = new Label("Espere...");
        condLabel.getStyleClass().add("center-condition-label");
        condLabel.setWrapText(true);
        condLabel.setTextAlignment(TextAlignment.CENTER);
        condLabel.setMaxWidth(280);

        VBox textContainer = new VBox(4, tempLabel, condLabel);
        textContainer.setAlignment(Pos.CENTER);

        VBox iconAndText = new VBox(15, mainIcon, textContainer);
        iconAndText.setAlignment(Pos.CENTER);
        iconAndText.setId("weather-container");

        card.getChildren().add(iconAndText);
        centerStack.getChildren().add(card);

        /* ================= PANELES DE INFORMACIÓN ================= */
        Label titleInfo = new Label("Detalles del día");
        titleInfo.getStyleClass().add("section-title");
        titleInfo.setPadding(new Insets(0, 16, 5, 16));

        infoPanel = new VBox();
        infoPanel.getStyleClass().add("info-panel");
        infoPanel.maxWidthProperty().bind(root.widthProperty().subtract(24));

        Label titleList = new Label("Próximos 7 días");
        titleList.getStyleClass().add("section-title");
        titleList.setPadding(new Insets(15, 16, 5, 16));

        dailyList = new VBox();
        dailyList.getStyleClass().add("daily-list");
        dailyList.setPadding(new Insets(10, 0, 10, 0));
        dailyList.maxWidthProperty().bind(root.widthProperty().subtract(24));

        // Contenedor vertical con scroll para todo el contenido
        VBox appContent = new VBox(10);
        appContent.setPadding(new Insets(0, 12, 16, 12));
        appContent.getChildren().addAll(top, centerStack, titleInfo, infoPanel, titleList, dailyList);

        ScrollPane mainScroll = new ScrollPane(appContent);
        mainScroll.setFitToWidth(true);
        mainScroll.setPannable(true);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.getStyleClass().add("no-scrollbar");

        root.setCenter(mainScroll);

        // Configuración de la escena
        Scene scene = new Scene(rootStack, 360, 700);
        changeBtn.setOnAction(e -> showCityOverlay(rootStack));
        scene.getStylesheets().add("estilos.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("App Clima");
        primaryStage.setResizable(false);
        primaryStage.show();

        // Carga inicial por defecto
        cargarClima("Salta, Argentina");
    }

    /**
     * Inicia la carga de datos en un hilo separado para evitar congelar la interfaz gráfica.
     */
    private void cargarClima(String ciudadBuscada) {
        new Thread(() -> {
            try {
                // Llamada bloqueante a la API (lógica de negocio)
                List<ClimaDia> datos = PronosticoTiempo.consultarApi(ciudadBuscada);

                // Actualización de la UI en el hilo principal de JavaFX
                Platform.runLater(() -> actualizarInterfaz(datos));
            } catch (Exception e) {
                Platform.runLater(() -> mostrarAlertaError("Error al cargar datos: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Actualiza los elementos visuales con los datos recibidos.
     */
    private void actualizarInterfaz(List<ClimaDia> datos) {
        if (datos.isEmpty()) return;
        ClimaDia hoy = datos.get(0);

        locationLabel.setText("📍 " + hoy.getNombreCiudad());
        tempLabel.setText(Math.round(hoy.getTempActual()) + "°");
        condLabel.setText(hoy.getDescripcion());

        // Actualizamos el estilo de la tarjeta según la condición
        actualizarColorTarjeta(hoy.getCondicionIngles());

        // Actualizamos el icono principal
        SVGPath nuevoIcono = obtenerIconoPorCondicion(hoy.getCondicionIngles());
        nuevoIcono.setId("main-icon");
        nuevoIcono.setScaleX(3.5);
        nuevoIcono.setScaleY(3.5);
        nuevoIcono.setTranslateY(-20);
        nuevoIcono.getStyleClass().add("sun-svg");

        Scene scene = tempLabel.getScene();
        if(scene != null) {
            VBox container = (VBox) scene.lookup("#weather-container");
            if(container != null && !container.getChildren().isEmpty()) {
                container.getChildren().set(0, nuevoIcono);
            }
        }

        // Detalles
        infoPanel.getChildren().clear();
        infoPanel.getChildren().addAll(
                createInfoRow("Máxima:", Math.round(hoy.getTempMax()) + "°"),
                createInfoRow("Mínima:", Math.round(hoy.getTempMin()) + "°"),
                createInfoRow("Humedad:", Math.round(hoy.getHumedad()) + "%"),
                createInfoRow("Sensación:", Math.round(hoy.getSensacionTermica()) + "°")
        );

        // Lista de próximos días
        dailyList.getChildren().clear();
        for (int i = 1; i < datos.size(); i++) {
            ClimaDia dia = datos.get(i);
            String nombreDia = obtenerNombreDia(dia.getFecha());
            SVGPath iconoChico = obtenerIconoPorCondicion(dia.getCondicionIngles());
            String temps = Math.round(dia.getTempMax()) + "° / " + Math.round(dia.getTempMin()) + "°";
            dailyList.getChildren().add(createDayRow(nombreDia, iconoChico, temps));
        }
    }

    // Muestra el modal para cambiar de ciudad
    private void showCityOverlay(StackPane rootStack) {
        Rectangle backdrop = new Rectangle();
        backdrop.setFill(Color.rgb(0, 0, 0, 0.45));
        backdrop.widthProperty().bind(rootStack.widthProperty());
        backdrop.heightProperty().bind(rootStack.heightProperty());

        VBox modal = new VBox(12);
        modal.getStyleClass().add("city-modal");
        modal.setAlignment(Pos.TOP_CENTER);
        modal.setPadding(new Insets(16));
        modal.setMaxWidth(280);
        modal.setMaxHeight(Region.USE_PREF_SIZE);

        if (rootStack.getStyleClass().contains("app-root-dark")) modal.getStyleClass().add("dark");

        Label closeBtn = new Label("✕"); closeBtn.getStyleClass().add("modal-close");
        HBox closeBox = new HBox(closeBtn); closeBox.setAlignment(Pos.TOP_RIGHT);
        Label title = new Label("Cambiar ciudad"); title.getStyleClass().add("modal-title");
        TextField input = new TextField();
        input.setPromptText("Ej: Madrid, España");
        input.getStyleClass().add("city-input");
        input.setPrefWidth(200);
        Label errorLabel = new Label(); errorLabel.getStyleClass().add("modal-error"); errorLabel.setVisible(false);
        Button ok = new Button("Buscar"); Button cancel = new Button("Cancelar");
        ok.getStyleClass().add("change-city-btn"); cancel.getStyleClass().add("change-city-btn");
        HBox buttonRow = new HBox(10, ok, cancel); buttonRow.setAlignment(Pos.CENTER);

        modal.getChildren().addAll(closeBox, title, input, errorLabel, buttonRow);
        StackPane overlay = new StackPane(backdrop, modal); StackPane.setAlignment(modal, Pos.CENTER);
        rootStack.getChildren().add(overlay);

        Runnable closeAction = () -> rootStack.getChildren().remove(overlay);
        closeBtn.setOnMouseClicked(e -> closeAction.run());
        cancel.setOnAction(e -> closeAction.run());

        ok.setOnAction(e -> {
            String city = input.getText().trim();
            if (city.isEmpty()) {
                errorLabel.setText("Ingrese una ciudad válida.");
                errorLabel.setVisible(true);
                return;
            }
            cargarClima(city);
            closeAction.run();
        });

        modal.setOpacity(0); modal.setScaleX(0.9); modal.setScaleY(0.9);
        FadeTransition ft = new FadeTransition(Duration.millis(150), modal); ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(150), modal); st.setToX(1); st.setToY(1);
        new ParallelTransition(ft, st).play();
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void toggleDarkMode(Region rootStack, boolean enable) {
        if (enable) {
            if (!rootStack.getStyleClass().contains("app-root-dark")) rootStack.getStyleClass().add("app-root-dark");
        } else {
            rootStack.getStyleClass().remove("app-root-dark");
        }
    }

    private String obtenerNombreDia(String fechaRaw) {
        try {
            LocalDate fecha = LocalDate.parse(fechaRaw);
            String dia = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            return dia.substring(0, 1).toUpperCase() + dia.substring(1);
        } catch (Exception e) { return fechaRaw; }
    }

    private SVGPath obtenerIconoPorCondicion(String condicion) {
        if (condicion == null) return iconSun();
        condicion = condicion.toLowerCase();
        if (condicion.contains("rain") || condicion.contains("shower") || condicion.contains("lluvia")) return iconRain();
        if (condicion.contains("cloud") || condicion.contains("nublado") || condicion.contains("overcast")) return iconCloud();
        if (condicion.contains("snow") || condicion.contains("nieve")) return iconPartly();
        if (condicion.contains("clear") || condicion.contains("sunny") || condicion.contains("despejado")) return iconSun();
        return iconSun();
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
        Label dayLabel = new Label(day); dayLabel.getStyleClass().add("day-label");
        icon.getStyleClass().add("day-icon"); icon.setScaleX(0.9); icon.setScaleY(0.9);
        Label tempLabel = new Label(temps); tempLabel.getStyleClass().add("temp-label");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(dayLabel, spacer, icon, tempLabel);
        return row;
    }

    private void actualizarColorTarjeta(String condicion) {
        card.getStyleClass().removeAll("card-sunny", "card-rain", "card-cloud", "card-storm", "card-snow", "card-default");
        if (condicion == null) {
            card.getStyleClass().add("card-default");
            return;
        }
        condicion = condicion.toLowerCase();
        if (condicion.contains("rain") || condicion.contains("shower") || condicion.contains("lluvia")) {
            card.getStyleClass().add("card-rain");
        } else if (condicion.contains("thunder") || condicion.contains("tormenta")) {
            card.getStyleClass().add("card-storm");
        } else if (condicion.contains("snow") || condicion.contains("nieve")) {
            card.getStyleClass().add("card-snow");
        } else if (condicion.contains("cloud") || condicion.contains("nublado") || condicion.contains("overcast") || condicion.contains("cubierto")) {
            card.getStyleClass().add("card-cloud");
        } else if (condicion.contains("clear") || condicion.contains("sunny") || condicion.contains("despejado") || condicion.contains("soleado")) {
            card.getStyleClass().add("card-sunny");
        } else {
            card.getStyleClass().add("card-default");
        }
    }

    // Vectores SVG para los iconos climáticos
    private SVGPath iconSun() { SVGPath p = new SVGPath(); p.setContent("M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z M12 1v2 M12 21v2 M4.2 4.2l1.4 1.4 M18.4 18.4l1.4 1.4 M1 12h2 M21 12h2 M4.2 19.8l1.4-1.4 M18.4 5.6l1.4-1.4"); return p; }
    private SVGPath iconCloud() { SVGPath p = new SVGPath(); p.setContent("M20 17.5A4.5 4.5 0 0 0 15.5 13h-1A6 6 0 0 0 6 16"); return p; }
    private SVGPath iconRain() { SVGPath p = new SVGPath(); p.setContent("M20 16.5A4.5 4.5 0 0 0 15.5 12h-1A6 6 0 0 0 6 15 M8 19l1 2 M12 19l1 2 M16 19l1 2"); return p; }
    private SVGPath iconTherm() { SVGPath p = new SVGPath(); p.setContent("M13 3a2 2 0 0 0-4 0v7a3 3 0 1 0 4 0z"); return p; }
    private SVGPath iconPartly() { SVGPath p = new SVGPath(); p.setContent("M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z M18 8h.01"); return p; }

    public static void main(String[] args) { launch(args); }
}