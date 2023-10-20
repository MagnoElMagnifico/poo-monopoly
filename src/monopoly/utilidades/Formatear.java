package monopoly.utilidades;

import monopoly.casillas.Casilla;

/**
 * Clase de ayuda para imprimir por Consola con colores y diferentes formatos.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 * @see <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">Guía de códigos ANSI</a>
 * @see <a href="https://stackoverflow.com/questions/4842424/list-of-ansi-color-escape-sequences">Códigos ANSI StackOverflow</a>
 */
public class Formatear {
    /**
     * Secuencia de inicio de un código ANSI
     */
    private static final String INICIO = "\u001b[";
    /**
     * Código ANSI de reseteo
     */
    private static final String FIN = "\u001b[0m";

    /**
     * Función de ayuda para generar el código ANSI de todos los estilos dados
     */
    private static String getCodigoAnsi(Estilo... estilos) {
        if (estilos.length == 0) {
            return "";
        }

        StringBuilder codigo = new StringBuilder();

        // Los códigos se separan con ';'
        for (int i = 0; i < estilos.length - 1; i++) {
            codigo.append(estilos[i].ordinal());
            codigo.append(';');
        }

        codigo.append(estilos[estilos.length - 1].ordinal());

        return codigo.toString();
    }

    /**
     * Formatea un mensaje con un código de color de fuente, código de color de fondo y unos estilo dados
     *
     * @param msg    Mensaje a formatear
     * @param fuente Código de color de la fuente (número entero sin signo, 0 a 255)
     * @param fondo  Código de color del fondo (número entero sin signo, 0 a 255)
     * @return La cadena formateada
     * @see <a href="https://user-images.githubusercontent.com/995050/47952855-ecb12480-df75-11e8-89d4-ac26c50e80b9.png">Codigos de Color</a>
     */
    public static String con(String msg, byte fuente, byte fondo, Estilo... estilos) {
        return "%s%s;38;5;%d;48;5;%dm%s%s".formatted(INICIO, Formatear.getCodigoAnsi(estilos),
                fuente & 0xFF, fondo & 0xFF, msg, FIN);
    }

    /**
     * Formatea un mensaje con un código de color de fuente y unos estilos dados
     */
    public static String con(String msg, byte fuente, Estilo... estilos) {
        return "%s%s;38;5;%dm%s%s".formatted(INICIO, Formatear.getCodigoAnsi(estilos),
                fuente & 0xFF, msg, FIN);
    }

    /**
     * Formatea un mensaje con un color de fuente y fondo, más unos estilos
     */
    public static String con(String msg, Color fuente, Color fondo, Estilo... estilos) {
        return Formatear.con(msg, (byte) fuente.ordinal(), (byte) fondo.ordinal(), estilos);
    }

    /**
     * Formatea un mensaje con un color de fuente y unos estilos dados
     */
    public static String con(String msg, Color fuente, Estilo... estilos) {
        return Formatear.con(msg, (byte) fuente.ordinal(), estilos);
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
     * Formatea un String de forma que `msg` tiene como máximo
     * tam-2 caracteres de longitud y está rodeado de espacios.
     * <p>
     * Por ejemplo:
     *
     * <pre>
     *     msg = estoesunejemplo
     *     tam = 10
     *     resultado -> " estoesu. "
     *
     *     msg = hola
     *     tam = 10
     *     resultado -> " hola     "
     * </pre>
     *
     * @param msg Mensaje que formatear
     * @param tam Tamaño del mensaje
     * @return Mensaje formateado
     */
    public static String celda(String msg, int tam) {
        // TODO: handle invalid/negative inputs
        if (msg.length() + 2 > tam) {
            return " %s. ".formatted(msg.substring(0, tam - 3));
        }
        return " %s%s".formatted(msg, " ".repeat(tam - msg.length() - 1));
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
        if (!c.isPropiedad()) {
            return Formatear.con(c.getNombre(), c.getGrupo().getCodigoColor());
        }

        return Formatear.con("%s, %s".formatted(c.getNombre(), c.getGrupo().getNombre()), c.getGrupo().getCodigoColor());
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
