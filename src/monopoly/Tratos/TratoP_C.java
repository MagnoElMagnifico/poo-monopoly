package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

public class TratoP_C extends Trato{
    private final Propiedad inter;
    private final long cantidad;

    public TratoP_C(String nombre, Jugador interesado, Jugador aceptador, long cantidad, Propiedad inter) {
        super(nombre, interesado, aceptador);
        this.cantidad=cantidad;
        this.inter=inter;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s
                
                """.formatted(super.toString(),inter.getCasilla().getNombreFmt(), Consola.num(cantidad));
    }

    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        if(!j2.cobrar(cantidad,false)) return;
        j2.anadirPropiedad(inter);
        j1.ingresar(cantidad);
        j2.quitarPropiedad(inter);
    }
}
