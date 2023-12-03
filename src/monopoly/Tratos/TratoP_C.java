package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;

public class TratoP_C extends Trato{
    private final Propiedad inter;
    private final long cantidad;

    public TratoP_C(String nombre, Jugador interesado, Jugador aceptador, long cantidad, Propiedad inter) {
        super(nombre, interesado, aceptador);
        this.cantidad=cantidad;
        this.inter=inter;
    }


    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        j2.anadirPropiedad(inter);
        j1.ingresar(cantidad);
        j2.cobrar(cantidad,true);
        j2.quitarPropiedad(inter);
    }
}
