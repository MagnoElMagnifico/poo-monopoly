package monopoly.casilla;

import monopoly.Calculadora;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.utilidades.Consola;

/**
 * Representa una edificación de un solar.
 * Puede ser de 4 tipos:
 * <li>Casa</li>
 * <li>Hotel</li>
 * <li>Piscina</li>
 * <li>Pista de deporte</li>
 *
 * @author Marcos Granja Grille
 * @date 5-11-2023
 * @see Propiedad
 */
public class Edificio {
    /**
     * Permite generar IDs para edificios de forma que nunca se repitan
     */
    private static int ultimoId = 1;

    private final int id;
    private final TipoEdificio tipo;
    private final long valor;
    private final Propiedad solar;

    public Edificio(TipoEdificio tipo, Propiedad solar) {
        this.id = ultimoId++;
        this.tipo = tipo;
        this.solar = solar;
        this.valor = Calculadora.precio(tipo, solar);
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
                {
                    id: %s
                    propietario: %s
                    solar: %s
                    grupo: %s
                    valor: %s
                }""".formatted(getNombreFmt(),
                               solar.getPropietario().getNombre(),
                               solar.getCasilla().getNombre(),
                               solar.getCasilla().getGrupo().getNombre(),
                               Consola.num(valor));
        // @formatter:on
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Edificio && ((Edificio) obj).id == this.id;
    }

    public String getNombreFmt() {
        return Consola.fmt("%s-%d".formatted(tipo, id), solar.getCasilla().getGrupo().getCodigoColor());
    }

    public TipoEdificio getTipo() {
        return tipo;
    }

    public Propiedad getSolar() {
        return solar;
    }

    public long getValor() {
        return valor;
    }

    public enum TipoEdificio {
        Casa, Hotel, Piscina, PistaDeporte
    }
}