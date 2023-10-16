package monopoly.casillas;

import monopoly.jugadores.Avatar;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;

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
     * Esta variable se usa para almacenar:
     *     - el bote del Parking,
     *     - la fianza de la Cárcel
     *     - el dinero a pagar en los impuestos
     * En otros casos, se almacena la información necesaria en propiedad.
     */
    private long dinero;

    /**
     * Construye una nueva casilla de tipo Propiedad
     */
    public Casilla(Grupo grupo, String nombre, Propiedad.Tipo tipoPropiedad) {
        this.nombre = nombre;
        this.propiedad = new Propiedad(this, tipoPropiedad);
        this.grupo = grupo;
        this.avatares = new ArrayList<>();
        this.dinero = -1; // Todavía no se le ha asignado un precio
    }

    /**
     * Construye una nueva casilla de tipo especial
     */
    public Casilla(Grupo grupo, String nombre) {
        this.nombre = nombre;
        this.propiedad = null;
        this.grupo = grupo;
        this.avatares = new ArrayList<>();
        this.dinero = -1; // Todavía no se le ha asignado un precio
    }

    // Comando describir
    @Override
    public String toString() {
        if (!isPropiedad()) {
            return switch (nombre) {
                case "Salida" -> """
                        %s: Casilla de inicio del juego.
                        Cada vez que un jugador pase por esta casilla recibirá %s.
                        """.formatted(Formatear.casillaNombre(this), Formatear.num(dinero));
                case "IrCárcel" -> """
                        %s: Si un jugador cae en esta casilla, se le enviará directamente
                        a la casilla Cárcel.
                        """.formatted(Formatear.casillaNombre(this));
                case "Comunidad", "Suerte" -> """
                        %s
                        """.formatted(Formatear.casillaNombre(this));
                case "Impuesto1", "Impuesto2" -> """
                        {
                            nombre: %s
                            importe: %s
                        }""".formatted(Formatear.casillaNombre(this), Formatear.num(dinero));
                case "Parking" -> """
                        {
                            nombre: %s
                            bote: %s
                        }""".formatted(Formatear.casillaNombre(this), Formatear.num(dinero)); // TODO: fortuna banca?
                case "Cárcel" -> """
                        {
                            nombre: %s
                            fianza: %s
                        }""".formatted(Formatear.casillaNombre(this), Formatear.num(dinero));
                default -> Formatear.con("ERROR: hay un nombre de casilla especial no soportado en el archivo de configuración de las casillas", Color.Rojo);
            };
        }

        return propiedad.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Casilla && ((Casilla) obj).getNombre().equalsIgnoreCase(this.nombre);
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

    public long getPrecio() {
        return dinero;
    }

    public void setPrecio(long precio) {
        if (isPropiedad()) {
            propiedad.setPrecio(precio);
        }

        if (precio > 0) {
            this.dinero = precio;
        }
        // TODO: lanzar un error en caso contrario
    }

    public void anadirAvatar(Avatar avatar) {
        avatares.add(avatar);
    }

    public void quitarAvatar(Avatar avatar) {
        avatares.remove(avatar);
    }
}