package monopoly.casilla.edificio;

import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorFatalLogico;

public final class Casa extends Edificio {

    public Casa(int id, String tipo, long valor, Solar solar, int cantidad) {
        super(id, tipo, valor, solar, cantidad);
    }

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

    @Override
    public long getValor() throws ErrorFatalLogico {
        return (long) (0.6 * getSolar().getPrecio());
    }

    @Override
    public long getAlquiler() {
        return 0;
    }
}



