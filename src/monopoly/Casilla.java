package monopoly;

import java.util.ArrayList;
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
    private final String nombre;
    /**
     * Si es una casilla especial, este campo está desactivado
     */
    private final Optional<Propiedad> propiedad;
    private final int codigoColor;
    private ArrayList<Avatar> avatares;

    /**
     * Construye una nueva casilla de tipo Propiedad
     */
    public Casilla(String nombre, Propiedad.Tipo tipoPropiedad, int precioInicial, int codigoColor) {
        this.nombre = nombre;
        this.propiedad = Optional.of(new Propiedad(this, tipoPropiedad, precioInicial));
        this.codigoColor = codigoColor;
        this.avatares = new ArrayList<>();
    }

    /**
     * Construye una nueva casilla de tipo especial
     */
    public Casilla(String nombre, int codigoColor) {
        this.nombre = nombre;
        this.propiedad = Optional.empty();
        this.codigoColor = codigoColor;
        this.avatares = new ArrayList<>();
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
                    jugadores: %s
                }""".formatted(nombre, propiedadStr, avatares);
    }

    // TODO: esto se puede usar?
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Casilla && ((Casilla) obj).getNombre().equals(this.nombre);
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

    public ArrayList<Avatar> getAvatares() {
        return avatares;
    }

    public void anadirAvatar(Avatar avatar) {
        avatares.add(avatar);
    }

    public void quitarAvatar(Avatar avatar) {
        avatares.remove(avatar);
    }
}