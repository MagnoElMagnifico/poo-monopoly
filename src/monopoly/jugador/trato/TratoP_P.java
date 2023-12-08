package monopoly.jugador.trato;

import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

public class TratoP_P extends Trato {
    private final Propiedad propInteresado;
    private final Propiedad propAceptador;

    public TratoP_P(Jugador interesado, Jugador benefactor, Propiedad propInteresado, Propiedad propAceptador) {
        super(interesado, benefactor);
        this.propInteresado = propInteresado;
        this.propAceptador = propAceptador;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s por %s
                                
                """.formatted(super.toString(), propInteresado.getNombreFmt(), propAceptador.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna, ErrorFatalLogico {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();

        j1.anadirPropiedad(propAceptador);
        j2.anadirPropiedad(propInteresado);

        j1.quitarPropiedad(propInteresado);
        j2.quitarPropiedad(propAceptador);

        super.aceptar();
    }
}
