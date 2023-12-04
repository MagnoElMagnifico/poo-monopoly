package monopoly.casillas.edificios;

public class Hotel extends Edificio {
    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        long precioSolar = solar.getPrecio();
        return (long) (0.6 * precioSolar);
    }

    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = p.getAlquiler();
        return 70 * alquilerSolar * cantidad;
    }
}