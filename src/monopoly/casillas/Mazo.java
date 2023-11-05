package monopoly.casillas;

import monopoly.jugadores.Jugador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Clase que almacena y gestiona las cartas de comunidad y suerte.
 *
 * @author Marcos Granja Grille
 * @date 5-11-2023
 * @see Carta
 */
public class Mazo {
    private final ArrayList<Carta> cartasComunidad;
    private final ArrayList<Carta> cartasSuerte;

    public Mazo(ArrayList<Carta> cartasComunidad, ArrayList<Carta> cartasSuerte) {
        this.cartasComunidad = cartasComunidad;
        this.cartasSuerte = cartasSuerte;
    }

    /**
     * Reordena aleatoriamente las cartas de suerte
     */
    public void barajarSuerte() {
        Collections.shuffle(cartasSuerte);
    }

    /**
     * Reordena aleatoriamente las cartas de comunidad
     */
    public void barajarComunidad() {
        Collections.shuffle(cartasComunidad);
    }

    /**
     * Función de ayuda que pregunta y devuelve una elección de carta al usuario
     */
    private int preguntarEleccion(Jugador jugador, int max) {
        Scanner scanner = new Scanner(System.in);
        int eleccion = -1;

        while (eleccion < 0 || eleccion >= max) {
            System.out.printf("%s, elige carta (1-%d): ", jugador.getNombre(), max);
            eleccion = scanner.nextInt() - 1;
        }

        return eleccion;
    }

    /**
     * Realiza los pasos requeridos de cuando un avatar cae en una casilla de suerte
     */
    public void caerSuerte(Jugador jugador) {
        barajarSuerte();
        Carta carta = cartasSuerte.get(preguntarEleccion(jugador, cartasSuerte.size()));
        System.out.print(carta);
        carta.accionCarta();
    }

    /**
     * Realiza los pasos requeridos de cuando un avatar cae en una casilla de comunidad
     */
    public void caerComunidad(Jugador jugador) {
        barajarComunidad();
        Carta carta = cartasComunidad.get(preguntarEleccion(jugador, cartasComunidad.size()));
        System.out.print(carta);
        carta.accionCarta();
    }
}
