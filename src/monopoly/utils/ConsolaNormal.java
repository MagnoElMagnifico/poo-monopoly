package monopoly.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Implementación de la interfaz Consola para
 * una terminal: <code>System.out</code> para
 * imprimir y <code>Scanner</code> para leer.
 * <p>
 * Esta implementación utiliza códigos ANSI.
 * @see <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">Guía de códigos ANSI</a>
 * @see <a href="https://stackoverflow.com/questions/4842424/list-of-ansi-color-escape-sequences">Códigos ANSI StackOverflow</a>
 */
public class ConsolaNormal implements Consola {
    /**
     * Secuencia de inicio de un código ANSI
     */
    private static final String INICIO = "\u001b[";
    /**
     * Código ANSI de reseteo
     */
    private static final String FIN = "\u001b[0m";

    @Override
    public void imprimir(String mensaje) {
        System.out.print(mensaje);
    }

    @Override
    public void error(String mensaje) {
        imprimir(fmt(mensaje, Color.Rojo));
    }

    @Override
    public String leer(String descripcion) {
        try (Scanner scanner = new Scanner(System.in)) {
            imprimir(descripcion);
            return scanner.nextLine();
        }
    }

    @Override
    public String fmt(String msg, int color, Estilo... estilos) {
        StringBuilder codEstilo = new StringBuilder();

        // Los códigos se separan con ';'
        for (Estilo estilo : estilos) {
            codEstilo.append(estilo.ordinal());
            codEstilo.append(';');
        }

        return String.format("%s%s38;5;%dm%s%s", INICIO, codEstilo, color & 0xFF, msg, FIN);
    }

    @Override
    public String fmt(String msg, Color color) {
        return String.format("%s0;%dm%s%s", INICIO, color.ordinal() + 30, msg, FIN);
    }

    @Override
    public String num(long numero) {
        String numStr = Long.toString(numero);

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

    @Override
    public <T> String listar(Collection<T> elementos, Function<T, String> funcion) {
        StringBuilder lista = new StringBuilder();
        lista.append('[');

        Iterator<T> iter = elementos.iterator();

        boolean primero = true;
        while (iter.hasNext()) {
            String elemento = funcion.apply(iter.next());

            if (elemento == null) {
                continue;
            }

            if (primero) {
                primero = false;
            } else {
                lista.append(", ");
            }

            lista.append(elemento);
        }

        lista.append(']');
        return lista.toString();
    }
}
