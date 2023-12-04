package monopoly.casillas.edificios;

public class Piscina extends Edificio {
    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        long precioSolar = solar.getPrecio();
        return (long) (1.25 * precioSolar);
    }

    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = solar.getAlquiler();
        return 25 * alquilerSolar * cantidad;
    }
}
