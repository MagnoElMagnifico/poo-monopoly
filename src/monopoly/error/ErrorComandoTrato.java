package monopoly.error;

import monopoly.jugador.Jugador;

public class ErrorComandoTrato extends ErrorComandoJugador {
    public ErrorComandoTrato(String mensaje, Jugador jugador) {
        super(mensaje, jugador);
    }
}
