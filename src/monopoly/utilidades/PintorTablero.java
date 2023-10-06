package monopoly.utilidades;

import monopoly.Casilla;

import java.util.ArrayList;

/**
 * Clase de ayuda a Tablero para poder dibujar el tablero por pantalla.
 *
 * @author Marcos Granja Grille
 * @date 6-10-2023
 * @see monopoly.Tablero
 * @see monopoly.Casilla
 */
public class PintorTablero {
    private static final int TAM = 15;
    private static final char ESQ_NO  = '┏'; // \u250F
    private static final char ESQ_NE  = '┓'; // \u2513
    private static final char ESQ_SO  = '┗'; // \u2517
    private static final char ESQ_SE  = '┛'; // \u251B
    private static final char HOR     = '━'; // \u2501
    private static final char VERT    = '┃'; // \u2503
    private static final char DER     = '┣'; // \u2523
    private static final char IZQ     = '┫'; // \u252B
    private static final char ABAJO   = '┳'; // \u2533
    private static final char ARRIBA  = '┻'; // \u253B
    private static final char INTER   = '╋'; // \u254B

    private static void pintarCelda(StringBuilder dst, Casilla c) {
        dst.append(VERT);
        dst.append(Formatear.con(Formatear.celda(c.getNombre(), TAM), (byte) c.getCodigoColor()));
    }

    public static String pintarTablero(ArrayList<Casilla> casillas) {
        final int nLado = casillas.size() / 4 + 1;

        // TODO: Se puede usar esto?
        StringBuilder tablero = new StringBuilder(); // TODO: calcular tamaño

        for (int i = 0; i < nLado; i++) {
            // En el caso de que sea la primera fila o la última,
            // se dibuja una lista de casillas.
            if (i == 0) {
                StringBuilder separador = new StringBuilder((TAM + 1) * nLado + 1);
                separador.append(ESQ_NO);

                for (int j = 0; j < nLado; j++) {
                    pintarCelda(tablero, casillas.get(j));
                    separador.append(Character.toString(HOR).repeat(TAM));
                    separador.append(j == nLado - 1? ESQ_NE : ABAJO);
                }

                tablero.append(VERT);
                separador.append('\n');

                // Insertar el separador al inicio
                tablero.insert(0, separador);

            }

            else if (i == nLado - 1) {
                StringBuilder separador = new StringBuilder((TAM + 1) * nLado + 1);
                separador.append(ESQ_SO);

                for (int j = 0; j < nLado; j++) {
                    pintarCelda(tablero, casillas.get(casillas.size() - (nLado - 1) - j));
                    separador.append(Character.toString(HOR).repeat(TAM));
                    separador.append(j == nLado - 1? ESQ_SE : ARRIBA);
                }

                tablero.append(VERT);
                tablero.append('\n');
                tablero.append(separador);
            }

            // En caso contrario, solo se dibujan 2
            else {
                pintarCelda(tablero, casillas.get(casillas.size() - 1));
                tablero.append(VERT);

                tablero.append(" ".repeat((TAM + 1) * (nLado - 2) - 1));

                pintarCelda(tablero, casillas.get(nLado + i - 1));
                tablero.append(VERT);
            }

            tablero.append('\n');
        }

        return tablero.toString();
    }
}
