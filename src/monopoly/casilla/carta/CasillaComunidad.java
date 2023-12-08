package monopoly.casilla.carta;

import monopoly.Juego;
import monopoly.error.ErrorFatal;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

import java.util.ArrayList;

public class CasillaComunidad extends CasillaAccion {
    private final ArrayList<CartaComunidad> cartas;

    public CasillaComunidad(int posicion, ArrayList<CartaComunidad> cartas) {
        super(posicion);
        this.cartas = cartas;
    }

    @Override
    public String toString() {
        return """
                Casilla de Caja de Comunidad: Si un jugador cae en esta casilla, tendrá que escoger una carta y
                se realizará su acción específica.
                """;
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) throws ErrorFatal {
        barajar(cartas);
        CartaComunidad carta = cartas.get(preguntarEleccion(jugadorTurno, cartas.size()));
        Juego.consola.imprimir(carta.toString());
        carta.accionCarta();
    }

    @Override
    public String getNombre() {
        return "Comunidad";
    }
}
