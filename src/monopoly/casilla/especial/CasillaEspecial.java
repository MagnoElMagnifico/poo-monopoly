package monopoly.casilla.especial;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.utils.Consola;

public abstract class CasillaEspecial extends Casilla {
    public CasillaEspecial(int posicion) {
        super(posicion);
    }

    @Override
    public String listar() {
        return getNombre();
    }

    @Override
    public String getNombreFmt() {
        return Juego.consola.fmt(getNombre(), 15, Consola.Estilo.Negrita);
    }
}
