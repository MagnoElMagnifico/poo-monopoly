package monopoly.casillas;

import monopoly.utilidades.Consola;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Representa un grupo lógico de casillas.
 *
 * @author Marcos Granja Grille
 * @date 10-10-2023
 * @see Casilla
 */
public class Grupo {
    /**
     * Este es un identificador único del grupo, dado por el orden de
     * declaración en el archivo de configuración de las casillas.
     * <p>
     * Idealmente, tienen esta forma:
     * <li> 0: Casillas especiales (Cárcel, Parking, IrCárcel, Impuestos...)
     * <li> 1: Transportes
     * <li> 2: Servicios
     * <li> El resto: solares.
     */
    private final int numero;
    private final String nombre;
    private final int codigoColor;
    private final HashSet<Casilla> casillas;

    public Grupo(int numero, String nombre, int codigoColor) {
        this.numero = numero;
        this.nombre = nombre;
        this.codigoColor = codigoColor;
        this.casillas = new HashSet<>(3);
    }

    @Override
    public String toString() {
        StringBuilder casillasStr = new StringBuilder();

        Iterator<Casilla> iter = casillas.iterator();
        casillasStr.append('[');

        while (iter.hasNext()) {
            casillasStr.append(iter.next().getNombre());

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
                }""".formatted(Consola.fmt(nombre, codigoColor), numero, casillasStr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Grupo && ((Grupo) obj).nombre.equals(this.nombre);
    }

    public void anadirCasilla(Casilla casilla) {
        casillas.add(casilla);
    }

    public int getNumeroCasillas() {
        return casillas.size();
    }

    public int getNumero() {
        return numero;
    }

    /**
     * Devuelve el número de solar del grupo
     */
    public int getNumeroSolar() {
        return numero - 3;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public HashSet<Casilla> getCasillas() {
        return casillas;
    }
}
