import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase encargada de la lógica de negocio y conexión con la API externa.
 * Realiza las peticiones HTTP y procesa la respuesta JSON manualmente.
 */
public class PronosticoTiempo {
    private static final String API_KEY = "25VDM9NS7SXZ6YW54G4TYPYUK";

    // Mapa estático para traducir las condiciones climáticas al español
    private static final Map<String, String> TRADUCCION_CONDICIONES = new HashMap<>();
    static {
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
        TRADUCCION_CONDICIONES.put("Fog", "Niebla");
        TRADUCCION_CONDICIONES.put("Mist", "Bruma");
        TRADUCCION_CONDICIONES.put("Windy", "Ventoso");
    }

    /**
     * Realiza la petición GET a la API de Visual Crossing.
     * @param ciudad Nombre de la ciudad a consultar.
     * @return Lista de objetos ClimaDia con el pronóstico.
     * @throws Exception Si la conexión falla o la ciudad no existe.
     */
    public static List<ClimaDia> consultarApi(String ciudad) throws Exception {
        // Codificamos la ciudad para evitar errores con espacios o caracteres especiales en la URL
        String ciudadCodificada = URLEncoder.encode(ciudad, StandardCharsets.UTF_8);

        // Se agrega el parámetro lang=es para solicitar datos localizados
        String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                + ciudadCodificada + "?unitGroup=metric&lang=es&key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new Exception("Error API: Código " + response.statusCode());
        }

        return procesarJson(response.body(), ciudad);
    }

    /**
     * Parsea la respuesta JSON cruda y la convierte en objetos Java.
     * Utiliza expresiones regulares y búsqueda de cadenas para extraer los valores.
     */
    private static List<ClimaDia> procesarJson(String jsonResponse, String ciudadBuscada) {
        List<ClimaDia> listaDias = new ArrayList<>();

        // Intento de extracción robusta del nombre completo de la ubicación
        String ciudadReal = extraerDireccionManual(jsonResponse);

        // Si la API no devuelve una dirección resuelta, utilizamos la ingresada por el usuario
        if (ciudadReal.isEmpty()) {
            ciudadReal = ciudadBuscada;
        }

        // Formateamos el nombre para que cada palabra comience con mayúscula
        ciudadReal = capitalizarTexto(ciudadReal);

        // Separamos el JSON en bloques por cada día
        List<String> diasJson = separarDias(jsonResponse);
        int limite = Math.min(diasJson.size(), 8);

        for (int i = 0; i < limite; i++) {
            String diaJson = diasJson.get(i);

            // Extracción de datos usando patrones Regex para mayor precisión
            String fecha = extraerConRegex(diaJson, "\"datetime\"\\s*:\\s*\"([^\"]+)\"");
            String condIngles = extraerConRegex(diaJson, "\"conditions\"\\s*:\\s*\"([^\"]+)\"");

            double temp = extraerDoubleRegex(diaJson, "\"temp\"\\s*:\\s*([-0-9.]+)");
            double max = extraerDoubleRegex(diaJson, "\"tempmax\"\\s*:\\s*([-0-9.]+)");
            double min = extraerDoubleRegex(diaJson, "\"tempmin\"\\s*:\\s*([-0-9.]+)");
            double hum = extraerDoubleRegex(diaJson, "\"humidity\"\\s*:\\s*([-0-9.]+)");
            double sensacion = extraerDoubleRegex(diaJson, "\"feelslike\"\\s*:\\s*([-0-9.]+)");

            String condEspanol = traducirCondiciones(condIngles);

            ClimaDia dia = new ClimaDia(ciudadReal, fecha, condEspanol, condIngles, temp, max, min, hum, sensacion);
            listaDias.add(dia);
        }
        return listaDias;
    }

    // Busca la clave "resolvedAddress" manualmente para evitar errores con comas o formatos inesperados
    private static String extraerDireccionManual(String json) {
        try {
            String key = "\"resolvedAddress\"";
            int idx = json.indexOf(key);
            if (idx == -1) return "";

            int colon = json.indexOf(":", idx);
            int quoteStart = json.indexOf("\"", colon + 1);
            int quoteEnd = json.indexOf("\"", quoteStart + 1);

            if (quoteStart != -1 && quoteEnd != -1) {
                return json.substring(quoteStart + 1, quoteEnd);
            }
        } catch (Exception e) { return ""; }
        return "";
    }

    private static String capitalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        String[] palabras = texto.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : palabras) {
            if (p.length() > 0) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    // Métodos auxiliares de extracción con Regex
    private static String extraerConRegex(String json, String regexPattern) {
        try {
            Pattern p = Pattern.compile(regexPattern);
            Matcher m = p.matcher(json);
            if (m.find()) return m.group(1);
        } catch(Exception e) {}
        return "";
    }

    private static double extraerDoubleRegex(String json, String regexPattern) {
        try {
            Pattern p = Pattern.compile(regexPattern);
            Matcher m = p.matcher(json);
            if (m.find()) return Double.parseDouble(m.group(1));
        } catch(Exception e) {}
        return 0.0;
    }

    // Separa el array de días del JSON en Strings individuales
    private static List<String> separarDias(String json) {
        List<String> dias = new ArrayList<>();
        int start = json.indexOf("\"days\"");
        if (start == -1) return dias;
        int open = json.indexOf("[", start);
        if (open == -1) return dias;
        int bal = 0, sObj = -1;
        for (int i = open; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if(bal==0) sObj=i; bal++; }
            else if (c == '}') { bal--; if(bal==0 && sObj!=-1) { dias.add(json.substring(sObj, i+1)); sObj=-1; } }
            else if (c == ']' && bal==0) break;
        }
        return dias;
    }

    private static String traducirCondiciones(String c) {
        for (Map.Entry<String, String> e : TRADUCCION_CONDICIONES.entrySet()) {
            if (c.contains(e.getKey())) return e.getValue();
        }
        return c;
    }
}