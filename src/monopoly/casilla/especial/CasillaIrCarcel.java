package monopoly.casilla.especial;

import monopoly.error.ErrorFatal;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class CasillaIrCarcel extends CasillaEspecial {
    private CasillaCarcel carcel;

    public CasillaIrCarcel(int posicion) {
        super(posicion);
        carcel = null;
    }

    public void setCarcel(CasillaCarcel carcel) {
        this.carcel = carcel;
    }

    @Override
    public String toString() {
        return """
                Ir a Cárcel: Si un jugador cae en esta casilla, se le enviará directamente
                a la casilla Cárcel.
                """;
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) throws ErrorFatal {
        if (carcel == null) {
            throw new ErrorFatal("No se ha inicializado la cárcel en IrCárcel");
        }

        jugadorTurno.getAvatar().irCarcel(carcel);
    }

    @Override
    public String getNombre() {
        return "IrCárcel";
    }
}
