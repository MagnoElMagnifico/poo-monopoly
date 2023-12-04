package monopoly.error;

/**
 * Representa un error lógico interno.
 * <br>
 * Ejemplos:
 *
 * <li>Ingresar una cantidad negativa</li>
 * <li>Cobrar una cantidad negativa</li>
 */
public class ErrorFatalLogico extends ErrorFatal {
    public ErrorFatalLogico(String mensaje) {
        super(mensaje);
    }
}
