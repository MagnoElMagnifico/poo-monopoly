package monopoly.utilidades;

import monopoly.casillas.Casilla;
import monopoly.casillas.Grupo;

/**
 * Clase de ayuda para imprimir por Consola con colores y diferentes formatos.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 * @see <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">Guía de códigos ANSI</a>
 * @see <a href="https://stackoverflow.com/questions/4842424/list-of-ansi-color-escape-sequences">Códigos ANSI StackOverflow</a>
 */
public class Consola {
    /**
     * Secuencia de inicio de un código ANSI
     */
    private static final String INICIO = "\u001b[";
    /**
     * Código ANSI de reseteo
     */
    private static final String FIN = "\u001b[0m";

    /** Formatea un mensaje con un color específico. */
    public static String format(String msg, Color color) {
        return String.format("%s38;5;%dm%s%s", INICIO, color.ordinal(), msg, FIN);
    }

    /**
     * Formatea un mensaje con un código de color de fuente y unos estilos dados
     *
     * @param msg     Mensaje a formatear
     * @param color   Código de color de la fuente (número entero sin signo, 0 a 255)
     * @param estilos Lista de estilos a aplicar
     * @return La cadena formateada
     * @see <a href="https://user-images.githubusercontent.com/995050/47952855-ecb12480-df75-11e8-89d4-ac26c50e80b9.png">Codigos de Color</a>
     */
    public static String format(String msg, byte color, Estilo... estilos) {
        StringBuilder codEstilo = new StringBuilder();

        // Generar una cadena de estilos si los hay
        if (estilos.length != 0) {
            // Los códigos se separan con ';'
            for (int i = 0; i < estilos.length - 1; i++) {
                codEstilo.append(estilos[i].ordinal());
                codEstilo.append(';');
            }

            // Excepto el último
            codEstilo.append(estilos[estilos.length - 1].ordinal());
        }

        return String.format("%s%s38;5;%dm%s%s", INICIO, codEstilo.toString(), color, msg, FIN);
    }

    /** Imprime un mensaje de error en rojo a la consola */
    public static void error(String msg) {
        System.out.printf("%s\n", Consola.format(msg, Color.Rojo));
    }

    /**
     * Convierte el long a String y lo formatea separando las centenas con espacios
     */
    public static String num(long n) {
        String numStr = Long.toString(n);

        // Si es un numero de 3 o menos cifras no hay que hacer nada más
        if (numStr.length() <= 3) {
            return numStr;
        }

        // Empezamos tomando los últimos tres dígitos
        StringBuilder resultado = new StringBuilder(4 * numStr.length() / 3);
        resultado.append(numStr.substring(numStr.length() - 3));

        // Y se sigue tomando grupos de 3 en 3.
        for (int i = numStr.length() - 3; i > 0; i -= 3) {
            // Se añade al inicio con un espacio
            // Salvo cuando se llega al principio, que se recoge lo que quede
            resultado.insert(0, ' ');
            resultado.insert(0, numStr.substring(Math.max(0, i - 3), i));
        }

        return resultado.toString();
    }

    /**
     * Formatea el nombre de una casilla.
     * <p>
     * Incluye el nombre de la misma, el nombre del grupo
     * si no es una casilla especial; y se le aplican los
     * colores de su grupo.
     *
     * @param c Casilla de la que formatear su nombre
     * @return El nombre de la casilla formateada.
     */
    public static String casillaNombre(Casilla c) {
        Grupo g = c.getGrupo();

        if (!c.isPropiedad()) {
            return Consola.format(c.getNombre(), g.getCodigoColor());
        }

        return Consola.format("%s, %s".formatted(c.getNombre(), g.getNombre()), g.getCodigoColor());
    }

    /**
     * Colores soportados
     */
    public enum Color {
        Negro, Rojo, Verde, Amarillo, Azul, Magenta, Cian, Blanco
    }

    /**
     * Estilos soportados
     * <hr>
     * NOTA: Puede que algunos no estén soportados por algunas terminales.
     */
    public enum Estilo {
        Normal, Negrita, Claro, Cursiva, Subrayado, ParpadeoLento, ParpadeoRapido, Invertir, Esconder
    }
}
