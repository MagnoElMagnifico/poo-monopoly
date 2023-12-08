package monopoly.casilla.edificio;

import monopoly.JuegoConsts;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorComando;
import monopoly.error.ErrorComandoEdificio;
import monopoly.error.ErrorFatalLogico;

public final class Hotel extends Edificio {
    public Hotel(Solar solar) throws ErrorComando {
        super(solar);

        final Grupo grupo = solar.getGrupo();
        final int maxEdificios = grupo.getNumeroPropiedades();

        // Comprobación del requisito de edificación
        if (grupo.contarEdificios("Hotel") > maxEdificios) {
            throw new ErrorComandoEdificio("No se pueden edificar más de %d hotel(es) en este grupo".formatted(maxEdificios));
        }

        if (solar.contarEdificios("Casa") < JuegoConsts.N_CASAS_PARA_HOTEL) {
            throw new ErrorComandoEdificio("Se necesitan %d casa(s) en el solar para edificar un hotel".formatted(JuegoConsts.N_CASAS_PARA_HOTEL));
        }

        // Quitar las 4 casas
        for (int ii = 0; ii < JuegoConsts.N_CASAS_PARA_HOTEL; ii++) {
            solar.demoler("Casa");
        }
    }

    public static long getValor(Solar solar) {
        return (long) (0.6 * (float) solar.getPrecio());
    }

    public static long getAlquiler(Solar solar) throws ErrorFatalLogico {
        return 70 * solar.getAlquiler();
    }

    @Override
    public long getValor() throws ErrorFatalLogico {
        return getValor(super.getSolar());
    }

    @Override
    public long getAlquiler() throws ErrorFatalLogico {
        return getAlquiler(super.getSolar());
    }
}