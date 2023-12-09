package monopoly.casilla.especial;

import monopoly.JuegoConsts;
import monopoly.casilla.Casilla;
import monopoly.utils.Consola;

public abstract class CasillaEspecial extends Casilla {
    public CasillaEspecial(int posicion) {
        super(posicion);
    }

    @Override
    public String listar() {
        return '\n' + getNombreFmt() + '\n';
    }

    @Override
    public int codColorRepresentacion() {
        return JuegoConsts.COD_COLOR_ESPECIAL;
    }

    @Override
    public Consola.Estilo estiloRepresentacion() {
        return JuegoConsts.EST_ESPECIAL;
    }
}
