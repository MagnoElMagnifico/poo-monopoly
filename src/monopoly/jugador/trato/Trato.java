package monopoly.jugador.trato;

import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public abstract class Trato {
    private static int ultimoTrato = 1;

    private final String nombre;  // tiene que ser único
    private final Jugador interesado; // quien propone el trato
    private final Jugador aceptador; // quien decide si acepta o no
    private boolean completado;

    public Trato(Jugador interesado, Jugador aceptador) {
        this.nombre = "Trato-" + ultimoTrato;
        ultimoTrato++;

        this.interesado = interesado;
        this.aceptador = aceptador;
        this.completado = false;
    }

    @Override
    public String toString() {
        return "Estado: %s\n%s: %s ofrece un trato a %s".formatted(completado ?"aceptado": "En espera",nombre, interesado.getNombre(), aceptador.getNombre());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Trato && ((Trato) obj).nombre.equalsIgnoreCase(nombre);
    }

    public Jugador getInteresado() {
        return interesado;
    }

    public Jugador getAceptador() {
        return aceptador;
    }


    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        completado = true;

    }

    public String getNombre() {
        return nombre;
    }

    public boolean isCompletado() {
        return completado;
    }
}
