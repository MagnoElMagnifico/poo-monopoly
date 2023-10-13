package monopoly.casillas;

import monopoly.jugador.Jugador;

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
 * @see Casilla
 * @see Jugador
 */
public class Propiedad {
    private final Casilla casilla;
    private final Tipo tipo;
    private final int precioInicial;
    private Optional<Jugador> propietario;

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
        this.precioInicial = precioInicial;
        this.propietario = Optional.empty();
    }

    @Override
    public String toString() {
        return """
                    {
                        nombre: %s
                        tipo: %s
                        precio inicial: %d
                    }
                """.formatted(casilla.getNombre(), tipo, precioInicial);
    }

    public Casilla getCasilla() {
        return casilla;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getPrecioInicial() {
        return precioInicial;
    }

    public Optional<Jugador> getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = Optional.of(propietario);
    }

    /**
     * Tipos de propiedades
     */
    public enum Tipo {
        Solar, Servicio, Transporte
    }
}
