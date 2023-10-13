package monopoly.casillas;

import monopoly.jugador.Avatar;

import java.util.ArrayList;

/**
 * La clase Casilla representa una casilla del tablero, que pueden ser de dos tipos:
 *
 * <li> Propiedad: se puede comprar por los jugadores (Solares, Servicios, Transporte).
 * <li> Casilla especial: Cárcel, Salida, IrACárcel, Parking.
 *
 * <p> Además, sabe si hay un avatar sobre la casilla (útil para dibujar el tablero).
 *
 * @see Propiedad
 * @see Avatar
 */
public class Casilla {
    private final String nombre;
    /**
     * Si es una casilla especial, este campo está desactivado
     */
    private final Propiedad propiedad;
    private final Grupo grupo;
    private final ArrayList<Avatar> avatares;

    /**
     * Construye una nueva casilla de tipo Propiedad
     */
    public Casilla(Grupo grupo, String nombre, Propiedad.Tipo tipoPropiedad) {
        this.nombre = nombre;
        this.propiedad = new Propiedad(this, tipoPropiedad, 0); // TODO
        this.grupo = grupo;
        this.avatares = new ArrayList<>();
    }

    /**
     * Construye una nueva casilla de tipo especial
     */
    public Casilla(Grupo grupo, String nombre) {
        this.nombre = nombre;
        this.propiedad = null;
        this.grupo = grupo;
        this.avatares = new ArrayList<>();
    }

    @Override
    public String toString() {
        String propiedadStr;
        if (propiedad == null) {
            propiedadStr = "No";
        } else {
            propiedadStr = """
                    {
                            tipo: %s
                            precio inicial: %d
                        }""".formatted(propiedad.getTipo(), propiedad.getPrecioInicial());
        }

        return """
                {
                    nombre: %s
                    propiedad: %s
                    jugadores: %s
                }""".formatted(nombre, propiedadStr, avatares);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Casilla && ((Casilla) obj).getNombre().equals(this.nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public boolean isPropiedad() {
        return propiedad != null;
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