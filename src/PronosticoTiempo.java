import java.util.Scanner;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PronosticoTiempo {
    private static final String API_KEY = "25VDM9NS7SXZ6YW54G4TYPYUK"; // SIN ESTE DATO NO FUNCIONA
    private static final Scanner scanner = new Scanner(System.in);

    // Mapa para traducir condiciones del inglés al español
    private static final Map<String, String> TRADUCCION_CONDICIONES = new HashMap<>();

    static {
        // Inicializar el mapa de traducciones
        TRADUCCION_CONDICIONES.put("Clear", "Despejado");
        TRADUCCION_CONDICIONES.put("Sunny", "Soleado");
        TRADUCCION_CONDICIONES.put("Partially cloudy", "Parcialmente nublado");
        TRADUCCION_CONDICIONES.put("Cloudy", "Nublado");
        TRADUCCION_CONDICIONES.put("Overcast", "Cubierto");
        TRADUCCION_CONDICIONES.put("Rain", "Lluvia");
        TRADUCCION_CONDICIONES.put("Light rain", "Lluvia ligera");
        TRADUCCION_CONDICIONES.put("Heavy rain", "Lluvia intensa");
        TRADUCCION_CONDICIONES.put("Showers", "Chubascos");
        TRADUCCION_CONDICIONES.put("Thunderstorm", "Tormenta eléctrica");
        TRADUCCION_CONDICIONES.put("Snow", "Nieve");
        TRADUCCION_CONDICIONES.put("Light snow", "Nieve ligera");
        TRADUCCION_CONDICIONES.put("Heavy snow", "Nieve intensa");
        TRADUCCION_CONDICIONES.put("Fog", "Niebla");
        TRADUCCION_CONDICIONES.put("Mist", "Bruma");
        TRADUCCION_CONDICIONES.put("Windy", "Ventoso");
        TRADUCCION_CONDICIONES.put("Humid", "Húmedo");
        TRADUCCION_CONDICIONES.put("Dry", "Seco");
        TRADUCCION_CONDICIONES.put("Hot", "Caluroso");
        TRADUCCION_CONDICIONES.put("Cold", "Frío");
    }

    public static void main(String[] args) {
        System.out.println("=== PRONÓSTICO DEL TIEMPO SEMANAL ===");
        System.out.print("Ingresa tu ciudad: ");
        String ciudad = scanner.nextLine();

        obtenerPronostico(ciudad);
        scanner.close();
    }

    public static void obtenerPronostico(String ciudad) {
        try {
            String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                    + ciudad + "?unitGroup=metric&key=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                mostrarDatosTiempoCompleto(response.body(), ciudad);
            } else {
                System.out.println("Error: Ciudad no encontrada. Código: " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("Error al conectar con el servicio: " + e.getMessage());
        }
    }

    public static void mostrarDatosTiempoCompleto(String jsonResponse, String ciudad) {
        try {
            // Primero mostramos el pronóstico actual
            mostrarPronosticoActual(jsonResponse, ciudad);

            // Luego mostramos el pronóstico semanal
            mostrarPronosticoSemanal(jsonResponse);

        } catch (Exception e) {
            System.out.println("Error al procesar los datos: " + e.getMessage());
        }
    }

    public static void mostrarPronosticoActual(String jsonResponse, String ciudad) {
        try {
            // Extraer datos del día actual (primer objeto en el array "days")
            String diaActual = extraerDiaEspecifico(jsonResponse, 0);

            if (diaActual != null) {
                String tempActual = extraerValor(diaActual, "\"temp\":");
                String tempMax = extraerValor(diaActual, "\"tempmax\":");
                String tempMin = extraerValor(diaActual, "\"tempmin\":");
                String condicionesIngles = extraerValorTexto(diaActual, "\"conditions\":");
                String condicionesEspanol = traducirCondiciones(condicionesIngles);
                String humedad = extraerValor(diaActual, "\"humidity\":");
                String fecha = extraerValorTexto(diaActual, "\"datetime\":");
                String fechaFormateada = formatearFecha(fecha);

                System.out.println("\n" + "═".repeat(50));
                System.out.println("🌤️ PRONÓSTICO ACTUAL PARA " + ciudad.toUpperCase());
                System.out.println("═".repeat(50));
                System.out.println("📅 Fecha: " + fechaFormateada);
                System.out.println("🌡️ Temperatura actual: " + tempActual + "°C");
                System.out.println("📊 Máxima/Mínima: " + tempMax + "°C / " + tempMin + "°C");
                System.out.println("☁️ Condiciones: " + condicionesEspanol);
                System.out.println("💧 Humedad: " + humedad + "%");
            }
            System.out.println("─".repeat(50));

        } catch (Exception e) {
            System.out.println("Error al obtener pronóstico actual: " + e.getMessage());
        }
    }

    public static void mostrarPronosticoSemanal(String jsonResponse) {
        try {
            System.out.println("\n📅 PRONÓSTICO SEMANAL");
            System.out.println("═".repeat(70));
            System.out.printf("%-12s %-20s %-12s %-12s %-10s\n",
                    "FECHA", "CONDICIONES", "MÁXIMA", "MÍNIMA", "HUMEDAD");
            System.out.println("─".repeat(70));

            // Extraer todos los días (mostramos 7 días)
            for (int i = 0; i < 7; i++) {
                String diaJson = extraerDiaEspecifico(jsonResponse, i);

                if (diaJson != null) {
                    String fecha = extraerValorTexto(diaJson, "\"datetime\":");
                    String tempMax = extraerValor(diaJson, "\"tempmax\":");
                    String tempMin = extraerValor(diaJson, "\"tempmin\":");
                    String condicionesIngles = extraerValorTexto(diaJson, "\"conditions\":");
                    String condicionesEspanol = traducirCondiciones(condicionesIngles);
                    String humedad = extraerValor(diaJson, "\"humidity\":");

                    // Formatear la fecha
                    String fechaFormateada = formatearFecha(fecha);

                    // Mostrar con formato de tabla
                    System.out.printf("%-12s %-20s %-12s %-12s %-10s\n",
                            fechaFormateada, condicionesEspanol, tempMax + "°C", tempMin + "°C", humedad + "%");
                }
            }
            System.out.println("═".repeat(70));
        } catch (Exception e) {
            System.out.println("Error al obtener pronóstico semanal: " + e.getMessage());
        }
    }

    // Método para traducir condiciones del inglés al español
    private static String traducirCondiciones(String condicionesIngles) {
        if (condicionesIngles.equals("N/A")) {
            return "No disponible";
        }

        // Buscar traducción exacta
        String traduccion = TRADUCCION_CONDICIONES.get(condicionesIngles);
        if (traduccion != null) {
            return traduccion;
        }

        // Si no encuentra traducción exacta, buscar por palabras clave
        for (Map.Entry<String, String> entry : TRADUCCION_CONDICIONES.entrySet()) {
            if (condicionesIngles.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Si no hay traducción, devolver el original
        return condicionesIngles;
    }

    // Método para formatear fecha de "YYYY-MM-DD" a "DD/MM"
    private static String formatearFecha(String fecha) {
        try {
            if (fecha.equals("N/A")) return fecha;

            String[] partes = fecha.split("-");
            if (partes.length == 3) {
                return partes[2] + "/" + partes[1];
            }
        } catch (Exception e) {
            // Si hay error, devolver la fecha original
        }
        return fecha;
    }

    private static String extraerDiaEspecifico(String jsonResponse, int indiceDia) {
        try {
            int startIndex = jsonResponse.indexOf("\"days\":[");
            if (startIndex == -1) return null;

            startIndex += 8; // Saltar "\"days\":["

            int currentIndex = startIndex;
            for (int i = 0; i <= indiceDia; i++) {
                if (currentIndex >= jsonResponse.length()) return null;

                int startObj = jsonResponse.indexOf('{', currentIndex);
                if (startObj == -1) return null;

                int endObj = encontrarFinObjeto(jsonResponse, startObj);
                if (endObj == -1) return null;

                if (i == indiceDia) {
                    return jsonResponse.substring(startObj, endObj + 1);
                }

                currentIndex = endObj + 1;
            }
        } catch (Exception e) {
            System.out.println("Error extrayendo día " + indiceDia + ": " + e.getMessage());
        }
        return null;
    }

    private static int encontrarFinObjeto(String json, int start) {
        int count = 1;
        int index = start + 1;

        while (index < json.length() && count > 0) {
            char c = json.charAt(index);
            if (c == '{') count++;
            else if (c == '}') count--;
            index++;
        }

        return (count == 0) ? index - 1 : -1;
    }

    private static String extraerValor(String json, String clave) {
        try {
            int startIndex = json.indexOf(clave);
            if (startIndex == -1) return "N/A";

            startIndex += clave.length();

            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            if (endIndex == -1) return "N/A";

            String valor = json.substring(startIndex, endIndex).trim();

            if (valor.startsWith("\"") && valor.endsWith("\"")) {
                valor = valor.substring(1, valor.length() - 1);
            }

            return valor;
        } catch (Exception e) {
            return "N/A";
        }
    }

    private static String extraerValorTexto(String json, String clave) {
        try {
            int startIndex = json.indexOf(clave);
            if (startIndex == -1) return "N/A";

            startIndex += clave.length();

            int quoteStart = json.indexOf("\"", startIndex);
            if (quoteStart == -1) return "N/A";

            int quoteEnd = json.indexOf("\"", quoteStart + 1);
            if (quoteEnd == -1) return "N/A";

            return json.substring(quoteStart + 1, quoteEnd);
        } catch (Exception e) {
            return "N/A";
        }
    }
}