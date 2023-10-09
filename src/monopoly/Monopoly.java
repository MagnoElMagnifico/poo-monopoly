package monopoly;

import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.Formatear.Estilo;

import java.util.Scanner;

/**
 * Clase principal del juego del Monopoly.
 * <p>
 * Se encarga de procesar los comandos y ejecutarlos.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 */
public class Monopoly {
    private static final String MSG_AYUDA = """
                %s
                    ayuda, help                   Muestra esta información de ayuda.
                    ver tablero, tablero, show    Muestra el tablero del juego.
                    jugador, turno, player        Muestra el jugador al que le toca jugar.
                    lanzar, lanzar dados          El jugador actual lanza 2 dados y mueve su avatar.
                    acabar turno, fin, end        Termina el turno del jugador actual.
                    salir, quit                   Cierra el programa.

                %s
                    crear jugador <nombre> <tipo>
                          Crea un jugador dado su nombre y tipo. Este último puede ser uno de los
                          4 siguientes:
                              - Coche (alias c)
                              - Esfinge (alias e)
                              - Sombrero (alias s)
                              - Pelota (alias p)

                    listar <casillas | enventa | jugadores | avatares>
                          Muestra información sobre las Casillas, la propiedades EnVenta, Jugadores
                          del juego y sus Avatares.

                    describir <nombre-casilla>
                          Muestra información sobre una casilla en concreto.

                    comprar <nombre-propiedad>
                          Permite permite al jugador actual comprar una propiedad.

                          NOTA: solo se puede comprar la propiedad en la que está su avatar y si no
                          tiene dueño.

                %s
                    Solo para probar el funcionamiento del juego.

                    exec <archivo>
                           Permite ejecutar un archivo que contiene un comando por línea.
                    
                    mover <n | nombre-casilla>
                           Mueve el jugador actual un número determinado de casillas, sin necesidad
                           de lanzar el dado.

                           Alternativamente se puede mover a la casilla dada.

                %s
                    - Los comandos no distinguen mayúsculas de minúsculas: "AyUda" es lo mismo que "ayuda".
                    - Se ignoran los espacios en blanco no necesarios: "lanzar     dados" es lo
                      mismo que "lanzar dados".
            """.formatted(Formatear.con("COMANDOS", Color.Rojo, Estilo.Negrita),
                          Formatear.con("COMANDOS CON ARGUMENTOS", Color.Rojo, Estilo.Negrita),
                          Formatear.con("COMANDOS DEBUG", Color.Rojo, Estilo.Negrita),
                          Formatear.con("NOTAS", Color.Rojo, Estilo.Negrita));

    private final Scanner scanner;
    private final Tablero tablero;

    public Monopoly() {
        scanner = new Scanner(System.in);
        tablero = new Tablero();
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        System.out.printf("Puedes usar el comando \"%s\" para ver las opciones disponibles\n", Formatear.con("ayuda", Color.Verde));
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
            // Comandos de manejo del juego
            case "salir", "quit" -> {
                System.exit(0);
                yield ""; // Si no devuelvo un objeto da error
            }
            case "ayuda", "help" -> MSG_AYUDA;

            // Comandos de información
            case "ver tablero", "tablero", "show" -> tablero;
            case "listar casillas" -> tablero.getCasillas().toString() + '\n';
            case "listar enventa" -> tablero.getEnVenta().toString() + '\n';
            case "listar jugadores" -> tablero.getJugadores().toString() + '\n';
            case "listar avatares" -> tablero.getAvatares().toString() + '\n';

            // Acciones de jugadores
            case "jugador", "turno", "player" ->
                    tablero.getJugadorTurno().isEmpty() ? Formatear.con("No hay jugadores\n", Color.Rojo) : tablero.getJugadorTurno().get();
            case "lanzar", "lanzar dados" -> tablero.lanzarDados();
            case "acabar turno", "fin", "end" -> tablero.acabarTurno();
            //case "salir carcel" -> tablero.salirCarcel();

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
            // case "comprar"
            // case "describir"
            // mover n: (debug) mueve el avatar un número de posiciones
            // exec archivo: (debug) ejecuta los comandos almacenados en el archivo

            case "crear" -> cmdCrear(args);
            case "describir" ->
            default -> Formatear.con("\"%s\": Comando no válido\n".formatted(args[0]), Color.Rojo);
        };
    }

    /**
     * Ejecuta el comando crear jugador
     */
    private String cmdCrear(String[] args) {
        if (args.length != 4) {
            return Formatear.con("Se esperaban 4 parámetros, se recibieron %d\n".formatted(args.length), Color.Rojo);
        }

        if (!args[1].equals("jugador")) {
            return Formatear.con("\"%s\": Subcomando de crear no válido\n".formatted(args[1]), Color.Rojo);
        }

        String nombre = args[2];
        Avatar.TipoAvatar tipo;
        switch (args[3]) {
            case "c", "coche" -> tipo = Avatar.TipoAvatar.Coche;
            case "e", "esfinge" -> tipo = Avatar.TipoAvatar.Esfinge;
            case "s", "sombrero" -> tipo = Avatar.TipoAvatar.Sombrero;
            case "p", "pelota" -> tipo = Avatar.TipoAvatar.Pelota;
            default -> {
                return Formatear.con("\"%s\": No es un tipo válido de Avatar (prueba con c, e, s, p)\n".formatted(args[3]), Color.Rojo);
            }
        }

        tablero.anadirJugador(nombre, tipo);
        return "Jugador %s creado con éxito.\n".formatted(Formatear.con(nombre, Color.Verde));
    }
}
