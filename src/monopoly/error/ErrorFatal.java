package monopoly.error;

/**
 * Error genérico del que el Juego no se puede recuperar.
 * <br>
 * Ejemplos:
 * <li>Falta un archivo de configuración</li>
 * <li>Un archivo de configuración no tiene el formato adecuado</li>
 * <li>Error lógico interno</li>
 *
 * @see ErrorFatalLogico
 * @see ErrorFatalConfig
 */
public class ErrorFatal extends ErrorJuego {
    public ErrorFatal(String mensaje) {
        super(mensaje);
    }

    /**
     * Termina la ejecución del programa debido al error
     */
    public void abortar() {
        System.exit(1);
    }
}
