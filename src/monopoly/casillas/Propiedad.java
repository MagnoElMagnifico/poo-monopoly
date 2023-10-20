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
    /** Tipos de propiedades */
    public enum TipoPropiedad {
        Transporte, Servicio, Solar
    }

    private final Casilla casilla;
    private final TipoPropiedad tipo;
    private long precio;
    private long alquiler;
    private Jugador propietario;

    /**
     * Crea una propiedad.
     *
     * @param casilla Casilla a la que está asociada
     * @param tipo    Tipo de casilla: Solar, Servicio o Transporte
     */
    public Propiedad(Casilla casilla, TipoPropiedad tipo) {
        this.casilla = casilla;
        this.tipo = tipo;
        this.propietario = null;
        this.precio = -1;   // Marcar como todavía no establecido
        this.alquiler = -1; // Marcar como todavía no establecido
    }

    // Para el comando listar enventa
    @Override
    public String toString() {
        return """
               {
                   nombre: %s
                   tipo: %s%s
                   precio; %s
                   alquiler: %s
                   propietario: %s
               }""".formatted(casilla.getNombre(),
                              tipo,
                              tipo == TipoPropiedad.Solar? "" : '\n' + casilla.getGrupo().getNombre(),
                              Formatear.num(precio),
                              Formatear.num(alquiler),
                              propietario == null ? "-" : propietario.getNombre());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Propiedad && ((Propiedad) obj).getCasilla().equals(this.casilla);
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
        if (precio > 0) {
            this.precio = precio;
            this.alquiler = Calculadora.calcularAlquiler(this);
        }

        // TODO: lanzar un error en caso contrario
    }

    public void aumentarPrecio() {
        this.precio= (long) (this.precio*1.05);
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
}
