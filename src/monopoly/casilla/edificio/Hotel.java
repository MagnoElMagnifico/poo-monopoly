package monopoly.casilla.edificio;

import monopoly.casilla.propiedad.Solar;

public class Hotel extends Edificio {
    public Hotel(int id, String tipo, long valor, Solar solar, int cantidad) {
        super(id, tipo, valor, solar, cantidad);
    }

    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        long precioSolar = solar.getPrecio();
        return (long) (0.6 * precioSolar);
    }

    @Override
    public long getValor() {
        return 0;
    }

    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = solar.getAlquiler();
        return 70 * alquilerSolar * cantidad;
    }
}