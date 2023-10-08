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
 * <p>
 * Formato esperado:
 *
 * <pre>
 *     nombre, tipo, precio, códigoColor,
 *     nombre, códigoColor,
 *     ...
 * </pre>
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

        while (scanner.hasNextLine()) {
            String linea = scanner.nextLine();

            // Ignorar comentarios y líneas en blanco
            if (linea.isBlank() || linea.stripLeading().startsWith("#")) {
                continue;
            }

            // Los elementos en la línea se separan por ','
            Scanner scannerLinea = new Scanner(linea).useDelimiter("\\s*,\\s*");

            // Se lee el nombre de la casilla
            String nombre = scannerLinea.next();

            // Si el siguiente elemento es un entero, pues es que es una casilla especial
            if (scannerLinea.hasNextInt()) {
                casillas.add(new Casilla(nombre, scannerLinea.nextInt()));
                continue;
            }

            // Se lee el tipo de la propiedad
            Propiedad.Tipo tipo = stringATipoPropiedad(scannerLinea.next());

            // Precio inicial de la casilla
            int precio = scannerLinea.nextInt();

            // Código de color de la casilla
            int codigoColor = scannerLinea.nextInt();

            casillas.add(new Casilla(nombre, tipo, precio, codigoColor));
        }

        scanner.close();

        if (casillas.size() % 4 != 0) {
            System.err.printf("ArchivoCasillas: en número de casillas debe ser múltiplo de 4, se leyeron %d\n", casillas.size());
            System.exit(1);
        }

        return casillas;
    }

    /**
     * Se convierte un String al tipo de dato enumerado
     */
    private static Propiedad.Tipo stringATipoPropiedad(String strTipo) {
        return switch (strTipo.toLowerCase()) {
            case "solar" -> Propiedad.Tipo.Solar;
            case "servicio" -> Propiedad.Tipo.Servicio;
            case "transporte" -> Propiedad.Tipo.Transporte;
            default -> {
                System.err.printf("ArchivoCasillas: \"%s\": tipo desconocido", strTipo);
                System.exit(1);
                yield null; // Nunca se ejecuta, pero si no se devuelve algo, da error.
            }
        };
    }
}
