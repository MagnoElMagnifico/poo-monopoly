package trato;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoC_P extends Trato{
    private final Propiedad aceptado;
    private final long cantidad;

    public TratoC_P(String nombre, Jugador interesado, Jugador aceptador, long cantidad, Propiedad acep) {
        super(nombre, interesado, aceptador);
        this.cantidad = cantidad;
        this.aceptado = acep;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s
                
                """.formatted(super.toString(), Juego.consola.num(cantidad), aceptado.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();

        j1.cobrar(cantidad);
        j1.anadirPropiedad(aceptado);

        j2.ingresar(cantidad);
        j2.quitarPropiedad(aceptado);
    }
}
