package monopoly;

import java.util.Optional;

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
 * @see monopoly.Casilla
 * @see monopoly.Jugador
 */
public class Propiedad {
    private final Casilla casilla;
    private final Tipo tipo;
    private int precio;
    private int alquiler;
    private Jugador propietario;
    /**
     * Crea una propiedad
     *
     * @param casilla       Casilla a la que está asociada
     * @param tipo          Tipo de casilla: Solar, Servicio o Transporte
     * @param precioInicial Precio inicial de la propiedad
     */
    public Propiedad(Casilla casilla, Tipo tipo, int precioInicial) {
        this.casilla = casilla;
        this.tipo = tipo;
        this.precio = precioInicial;
        this.alquiler= (int) (precio*0.1);
        this.propietario = null;
    }

    @Override
    public String toString() {
        return """
                    {
                        nombre: %s
                        tipo: %s
                        precio inicial: %d
                    }
                """.formatted(casilla.getNombre(), tipo, precio);
    }

    public Casilla getCasilla() {
        return casilla;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getPrecio() {
        return precio;
    }

    public void setAlquiler(int alquiler) {
        this.alquiler = alquiler;
    }

    public int getAlquiler() {
        return alquiler;
    }

    public void serPrecio(int precio){
        this.precio=precio;
    }
    public Jugador getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = propietario;
    }

    public enum Tipo {
        Solar, Servicio, Transporte
    }
}
