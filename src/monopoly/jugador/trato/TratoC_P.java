package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoJugador;
import monopoly.error.ErrorComandoTrato;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoC_P extends Trato {
    private final Propiedad propAcepta;
    private final long cantidadPropone;

    public TratoC_P(Jugador jugPropone, Jugador jugAcepta, long cantidadPropone, Propiedad propAcepta) throws ErrorComandoTrato {
        super(jugPropone, jugAcepta);

        if (jugPropone.getFortuna() < cantidadPropone) {
            throw new ErrorComandoTrato("No tienes suficiente dinero para proponer el trato", jugPropone);
        }

        if (!propAcepta.perteneceAJugador(jugAcepta)) {
            throw new ErrorComandoTrato("No puedes proponer un trato con propiedades que el jugador que acepta no posee", jugPropone);
        }

        this.cantidadPropone = cantidadPropone;
        this.propAcepta = propAcepta;
    }

    @Override
    public String toString() {
        return """
                {
                %s
                    trato: cambiar %s por %s
                }
                """.formatted(super.toString(), Juego.consola.num(cantidadPropone), propAcepta.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico, ErrorComandoTrato {
        if (!propAcepta.perteneceAJugador(getJugadorAcepta())) {
            throw new ErrorComandoTrato("El trato ya no es válido", getJugadorPropone());
        }

        getJugadorPropone().cobrar(cantidadPropone);
        getJugadorPropone().anadirPropiedad(propAcepta);
        propAcepta.setPropietario(getJugadorPropone());

        getJugadorAcepta().ingresar(cantidadPropone);
        getJugadorAcepta().quitarPropiedad(propAcepta);

        super.aceptar();
    }
}
