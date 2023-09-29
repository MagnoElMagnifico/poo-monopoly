package monopoly;

import java.util.Scanner;

import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.Formatear.Estilo;

/**
 * Clase principal del juego del Monopoly.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 */
public class Monopoly {
    private final String msgAyuda;
    private Scanner scanner;
    private Dado dado;

    public Monopoly() {
        scanner = new Scanner(System.in);
        dado = new Dado();

        msgAyuda = """
               \tayuda, help\t\tMuestra esta información de ayuda.
               """;
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        while(true) {
            System.out.print(Formatear.con("$> ", Color.Verde, Estilo.Negrita));
            this.procesarComando(scanner.nextLine());
        }
    }

    /** Procesa el comando dado y realiza las llamadas pertinentes para ejecutarlo  */
    private void procesarComando(String cmd) {
        // Ignorar comandos en blanco o con solo espacios
        if (cmd.isBlank()) {
            return;
        }

        // Normalizar
        String cmdNorm = cmd.strip().replaceAll("\\s+", " ").toLowerCase();

        // Separar en argumentos
        // String[] args = cmdNorm.split("\\s");

        // Ver: https://docs.oracle.com/en/java/javase/17/language/switch-expressions.html
        System.out.print(switch (cmdNorm) {
            case "ayuda", "help"          -> this.msgAyuda;
            case "lanzar", "lanzar dados" -> "Resultado: %s\n".formatted(Formatear.con(Integer.toString(dado.lanzar2Dados()), Color.Azul));
            default                       -> Formatear.con("El comando \"%s\" no está soportado\n".formatted(cmdNorm), Color.Rojo);
        });
    }
}
