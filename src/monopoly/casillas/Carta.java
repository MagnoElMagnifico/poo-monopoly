package monopoly.casillas;

import monopoly.Tablero;
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
        // TODO: implementar acción para cada tipo de carta
        switch (id) {
            //case 1 -> System.out.printf("TODO: acción de carta %d con descripción %s\n", id, descripcion);
            case 1 -> {
                System.out.println();
                tablero.getJugadorTurno().ingresar(500000);
            }
            case 2 -> {
                System.out.println();
                tablero.getJugadorTurno().ingresar(1000000);
            }
            case 3 -> {
                System.out.println();
                if (!tablero.getJugadorTurno().cobrar(1500000)) {
                    Consola.error("No tiene suficiente dinero");
                }
            }
            case 4 -> // TODO: pagar por casa, hotel, piscina y pista de deportes
                    System.out.println();
            case 5 -> {
                System.out.println();
                tablero.getJugadorTurno().cobrar(250000L *tablero.getJugadores().size());
                for (int i = 0; i< tablero.getJugadores().size(); i++) {
                    tablero.getJugadores().get(i).ingresar(250000);
                }
            }

            case 6 -> {
                System.out.println();
                tablero.getJugadorTurno().cobrar(150000);
            }
            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }
    }

    /**
     * Función de ayuda que realiza las acciones de las cartas de Comunidad
     */
    private void accionComunidad() {
        // TODO: implementar acción para cada tipo de carta
        switch (id) {
            //case 1 -> System.out.println(descripcion);
            case 1 -> {
                if (!tablero.getJugadorTurno().cobrar(150000)) {
                    Consola.error("No tiene suficiente dinero");
                }
            }
            case 2 -> tablero.getJugadorTurno().ingresar(2000000);
            case 3 -> {
                if (tablero.getJugadorTurno().cobrar(1000000)) {
                    System.out.println("No tiene suficiente dinero");
                }
            }
            case 4 -> tablero.getJugadorTurno().ingresar(500000);
            case 5 -> {
                if (tablero.getJugadorTurno().cobrar(200000L *tablero.getJugadores().size())) {
                    System.out.println("No tiene suficiente dinero");
                }
                for (int i = 0; i< tablero.getJugadores().size(); i++) {
                    tablero.getJugadores().get(i).ingresar(200000);
                }
            }
            case 6 -> tablero.getJugadorTurno().ingresar(1000000);

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }
    }

    public enum TipoCarta {
        Suerte, Comunidad
    }
}
