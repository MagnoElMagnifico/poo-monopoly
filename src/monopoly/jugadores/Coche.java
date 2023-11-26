package monopoly.jugadores;

import monopoly.Tablero;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Dado;

public class Coche extends Avatar{

    public Coche(char id, Jugador jugador, Casilla casillaInicial) {
        super(id, jugador, casillaInicial);
    }

    @Override
    public boolean mover(Dado dado, Tablero tablero) {
        return false;
    }

    @Override
    public Casilla moverEspecial(Dado dado, Casilla carcel) {
        return null;
    }

    @Override
    public void cambiarModo() {

    }
}
