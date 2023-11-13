package monopoly.utilidades;

import monopoly.casillas.Casilla;

public class EstadisticasCasilla {
    private final Casilla casilla;

    private long alquilerTotalCobrado;
    private int nEstancias;

    public EstadisticasCasilla(Casilla casilla) {
        this.casilla = casilla;

        alquilerTotalCobrado = 0;
        nEstancias = 0;
    }

    @Override
    public String toString() {
        return """
               {
                   alquiler total cobrado: %s
                   número de estancias: %d
               }
               """.formatted(Consola.num(alquilerTotalCobrado), nEstancias);
    }

    public void anadirEstancia() {
        nEstancias++;
    }

    public void anadirCobroAlquiler(long cantidad) {
        alquilerTotalCobrado += cantidad;
    }

    public int getEstancias() {
        return nEstancias;
    }

    public long getAlquilerTotalCobrado() {
        return alquilerTotalCobrado;
    }
}
