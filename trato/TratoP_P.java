package trato;

import monopoly.casilla.propiedad.Propiedad;
import monopoly.jugador.JugadorOld;

public class TratoP_P extends Trato{
    private final Propiedad inter;
    private final Propiedad acept;
    public TratoP_P(String nombre, JugadorOld interesado, JugadorOld benefactor, Propiedad inter, Propiedad acept) {
        super(nombre, interesado, benefactor);
        this.inter=inter;
        this.acept=acept;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s
                
                """.formatted(super.toString(),inter.getNombreFmt(),acept.getNombreFmt());
    }

    @Override
    public void aceptar() {
        JugadorOld j1 = getInteresado();
        JugadorOld j2 = getAceptador();
        j1.anadirPropiedad(acept);
        j2.anadirPropiedad(inter);
        j1.quitarPropiedad(inter);
        j2.quitarPropiedad(acept);

    }
}
