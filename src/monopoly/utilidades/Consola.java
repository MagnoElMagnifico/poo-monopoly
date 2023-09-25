package monopoly.utilidades;

/**
 * Clase de ayuda para imprimir con colores.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 * @see https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
 */
public class Consola {
    private static final String BEGIN = "\u001b[";
    private static final String END = "\u001b[0m";

    public enum Estilo {
        Ninguno(0), Negrita(1), Clarito(2), Cursiva(3), Subrayado(4), Parpadeo(5), Tachado(9),
        FuenteDefecto(39), FuenteNegro(30), FuenteRojo(31), FuenteVerde(32), FuenteAmarillo(33), FuenteAzul(34), FuenteMagenta(35), FuenteCyan(36), FuenteBlanco(37),
        FondoDefecto(49), FondoNegro(40), FondoRojo(41), FondoVerde(42), FondoAmarillo(43), FondoAzul(44), FondoMagenta(45), FondoCyan(46), FondoBlanco(47);

        private final int valor;
        private Estilo(int valor) {
            this.valor = valor;
        }
    }

    public static void print(String msg, Estilo... estilos) {
        String codigoAnsi = "";

        for (int i = 0; i < estilos.length; i++) {
            codigoAnsi += Integer.toString(estilos[i].valor);

            if (i != estilos.length - 1) {
                codigoAnsi += ";";
            }
        }

        System.out.print(BEGIN + codigoAnsi + 'm' + msg + END);
    }

    public static void println(String msg, Estilo... estilos) {
        Consola.print(msg + '\n', estilos);
    }
}
