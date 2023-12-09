package monopoly.jugador.trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoPC_P extends Trato {
    private final Propiedad propInteresado;
    private final Propiedad propAceptador;
    private final long cantidad;

    public TratoPC_P(Jugador interesado, Jugador benefactor, Propiedad propInteresado, long cantidad, Propiedad propAceptador) {
        super(interesado, benefactor);
        this.propInteresado = propInteresado;
        this.propAceptador = propAceptador;
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s y %s por %s
                """.formatted(
                super.toString(),
                propInteresado.getNombreFmt(), Juego.consola.num(cantidad), propAceptador.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();

        j1.cobrar(cantidad);
        j1.anadirPropiedad(propAceptador);
        j1.quitarPropiedad(propInteresado);

        propAceptador.setPropietario(j1);
        propInteresado.setPropietario(j2);

        j2.ingresar(cantidad);
        j2.anadirPropiedad(propInteresado);
        j2.quitarPropiedad(propAceptador);

        super.aceptar();
    }
}
