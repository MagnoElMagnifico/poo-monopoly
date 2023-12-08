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
                %s
                Cambiar %s por %s
                                
                """.formatted(super.toString(), propiedad.getNombreFmt(), Juego.consola.num(cantidad));
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();

        j2.cobrar(cantidad);
        j2.anadirPropiedad(propiedad);

        j1.ingresar(cantidad);
        j2.quitarPropiedad(propiedad);

        super.aceptar();
    }
}
