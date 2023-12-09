package monopoly.casilla.edificio;

import monopoly.JuegoConsts;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorComandoEdificio;
import monopoly.error.ErrorFatalLogico;

public final class Piscina extends Edificio {
    public Piscina(Solar solar) throws ErrorComandoEdificio {
        super(solar);

        final Grupo grupo = solar.getGrupo();
        final int maxEdificios = grupo.getNumeroPropiedades();

        // Comprobación del requisito de edificación
        if (grupo.contarEdificios("Piscina") > maxEdificios) {
            throw new ErrorComandoEdificio("No se pueden edificar más de %d piscina(s) en este grupo".formatted(maxEdificios));
        }

        if (grupo.contarEdificios("Hotel") < JuegoConsts.N_HOTELES_PARA_PISCINA || grupo.contarEdificios("Casa") < JuegoConsts.N_CASAS_PARA_PISCINA) {
            throw new ErrorComandoEdificio("Se necesita %d hotel(es) y %d casa(s) en el grupo para edificar una piscina".formatted(JuegoConsts.N_HOTELES_PARA_PISCINA, JuegoConsts.N_CASAS_PARA_PISCINA));
        }
    }

    @Override
    public Edificio clone() {
        try {
            return new Piscina(this.getSolar());
        } catch (ErrorComandoEdificio e) {
            throw new RuntimeException(e);
        }
    }

    public static long getValor(Solar solar) {
        return (long) (0.4 * (float) solar.getPrecio());
    }

    public static long getAlquiler(Solar solar) throws ErrorFatalLogico {
        return 25 * solar.getAlquilerBase();
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
