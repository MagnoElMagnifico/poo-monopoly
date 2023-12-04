package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

public class TratoC_P extends Trato{
    private final Propiedad acept;
    private final long cantidad;

    public TratoC_P(String nombre, Jugador interesado, Jugador aceptador, long cantidad, Propiedad acep) {
        super(nombre, interesado, aceptador);
        this.cantidad=cantidad;
        this.acept=acep;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s
                
                """.formatted(super.toString(), Consola.num(cantidad),acept.getCasilla().getNombreFmt());
    }

    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        if(!j1.cobrar(cantidad,false)) return;
        j1.anadirPropiedad(acept);
        j2.ingresar(cantidad);
        j2.quitarPropiedad(acept);
    }
}
