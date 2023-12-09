package monopoly.casilla.edificio;

import monopoly.JuegoConsts;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorComandoEdificio;
import monopoly.error.ErrorFatalLogico;

public final class Casa extends Edificio {
    public Casa(Solar solar) throws ErrorComandoEdificio {
        super(solar);

        final Grupo grupo = solar.getGrupo();
        final int maxEdificios = grupo.getNumeroPropiedades();

        // Comprobación del requisito de edificación
        //
        // Si no hay el máximo de edificios, se puede tener hasta 4 casas.
        // Si no, solo hasta maxEdificios.
        if (grupo.contarEdificios("Hotel") < maxEdificios) {
            if (solar.contarEdificios("Casa") >= JuegoConsts.N_CASAS_SIN_MAX_HOTELES) {
                throw new ErrorComandoEdificio("No se pueden edificar más de %d casas en un solar cuando no hay el máximo de hoteles".formatted(JuegoConsts.N_CASAS_SIN_MAX_HOTELES));
            }
        } else if (grupo.contarEdificios("Casa") > maxEdificios) {
            throw new ErrorComandoEdificio("No se pueden edificar más de %d casas en un grupo cuando hay el número máximo de hoteles".formatted(maxEdificios));
        }
    }

    @Override
    public Casa clone() {
        try {
            return new Casa(this.getSolar());
        } catch (ErrorComandoEdificio e) {
            throw new RuntimeException(e);
        }
    }

    public static long getValor(Solar solar) {
        return (long) (0.6 * (float) solar.getPrecio());
    }

    public static long getAlquiler(Solar solar, int cantidad) throws ErrorFatalLogico {
        return switch (cantidad) {
            case 0 -> 0;
            case 1 -> 5 * solar.getAlquilerBase();
            case 2 -> 15 * solar.getAlquilerBase();
            case 3 -> 35 * solar.getAlquilerBase();
            default -> 50 * solar.getAlquilerBase();
        };
    }

    @Override
    public long getValor() throws ErrorFatalLogico {
        return getValor(super.getSolar());
    }

    @Override
    public long getAlquiler() throws ErrorFatalLogico {
        throw new ErrorFatalLogico("No se puede usar getAlquiler() directamente, debes usar el método estático");
    }
}
