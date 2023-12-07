package monopoly.casilla.edificio;

import monopoly.casilla.propiedad.Solar;

public class PistaDeporte extends Edificio {
    public PistaDeporte(int id, String tipo, long valor, Solar solar, int cantidad) {
        super(id, tipo, valor, solar, cantidad);
    }


    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        return (long) (cantidad * getValor());
    }

 
    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = solar.getAlquiler();
        return 25 * alquilerSolar * cantidad;
    }

    public long getValor() {
        return (long) (0.4 * getSolar().getPrecio());
    }
}
