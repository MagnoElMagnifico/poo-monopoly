package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoC_P extends Trato {
    private final Propiedad propiedad;
    private final long cantidad;

    public TratoC_P(Jugador interesado, Jugador aceptador, long cantidad, Propiedad propiedad) {
        super(interesado, aceptador);
        this.cantidad = cantidad;
        this.propiedad = propiedad;
    }

    @Override
    public String toString() {
        return """
                {
                %s
                    trato: cambiar %s por %s
                }
                """.formatted(super.toString(), Juego.consola.num(cantidad), propiedad.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        getJugadorPropone().cobrar(cantidad);
        getJugadorPropone().anadirPropiedad(propiedad);
        propiedad.setPropietario(getJugadorPropone());

        getJugadorAcepta().ingresar(cantidad);
        getJugadorAcepta().quitarPropiedad(propiedad);

        super.aceptar();
    }
}
