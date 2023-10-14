package monopoly.jugador;

import monopoly.jugador.Avatar.TipoAvatar;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Formatear;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 *
 * @date 2-10-2023
 * @see Avatar
 */
public class Jugador {
    private final String nombre;
    private final Avatar avatar;
    private int fortuna;

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */
    public Jugador(String nombre, TipoAvatar tipo, char id, Casilla casillaInicial) {
        avatar = new Avatar(tipo, id, this, casillaInicial);
        this.nombre = nombre;
        fortuna = 0;
    }

    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                }""".formatted(nombre, avatar.getId(), Formatear.num(fortuna));
        // TODO: propiedades
        // TODO: hipotecas
        // TODO: edificios
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Jugador && ((Jugador) obj).getAvatar() == avatar;
    }

    /**
     * Cobra al jugador una cantidad de dinero
     */
    public void cobrar(int cantidad) {
        if (cantidad > 0) {
            fortuna -= cantidad;
        }
        // TODO: lanzar error de lo contrario
        // TODO: lanzar error en caso de que no tenga suficientes fondos
    }

    /**
     * Ingresa una cantidad de dinero al jugador
     */
    public void ingresar(int cantidad) {
        if (cantidad > 0) {
            fortuna += cantidad;
        }
        // TODO: lanzar error de lo contrario
    }

    public String getNombre() {
        return nombre;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public int getFortuna() {
        return fortuna;
    }
}
