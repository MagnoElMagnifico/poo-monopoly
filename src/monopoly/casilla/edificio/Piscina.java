package monopoly.casilla.edificio;

import monopoly.JuegoConsts;
import monopoly.casilla.propiedad.Solar;
import monopoly.casilla.propiedad.Grupo;
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

        solar.edificar(this);
    }

    @Override
    public long getValor() throws ErrorFatalLogico {
        return (long)(0.4 * (float) super.getSolar().getPrecio());
    }

    @Override
    public long getAlquiler() throws ErrorFatalLogico {
        return 25 * super.getSolar().getAlquiler();
    }
}
