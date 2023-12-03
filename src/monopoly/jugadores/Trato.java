package monopoly.jugadores;

import monopoly.casillas.Propiedad;
import monopoly.errores.ErrorComando;

public abstract class Trato {
    private final Jugador interesado; // quien propone el trato
    private final Jugador aceptador; // quien decide si acepta o no


    public Trato(Jugador interaso, Jugador benefactor){
        this.interesado = interaso;
        this.aceptador = benefactor;
    }

    public Jugador getInteresado() {
        return interesado;
    }

    public Jugador getAceptador() {
        return aceptador;
    }

    public abstract void aceptar() throws ErrorComando;
}
