package monopoly.casillas.cartas;

import monopoly.Tablero;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;
import monopoly.error.*;

/**
 * La clase CartaComunidad representa una carta del mazo de cartas de comunidad en el juego de Monopoly.
 * Esta clase hereda de la clase Carta y define la acción que se debe realizar al sacar esta carta.
 */

public class CartaComunidad extends Carta {
    /**
     * Crea una nueva instancia de la clase CartaComunidad.
     * Llama al constructor de la clase padre (Carta) para inicializar los atributos heredados.
     */
    public CartaComunidad() {
        super();
    }
    
    /**
     * Realiza la acción correspondiente a la carta de comunidad.
     * La acción puede ser ingresar una cantidad de dinero al jugador o cobrarle una cantidad.
     * Además, puede haber acciones especiales como pagar a otros jugadores.
     */
    @Override
    public void accion() {
        Tablero tablero = getTablero();
        Jugador jugadorTurno = tablero.getJugadorTurno();

        // Se ingresa si la cantidad es positiva y se cobra si es negativa
        long cantidad = 0;

        switch (getId()) {
            case 4 -> cantidad = 2_000_000L;
            case 6 -> cantidad = 500_000L;
            case 9 -> cantidad = 1_000_000L;
            case 1 -> cantidad = -150_000L;
            case 5 -> cantidad = -1_000_000;
            case 8 -> {
                long cantidadPorJugador = 250_000L;
                if (!jugadorTurno.cobrar(cantidadPorJugador * (tablero.getJugadores().size() - 1), true)) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
                    // SinFortuna();
                }

                // NOTA: el pago a otros jugadores no se considera una tasa.
                // No hace falta actualizar las estadísticas del jugadorTurno

                // NOTA: se está ingresando el dinero a cada jugador incluso si el jugador no puede pagarlo

                for (Jugador j : tablero.getJugadores()) {
                    // No ingresar la cantidad a sí mismo
                    if (j.equals(jugadorTurno)) {
                        continue;
                    }

                    j.ingresar(cantidadPorJugador);
                    j.getEstadisticas().anadirPremio(cantidadPorJugador);
                }

                return;
            }

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
            // default -> ErrorLogico()
        }

        if (cantidad > 0) {
            jugadorTurno.ingresar(cantidad);
            jugadorTurno.getEstadisticas().anadirPremio(cantidad);
        } else {
            if (jugadorTurno.cobrar(-cantidad, true)) {
                jugadorTurno.getEstadisticas().anadirTasa(-cantidad);
                tablero.getBanca().ingresar(-cantidad);
            } else {
                Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
                // ErrorLogico
            }
        }
    }
}



