package monopoly.jugador.trato;


import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_PC extends Trato {
    private final Propiedad propInteresado;
    private final Propiedad propAceptador;
    private final long cantidad;

    public TratoP_PC(Jugador interesado, Jugador benefactor, Propiedad propInteresado, Propiedad propAceptador, long cantidad) {
        super(interesado, benefactor);
        this.propInteresado = propInteresado;
        this.propAceptador = propAceptador;
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s y %s
                """.formatted(
                super.toString(),
                propInteresado.getNombreFmt(), Juego.consola.num(cantidad), propAceptador.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();

        j1.ingresar(cantidad);
        j1.anadirPropiedad(propAceptador);
        j1.quitarPropiedad(propInteresado);

        propAceptador.setPropietario(j1);
        propInteresado.setPropietario(j2);

        j2.cobrar(cantidad);
        j2.anadirPropiedad(propInteresado);
        j2.quitarPropiedad(propAceptador);

        super.aceptar();
    }
}
