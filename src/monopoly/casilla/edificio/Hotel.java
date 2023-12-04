package monopoly.casilla.edificio;

import monopoly.casilla.propiedad.Solar;

public class Hotel extends Edificio {
    public Hotel(int id, String tipo, long valor, Solar solar, int cantidad) {
        super(id, tipo, valor, solar, cantidad);
    }

    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        return (long) (getValor() * cantidad);
    }

    @Override
    public long getValor() {
        return (long) (0.6 * getSolar().getPrecio());
    }

    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = solar.getAlquiler();
        return 70 * alquilerSolar * cantidad;
    }
}