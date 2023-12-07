package monopoly.Tratos;

import monopoly.casilla.propiedad.Propiedad;
import monopoly.jugador.Jugador;

public class TratoP_PNA extends Trato {
    // Lo mismo que los tratos anteriores (del mismo paquete), pero cambiando que se cambia una propiedad por una propiedad
    // y no pagar alquiler durante unos turnos
    // por ejemplo: trato Eva: cambiar (Solar1, Solar2) y noalquiler(Solar3, 4)
    // Eva, ¿te doy Solar1 y tú me das Solar14 y no pago alquiler en Solar3
    // durante 3 turnos?

    private final Propiedad inter;
    private final Propiedad acept;
    private final int turnos;

    public TratoP_PNA(String nombre, Jugador interesado, Jugador benefactor, Propiedad inter, Propiedad acept, int turnos) {
        super(nombre, interesado, benefactor);
        this.inter = inter;
        this.acept = acept;
        this.turnos = turnos;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s y no pagar alquiler en %s durante %d turnos
                
                """.formatted(super.toString(), inter.getNombreFmt(), acept.getNombreFmt(), acept.getNombreFmt(), turnos);
    }

    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        j1.anadirPropiedad(acept);
        j2.anadirPropiedad(inter);
        j1.quitarPropiedad(inter);
        j2.quitarPropiedad(acept);
        j1.anadirNoAlquiler(acept, turnos);
    }



}
