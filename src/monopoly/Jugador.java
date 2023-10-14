package monopoly;

import monopoly.Avatar.TipoAvatar;

import java.util.ArrayList;

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
    private ArrayList<Propiedad> propiedades;


    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */


    public Jugador(){
        this.nombre="Banca";
        this.avatar=null;
        this.fortuna=0;
        this.propiedades=new ArrayList<Propiedad>(28);

    }
    public Jugador(String nombre, TipoAvatar tipo, char id, Casilla casillaInicial) {
        avatar = new Avatar(tipo, id, this, casillaInicial);
        this.nombre = nombre;
        fortuna = 200;
        this.propiedades=new ArrayList<Propiedad>();
    }

    public Jugador(String nombre){
        avatar=null;
        this.nombre=nombre;
        fortuna=0;
        this.propiedades=null;
    }
    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    Propiedades: %s
                }""".formatted(nombre, avatar.getId(), fortuna,propiedades);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Jugador && ((Jugador) obj).nombre.equalsIgnoreCase(this.nombre);
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

    public ArrayList<Propiedad> getPropiedades(){
        return this.propiedades;
    }

}

