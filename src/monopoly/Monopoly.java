package monopoly;

import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.Formatear.Estilo;

import java.io.File;
import java.io.FileNotFoundException;
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
    /**
     * Resultado del comando de ayuda
     */
    private static final String MSG_AYUDA = """
                %s
                    ayuda, help                   Muestra esta información de ayuda.
                    ver tablero, tablero, show    Muestra el tablero del juego.
                    iniciar, start                Inicia la partida. Ya no se podrán añadir jugadores.
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
                    - Lo que empiece por el caracter '#' se considerará un comentario, lo que se ignorará.
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
     * <p>
     * En los parámetros de línea de comandos se espera
     * un archivo de comandos para ejecutar. Si los parámetros
     * no son correctos, el programa termina.
     * <p>
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola(String[] args) {
        if (args.length > 1) {
            System.err.println("Demasiados parámetros. Se esperaba 0 o 1");
            System.exit(1);
        }

        // Se ejecuta el archivo y se inicia la consola
        System.out.println(ejecutarArchivo(args[0]));
        iniciarConsola();
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        System.out.printf("Puedes usar el comando \"%s\" para ver las opciones disponibles\n", Formatear.con("ayuda", Color.Verde));
        while (true) {
            System.out.print(Formatear.con("$> ", Color.Verde, Estilo.Negrita));
            System.out.print(this.procesarCmd(scanner.nextLine()));
        }
    }

    /**
     * Procesa el comando dado y realiza las llamadas pertinentes para ejecutarlo
     */
    private String procesarCmd(String cmd) {
        // Ignorar comandos en blanco o con solo espacios
        if (cmd.isBlank() || cmd.stripLeading().startsWith("#")) {
            return "";
        }

        // Normalizar:
        //   - Eliminar espacio al inicio y al final
        //   - Eliminar más de dos espacios seguidos
        //   - Convertir a minúsculas
        String cmdNorm = cmd.strip().replaceAll(" +", " ").toLowerCase();

        // Ver: https://docs.oracle.com/en/java/javase/17/language/switch-expressions.html
        return switch (cmdNorm) {
            // Comandos de manejo del juego
            case "salir", "quit" -> {
                System.exit(0);
                yield ""; // Si no devuelvo un objeto da error
            }
            case "ayuda", "help" -> MSG_AYUDA;
            case "iniciar", "start" -> tablero.iniciar();

            // Comandos de información
            case "ver tablero", "tablero", "show" -> tablero.toString();
            case "listar casillas" -> tablero.getCasillas().toString() + '\n';
            case "listar enventa" -> tablero.getEnVenta().toString() + '\n';
            case "listar jugadores" -> tablero.getJugadores().toString() + '\n';
            case "listar avatares" -> tablero.getAvatares().toString() + '\n';

            // Acciones de jugadores
            case "jugador", "turno", "player" ->
                    tablero.getJugadorTurno() == null ? Formatear.con("No hay jugadores\n", Color.Rojo) : tablero.getJugadorTurno().toString() + '\n';
            case "lanzar", "lanzar dados" -> tablero.lanzarDados();
            case "acabar turno", "fin", "end" -> tablero.acabarTurno();
            // TODO: case "salir carcel" -> tablero.salirCarcel();

            default -> this.cmdConArgumentos(cmdNorm);
        };
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

            case "crear" -> cmdCrear(args);
            case "mover" -> {
                if (args.length == 2) {
                    yield tablero.moverJugador(Integer.parseInt(args[1]));
                }

                if (args.length == 3) {
                    yield tablero.moverJugador(Integer.parseInt(args[1]) + Integer.parseInt(args[2]));
                }

                yield Formatear.con("Se esperaba 1 o 2 parámetros, se recibieron %d\n".formatted(args.length), Color.Rojo);
            }
            case "exec" -> {
                if (args.length != 2) {
                    yield Formatear.con("Se esperaba 1 parámetro, se recibieron %d\n".formatted(args.length), Color.Rojo);
                }

                yield ejecutarArchivo(args[1]);
            }
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

        // Como se pasa todo a minúsculas, los nombres quedan mal
        // Con esto se pasa a mayúscula la primera letra
        String nombre = args[2].substring(0, 1).toUpperCase() + args[2].substring(1);

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

        return tablero.anadirJugador(nombre, tipo);
    }

    private String ejecutarArchivo(String nombreArchivo) {
        // Se abre el archivo a ejecutar
        File archivo = new File(nombreArchivo);
        Scanner scanner;

        try {
            scanner = new Scanner(archivo);
        } catch (FileNotFoundException e) {
            return Formatear.con("\"%s\": no se ha encontrado\n".formatted(nombreArchivo), Color.Rojo);
        }

        // String para almacenar la salida del comando
        StringBuilder salida = new StringBuilder();

        // Se procesa línea a línea, ejecutando cada comando
        while (scanner.hasNextLine()) {
            salida.append(procesarCmd(scanner.nextLine()));
        }

        return salida.toString();
    }
}
