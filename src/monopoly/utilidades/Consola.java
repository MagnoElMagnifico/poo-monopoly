package monopoly.utilidades;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

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

    /**
     * Formatea un mensaje con un color específico.
     */
    public static String fmt(String msg, Color color) {
        return String.format("%s0;%dm%s%s", INICIO, color.ordinal() + 30, msg, FIN);
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
    public static String fmt(String msg, int color, Estilo... estilos) {
        StringBuilder codEstilo = new StringBuilder();

        // Los códigos se separan con ';'
        for (Estilo estilo : estilos) {
            codEstilo.append(estilo.ordinal());
            codEstilo.append(';');
        }

        return String.format("%s%s38;5;%dm%s%s", INICIO, codEstilo, color & 0xFF, msg, FIN);
    }

    /**
     * Imprime un mensaje de error en rojo a la consola
     */
    public static void error(String msg) {
        System.out.printf("%s\n", Consola.fmt(msg, Color.Rojo));
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
     * Función que permite listar en una línea una colección de elementos.
     * <p>
     * Por ejemplo:
     * <pre>
     *     // jugadores.Jugador.toString()
     *     Consola.listar(propiedades, (p) -> p.getNombre());
     * </pre>
     * Devuelve <pre>[Varsovia, Roma, Madrid]</pre>.
     *
     * @param elementos Lista con los elementos a mostrar
     * @param funcion   Función lambda especial que toma un elemento de
     *                  la colección <pre>elementos</pre> y devuelve su
     *                  representación String que aparecerá en el resultado.
     * @return String con la lista de los elementos en una sola línea
     */
    public static<T> String listar(Iterator<T> elementos, Function<T, String> funcion) {
        StringBuilder lista = new StringBuilder();
        lista.append('[');

        while (elementos.hasNext()) {
            String elemento = funcion.apply(elementos.next());

            if (elemento == null) {
                continue;
            }

            lista.append(elemento);

            if (elementos.hasNext()) {
                lista.append(", ");
            }
        }

        lista.append(']');
        return lista.toString();
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
