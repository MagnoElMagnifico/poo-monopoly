package monopoly.casilla.edificio;

import monopoly.JuegoConsts;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorComandoEdificio;
import monopoly.error.ErrorFatalLogico;

public final class PistaDeporte extends Edificio {
    public PistaDeporte(Solar solar) throws ErrorComandoEdificio {
        super(solar);

        final Grupo grupo = solar.getGrupo();
        final int maxEdificios = grupo.getNumeroPropiedades();

        // Comprobación del requisito de edificación
        if (grupo.contarEdificios("PistaDeporte") >= maxEdificios) {
            throw new ErrorComandoEdificio("No se pueden edificar más de %d pista(s) de deporte en este grupo".formatted(maxEdificios));
        }

        if (grupo.contarEdificios("Hotel") < JuegoConsts.N_HOTELES_PARA_PISTA) {
            throw new ErrorComandoEdificio("Se necesitan %d hotel(es) en el grupo para construir una pista de deporte".formatted(JuegoConsts.N_HOTELES_PARA_PISTA));
        }
    }

    @Override
    public PistaDeporte clonar() throws ErrorComandoEdificio {
        return new PistaDeporte(this.getSolar());
    }

    public static long getValor(Solar solar) {
        return (long) (1.15 * (float) solar.getPrecio());
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
