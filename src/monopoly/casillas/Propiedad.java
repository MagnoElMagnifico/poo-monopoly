package monopoly.casillas;

import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

import java.util.ArrayList;

/**
 * Representa una casilla que se puede comprar por un jugador.
 * <p>
 * Existen tres tipos:
 *
 * <li>Solares</li>
 * <li>Servicios</li>
 * <li>Transporte</li>
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see Casilla
 * @see Jugador
 */
public class Propiedad {
    private final String nombre;
    private final Casilla casilla;
    private final TipoPropiedad tipo;
    private final ArrayList<Edificio> edificios;
    private long precio;
    private long alquiler;
    private Jugador propietario;

    /**
     * Crea una propiedad.
     *
     * @param casilla Casilla a la que está asociada
     * @param tipo    Tipo de casilla: Solar, Servicio o Transporte
     */
    public Propiedad(String nombre, Casilla casilla, TipoPropiedad tipo) {
        this.nombre = nombre;
        this.casilla = casilla;
        this.tipo = tipo;
        this.propietario = null;
        this.precio = -1;   // Marcar como todavía no establecido
        this.alquiler = -1; // Marcar como todavía no establecido
        this.edificios = new ArrayList<>();
    }

    // Para el comando listar enventa
    @Override
    public String toString() {
        // @formatter:off
        return """
               {
                   nombre: %s
                   tipo: %s%s
                   precio: %s
                   alquiler: %s
                   propietario: %s
               }""".formatted(nombre,
                              tipo,
                              tipo == TipoPropiedad.Solar? "\n    grupo: " + casilla.getGrupo().getNombre() : "",
                              Consola.num(precio),
                              Consola.num(alquiler),
                              propietario == null ? "-" : propietario.getNombre());
        // @formatter:on
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Propiedad && ((Propiedad) obj).nombre.equalsIgnoreCase(this.nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public Casilla getCasilla() {
        return casilla;
    }

    public TipoPropiedad getTipo() {
        return tipo;
    }

    public long getPrecio() {
        return precio;
    }

    public void setPrecio(long precio) {
        if (precio <= 0) {
            Consola.error("[Propiedad] No se puede asignar un precio negativo o nulo a una propiedad");
            return;
        }

        this.precio = precio;
    }

    public long getAlquiler() {
        return alquiler;
    }

    public void setAlquiler(long alquiler) {
        if (alquiler <= 0) {
            Consola.error("[Propiedad] No se puede asignar un alquiler negativo o nulo a una propiedad");
            return;
        }

        this.alquiler = alquiler;
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = propietario;
    }

    public ArrayList<Edificio> getEdificios() {
        return edificios;
    }

    public void anadirEdificio(Edificio e) {
        edificios.add(e);
    }

    /**
     * Tipos de propiedades
     */
    public enum TipoPropiedad {
        Transporte, Servicio, Solar
    }
}
