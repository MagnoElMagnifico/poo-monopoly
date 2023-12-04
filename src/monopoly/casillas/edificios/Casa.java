package monopoly.casillas.edificios;



public class Casa extends Edificio {
    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        long precioSolar = solar.getPrecio();
        return (long) (0.6 * precioSolar);
    }

    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = solar.getAlquiler();
        long l = switch (cantidad) {
            case 0 -> 0;
            case 1 -> 5 * alquilerSolar;
            case 2 -> 15 * alquilerSolar;
            case 3 -> 35 * alquilerSolar;
            default -> 50 * alquilerSolar;
        };
        return l;
    }
}



