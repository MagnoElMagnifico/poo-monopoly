package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;

public class TratoC_P extends Trato{
    private final Propiedad acept;
    private final long cantidad;

    public TratoC_P(String nombre, Jugador interesado, Jugador aceptador, long cantidad, Propiedad acep) {
        super(nombre, interesado, aceptador);
        this.cantidad=cantidad;
        this.acept=acep;
    }


    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        j1.anadirPropiedad(acept);
        j2.ingresar(cantidad);
        j1.cobrar(cantidad,true);
        j2.quitarPropiedad(acept);
    }
}
