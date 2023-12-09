package monopoly.jugador.trato;

import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_PNA extends Trato {
    private final Propiedad propInteresado;
    private final Propiedad propAceptador;
    private final Propiedad propNoAlquiler;
    private int turnos;

    public TratoP_PNA(Jugador interesado, Jugador benefactor, Propiedad propInteresado, Propiedad propAceptador, Propiedad propNoAlquiler, int turnos) {
        super(interesado, benefactor);
        this.propInteresado = propInteresado;
        this.propAceptador = propAceptador;
        this.propNoAlquiler = propNoAlquiler;
        this.turnos = turnos;
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
                {
                %s
                    trato: cambiar %s por %s y no pagar alquiler en %s durante %d turnos.
                }""".formatted(
                        super.toString().indent(4),
                        propInteresado.getNombreFmt(),
                        propAceptador.getNombreFmt(),
                        propAceptador.getNombreFmt(),
                        turnos);
        // @formatter:on
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getJugadorPropone();
        Jugador j2 = getJugadorAcepta();

        j1.anadirPropiedad(propAceptador);
        j2.anadirPropiedad(propInteresado);

        propAceptador.setPropietario(j1);
        propInteresado.setPropietario(j2);

        j1.quitarPropiedad(propInteresado);
        j2.quitarPropiedad(propAceptador);
    }

    public Propiedad getPropNoAlquiler() {
        return propNoAlquiler;
    }

    public int getTurnos() {
        return turnos;
    }

    public void quitarTurno() throws ErrorFatalLogico {
        if (turnos <= 0) {
            throw new ErrorFatalLogico("No se puede quitar un turno cuando ya es 0");
        }

        turnos--;
    }
}
