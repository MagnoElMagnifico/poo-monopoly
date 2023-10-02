package monopoly;

import java.util.ArrayList;


public class Jugador {
    private String nombre;
    private char avatar;
    private byte turno;
    private int fortuna;
    private ArrayList<String> propiedades;
    private ArrayList<String> hipotecas;
    private ArrayList<String> edificios;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public char getAvatar() {
        return avatar;
    }

    public void setAvatar(char avatar) {
        this.avatar = avatar;
    }

    public byte getTurno() {
        return turno;
    }

    public void setTurno(byte turno) {
        this.turno = turno;
    }

    public int getFortuna() {
        return fortuna;
    }

    public void setFortuna(int fortuna) {
        this.fortuna = fortuna;
    }

    public ArrayList<String> getPropiedades() {
        return propiedades;
    }

    public void setPropiedades(ArrayList<String> propiedades) {
        this.propiedades = propiedades;
    }

    public ArrayList<String> getHipotecas() {
        return hipotecas;
    }

    public void setHipotecas(ArrayList<String> hipotecas) {
        this.hipotecas = hipotecas;
    }

    public ArrayList<String> getEdificios() {
        return edificios;
    }

    public void setEdificios(ArrayList<String> edificios) {
        this.edificios = edificios;
    }

    /*
    @Override
    public String toString() {
        return "{\n" +
                "nombre: " + nombre + ",\n" +
                "avatar: " + avatar + "\n" +
                "}";
    }

    public void jugador() {
        System.out.println(toString());
    }*/
    public void jugador(){
        System.out.println("nombre: " + getNombre() + ",");
        System.out.println("avatar: " + getAvatar() + ",");
    }


    public void crearJugador(String nombre, char avatar){
        setNombre(nombre);
        setAvatar(avatar);
        jugador();
    }
}
