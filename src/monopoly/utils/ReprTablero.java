package monopoly.utils;

import monopoly.Juego;

/**
 * Define los métodos necesarios para poder representar
 * un elemento como parte del tablero.
 * <br>
 * Esto incluye una cadena de texto a modo de nombre,
 * un estilo de representación y un color.
 */
public interface ReprTablero {
    /**
     * Espacio dedicado para el nombre de la casilla dentro de la celda
     */
    int TAM_TEXTO = 12;
    /**
     * Espacio dedicado para los avatares que ocupan la casilla
     */
    int TAM_AVATAR = 4;
    /**
     * Tamaño total de la celda
     */
    int TAM_CELDA = TAM_TEXTO + TAM_AVATAR;

    // Bordes del tablero
    // @formatter:off
    char ESQ_NO = '┏'; // \u250F
    char ESQ_NE = '┓'; // \u2513
    char ESQ_SO = '┗'; // \u2517
    char ESQ_SE = '┛'; // \u251B
    char HOR    = '━'; // \u2501
    char VERT   = '┃'; // \u2503
    char DER    = '┣'; // \u2523
    char IZQ    = '┫'; // \u252B
    char ABAJO  = '┳'; // \u2533
    char ARRIBA = '┻'; // \u253B
    char INTER  = '╋'; // \u254B
    // @formatter:on

    /**
     * Obtiene el nombre que representará el elemento en el tablero
     */
    String representacionTablero();

    /**
     * Color a asignar al nombre de representación
     */
    int codColorRepresentacion();

    /**
     * Estilo a asignar al nombre de representación
     */
    Consola.Estilo estiloRepresentacion();

    /**
     * Obtiene la representación del elemento
     */
    default String representar() {
        return Juego.consola.fmt(celda(representacionTablero()), codColorRepresentacion(), estiloRepresentacion());
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
     * @return Mensaje formateado
     */
    private String celda(String msg) {
        // Contar las letras del mensaje
        if (msg.length() + 2 > TAM_TEXTO) {
            return " %s. ".formatted(msg.substring(0, TAM_TEXTO - 3));
        }

        return " %s%s".formatted(msg, " ".repeat(TAM_TEXTO - msg.length() - 1));
    }
}
