package monopoly.casilla.edificio;

import monopoly.casilla.propiedad.Solar;

public class Piscina extends Edificio {
    public Piscina(int id, String tipo, long valor, Solar solar, int cantidad) {
        super(id, tipo, valor, solar, cantidad);
    }
    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        long precioSolar = solar.getPrecio();
        return (long) (1.25 * precioSolar);
    }

    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquiler = solar.getAlquiler();
        return 25 * alquiler * cantidad;
    }
    @Override
    public long getValor() {
        return (long) (1.25 * getSolar().getPrecio());
    }

}
