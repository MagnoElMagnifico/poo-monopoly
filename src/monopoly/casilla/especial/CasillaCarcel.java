package monopoly.casilla.especial;

import monopoly.Juego;
import monopoly.error.ErrorFatal;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class CasillaCarcel extends CasillaEspecial {
    private long fianza;

    public CasillaCarcel(int posicion) {
        super(posicion);
        fianza = -1;
    }

    public long getFianza() {
        return fianza;
    }

    public void setFianza(long fianza) {
        this.fianza = fianza;
    }

    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    fianza: %s
                }""".formatted(getNombreFmt(), Juego.consola.num(fianza));
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) throws ErrorFatal {
        if (fianza <= 0) {
            throw new ErrorFatal("No se ha asignado una fianza a la cárcel");
        }

        Juego.consola.imprimir("El avatar se coloca en la Cárcel. Solo está de visita.");
    }

    @Override
    public String getNombre() {
        return "Cárcel";
    }
}
