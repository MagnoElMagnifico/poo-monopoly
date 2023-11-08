package monopoly.casillas;

import monopoly.Calculadora;
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

        if (tipo == TipoPropiedad.Solar) {
            this.edificios = new ArrayList<>();
        } else {
            this.edificios = null;
        }
    }

    // Para el comando listar enventa
    @Override
    public String toString() {
        StringBuilder edificiosStr = new StringBuilder();
        if (tipo == TipoPropiedad.Solar) {
            edificiosStr.append("    edificios: ");
            edificiosStr.append(Consola.listar(edificios.iterator(), Edificio::getNombreFmt));
            edificiosStr.append('\n');
        }

        // @formatter:off
        return """
               {
                   nombre: %s
                   tipo: %s%s
                   precio: %s
                   alquiler: %s
                   propietario: %s
               %s}""".formatted(nombre,
                              tipo,
                              tipo == TipoPropiedad.Solar? "\n    grupo: " + casilla.getGrupo().getNombre() : "",
                              Consola.num(precio),
                              Consola.num(alquiler),
                              propietario == null ? "-" : propietario.getNombre(),
                              edificiosStr);
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

    public void actualizarAlquiler() {
        this.alquiler = Calculadora.calcularAlquiler(this);
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = propietario;
    }

    public void anadirEdificio(Edificio e) {
        if (tipo != TipoPropiedad.Solar) {
            Consola.error("[Propiedad] No se puede añadir un edificio a un %s".formatted(tipo));
            return;
        }

        edificios.add(e);
    }

    public boolean quitarEdificio(Edificio.TipoEdificio tipo) {
        if (this.tipo != TipoPropiedad.Solar) {
            Consola.error("[Propiedad] No se puede añadir un edificio a un %s".formatted(tipo));
            return false;
        }

        for (int ii = 0; ii < edificios.size(); ii++) {
            if (edificios.get(ii).getTipo() == tipo) {
                edificios.remove(ii);
                return true;
            }
        }

        return false;
    }

    public int contarEdificio(Edificio.TipoEdificio tipo) {
        if (this.tipo != TipoPropiedad.Solar) {
            Consola.error("[Propiedad] %s no tiene edificios".formatted(tipo));
            return -1;
        }

        int numero = 0;
        for (Edificio e : edificios) {
            if (e.getTipo() == tipo) {
                numero++;
            }
        }

        return numero;
    }

    public ArrayList<Edificio> getEdificios() {
        if (this.tipo != TipoPropiedad.Solar) {
            Consola.error("[Propiedad] %s no tiene edificios".formatted(tipo));
            return null;
        }

        return edificios;
    }

    /**
     * Tipos de propiedades
     */
    public enum TipoPropiedad {
        Transporte, Servicio, Solar
    }
}
