package monopoly.error;

import monopoly.jugador.Jugador;

public class ErrorComandoJugador extends ErrorComando{
    private final Jugador jugador;
    public ErrorComandoJugador(String mensaje, Jugador jugador) {
        super(mensaje);
        this.jugador=jugador;
    }


    public Jugador getJugador() {
        return jugador;
    }
}
