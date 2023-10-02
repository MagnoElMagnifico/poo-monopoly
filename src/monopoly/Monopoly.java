package monopoly;

import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.Formatear.Estilo;

import java.util.Scanner;

/**
 * Clase principal del juego del Monopoly.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 */
public class Monopoly {
    private static final String MSG_AYUDA = """
                    ayuda, help      Muestra esta información de ayuda.
                    salir, quit      Cierra el programa.
            """;
    private final Scanner scanner;
    private final Dado dado;

    public Monopoly() {
        scanner = new Scanner(System.in);
        dado = new Dado();
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        while (true) {
            System.out.print(Formatear.con("$> ", Color.Verde, Estilo.Negrita));
            this.procesarCmd(scanner.nextLine());
        }
    }

    /**
     * Procesa el comando dado y realiza las llamadas pertinentes para ejecutarlo
     */
    private void procesarCmd(String cmd) {
        // Ignorar comandos en blanco o con solo espacios
        if (cmd.isBlank()) {
            return;
        }

        // Normalizar:
        //   - Eliminar espacio al inicio y al final
        //   - Eliminar más de dos espacios seguidos
        //   - Convertir a minúsculas
        String cmdNorm = cmd.strip().replaceAll(" +", " ").toLowerCase();

        // Ver: https://docs.oracle.com/en/java/javase/17/language/switch-expressions.html
        System.out.print(switch (cmdNorm) {
            case "ayuda", "help" -> MSG_AYUDA;
            case "salir", "quit" -> {
                System.exit(0);
                yield ""; // Si no devuelvo un objeto da error
            }
            case "lanzar", "lanzar dados" ->
                    "Resultado: %s\n".formatted(Formatear.con(Integer.toString(dado.lanzar2Dados()), Color.Azul));
            // TODO:
            // case "jugador"
            // case "acabar turno"
            // case "salir carcel"
            // case "ver tablero"
            // case "listar jugadores"
            // case "listar avatares"
            // case "listar enventa"
            default -> this.cmdConArgumentos(cmdNorm);
        });
    }

    /**
     * Función de ayuda que procesa y ejecuta un comando con argumentos.
     *
     * @param cmd Comando previamente normalizado.
     * @return String con el resultado del comando.
     */
    private String cmdConArgumentos(String cmd) {
        String[] args = cmd.split(" ");
        return switch (args[0]) {
            // TODO:
            // case "crear"
            // case "comprar"
            // case "describir"

            // A modo de prueba:
            case "format", "formatear" -> {
                if (args.length != 2) {
                    yield Formatear.con("Número de argumentos incorrecto: recibí %d, esperaba 2\n".formatted(args.length), Color.Rojo);
                }

                try {
                    yield Formatear.num(Long.parseLong(args[1])) + "\n";
                } catch (NumberFormatException e) {
                    yield Formatear.con("\"%s\" no es un número válido\n".formatted(args[1]), Color.Rojo);
                }
            }
            default -> Formatear.con("\"%s\": Comando no válido\n".formatted(args[0]), Color.Rojo);
        };
    }
}
