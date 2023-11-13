package monopoly.casillas;

import monopoly.Tablero;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

/**
 * Representa una carta de Comunidad o Suerte y realiza
 * la acción personalizada para cada una de ellas.
 *
 * @author Marcos Granja Grille
 * @date 05-11-2023
 * @see Mazo
 */
public class Carta {
    private final int id;
    private final TipoCarta tipo;
    private final String descripcion;
    private final Tablero tablero;

    public Carta(Tablero tablero, int id, TipoCarta tipo, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.tablero = tablero;
    }

    @Override
    public String toString() {
        // TODO?: Mejorar presentación de la carta
        return "%s: %s\n".formatted(Consola.fmt(tipo == TipoCarta.Suerte ? "Carta de Suerte" : "Carta de Comunidad", Consola.Color.Azul), descripcion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Carta carta)) {
            return false;
        }

        return carta.id == this.id && carta.tipo == this.tipo;
    }

    /**
     * Realiza la acción de la carta en concreto
     */
    public void accionCarta() {
        switch (tipo) {
            case Suerte -> accionSuerte();
            case Comunidad -> accionComunidad();
        }
    }

    /**
     * Función de ayuda que realiza las acciones de las cartas de Suerte
     */
    private void accionSuerte() {
        Jugador jugadorTurno = tablero.getJugadorTurno();

        // Se ingresa si la cantidad es positiva y se cobra si es negativa
        long cantidad = 0;

        switch (id) {
            case 3 -> cantidad = 500_000L;
            case 6 -> cantidad = 1_000_000L;
            case 7 -> cantidad = -1_500_000L;
            case 12 -> cantidad = -150_000L;
            case 8 -> {
                for (Propiedad p : jugadorTurno.getPropiedades()) {
                    for (Edificio e : p.getEdificios()) {
                        cantidad -= switch (e.getTipo()) {
                            case Casa -> 4_000_000L;
                            case Hotel -> 1_500_000L;
                            case Piscina -> 200_000L;
                            case PistaDeporte -> 750_000L;
                        };
                    }
                }
            }
            case 10 -> {
                long cantidadPorJugador = 250_000L;
                if (!jugadorTurno.cobrar(cantidadPorJugador * tablero.getJugadores().size())) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
                }

                // NOTA: el pago a otros jugadores no se considera una tasa.
                // No hace falta actualizar las estadísticas del jugadorTurno

                // NOTA: se está ingresando el dinero a cada jugador incluso si el jugador no puede pagarlo

                for (Jugador j : tablero.getJugadores()) {
                    j.ingresar(cantidadPorJugador);
                    j.getEstadisticas().anadirPremio(cantidadPorJugador);
                }

                return;
            }

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }

        if (cantidad > 0) {
            jugadorTurno.ingresar(cantidad);
            jugadorTurno.getEstadisticas().anadirPremio(cantidad);
        } else {
            if (jugadorTurno.cobrar(-cantidad)) {
                jugadorTurno.getEstadisticas().anadirTasa(-cantidad);
                tablero.getBanca().ingresar(-cantidad);
            } else {
                Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
            }
        }
    }

    /**
     * Función de ayuda que realiza las acciones de las cartas de Comunidad
     */
    private void accionComunidad() {
        Jugador jugadorTurno = tablero.getJugadorTurno();

        // Se ingresa si la cantidad es positiva y se cobra si es negativa
        long cantidad = 0;

        switch (id) {
            case 4 -> cantidad = 2_000_000L;
            case 6 -> cantidad = 500_000L;
            case 9 -> cantidad = 1_000_000L;
            case 1 -> cantidad = -150_000L;
            case 5 -> cantidad = -1_000_000;
            case 8 -> {
                long cantidadPorJugador = 250_000L;
                if (!jugadorTurno.cobrar(cantidadPorJugador * tablero.getJugadores().size())) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
                }

                // NOTA: el pago a otros jugadores no se considera una tasa.
                // No hace falta actualizar las estadísticas del jugadorTurno

                // NOTA: se está ingresando el dinero a cada jugador incluso si el jugador no puede pagarlo

                for (Jugador j : tablero.getJugadores()) {
                    j.ingresar(cantidadPorJugador);
                    j.getEstadisticas().anadirPremio(cantidadPorJugador);
                }

                return;
            }

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }

        if (cantidad > 0) {
            jugadorTurno.ingresar(cantidad);
            jugadorTurno.getEstadisticas().anadirPremio(cantidad);
        } else {
            if (jugadorTurno.cobrar(-cantidad)) {
                jugadorTurno.getEstadisticas().anadirTasa(-cantidad);
                tablero.getBanca().ingresar(-cantidad);
            } else {
                Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
            }
        }
    }

    public enum TipoCarta {
        Suerte, Comunidad
    }
}
