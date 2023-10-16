package monopoly.casillas;

import monopoly.Calculadora;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Formatear;

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
    private final Casilla casilla;
    private final Tipo tipo;
    private long precio;
    private long alquiler;
    private Jugador propietario;

    /**
     * Crea una propiedad
     *
     * @param casilla       Casilla a la que está asociada
     * @param tipo          Tipo de casilla: Solar, Servicio o Transporte
     */
    public Propiedad(Casilla casilla, Tipo tipo) {
        this.casilla = casilla;
        this.tipo = tipo;
        this.propietario = null;
        this.precio = -1; // Marcar como todavía no establecido
        this.alquiler = -1; // Marcar como todavía no establecido
    }

    // Para el comando listar enventa
    @Override
    public String toString() {
        // TODO?: obtener el resto de información con la calculadora

        // Solo tiene sentido mostrar el nombre del grupo si es un solar
        // Mostrar también precio de las edificaciones
        if (tipo == Tipo.Solar) {
            return """
                    {
                        nombre: %s
                        tipo: Solar
                        grupo: %s
                        precio: %s
                        alquiler: %s
                        propietario: %s
                    }""".formatted(casilla.getNombre(), casilla.getGrupo().getNombre(), Formatear.num(precio), Formatear.num(alquiler), propietario == null ? "-" : propietario.getNombre());
        }

        return """
                {
                    nombre: %s
                    tipo: %s
                    precio: %s
                    alquiler: %s
                    propietario: %s
                }""".formatted(casilla.getNombre(), tipo, Formatear.num(precio), Formatear.num(alquiler), propietario == null? "-" : propietario.getNombre());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Propiedad && ((Propiedad) obj).getCasilla() == this.casilla;
    }

    public Casilla getCasilla() {
        return casilla;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public long getPrecio() {
        return precio;
    }

    public void setPrecio(long precio) {
        if (precio > 0) {
            this.precio = precio;
            this.alquiler = Calculadora.calcularAlquiler(this);
        }

        // TODO: lanzar un error en caso contrario
    }

    public long getAlquiler() {
        return alquiler;
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = propietario;
    }

    /**
     * Tipos de propiedades
     */
    public enum Tipo {
        Transporte, Servicio, Solar
    }
}
