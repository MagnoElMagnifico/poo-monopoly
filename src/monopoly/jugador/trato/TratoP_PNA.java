package monopoly.jugador.trato;

import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoJugador;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_PNA extends Trato {
    private final Propiedad propPropone;
    private final Propiedad propAcepta;
    private final Propiedad propNoAlquiler;
    private int turnos;

    public TratoP_PNA(Jugador jugPropone, Jugador jugAcepta, Propiedad propPropone, Propiedad propAcepta, Propiedad propNoAlquiler, int turnos) throws ErrorComandoTrato {
        super(jugPropone, jugAcepta);

        if (!propPropone.perteneceAJugador(jugPropone) || !propAcepta.perteneceAJugador(jugAcepta)) {
            throw new ErrorComandoTrato("No puedes ofrecer un trato con propiedades que no os pertenecen.", jugPropone);
        }

        if (turnos <= 0) {
            throw new ErrorComandoTrato("El número de turnos no puede ser negativo o 0", jugPropone);
        }

        this.propPropone = propPropone;
        this.propAcepta = propAcepta;
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
                        propPropone.getNombreFmt(),
                        propAcepta.getNombreFmt(),
                        propAcepta.getNombreFmt(),
                        turnos);
        // @formatter:on
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getJugadorPropone();
        Jugador j2 = getJugadorAcepta();

        j1.anadirPropiedad(propAcepta);
        j2.anadirPropiedad(propPropone);

        propAcepta.setPropietario(j1);
        propPropone.setPropietario(j2);

        j1.quitarPropiedad(propPropone);
        j2.quitarPropiedad(propAcepta);
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
