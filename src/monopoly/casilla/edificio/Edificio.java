package monopoly.casilla.edificio;

import monopoly.Juego;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorFatalLogico;

public abstract class Edificio {
    private static int ultimoId = 1;

    private int id;
    private Solar solar;

    public Edificio(Solar solar) {
        this.id = ultimoId++;
        this.solar = solar;
    }

    @Override
    public String toString() {
        try {
            // @formatter:off
            return """
                    {
                        id: %s
                        propietario: %s
                        solar: %s
                        grupo: %s
                        valor: %s
                    }""".formatted(getId(),
                        solar.getPropietario(),
                        solar.getNombre(),
                        solar.getGrupo().getNombre(),
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
        return Juego.consola.fmt("%s-%s".formatted(this.getClass().getName(), id), solar.getGrupo().getCodigoColor());
    }

    public abstract long getValor() throws ErrorFatalLogico;

    public abstract long getAlquiler() throws ErrorFatalLogico;
}

