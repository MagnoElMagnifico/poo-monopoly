package monopoly.casilla.carta;

import monopoly.error.ErrorFatal;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

/**
 * Representa una carta de Comunidad o Suerte y realiza
 * la acción personalizada para cada una de ellas.
 * <br>
 * Se trata de una clase abstracta porque se desconoce la
 * implementación de cada acción de carta.
 */
public abstract class Carta {
    private final int id;
    private final String descripcion;

    public Carta(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Carta carta)) {
            return false;
        }

        return carta.id == this.id;
    }

    protected int getId() {
        return id;
    }

    protected String getDescripcion() {
        return descripcion;
    }

    /**
     * Método común para aplicar una cantidad al jugador (cobrar o ingresar).
     * Útil para las posibles clases derivadas.
     */
    protected void aplicarCantidad(long cantidad, Jugador jugadorTurno, Jugador banca) throws ErrorFatalLogico {
        if (cantidad > 0) {
            jugadorTurno.ingresar(cantidad);
            jugadorTurno.getEstadisticas().anadirPremio(cantidad);
        } else {
            jugadorTurno.cobrar(-cantidad, banca);
            jugadorTurno.getEstadisticas().anadirTasa(-cantidad);
            banca.ingresar(-cantidad);
        }
    }

    /**
     * Realiza la acción de la carta en concreto
     * <br>
     * <b>NOTA</b>: requerida por la especificación de la entrega 3.
     *
     * @throws ErrorFatal En caso de que se intente ejecutar la acción
     *                    de una carta con un ID no soportado.
     */
    public abstract void accionCarta() throws ErrorFatal;
}
