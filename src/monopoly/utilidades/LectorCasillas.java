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
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see Casilla
 * @see monopoly.Tablero
 */
public class LectorCasillas {

    /**
     * Lee el archivo dada su dirección.
     *
     * @param path Dirección del achivo a leer.
     * @return La lista de casillas contenida en el archivo
     */
    public static ArrayList<Casilla> leerCasillas(String path) {
        File archivo = new File(path);
        Scanner scanner = null;

        try {
            scanner = new Scanner(archivo);
        } catch (FileNotFoundException e) {
            System.err.println("[FATAL] No se ha encontrado el archivo de configuración de las casillas");
            System.exit(1);
        }

        // Hay 8 grupos de solares, 1 de transporte,
        // 1 de servicios y 1 de casillas especiales.
        ArrayList<Grupo> grupos = new ArrayList<>(11);
        ArrayList<Casilla> casillas = new ArrayList<>(40);

        for (int nLinea = 1; scanner.hasNextLine(); nLinea++) {
            String linea = scanner.nextLine();

            // Se ignoran los comentarios y líneas en blanco
            if (linea.isBlank() || linea.stripLeading().startsWith("#")) {
                continue;
            }

            // Se quitan los espacios y se separa por las comas
            String[] campos = linea.strip().replaceAll(" +", "").split(",");

            if (campos.length < 2) {
                System.err.printf("[FATAL] Número de campos incorrecto en la línea %d\n", nLinea);
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
                    System.err.printf("[FATAL] Número de grupo inválido en la línea %d, se obtuvo %d\n", nLinea, nGrupo);
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
                System.err.printf("ArchivoCasillas: \"%s\": tipo desconocido", strTipo);
                System.exit(1);
                yield null; // Nunca se ejecuta, pero si no se devuelve algo, da error.
            }
        };
    }
}
