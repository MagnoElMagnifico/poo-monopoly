package monopoly.casillas;

import monopoly.jugador.Jugador;

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
    private Jugador propietario;

    // ???
    private int precio;
    private int alquiler;

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
    }

    // Para el comando listar enventa
    @Override
    public String toString() {
        // TODO?: obtener el resto de información con la calculadora

        String grupoSolar = "";

        // Solo tiene sentido mostrar el nombre del grupo si es un solar
        // Mostrar también precio de las edificaciones
        if (tipo == Tipo.Solar) {
            grupoSolar = "grupo: %s".formatted(casilla.getGrupo().getNombre())
            return """
                    {
                        nombre: %s
                        tipo: Solar
                        grupo: %s
                        propietario: %s
                        
                        valor: %s
                        alquiler: %s
                    }""";
        }

        return """
                {
                    nombre: %s
                    tipo: %s
                    propietario: %s
                %s%s}""".formatted(casilla.getNombre(), tipo, propietario.getNombre());
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
