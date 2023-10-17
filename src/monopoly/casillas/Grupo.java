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
    private final byte codigoColor;
    private final ArrayList<Casilla> casillas;

    public Grupo(int numero, String nombre, byte codigoColor) {
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

    public void anadirCasilla(Casilla casilla) {
        casillas.add(casilla);
    }

    public int getNumeroCasillas() {
        return casillas.size();
    }

    public int getNumero() {
        return numero;
    }

    /** Devuelve el número de solar del grupo */
    public int getNumeroSolar() {
        return numero < 3 ? 0 : numero - 3;
    }

    public String getNombre() {
        return nombre;
    }

    public byte getCodigoColor() {
        return codigoColor;
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }
}
