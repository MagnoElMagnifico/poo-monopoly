package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_C extends Trato {
    private final Propiedad propiedad;
    private final long cantidad;

    public TratoP_C(Jugador interesado, Jugador aceptador, Propiedad propiedad, long cantidad) {
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
                }""".formatted(super.toString().indent(4), propiedad.getNombreFmt(), Juego.consola.num(cantidad));
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        getJugadorAcepta().cobrar(cantidad);
        getJugadorAcepta().anadirPropiedad(propiedad);
        propiedad.setPropietario(getJugadorAcepta());

        getJugadorPropone().ingresar(cantidad);
        getJugadorAcepta().quitarPropiedad(propiedad);

        super.aceptar();
    }
}
