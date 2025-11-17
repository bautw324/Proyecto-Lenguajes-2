/**
 * Clase modelo que representa la información climática de un día específico.
 * Se utiliza para transportar los datos desde la capa lógica (PronosticoTiempo)
 * hacia la interfaz gráfica.
 */
public class ClimaDia {
    private String nombreCiudad;
    private String fecha;
    private String descripcion;
    private String condicionIngles;
    private double tempActual;
    private double tempMax;
    private double tempMin;
    private double humedad;
    private double sensacionTermica;

    public ClimaDia(String nombreCiudad, String fecha, String descripcion, String condicionIngles,
                    double tempActual, double tempMax, double tempMin, double humedad, double sensacionTermica) {
        this.nombreCiudad = nombreCiudad;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.condicionIngles = condicionIngles;
        this.tempActual = tempActual;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.humedad = humedad;
        this.sensacionTermica = sensacionTermica;
    }

    // Métodos de acceso (Getters)
    public String getNombreCiudad() { return nombreCiudad; }
    public String getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getCondicionIngles() { return condicionIngles; }
    public double getTempActual() { return tempActual; }
    public double getTempMax() { return tempMax; }
    public double getTempMin() { return tempMin; }
    public double getHumedad() { return humedad; }
    public double getSensacionTermica() { return sensacionTermica; }
}