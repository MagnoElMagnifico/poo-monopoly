package monopoly.casillas.edificios;

import monopoly.casillas.Propiedad;
import monopoly.utilidades.Consola;

public abstract class Edificio {
    private static int ultimoId = 1;

    private int id;
    private String tipo;
    private long valor;
    private Solar solar; // Implementar esta clase 
    private int cantidad;

    public Edificio(int id, String tipo, long valor, Solar solar, int cantidad) {
        this.id = ultimoId++;
        this.solar = solar;
        this.valor = 0;
        this.cantidad = 0;

    }
    // cambiar cuando se cree solar, y cambiar el formateado 
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

    public Edificio.TipoEdificio getTipo() {
        return tipo;
    }

    public Propiedad getSolar() {
        return solar;
    }

    public long getValor() {
        return valor;
    }

    public abstract long alquilerEdificio(Solar solar, int cantidad);

    public abstract long precioEdificio(Solar solar, int cantidad);
}

