package monopoly.casilla.edificio;

import monopoly.Juego;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorComando;
import monopoly.error.ErrorComandoEdificio;
import monopoly.error.ErrorFatalLogico;
import monopoly.utils.Listable;

public abstract class Edificio implements Listable {
    private static int ultimoId = 1;

    private final int id;
    private final Solar solar;

    public Edificio(Solar solar) throws ErrorComandoEdificio {
        this.id = ultimoId++;

        if (solar.isHipotecada()) {
            throw new ErrorComandoEdificio("No se puede edificar sobre un Solar hipotecado");
        }

        this.solar = solar;
    }

    @Override
    public String listar() {
        try {
            // @formatter:off
            return """
                   {
                       id: %s-%d
                       propietario: %s
                       solar: %s
                       grupo: %s
                       precio: %s
                   }
                   """.formatted(
                           getClass().getSimpleName(), id,
                           getSolar().getPropietario().getNombre(),
                           getSolar().getNombreFmt(),
                           getSolar().getGrupo().getNombreFmt(),
                           Juego.consola.num(getValor()));
            // @formatter:on
        } catch (ErrorFatalLogico e) {
            throw new RuntimeException(e);
        }
    }

    // NOTA: no se puede usar clone() porque se necesita lanzar la excepción
    // de restricciones de edificación.
    public abstract Edificio clonar() throws ErrorComando;

    @Override
    public String toString() {
        try {
            // @formatter:off
            return """
                    {
                        id: %s
                        propietario: %s
                        solar: %s
                        valor: %s
                    }
                    """.formatted(getId(),
                        solar.getPropietario(),
                        solar.getNombre(),
                        Juego.consola.num(getValor()));
            // @formatter:on
        } catch (ErrorFatalLogico e) {
            // No se puede lanzar otro tipo de excepción,
            // porque se tendría que añadir el throws en
            // la clase Object.
            throw new RuntimeException(e);
        }
    }

    public Solar getSolar() {
        return solar;
    }

    public int getId() {
        return id;
    }

    public String getNombreFmt() {
        return Juego.consola.fmt("%s-%s".formatted(this.getClass().getSimpleName(), id), solar.getGrupo().getCodigoColor());
    }

    public abstract long getValor() throws ErrorFatalLogico;

    public abstract long getAlquiler() throws ErrorFatalLogico;
}

