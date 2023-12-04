package monopoly.error;

/**
 * Representa un error generado por la ejecución de un
 * comando del Juego.
 * <br>
 * Simplemente, se aborta el comando actual, no es
 * necesario terminar todo el programa.
 *
 * @see ErrorComandoAvatar
 * @see ErrorComandoEdificio
 * @see ErrorComandoEstadoPartida
 * @see ErrorComandoFortuna
 */
public class ErrorComando extends ErrorJuego {
    public ErrorComando(String mensaje) {
        super(mensaje);
    }
}
