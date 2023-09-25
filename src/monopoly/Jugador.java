package monopoly;
public class Jugador {
    private String nombre;
    private char Avatar;

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

    public void crearJugador(){
        System.out.println("nombre");
        nombre=getNombre();
        System.out.println("avatar");
        avatar=getAvatar();

    }


}
