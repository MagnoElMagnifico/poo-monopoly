package monopoly;

import java.util.Optional;

/**
 * La clase Casilla representa una casilla del tablero, que pueden ser de dos tipos:
 *
 * <li> Propiedad: se puede comprar por los jugadores (Solares, Servicios, Transporte).
 * <li> Casilla especial: Cárcel, Salida, IrACárcel, Parking.
 *
 * <p> Además, sabe si hay un avatar sobre la casilla (útil para dibujar el tablero).
 *
 * @see monopoly.Propiedad
 * @see monopoly.Avatar
 */
public class Casilla {
    private String nombre;
    /** Si es una casilla especial, este campo está desactivado */
    private Optional<Propiedad> propiedad;
    private int codigoColor;

    /** Construye una nueva casilla de tipo Propiedad */
    public Casilla(String nombre, Propiedad.Tipo tipoPropiedad, int precioInicial, int codigoColor) {
        this.nombre = nombre;
        this.propiedad = Optional.of(new Propiedad(this, tipoPropiedad, precioInicial));
        this.codigoColor = codigoColor;
    }

    /** Construye una nueva casilla de tipo especial */
    public Casilla(String nombre, int codigoColor) {
        this.nombre = nombre;
        this.propiedad = Optional.empty();
        this.codigoColor = codigoColor;
    }

    @Override
    public String toString() {
        String propiedadStr;
        if (propiedad.isEmpty()) {
            propiedadStr = "No";
        } else {
            propiedadStr = """
            {
                    tipo: %s
                    precio inicial: %d
                }""".formatted(propiedad.get().getTipo(), propiedad.get().getPrecioInicial());
        }

        return """
                {
                    nombre: %s
                    propiedad: %s
                }""".formatted(nombre, propiedadStr);
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public Optional<Propiedad> getPropiedad() {
        return propiedad;
    }
}