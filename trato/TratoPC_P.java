package trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.jugador.JugadorOld;

public class TratoPC_P extends Trato{
    private final Propiedad inter;
    private final Propiedad acept;
    private final long cantidad;
    public TratoPC_P(String nombre, JugadorOld interesado, JugadorOld benefactor, Propiedad inter, long cantidad, Propiedad acept) {
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
                        inter.getNombreFmt(), Juego.consola.num(cantidad), acept.getNombreFmt());
    }

    @Override
    public void aceptar() {
        JugadorOld j1 = getInteresado();
        JugadorOld j2 = getAceptador();
        if(j1.cobrar(cantidad)) return;
        j1.anadirPropiedad(acept);
        j2.anadirPropiedad(inter);
        j1.quitarPropiedad(inter);
        j2.quitarPropiedad(acept);
        j2.ingresar(cantidad);


    }
}
