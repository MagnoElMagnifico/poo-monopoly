package monopoly;

import monopoly.Avatar.TipoAvatar;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 *
 * @date 2-10-2023
 * @see monopoly.Avatar
 */
public class Jugador {
    private final String nombre;
    private final Avatar avatar;
    private int fortuna;

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */


    public Jugador(){
        this.nombre="Banca";
        this.avatar=null;
        this.fortuna=0;

    }
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

    public String getNombre() {
        return nombre;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public int getFortuna() {
        return fortuna;
    }

    public void setFortuna(int fortuna){
        this.fortuna=fortuna;
    }
}

