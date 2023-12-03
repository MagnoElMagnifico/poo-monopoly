package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

public class TratoPC_P extends Trato{
    private final Propiedad inter;
    private final Propiedad acept;
    private final long cantidad;
    public TratoPC_P(String nombre, Jugador interesado, Jugador benefactor, Propiedad inter, long cantidad, Propiedad acept) {
        super(nombre, interesado, benefactor);
        this.inter=inter;
        this.acept=acept;
        this.cantidad=cantidad;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s y %s por %s
                
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
        j2.ingresar(cantidad);
        j1.cobrar(cantidad,true);

    }
}
