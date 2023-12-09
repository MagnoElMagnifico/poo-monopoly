package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_PC extends Trato {
    private final Propiedad propPropone;
    private final Propiedad propAcepta;
    private final long cantidad;

    public TratoP_PC(Jugador jugPropone, Jugador jugAcepta, Propiedad propPropone, Propiedad propAcepta, long cantidadAcepta) throws ErrorComandoTrato {
        super(jugPropone, jugAcepta);

        if (!propPropone.perteneceAJugador(jugPropone) || !propAcepta.perteneceAJugador(jugAcepta)) {
            throw new ErrorComandoTrato("No puedes ofrecer un trato con propiedades que no te pertenecen.", jugPropone);
        }

        this.propPropone = propPropone;
        this.propAcepta = propAcepta;
        this.cantidad = cantidadAcepta;
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
                {
                %s
                    trato: cambiar %s por %s y %s
                }""".formatted(
                    super.toString().indent(4),
                    propPropone.getNombreFmt(),
                    Juego.consola.num(cantidad),
                    propAcepta.getNombreFmt());
        // @formatter:on
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getJugadorPropone();
        Jugador j2 = getJugadorAcepta();

        j1.ingresar(cantidad);
        j1.anadirPropiedad(propAcepta);
        j1.quitarPropiedad(propPropone);

        propAcepta.setPropietario(j1);
        propPropone.setPropietario(j2);

        j2.cobrar(cantidad);
        j2.anadirPropiedad(propPropone);
        j2.quitarPropiedad(propAcepta);

        super.aceptar();
    }
}
