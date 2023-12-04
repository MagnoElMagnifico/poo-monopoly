package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.errores.ErrorComando;
import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;

public abstract class Trato {
    private final String nombre;
    private final Jugador interesado; // quien propone el trato
    private final Jugador aceptador; // quien decide si acepta o no


    public Trato(String nombre, Jugador interesado, Jugador aceptador){
        this.nombre=nombre;
        this.interesado = interesado;
        this.aceptador = aceptador;
    }

    @Override
    public String toString() {
        return "%s: %s ofrece un trato a %s:\n".formatted(nombre,interesado.getNombre(),aceptador.getNombre());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // Aquí se puede usar == dado que char es primitivo
        return obj instanceof Trato && ((Trato) obj).nombre.equalsIgnoreCase(nombre);
    }

    public Jugador getInteresado() {
        return interesado;
    }

    public Jugador getAceptador() {
        return aceptador;
    }

    public abstract void aceptar();

    public String getNombre() {
        return nombre;
    }
}
