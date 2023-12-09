package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoJugador;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_C extends Trato {
    private final Propiedad propPropone;
    private final long cantidadAcepta;

    public TratoP_C(Jugador jugPropone, Jugador jugAcepta, Propiedad propPropone, long cantidadAcepta) throws ErrorComandoTrato {
        super(jugPropone, jugAcepta);

        if (!propPropone.perteneceAJugador(jugPropone)) {
            throw new ErrorComandoTrato("No puedes ofrecer un trato con propiedades que no te pertenecen", jugPropone);
        }

        this.cantidadAcepta = cantidadAcepta;
        this.propPropone = propPropone;
    }

    @Override
    public String toString() {
        return """
                {
                %s
                    trato: cambiar %s por %s
                }""".formatted(super.toString().indent(4), propPropone.getNombreFmt(), Juego.consola.num(cantidadAcepta));
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        getJugadorPropone().ingresar(cantidadAcepta);
        getJugadorPropone().quitarPropiedad(propPropone);

        getJugadorAcepta().cobrar(cantidadAcepta);
        getJugadorAcepta().anadirPropiedad(propPropone);
        propPropone.setPropietario(getJugadorAcepta());

        super.aceptar();
    }
}
