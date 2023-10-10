package monopoly;

/** Representa un grupo lógico de casillas */
public class Grupo {
    private int numero;
    private int codigoColor;
    private String nombre;

    public Grupo(int numero, String nombre, int codigoColor) {
        this.numero = numero;
        this.nombre = nombre;
        this.codigoColor = codigoColor;
    }

    public int getNumero() {
        return numero;
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public String getNombre() {
        return nombre;
    }
}
