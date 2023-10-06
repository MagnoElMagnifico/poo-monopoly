package monopoly;

import java.util.Optional;

/**
 * Representa una casilla que se puede comprar por un jugador.
 *
 * Existen tres tipos:
 *
 * <li>Solares</li>
 * <li>Servicios</li>
 * <li>Transporte</li>
 *
 * @see monopoly.Casilla
 * @see monopoly.Jugador
 * @author Marcos Granja Grille
 * @date 2-10-2023
 */
public class Propiedad {
    public enum Tipo {
        Solar, Servicio, Transporte
    }

    private Casilla casilla;
    private Tipo tipo;
    private int precioInicial;
    private Optional<Jugador> propietario;

    /**
     * Crea una propiedad
     *
     * @param casilla        Casilla a la que está asociada
     * @param tipo           Tipo de casilla: Solar, Servicio o Transporte
     * @param precioInicial  Precio inicial de la propiedad
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
}
