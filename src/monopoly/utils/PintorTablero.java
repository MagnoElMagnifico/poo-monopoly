package monopoly.utils;

import monopoly.casilla.Casilla;
import java.util.ArrayList;
import static monopoly.utils.ReprTablero.*;

/**
 * Clase de ayuda a Tablero para poder dibujar el tablero por pantalla.
 *
 * @see monopoly.Juego
 * @see Casilla
 */
public class PintorTablero {

    /**
     * Función privada de ayuda que añade a <code>dst</code> el
     * el contenido de una celda: el nombre de la casilla y sus
     * avatares.
     */
    private static void pintarCelda(StringBuilder dst, Casilla c) {
        dst.append(VERT);

        dst.append(c.representar());

        for (int i = 0; i < TAM_AVATAR-1; i++) {
            if (i < c.getAvatares().size()) {
                dst.append(c.getAvatares().get(i).representar());
            } else {
                dst.append(' ');
            }
        }

        dst.append(' ');
    }

    /**
     * Pinta el tablero dado.
     * <p>
     * NOTA: solo se accede al <code>ArrayList</code> de las casillas y
     * al jugador actual (para poder mostrarlo con un estilo diferente).
     */
    public static String pintarTablero(ArrayList<Casilla> casillas) {
        // Número de casillas por lado:
        // Es el total de casillas entre cada lado (4) más la casilla
        // extra que pertenece al lado siguiente.
        final int N_LADO = casillas.size() / 4 + 1;
        // Tamaño de la línea:
        // Es la barra vertical más el tamaño de celda, por el número de
        // casillas que hay en un lado. Adicionalmente, hay que sumar la última
        // barra vertical y el salto de línea.
        final int TAM_LINEA = (TAM_CELDA + 1) * N_LADO + 2;
        // Número de líneas:
        // Hay un separador encima de cada línea y uno extra abajo de todo, a
        // mayores de las casillas que hay en un lado.
        final int N_LINEAS = 2 * N_LADO + 1;

        StringBuilder tableroStr = new StringBuilder(TAM_LINEA * N_LINEAS);

        // Se van añadiendo fila a fila
        for (int i = 0; i < N_LADO; i++) {

            // En el caso de que sea la primera fila o la última,
            // se dibuja una lista de casillas.
            if (i == 0) {
                StringBuilder bordeSuperior = new StringBuilder(TAM_LINEA);
                bordeSuperior.append(ESQ_NO);

                for (int j = 0; j < N_LADO; j++) {
                    pintarCelda(tableroStr, casillas.get(j));

                    // Se añade una línea horizontal por encima de las celdas
                    bordeSuperior.append(Character.toString(HOR).repeat(TAM_CELDA));
                    // Y luego se añade la conexión con la línea vertical
                    // (al final se hace esquina).
                    bordeSuperior.append(j == N_LADO - 1 ? ESQ_NE : ABAJO);
                }

                tableroStr.append(VERT);

                // Insertar el borde al inicio
                bordeSuperior.append('\n');
                tableroStr.insert(0, bordeSuperior);

            } else if (i == N_LADO - 1) {
                // Este caso es análogo al anterior
                StringBuilder bordeInferior = new StringBuilder((TAM_CELDA + 1) * N_LADO + 1);
                bordeInferior.append(ESQ_SO);

                for (int j = 0; j < N_LADO; j++) {
                    // Para obtener la casilla, hay que quitar al número total la cantidad
                    // de celdas en el lado izquierdo del tablero.
                    pintarCelda(tableroStr, casillas.get(casillas.size() - (N_LADO - 1) - j));

                    bordeInferior.append(Character.toString(HOR).repeat(TAM_CELDA));
                    bordeInferior.append(j == N_LADO - 1 ? ESQ_SE : ARRIBA);
                }

                tableroStr.append(VERT);
                tableroStr.append('\n');
                tableroStr.append(bordeInferior);
            }

            // En caso contrario, solo se dibujan 2 casillas,
            // separadas por un separador y espacio en blanco.
            else {
                if (i == 1) {
                    tableroStr.append(construirSeparador(0, TAM_LINEA, N_LADO));
                    tableroStr.append('\n');
                }

                // En el lado de la derecha, se toman las casillas desde el
                // final dado que son las últimas.
                pintarCelda(tableroStr, casillas.get(casillas.size() - i));
                tableroStr.append(VERT);

                // El resto se llena con espacios
                tableroStr.append(" ".repeat((TAM_CELDA + 1) * (N_LADO - 2) - 1));

                // Y en el lado de la izquierda, hay que sumarle el número de
                // casillas de la primera fila a la fila actual.
                pintarCelda(tableroStr, casillas.get(N_LADO + i - 1));
                tableroStr.append(VERT);
                tableroStr.append('\n');

                tableroStr.append(construirSeparador(i, TAM_LINEA, N_LADO));
            }

            tableroStr.append('\n');
        }

        return tableroStr.toString();
    }

    /**
     * Construye un separador horizontal del tablero.
     */
    private static String construirSeparador(int fila, int TAM_LINEA, int N_LADO) {
        StringBuilder separador = new StringBuilder(TAM_LINEA);

        // Cubrir la primera celda
        separador.append(DER);
        separador.append(Character.toString(HOR).repeat(TAM_CELDA));

        // Si es la primera o la última fila, hay que cubrir con lína horizontal
        if (fila == 0 || fila == N_LADO - 2) {
            separador.append(INTER);

            for (int i = 0; i < N_LADO - 2; i++) {
                separador.append(Character.toString(HOR).repeat(TAM_CELDA));

                // Si no es el final de la línea, se añade un carácter
                // que conecte con las líneas verticales de arriba o abajo.
                if (i != N_LADO - 3) {
                    separador.append(fila == 0 ? ARRIBA : ABAJO);
                }
            }

            separador.append(INTER);
        } else {
            // De lo contrario, simplemente se rellena con espacios
            separador.append(IZQ);
            separador.append(" ".repeat((TAM_CELDA + 1) * (N_LADO - 2) - 1));
            separador.append(DER);
        }

        // Finalmente, se rellena la última celda
        separador.append(Character.toString(HOR).repeat(TAM_CELDA));
        separador.append(IZQ);

        return separador.toString();
    }
}
