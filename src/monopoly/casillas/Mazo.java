package monopoly.casillas;

import monopoly.jugadores.Jugador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Mazo {
    private final ArrayList<Carta> cartasComunidad;
    private final ArrayList<Carta> cartasSuerte;

    public Mazo(ArrayList<Carta> cartasComunidad, ArrayList<Carta> cartasSuerte) {
        this.cartasComunidad = cartasComunidad;
        this.cartasSuerte = cartasSuerte;
    }

    public void barajarSuerte() {
        Collections.shuffle(cartasSuerte);
    }

    public void barajarComunidad() {
        Collections.shuffle(cartasComunidad);
    }

    private int preguntarEleccion(Jugador jugador, int max) {
        Scanner scanner = new Scanner(System.in);
        int eleccion = -1;

        while (eleccion < 0 || eleccion >= max) {
            System.out.printf("%s, elige carta (1-%d): ", jugador.getNombre(), max);
            eleccion = scanner.nextInt() - 1;
        }

        return eleccion;
    }

    public void caerSuerte(Jugador jugador) {
        barajarSuerte();
        Carta carta = cartasSuerte.get(preguntarEleccion(jugador, cartasSuerte.size()));
        System.out.print(carta);
        carta.accionCarta();
    }

    public void caerComunidad(Jugador jugador) {
        barajarComunidad();
        Carta carta = cartasComunidad.get(preguntarEleccion(jugador, cartasComunidad.size()));
        System.out.print(carta);
        carta.accionCarta();
    }
}
