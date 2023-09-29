package monopoly.utilidades;

/**
 * Clase de ayuda para imprimir por Consola con colores y diferentes formatos.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 * @see <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">Guía de códigos ANSI</a>
 * @see <a href="https://stackoverflow.com/questions/4842424/list-of-ansi-color-escape-sequences">Códigos ANSI StackOverflow</a>
 */
public class Formatear {
    // TODO: Preguntar si hay otra forma de crear constantes
    /** Secuencia de inicio de un código ANSI */
    private static final String INICIO = "\u001b[";
    /** Código ANSI de reseteo */
    private static final String FIN = "\u001b[0m";
    /** Se le suma a la posición del enumerado Color para obtener el color de la fuente */
    private static final int OFFSET_FUENTE = 30;
    /** Se le suma a la posición del enumerado Color para obtener el color del fondo */
    private static final int OFFSET_FONDO = 40;

    /** Colores soportados */
    public enum Color {
        Negro, Rojo, Verde, Amarillo, Azul, Magenta, Cian, Blanco, Set, Defecto
    }

    /**
     * Estilos soportados
     * <hr>
     * NOTA: Puede que algunos no estén soportados por algunas terminales.
     */
    public enum Estilo {
        Normal, Negrita, Claro, Cursiva, Subrayado, ParpadeoLento, ParpadeoRapido, Invertir, Esconder
    }

    /** Función de ayuda para generar el código ANSI de todos los estilos dados */
    private static String getCodigoAnsi(Estilo... estilos) {
        if (estilos.length == 0) {
            return "";
        }

        String codigo = "";
        for (int i = 0; i < estilos.length - 1; i++) {
            codigo = "%s%s;".formatted(codigo, estilos[i].ordinal());
        }

        return "%s%s".formatted(codigo, estilos[estilos.length - 1].ordinal());
    }

    /** Formatea un mensaje con un color de fuente, color de fondo y unos estilo dados */
    public static String con(String msg, Color fuente, Color fondo, Estilo... estilos) {
        return "%s%s;%s;%sm%s%s".formatted(INICIO, Formatear.getCodigoAnsi(estilos),
                fuente.ordinal() + OFFSET_FUENTE, fondo.ordinal() + OFFSET_FONDO, msg, FIN);
    }

    /** Formatea un mensaje con un color de fuente */
    public static String con(String msg, Color fuente) {
        return Formatear.con(msg, fuente, Color.Defecto);
    }

    /** Formatea un mensaje con un color de fuente y unos estilos dados */
    public static String con(String msg, Color fuente, Estilo... estilos) {
        return Formatear.con(msg, fuente, Color.Defecto, estilos);
    }

    // TODO: Preguntar si esto se puede hacer
    /*
    public enum Estilo {
        Ninguno(0), Negrita(1), Clarito(2), Cursiva(3), Subrayado(4), Parpadeo(5), Tachado(9),
        FuenteDefecto(39), FuenteNegro(30), FuenteRojo(31), FuenteVerde(32), FuenteAmarillo(33), FuenteAzul(34), FuenteMagenta(35), FuenteCyan(36), FuenteBlanco(37),
        FondoDefecto(49), FondoNegro(40), FondoRojo(41), FondoVerde(42), FondoAmarillo(43), FondoAzul(44), FondoMagenta(45), FondoCyan(46), FondoBlanco(47);

        private final int valor;
        private Estilo(int valor) {
            this.valor = valor;
        }
    }

    public static String colorear(String msg, Estilo... estilos) {
        String codigoAnsi = "";

        for (int i = 0; i < estilos.length; i++) {
            codigoAnsi += Integer.toString(estilos[i].valor);

            if (i != estilos.length - 1) {
                codigoAnsi += ";";
            }
        }

        return "%s%sm%s%s".formatted(BEGIN, codigoAnsi, msg, END);
    }
    */

    /** Convierte el long a String y lo formatea separando las centenas con espacios */
    public static String num(long n) {
        String numStr = Long.toString(n);

        // Si es un numero de 3 o menos cifras no hay que hacer nada más
        if (numStr.length() <= 3) {
            return numStr;
        }

        // Empezamos tomando los últimos tres dígitos
        String resultado = numStr.substring(numStr.length() - 3);

        for (int i = numStr.length() - 3; i > 0; i -= 3) {
            // Y se sigue tomando grupos de 3 en 3
            // Salvo cuando se llega al principio, que se recoge lo que quede
            String grupo = numStr.substring(Math.max(0, i-3), i);
            // Se añade al resultado con un espacio
            resultado = "%s %s".formatted(grupo, resultado);
        }

        return resultado;
    }
}
