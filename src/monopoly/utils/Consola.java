package monopoly.utils;

import java.util.Collection;
import java.util.function.Function;

/**
 * Define la interacción básica con el usuario para
 * el Juego del Monopoly.
 */
public interface Consola {
    /**
     * Muestra un mensaje al usuario.
     *
     * @param mensaje Es el mensaje que se desea mostrar al usuario.
     */
    void imprimir(String mensaje);

    /**
     * Pide un dato al usuario.
     *
     * @param descripcion Prompt a mostrar cuando se pida el dato.
     * @return La cadena de texto introducida por el usuario.
     */
    String leer(String descripcion);

    /**
     * Formatea un mensaje con un código de color de fuente y unos estilos dados
     *
     * @param msg     Mensaje a formatear
     * @param color   Código de color de la fuente (número entero sin signo, 0 a 255)
     * @param estilos Lista de estilos a aplicar
     * @return La cadena formateada
     * @see <a href="https://user-images.githubusercontent.com/995050/47952855-ecb12480-df75-11e8-89d4-ac26c50e80b9.png">Codigos de Color</a>
     */
    String fmt(String msg, int color, Estilo... estilos);

    /**
     * Formatea un mensaje con un color específico.
     */
    default String fmt(String msg, Color color) {
        return fmt(msg, color.ordinal());
    }

    /**
     * Formatea el número dado para mejorar su legibilidad
     */
    String num(long numero);

    /**
     * Función que permite listar en una línea una colección de elementos.
     * <p>
     * Por ejemplo:
     * <pre>
     *     // jugadores.Jugador.toString()
     *     Consola.listar(propiedades, (p) -> p.getNombre());
     * </pre>
     * Devuelve <code>[Varsovia, Roma, Madrid]</code>.
     *
     * @param elementos Lista con los elementos a mostrar
     * @param funcion   Función lambda especial que toma un elemento de
     *                  la colección <pre>elementos</pre> y devuelve su
     *                  representación String que aparecerá en el resultado.
     * @return String con la lista de los elementos en una sola línea
     */
    <T extends Listable> String listar(Collection<T> elementos, Function<T, String> funcion);

    /**
     * Lista una colección de elementos que implementen Listable
     */
    default <T extends Listable> void imprimirLista(Collection<T> elementos) {
        imprimir(listar(elementos, Listable::listar) + '\n');
    }

    /**
     * Permite describir una colección de elementos.
     * <p>
     * Se iterará elemento a elemento y se ejecutará la función dada por cada uno de ellos.
     * En caso de que la función devuelva <code>true</code>, entonces se describirá el elemento
     * en concreto.
     * <p>
     * Describir un elemento consiste en llamar a su función <code>toString</code>.
     * <p>
     * Por ejemplo:
     * <pre>
     *     // describir casillas en venta
     *     Consola.listar(casillas, (c) -> c instanceof Propiedad && !((Propiedad) c).getPropietario() instanceof Banca);
     * </pre>
     *
     * @param elementos Colección de elementos a describir
     * @param funcion   Se evalúa por cada elemento para determinar si se describe o no.
     */
    default <T> void describir(Collection<T> elementos, Function<T, Boolean> funcion) {
        for (T e : elementos) {
            if (funcion.apply(e)) {
                imprimir(e.toString());
            }
        }
    }

    /**
     * Genera un mensaje de error para notificar al usuario sobre
     * algún problema inesperado o alguna acción que no se ha podido
     * realizar con éxito.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    void error(String mensaje);

    /**
     * Colores soportados
     */
    enum Color {
        Negro, Rojo, Verde, Amarillo, Azul, Magenta, Cian, Blanco
    }

    /**
     * Estilos soportados
     */
    enum Estilo {
        Normal, Negrita, Claro, Cursiva, Subrayado, ParpadeoLento, ParpadeoRapido, Invertir, Esconder
    }
}
