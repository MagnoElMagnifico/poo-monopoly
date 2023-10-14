package monopoly.casillas;

import java.util.ArrayList;

/**
 * Representa un grupo lógico de casillas.
 *
 * @author Marcos Granja Grille
 * @date 10-10-2023
 * @see Casilla
 */
public class Grupo {
    private final int numero;
    private final String nombre;
    private final int codigoColor;
    private ArrayList<Casilla> casillas;

    public Grupo(int numero, String nombre, int codigoColor) {
        this.numero = numero;
        this.nombre = nombre;
        this.codigoColor = codigoColor;
        this.casillas = new ArrayList<>(3);
    }

    public void anadirCasilla(Casilla casilla)  {
        casillas.add(casilla);
    }

    public int getNumeroCasillas() {
        return casillas.size();
    }

    public int getNumero() {
        return numero;
    }

    public int getNumeroSolar() {
        return numero < 4? 0 : numero - 4;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }
}
