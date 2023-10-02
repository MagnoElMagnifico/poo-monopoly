package monopoly;

import java.util.ArrayList;
import monopoly.Avatar.TipoAvatar;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 * @date 2-10-2023
 * @see monopoly.Avatar
 * */
public class Jugador {
    private String nombre;
    private Avatar avatar;

    private int fortuna;
    private ArrayList<String> propiedades;
    private ArrayList<String> hipotecas;
    private ArrayList<String> edificios;

    /** Crea un Jugador dado su nombre, tipo de avatar e id */
    public Jugador(String nombre, TipoAvatar tipo, char id) {
        avatar = new Avatar(tipo, id, this);
        this.nombre = nombre;

        fortuna = 0;
        propiedades = new ArrayList<>();
        hipotecas = new ArrayList<>();
        edificios = new ArrayList<>();
    }

    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    propiedades: %s
                    hipotecas: %s
                    edificios: %s
                }
                """.formatted(nombre, avatar.getId(), fortuna, propiedades, hipotecas, edificios);
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

    public ArrayList<String> getPropiedades() {
        return propiedades;
    }

    public ArrayList<String> getHipotecas() {
        return hipotecas;
    }

    public ArrayList<String> getEdificios() {
        return edificios;
    }
}
