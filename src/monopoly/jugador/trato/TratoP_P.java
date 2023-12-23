package monopoly.jugador.trato;

import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoJugador;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_P extends Trato {
    private final Propiedad propPropone;
    private final Propiedad propAcepta;

    public TratoP_P(Jugador jugPropone, Jugador jugAcepta, Propiedad propPropone, Propiedad propAcepta) throws ErrorComandoTrato {
        super(jugPropone, jugAcepta);

        if (!propPropone.perteneceAJugador(jugPropone)
                || !propAcepta.perteneceAJugador(jugAcepta)) {
            throw new ErrorComandoTrato("No puedes ofrecer un trato con propiedades que no os pertenecen", jugPropone);
        }

        this.propPropone = propPropone;
        this.propAcepta = propAcepta;
    }

    @Override
    public String toString() {
        return """
                {
                %s
                    trato: cambiar %s por %s
                }""".formatted(super.toString().indent(4), propPropone.getNombreFmt(), propAcepta.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico, ErrorComandoTrato {
        if (!propPropone.perteneceAJugador(getJugadorPropone()) || !propAcepta.perteneceAJugador(getJugadorAcepta())) {
            throw new ErrorComandoTrato("El trato ya no es válido", getJugadorPropone());
        }

        Jugador j1 = getJugadorPropone();
        Jugador j2 = getJugadorAcepta();

        j1.anadirPropiedad(propAcepta);
        j2.anadirPropiedad(propPropone);

        propAcepta.setPropietario(j1);
        propPropone.setPropietario(j2);

        j1.quitarPropiedad(propPropone);
        j2.quitarPropiedad(propAcepta);

        super.aceptar();
    }
}
