package monopoly.utils;

import monopoly.JuegoConsts;
import monopoly.Tablero;
import monopoly.casilla.carta.Carta;
import monopoly.casilla.carta.Carta.TipoCarta;
import monopoly.casilla.Casilla;
import monopoly.casilla.Casilla.TipoCasilla;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.carta.Mazo;
import monopoly.casilla.propiedad.Propiedad.TipoPropiedad;
import monopoly.error.ErrorFatalConfig;

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
 * <p>
 * También se usa para leer el nombre y descripción de las
 * Cartas de Comunidad y Suerte.
 * <p>
 * Formato:
 *
 * <pre>
 *     S:X:descripción
 *     C:X: descripción
 *     ...
 * </pre>
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see Casilla
 * @see monopoly.Juego
 */
public class Lector {
    private final ArrayList<Casilla> casillas;
    private final ArrayList<Grupo> grupos;
    private final ArrayList<Carta> cartasComunidad;
    private final ArrayList<Carta> cartasSuerte;

    public Lector() throws ErrorFatalConfig {
        // @formatter:off
        casillas        = new ArrayList<>(JuegoConsts.N_CASILLAS);
        grupos          = new ArrayList<>(JuegoConsts.N_GRUPOS);
        cartasComunidad = new ArrayList<>(JuegoConsts.N_CARTAS_COMUNIDAD);
        cartasSuerte    = new ArrayList<>(JuegoConsts.N_CARTAS_SUERTE);
        // @formatter:on

        leerCasillas();
        leerCartas();
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    public ArrayList<Carta> getCartasComunidad() {
        return cartasComunidad;
    }

    public ArrayList<Carta> getCartasSuerte() {
        return cartasSuerte;
    }

    /**
     * Función de ayuda que abre un Scanner para leer un archivo.
     * @throws ErrorFatalConfig Si no se encuentra el archivo.
     */
    private static Scanner abrirArchivo(String path) throws ErrorFatalConfig {
        Scanner scanner;

        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            throw new ErrorFatalConfig("No se ha encontrado", path, 0);
        }

        return scanner;
    }

    /**
     * Función de ayuda que ignora líneas en blanco y comentarios
     */
    private static boolean ignorarLinea(String linea) {
        return linea.isBlank() || linea.stripLeading().startsWith("#");
    }

    /**
     * Lee el archivo de configuración de Casillas del archivo de
     * configuración especificado en <code>JuegoConsts.CONFIG_CASILLAS</code>.
     * @throws ErrorFatalConfig Si no se encuentra el archivo o si el
     *                          formato no es correcto.
     */
    private void leerCasillas() throws ErrorFatalConfig {
        Scanner scanner = abrirArchivo(JuegoConsts.CONFIG_CASILLAS);

        for (int nLinea = 1; scanner.hasNextLine(); nLinea++) {
            String linea = scanner.nextLine();

            // Se ignoran los comentarios y líneas en blanco
            if (ignorarLinea(linea)) {
                continue;
            }

            // Se quitan los espacios y se separa por las comas
            String[] campos = linea.strip().replaceAll(" +", "").split(",");

            if (campos.length < 2) {
                scanner.close();
                throw new ErrorFatalConfig("Número de campos incorrecto", JuegoConsts.CONFIG_CASILLAS, nLinea);
            }

            if (campos[0].startsWith("grupo:")) {
                // Declaración de un Grupo:
                // Se quita la etiqueta del grupo, son 6 caracteres
                String nombre = campos[0].substring(6);
                int codigoColor = Integer.parseInt(campos[1]);

                // El número de grupo es la posición en este Array
                grupos.add(new Grupo(grupos.size(), nombre, codigoColor));

            } else {
                // Declaración de una Casilla
                int nGrupo = Integer.parseInt(campos[1]);

                if (nGrupo > grupos.size()) {
                    scanner.close();
                    throw new ErrorFatalConfig("Número de grupo inválido", JuegoConsts.CONFIG_CASILLAS, nLinea);
                }

                // Si hay 3 campos, es una propiedad. Si no es una casilla especial
                Casilla c = campos.length == 3 ?
                        new Casilla(casillas.size(), grupos.get(nGrupo), campos[0], TipoPropiedad.valueOf(campos[2])) :
                        new Casilla(casillas.size(), grupos.get(nGrupo), TipoCasilla.valueOf(campos[0]));

                casillas.add(c);
                grupos.get(nGrupo).anadirCasilla(c);
            }
        }

        scanner.close();
    }

    /**
     * Lee el archivo de configuración de cartas del archivo de
     * configuración especificado en <code>JuegoConsts.CONFIG_CARTAS</code>.
     * @throws ErrorFatalConfig Si no se encuentra el archivo o si el
     *                          formato no es correcto.
     */
    public void leerCartas() throws ErrorFatalConfig {
        Scanner scanner = abrirArchivo(JuegoConsts.CONFIG_CARTAS);

        for (int nLinea = 0; scanner.hasNextLine(); nLinea++) {
            String linea = scanner.nextLine().strip();

            // Se ignoran los comentarios y líneas en blanco
            if (ignorarLinea(linea)) {
                continue;
            }

            // Se limpia la línea y se separa en campos
            String[] campos = linea.strip().replaceAll(" *: *", ":").replaceAll("  +", " ").split(":");

            if (campos.length != 3) {
                scanner.close();
                throw new ErrorFatalConfig("Número de campos incorrecto", JuegoConsts.CONFIG_CARTAS, nLinea);
            }

            switch (campos[0]) {
                case "C" ->
                        cartasComunidad.add(new Carta(tablero, Integer.parseInt(campos[1]), TipoCarta.Comunidad, campos[2]));
                case "S" ->
                        cartasSuerte.add(new Carta(tablero, Integer.parseInt(campos[1]), TipoCarta.Suerte, campos[2]));
                default -> {
                    scanner.close();
                    throw new ErrorFatalConfig("\"%s\": tipo de carta desconocido".formatted(campos[0]), JuegoConsts.CONFIG_CARTAS, nLinea);
                }
            }
        }
    }
}
