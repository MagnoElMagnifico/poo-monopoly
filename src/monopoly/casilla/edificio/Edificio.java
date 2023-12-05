package monopoly.casilla.edificio;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.casilla.propiedad.Solar;

public abstract class Edificio {
    private static int ultimoId = 1;

    private int id;

    private long valor;
    private Solar solar;
    private int cantidad;

    public Edificio(int id, String tipo, long valor, Solar solar, int cantidad) {
        this.id = ultimoId++;
        this.solar = solar;
        this.valor = 0;
        this.cantidad = 0;

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
                }""".formatted(getId(),
                solar.getPropietario(),
                solar.getNombre(),
                solar.getGrupo().getNombre(),
                Juego.consola.num(valor));
        // @formatter:on
    }

    public Propiedad getSolar() {
        return solar;
    }

    public int getId() {
        return id;
    }

    public abstract long getValor();

    public abstract long alquilerEdificio(Solar solar, int cantidad);

    public abstract long precioEdificio(Solar solar, int cantidad);
}

