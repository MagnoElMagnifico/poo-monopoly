package monopoly.casilla;

import java.util.ArrayList;

import monopoly.JuegoConsts;
import monopoly.error.ErrorFatal;
import monopoly.utils.Listable;
import monopoly.casilla.especial.CasillaEspecial;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.jugador.Avatar;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

/**
 * La clase Casilla representa una casilla del tablero, que pueden ser
 * de los siguientes tipos:
 *
 * <li> Propiedad: se puede comprar por los jugadores (Solares, Servicios, Transporte).
 * <li> Especial: Cárcel, Salida, IrACárcel, Parking, Impuestos, CartaComunidad, CartaSuerte.
 * <li> Acción: CartaSuerte y CartaComunidad
 * <li> Impuesto
 *
 * <p>
 * Además, sabe si hay un avatar sobre la casilla (útil para dibujar el tablero).
 * <p>
 * Se trata de una clase abstracta porque hay ciertas funcionalidades que dependen de cada
 * tipo de casilla, como la acción que se debe realizar cuando un jugador cae en ella.
 *
 * @see Propiedad
 * @see CasillaEspecial
 * @see monopoly.casilla.carta.CasillaAccion
 * @see CasillaImpuesto
 */
public abstract class Casilla implements Listable {
    private final int posicion;
    private final ArrayList<Avatar> avatares;

    // Para las estadísticas
    private int nEstancias;

    public Casilla(int posicion) {
        this.posicion = posicion;

        avatares = new ArrayList<>(JuegoConsts.MAX_JUGADORES);
        nEstancias = 0;
    }

    /**
     * Se usa para mostrar toda la información sobre la casilla (comandos de describir).
     * <br>
     * <b>NOTA</b>: requerida por la especificación de la entrega 3.
     */
    @Override
    public String toString() {
        return getNombreFmt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Casilla && ((Casilla) obj).posicion == this.posicion;
    }

    /**
     * Ejecuta la acción correspondiente cuando un avatar cae en
     * esta casilla.
     */
    public abstract void accion(Jugador jugadorTurno, Dado dado) throws ErrorFatal;

    /** Nombre de la casilla sin ningún formato */
    public abstract String getNombre();

    /**
     * Obtiene el nombre formateado (con colores) de la casilla
     * <br>
     * Se usa para ponerla en el tablero.
     */
    public abstract String getNombreFmt();

    /**
     * Devuelve <code>true</code> en caso de que el avatar en concreto
     * se encuentre en la casilla actual.
     * <p>
     * <b>NOTA</b>: requerida por la especificación de la entrega 3.
     */
    public boolean estaAvatar(Avatar avatar) {
        return avatares.contains(avatar);
    }

    /**
     * Devuelve la cantidad de veces que un avatar ha caído en
     * esta casilla.
     * <p>
     * <b>NOTA</b>: requerida por la especificación de la entrega 3.
     */
    public int frecuenciaVisita() {
        return nEstancias;
    }

    public int getPosicion() {
        return posicion;
    }

    public ArrayList<Avatar> getAvatares() {
        return avatares;
    }

    public void anadirAvatar(Avatar avatar) {
        nEstancias++;
        avatares.add(avatar);
    }

    public void quitarAvatar(Avatar avatar) {
        avatares.remove(avatar);
    }
}
