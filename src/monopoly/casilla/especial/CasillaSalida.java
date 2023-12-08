package monopoly.casilla.especial;

import monopoly.Juego;
import monopoly.error.ErrorFatal;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class CasillaSalida extends CasillaEspecial {
    private long abonoSalida;

    public CasillaSalida(int posicion) {
        super(posicion);
        abonoSalida = -1;
    }

    public long getAbonoSalida() throws ErrorFatal {
        if (abonoSalida <= 0) {
            throw new ErrorFatal("No se ha establecido un abono de salida");
        }

        return abonoSalida;
    }

    public void setAbonoSalida(long abonoSalida) {
        this.abonoSalida = abonoSalida;
    }

    @Override
    public String listar() {
        return getNombreFmt();
    }

    @Override
    public String toString() {
        return """
                Salida: Casilla de inicio del juego.
                Cada vez que un jugador pase por esta casilla recibirá %s.
                """.formatted(Juego.consola.num(abonoSalida));
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) {
    }

    @Override
    public String getNombre() {
        return "Salida";
    }
}
