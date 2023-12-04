package monopoly.casilla.propiedad;

import monopoly.jugador.Avatar;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class Transporte extends Propiedad {
    public Transporte(int posicion, Grupo grupo, String nombre, Jugador banca) {
        super(posicion, grupo, nombre, banca);
    }

    @Override
    public void accion(Avatar avatarTurno, Dado dado) {

    }

    @Override
    public String getNombreFmt() {
        return null;
    }

    @Override
    public long getPrecio() {
        return 0;
    }

    @Override
    public long getAlquiler() {
        return 0;
    }
}
