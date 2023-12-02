package monopoly.error;

/**
 * Error de formato de un comando:
 *
 * <li>Comando no válido</li>
 * <li>Se esperaban X parámetros, se recibieron Y</li>
 *
 * @see monopoly.Juego
 */
public class ErrorComandoFormato extends ErrorComando {
    public ErrorComandoFormato(String comando) {
        super("\"%s\": comando no válido".formatted(comando));
    }

    public ErrorComandoFormato(int esperados, int recibidos) {
        super("Se esperaba(n) %d parámetro(s), se recibieron %d".formatted(esperados, recibidos));
    }
}
