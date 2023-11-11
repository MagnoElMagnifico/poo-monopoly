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
        switch (id) {
            case 3 -> tablero.getJugadorTurno().ingresar(500_000L);
            case 6 -> tablero.getJugadorTurno().ingresar(1_000_000L);
            case 7 -> {
                if (!tablero.getJugadorTurno().cobrar(1_500_000L)) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
                }
            }
            case 12 -> {
                if (!tablero.getJugadorTurno().cobrar(150_000L)) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
                }
            }
            // TODO: pagar por casa, hotel, piscina y pista de deportes
            case 8 -> Consola.error("No implementado");
            case 10 -> {
                if (!tablero.getJugadorTurno().cobrar(250_000L * tablero.getJugadores().size())) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
                }

                for (Jugador j : tablero.getJugadores()) {
                    j.ingresar(250_000L);
                }
            }

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }
    }

    /**
     * Función de ayuda que realiza las acciones de las cartas de Comunidad
     */
    private void accionComunidad() {
        switch (id) {
            case 4 -> tablero.getJugadorTurno().ingresar(2_000_000L);
            case 6 -> tablero.getJugadorTurno().ingresar(500_000L);
            case 9 -> tablero.getJugadorTurno().ingresar(1_000_000L);
            case 1 -> {
                if (!tablero.getJugadorTurno().cobrar(150_000L)) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
                }
            }
            case 5 -> {
                if (!tablero.getJugadorTurno().cobrar(1_000_000L)) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
                }
            }
            case 8 -> {
                if (!tablero.getJugadorTurno().cobrar(200_000L * tablero.getJugadores().size())) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su acción de comunidad");
                }

                for (Jugador j : tablero.getJugadores()) {
                    j.ingresar(200_000L);
                }
            }

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }
    }

    public enum TipoCarta {
        Suerte, Comunidad
    }
}
