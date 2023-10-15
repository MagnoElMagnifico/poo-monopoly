package monopoly.casillas;

import monopoly.utilidades.Formatear;

import java.util.ArrayList;
import java.util.Iterator;

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

    @Override
    public String toString() {
        StringBuilder casillasStr = new StringBuilder();

        Iterator<Casilla> iter = casillas.iterator();
        casillasStr.append('[');

        while (iter.hasNext()) {
            casillasStr.append(Formatear.con(iter.next().getNombre(), (byte) codigoColor));

            if (iter.hasNext()) {
                casillasStr.append(", ");
            }
        }
        casillasStr.append(']');

        return """
                {
                    nombre: %s
                    número: %d
                    casillas: %s
                }""".formatted(Formatear.con(nombre, (byte) codigoColor), numero, casillasStr);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Grupo && ((Grupo) obj).getNombre().equals(nombre);
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
        return numero < 4? 0 : numero - 3;
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
