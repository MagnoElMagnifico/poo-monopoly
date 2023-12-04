package monopoly.error;

import monopoly.jugador.Jugador;

/**
 * Cualquier error relacionado con la fortuna del jugador,
 * compras, edificar, vender, etc.
 * <br>
 * Ejemplos:
 *
 * <li>El jugador está endeudado</li>
 * <li>Ya has comprado esta propiedad</li>
 * <li>No se puede comprar esta casilla</li>
 * <li>No hay suficiente fortuna</li>
 * <li>Ya ha realizado una compra en este turno</li>
 */
public class ErrorComandoFortuna extends ErrorComando {
    /** Jugador que causó el problema */
    private final Jugador jugador;

    public ErrorComandoFortuna(String mensaje, Jugador jugador) {
        super(mensaje);
        this.jugador = jugador;
    }

    public Jugador getJugador() {
        return jugador;
    }
}
