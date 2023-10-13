package monopoly.jugador;

import monopoly.jugador.Avatar.TipoAvatar;
import monopoly.casillas.Casilla;

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
    private final int fortuna;

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
                }""".formatted(nombre, avatar.getId(), fortuna);
    }

    @Override
    public boolean equals(Object obj) {
        // Dos jugadores son iguales si sus avatares tienen el mismo ID
        return obj instanceof Jugador && ((Jugador) obj).getAvatar().getId() == this.getAvatar().getId();
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
