package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoJugador;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoPC_P extends Trato {
    private final Propiedad propPropone;
    private final Propiedad propAcepta;
    private final long cantidadPropone;

    public TratoPC_P(Jugador jugPropone, Jugador jugAcepta, Propiedad propPropone, long cantidadPropone, Propiedad propAcepta) throws ErrorComandoTrato {
        super(jugPropone, jugAcepta);

        if (!propPropone.perteneceAJugador(jugPropone) || !propAcepta.perteneceAJugador(jugAcepta)) {
            throw new ErrorComandoTrato("No puedes ofrecer un trato con propiedades que no te pertenecen.", jugPropone);
        }

        if (jugPropone.getFortuna() < cantidadPropone) {
            throw new ErrorComandoTrato("No tienes suficiente dinero para ofrecer el trato", jugPropone);
        }

        this.propPropone = propPropone;
        this.propAcepta = propAcepta;
        this.cantidadPropone = cantidadPropone;
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
                {
                %s
                    trato: cambiar %s y %s por %s
                }""".formatted(
                    super.toString().indent(4),
                    propPropone.getNombreFmt(),
                    Juego.consola.num(cantidadPropone),
                    propAcepta.getNombreFmt());
        // @formatter:on
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getJugadorPropone();
        Jugador j2 = getJugadorAcepta();

        j1.cobrar(cantidadPropone);
        j1.anadirPropiedad(propAcepta);
        j1.quitarPropiedad(propPropone);

        propAcepta.setPropietario(j1);
        propPropone.setPropietario(j2);

        j2.ingresar(cantidadPropone);
        j2.anadirPropiedad(propPropone);
        j2.quitarPropiedad(propAcepta);

        super.aceptar();
    }
}
