package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

public class TratoP_PC extends Trato{
    private final Propiedad inter;
    private final Propiedad acept;
    private final long cantidad;
    public TratoP_PC(String nombre, Jugador interesado, Jugador benefactor, Propiedad inter, long cantidad, Propiedad acept) {
        super(nombre, interesado, benefactor);
        this.inter=inter;
        this.acept=acept;
        this.cantidad=cantidad;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s y %s
                
                """.formatted(
                super.toString(),
                inter.getCasilla().getNombreFmt(), Consola.num(cantidad), acept.getCasilla().getNombreFmt());
    }

    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        j1.anadirPropiedad(acept);
        j2.anadirPropiedad(inter);
        j1.quitarPropiedad(inter);
        j2.quitarPropiedad(acept);
        j1.ingresar(cantidad);
        j2.cobrar(cantidad,true);
    }
}
