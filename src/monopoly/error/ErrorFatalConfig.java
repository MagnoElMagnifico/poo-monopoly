package monopoly.error;

/**
 * Error de configuración del juego.
 * <br>
 * Puede haberse causado porque falta un archivo de
 * configuración o porque su formato es incorrecto.
 *
 * @see monopoly.utilidades.Lector
 */
public class ErrorFatalConfig extends ErrorFatal {
    private final String causa;
    private final String archivo;
    private final int linea;

    public ErrorFatalConfig(String causa, String archivo, int linea) {
        super("%s:%s -> %s".formatted(archivo, linea, causa));

        this.causa = causa;
        this.archivo = archivo;
        this.linea = linea;
    }

    public int getLinea() {
        return linea;
    }

    public String getArchivo() {
        return archivo;
    }

    public String getCausa() {
        return causa;
    }
}
