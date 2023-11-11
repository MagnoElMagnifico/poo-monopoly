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
    private boolean hipotecada;
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
        this.hipotecada = false;

        if (tipo == TipoPropiedad.Solar) {
            this.edificios = new ArrayList<>();
        } else {
            this.edificios = null;
        }
    }

    private String toStringSolar() {
        // TODO
        // valor casa: %s
        // valor hotel: %s
        // valor piscina: %s
        // valor pista de deporte: %s
        // alquiler una casa: %s
        // alquiler dos casas: %s
        // alquiler tres casas: %s
        // alquiler cuatro casas: %s
        // alquiler hotel: %s
        // alquiler piscina: %s
        // alquiler pista de deporte: %s
        // @formatter:off
        return """
                {
                    tipo: %s
                    nombre: %s
                    grupo: %s
                    precio: %s
                    alquiler: %s
                    propietario: %s
                    edificios: %s
                }""".formatted(tipo,
                               nombre,
                               casilla.getGrupo().getNombre(),
                               Consola.num(precio),
                               Consola.num(alquiler),
                               propietario.getNombre(),
                               Consola.listar(edificios.iterator(), Edificio::getNombreFmt));
        // @formatter:on
    }

    @Override
    public String toString() {
        if (tipo == TipoPropiedad.Solar) {
            return toStringSolar();
        }

        // @formatter:off
        return """
               {
                   tipo: %s
                   nombre: %s
                   precio: %s
                   alquiler: %s
                   propietario: %s
               }""".formatted(nombre,
                              tipo,
                              Consola.num(precio),
                              Consola.num(alquiler),
                              propietario == null ? "Banca" : propietario.getNombre());
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

    public int contarEdificios(Edificio.TipoEdificio tipo) {
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

    public boolean isHipotecada() {
        return hipotecada;
    }

    public void setHipotecada(boolean hipotecada) {
        this.hipotecada = hipotecada;
    }

    /**
     * Tipos de propiedades
     */
    public enum TipoPropiedad {
        Transporte, Servicio, Solar
    }
}
