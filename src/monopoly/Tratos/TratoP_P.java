package monopoly.Tratos;

import monopoly.casillas.Propiedad;
import monopoly.errores.ErrorComando;
import monopoly.jugadores.Jugador;

public class TratoP_P extends Trato{
    private final Propiedad inter;
    private final Propiedad acept;
    public TratoP_P(String nombre, Jugador interesado, Jugador benefactor, Propiedad inter, Propiedad acept) {
        super(nombre, interesado, benefactor);
        this.inter=inter;
        this.acept=acept;
    }

    @Override
    public String toString() {
        return """
                %s con %s\n
                Cambiar %s por %s\n
                """.formatted(getNombre(),getInteresado(),inter.getCasilla().getNombreFmt(),acept.getCasilla().getNombreFmt());
    }

    @Override
    public void aceptar() {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        j1.anadirPropiedad(acept);
        j2.anadirPropiedad(inter);
        j1.quitarPropiedad(inter);
        j2.quitarPropiedad(acept);

    }
}
