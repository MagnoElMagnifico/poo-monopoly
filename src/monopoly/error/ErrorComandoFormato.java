package monopoly.error;

/**
 * Error de formato de un comando:
 *
 * <li>Comando no válido</li>
 * <li>Se esperaban X parámetros, se recibieron Y</li>
 * <li>No se encontró X (listar, describir)</li>
 *
 * @see monopoly.Juego
 */
public class ErrorComandoFormato extends ErrorFatal {
    public ErrorComandoFormato(String mensaje) {
        super(mensaje);
    }

    public ErrorComandoFormato(int esperados, int recibidos) {
        super("Se esperaba(n) %d parámetro(s), se recibieron %d".formatted(esperados, recibidos));
    }
}
