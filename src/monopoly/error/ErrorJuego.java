package monopoly.error;

import monopoly.Juego;

/**
 * Tipo de excepción básico para todos los errores dentro del juego.
 * <br>
 * Se distinguen varios tipos:
 *
 * <li><b>Fatal</b>: error que impide que el juego continue.</li>
 * <li><b>Comando</b>: error producido por un comando, por lo que este simplemente se cancela.</li>
 *
 * @see ErrorFatal
 * @see ErrorComando
 */
public class ErrorJuego extends Exception {
    public ErrorJuego(String mensaje) {
        super(mensaje);
    }

    public void imprimirMsg() {
        Juego.consola.error(getMessage());
    }
}
