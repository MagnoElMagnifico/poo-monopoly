package monopoly.utilidades;

import monopoly.casillas.Casilla;
import monopoly.casillas.Grupo;
import monopoly.casillas.Propiedad.TipoPropiedad;

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
 * También se usa para leer el nombre y descripción de las
 * Cartas de Comunidad y Suerte.
 * <p>
 * Formato:
 *
 * <pre>
 *     Suerte:<num>:descripción
 *     Comunidad:<num>: descripción
 *     ...
 * </pre>
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see Casilla
 * @see monopoly.Tablero
 */
public class Lector {

    private static Scanner abrirArchivo(String path) {
        Scanner scanner = null;

        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.err.printf("[FATAL] No se ha encontrado el archivo de configuración \"%s\": %s\n", path, e);
            System.exit(1);
        }

        return scanner;
    }

    private static boolean ignorarLinea(String linea) {
        return linea.isBlank() || linea.stripLeading().startsWith("#");
    }

    /**
     * Lee el archivo dada su dirección.
     *
     * @param path Dirección del achivo a leer.
     * @return La lista de casillas contenida en el archivo
     */
    public static ArrayList<Casilla> leerCasillas(String path) {
        Scanner scanner = abrirArchivo(path);

        // Hay 8 grupos de solares, 1 de transporte,
        // 1 de servicios y 1 de casillas especiales.
        ArrayList<Grupo> grupos = new ArrayList<>(11);
        ArrayList<Casilla> casillas = new ArrayList<>(40);

        for (int nLinea = 1; scanner.hasNextLine(); nLinea++) {
            String linea = scanner.nextLine();

            // Se ignoran los comentarios y líneas en blanco
            if (ignorarLinea(linea)) {
                continue;
            }

            // Se quitan los espacios y se separa por las comas
            String[] campos = linea.strip().replaceAll(" +", "").split(",");

            if (campos.length < 2) {
                System.err.printf("[FATAL] ArchivoCasillas línea %d: Número de campos incorrecto\n", nLinea);
                System.exit(1);
            }

            if (campos[0].startsWith("grupo:")) {
                // Declaración de un Grupo:
                // Se quita la etiqueta del grupo, son 6 caracteres
                String nombre = campos[0].substring(6);
                byte codigoColor = (byte) Integer.parseInt(campos[1]);

                // El número de grupo es la posición en este Array
                grupos.add(new Grupo(grupos.size(), nombre, codigoColor));

            } else {
                // Declaración de una Casilla Propiedad:
                int nGrupo = Integer.parseInt(campos[1]);

                if (nGrupo > grupos.size()) {
                    System.err.printf("[FATAL] ArchivoCasillas linea %d: %d número de grupo inválido\n", nLinea, nGrupo);
                    System.exit(1);
                }

                Casilla c = campos.length == 3 ?
                        new Casilla(grupos.get(nGrupo), campos[0], stringATipoPropiedad(campos[2])) :
                        new Casilla(grupos.get(nGrupo), campos[0]);

                casillas.add(c);
                grupos.get(nGrupo).anadirCasilla(c);
            }
        }

        scanner.close();
        return casillas;
    }

    /**
     * Se convierte un String al tipo de dato enumerado
     */
    private static TipoPropiedad stringATipoPropiedad(String strTipo) {
        return switch (strTipo.toLowerCase()) {
            case "solar" -> TipoPropiedad.Solar;
            case "servicio" -> TipoPropiedad.Servicio;
            case "transporte" -> TipoPropiedad.Transporte;
            default -> {
                System.err.printf("[FATAL] ArchivoCasillas: \"%s\": tipo desconocido", strTipo);
                System.exit(1);
                yield null; // Nunca se ejecuta, pero si no se devuelve algo, da error.
            }
        };
    }
}
