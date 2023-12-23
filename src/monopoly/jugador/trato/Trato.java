package monopoly.jugador.trato;

import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;
import monopoly.utils.Buscar;
import monopoly.utils.Listable;

public abstract class Trato implements Listable, Buscar {
    private static int ultimoTrato = 1;

    private final String nombre;  // tiene que ser único
    private final Jugador jugPropone; // quien propone el trato
    private final Jugador jugAcepta; // quien decide si acepta o no
    private boolean aceptado;

    public Trato(Jugador jugPropone, Jugador jugAcepta) {
        this.nombre = "Trato-" + ultimoTrato;
        ultimoTrato++;

        this.jugPropone = jugPropone;
        this.jugAcepta = jugAcepta;
        this.aceptado = false;
    }

    @Override
    public String listar() {
        return this.toString();
    }

    @Override
    public String toString() {
        return """
               nombre: %s
               estado: %s
               propuesto por: %s
               propuesto a: %s
               """.formatted(nombre, aceptado ? "Aceptado" : "En espera", jugPropone.getNombre(), jugAcepta.getNombre());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Trato && ((Trato) obj).nombre.equalsIgnoreCase(nombre);
    }

    public Jugador getJugadorPropone() {
        return jugPropone;
    }

    public Jugador getJugadorAcepta() {
        return jugAcepta;
    }


    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico, ErrorComandoTrato {
        aceptado = true;

    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public boolean isAceptado() {
        return aceptado;
    }
}
