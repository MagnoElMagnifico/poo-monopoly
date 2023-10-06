package monopoly.utilidades;

import monopoly.Casilla;
import monopoly.Propiedad;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Clase de ayuda que lee de un archivo una lista de casillas
 * y crea los objetos acordes.
 *
 * Formato esperado:
 *
 * <code>
 *     nombre, tipo, precio, códigoColor,
 *     nombre, códigoColor,
 *     ...
 * </code>
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see monopoly.Casilla
 * @see monopoly.Tablero
 */
public class LectorCasillas {
    public static ArrayList<Casilla> leerCasillas(String path) throws FileNotFoundException {
        File archivo = new File(path);
        Scanner scanner = new Scanner(archivo);
        ArrayList<Casilla> casillas = new ArrayList<>(40);

        int linea = 0;
        while (scanner.hasNextLine()) {
            linea++;

            // Los elementos en la línea se separan por ','
            Scanner scannerLinea = new Scanner(scanner.nextLine()).useDelimiter("\\s*,\\s*");

            // Se lee el nombre de la casilla
            assert scannerLinea.hasNext() : "se esperaba el nombre de la casilla, linea %d".formatted(linea);
            String nombre = scannerLinea.next();

            // Si el siguiente elemento es un entero, pues es que es una casilla especial
            if (scannerLinea.hasNextInt()) {
                casillas.add(new Casilla(nombre, scannerLinea.nextInt()));
                continue;
            }

            // Se lee el tipo de la propiedad
            assert scannerLinea.hasNext() : "se esperaba el tipo de propiedad, linea %d".formatted(linea);
            Propiedad.Tipo tipo = stringATipoPropiedad(scannerLinea.next(), linea);

            // Finalmente, se leen los dos enteros que faltan
            // Precio inicial de la casilla
            assert scannerLinea.hasNextInt() : "se esperaba el precio de la propiedad, línea %d".formatted(linea);
            int precio = scannerLinea.nextInt();

            // Código de color de la casilla
            assert scannerLinea.hasNextInt() : "se esperaba el código de color, línea %d".formatted(linea);
            int codigoColor = scannerLinea.nextInt();

            casillas.add(new Casilla(nombre, tipo, precio, codigoColor));
        }

        scanner.close();
        return casillas;
    }

    /** Se convierte un String al tipo de dato enumerado */
    private static Propiedad.Tipo stringATipoPropiedad(String strTipo, int linea) {
        return switch (strTipo.toLowerCase()) {
            case "solar" -> Propiedad.Tipo.Solar;
            case "servicio" -> Propiedad.Tipo.Servicio;
            case "transporte" -> Propiedad.Tipo.Transporte;
            default -> {
                assert false : "\"%s\": tipo desconocido, linea %d".formatted(strTipo, linea);
                yield null; // Nunca se ejecuta, pero si no se devuelve algo, da error.
            }
        };
    }
}
