package monopoly;

import monopoly.casillas.Edificio.TipoEdificio;
import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Consola.Color;
import monopoly.utilidades.Consola.Estilo;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Lector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Clase principal del juego del Monopoly.
 * <p>
 * Se encarga de interpretar los comandos y hacer
 * llamadas al tablero para ejecutarlos.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 * @see Tablero
 */
public class Monopoly {
    // http://www.patorjk.com/software/taag/#p=display&f=Roman&t=Monopoly
    private final static String MSG_INICIO = """
                                            BIENVENIDO AL JUEGO DEL
            ooo        ooooo                                                      oooo             \s
            `88.       .888'                                                      `888             \s
             888b     d'888   .ooooo.  ooo. .oo.    .ooooo.  oo.ooooo.   .ooooo.   888  oooo    ooo\s
             8 Y88. .P  888  d88' `88b `888P"Y88b  d88' `88b  888' `88b d88' `88b  888   `88.  .8' \s
             8  `888'   888  888   888  888   888  888   888  888   888 888   888  888    `88..8'  \s
             8    Y     888  888   888  888   888  888   888  888   888 888   888  888     `888'   \s
            o8o        o888o `Y8bod8P' o888o o888o `Y8bod8P'  888bod8P' `Y8bod8P' o888o     .8'    \s
                                                              888                       .o..P'     \s
                                                             o888o                      `Y8P'      \s
            """;
    // http://www.patorjk.com/software/taag/#p=display&f=Roman&t=A%20jugar!
    private final static String MSG_JUGAR = """
                  
                  .o.                o8o                                             .o.\s
                 .888.               `"'                                             888\s
                .8"888.             oooo oooo  oooo   .oooooooo  .oooo.   oooo d8b   888\s
               .8' `888.            `888 `888  `888  888' `88b  `P  )88b  `888""8P   Y8P\s
              .88ooo8888.            888  888   888  888   888   .oP"888   888       `8'\s
             .8'     `888.           888  888   888  `88bod8P'  d8(  888   888       .o.\s
            o88o     o8888o          888  `V88V"V8P' `8oooooo.  `Y888""8o d888b      Y8P\s
                                     888             d"     YD                          \s
                                 .o. 88P             "Y88888P'                          \s
                                 `Y888P                                                 \s
            """;
    // http://www.patorjk.com/software/taag/#p=display&f=Roman&t=Fin%20de%20Partida
    private final static String MSG_FIN = """
            
            oooooooooooo  o8o                         .o8                 ooooooooo.                          .    o8o        .o8           \s
            `888'     `8  `"'                        "888                 `888   `Y88.                      .o8    `"'       "888           \s
             888         oooo  ooo. .oo.         .oooo888   .ooooo.        888   .d88'  .oooo.   oooo d8b .o888oo oooo   .oooo888   .oooo.  \s
             888oooo8    `888  `888P"Y88b       d88' `888  d88' `88b       888ooo88P'  `P  )88b  `888""8P   888   `888  d88' `888  `P  )88b \s
             888    "     888   888   888       888   888  888ooo888       888          .oP"888   888       888    888  888   888   .oP"888 \s
             888          888   888   888       888   888  888    .o       888         d8(  888   888       888 .  888  888   888  d8(  888 \s
            o888o        o888o o888o o888o      `Y8bod88P" `Y8bod8P'      o888o        `Y888""8o d888b      "888" o888o `Y8bod88P" `Y888""8o\s
            """;

    private final Scanner scanner;
    private Tablero tablero;
    private String msgAyuda;

    public Monopoly() {
        scanner = new Scanner(System.in);

        // En lugar de añadir con código las casillas, se leen de
        // un archivo de configuración.
        //
        // NOTA: Esto es potencialmente un problema de seguridad,
        // dado que el usuario puede modificarlo sin reparos.
        tablero = Lector.leerCasillas("src/casillas.txt");

        try {
            msgAyuda = Files.readString(Path.of("src/ayuda.txt"));
        } catch (IOException e) {
            Consola.error("[Monopoly] Error abriendo el archivo de ayuda: " + e);
            System.exit(1);
        }
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
        if (args.length == 1) {
            ejecutarArchivo(args[0]);
        }

        iniciarConsola();
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        System.out.println(Consola.fmt(MSG_INICIO, Color.Amarillo));
        System.out.printf("Puedes usar el comando \"%s\" para ver las opciones disponibles\n", Consola.fmt("ayuda", Color.Verde));
        while (true) {
            System.out.print(Consola.fmt("$> ", 2, Estilo.Negrita));
            this.procesarCmd(scanner.nextLine());
        }
    }

    /**
     * Lee y ejecuta el contenido del archivo pasado por parámetro
     */
    private void ejecutarArchivo(String nombreArchivo) {
        // Se abre el archivo a ejecutar
        File archivo = new File(nombreArchivo);
        Scanner scanner;

        try {
            scanner = new Scanner(archivo);
        } catch (FileNotFoundException e) {
            Consola.error("\"%s\": no se ha encontrado".formatted(nombreArchivo));
            return;
        }

        // Se procesa línea a línea, ejecutando cada comando
        while (scanner.hasNextLine()) {
            procesarCmd(scanner.nextLine());
        }
    }

    /**
     * Procesa el comando dado y realiza las llamadas pertinentes para ejecutarlo
     */
    private void procesarCmd(String cmd) {
        // Ignorar comandos en blanco o con solo espacios
        if (cmd.isBlank() || cmd.stripLeading().startsWith("#")) {
            return;
        }

        // Normalizar:
        //   - Eliminar espacio al inicio y al final
        //   - Eliminar más de dos espacios seguidos
        //   - Convertir a minúsculas
        String cmdNorm = cmd.strip().replaceAll(" +", " ").toLowerCase();

        // @formatter:off
        switch (cmdNorm) {
            // Comandos de manejo del juego
            case "salir", "quit", "exit" -> System.exit(0);
            case "ayuda", "help"    -> System.out.print(msgAyuda);
            case "iniciar", "start" -> {
                if (tablero.iniciar()) {
                    System.out.println(Consola.fmt(MSG_JUGAR, Color.Amarillo));
                }
            }

            // Comandos de información
            case "ver tablero", "tablero", "show" -> System.out.print(tablero);

            // Acciones de jugadores
            case "jugador", "turno", "player" -> {
                if (tablero.getJugadorTurno() == null) {
                    Consola.error("No hay jugadores");
                } else {
                    System.out.println(tablero.getJugadorTurno());
                }
            }
            case "salir carcel"               -> tablero.salirCarcel();
            case "cambiar modo"               -> tablero.cambiarModo();

            case "lanzar", "lanzar dados"     -> tablero.moverAvatar(new Dado());
            case "siguiente", "sig", "next"   -> tablero.moverAvatar(null);

            case "acabar turno", "fin", "end" -> tablero.acabarTurno();
            case "bancarrota"                 -> {
                // Cuando es el fin de la partida se resetea todo el tablero
                // Para ello se crea uno nuevo, efectivamente perdiendo la referencia
                // al tablero anterior, para que lo recoja el GC.
                if (tablero.bancarrota()) {
                    System.out.println(Consola.fmt(MSG_FIN, Color.Amarillo));
                    System.out.println("\nReseteando tablero...\n");
                    tablero = Lector.leerCasillas("src/casillas.txt");
                }
            }

            default -> this.cmdConArgumentos(cmdNorm);
        }
        // @formatter:on
    }

    /**
     * Función de ayuda que procesa y ejecuta un comando con argumentos.
     *
     * @param cmd Comando previamente normalizado.
     * @return String con el resultado del comando.
     */
    private void cmdConArgumentos(String cmd) {
        // @formatter:off
        String[] args = cmd.split(" ");
        switch (args[0]) {
            case "crear"        -> cmdCrear(args);
            case "comprar"      -> cmdComprar(args);
            case "describir"    -> cmdDescribir(args);
            case "mover"        -> cmdMover(args);
            case "exec"         -> cmdExec(args);
            case "fortuna"      -> cmdFortuna(args);
            case "edificar"     -> cmdEdificar(args);
            case "hipotecar",
                 "deshipotecar" -> cmdHipoteca(args);
            case "vender"       -> cmdVender(args);
            case "listar"       -> cmdListar(args);
            case "estadisticas" -> cmdEstadisticas(args);
            default             -> Consola.error("\"%s\": Comando no válido".formatted(args[0]));
        }
        // @formatter:on
    }

    /**
     * Ejecuta el comando crear jugador
     * <pre>
     *     crear jugador {nombre} { c, coche | e, esfinge | s, sombrero | p, pelota }
     * </pre>
     */
    private void cmdCrear(String[] args) {
        if (args.length != 4) {
            Consola.error("Se esperaban 3 parámetros, se recibieron %d".formatted(args.length - 1));
            return;
        }

        if (!args[1].equals("jugador")) {
            Consola.error("\"%s\": Subcomando de crear no válido".formatted(args[1]));
            return;
        }

        // Como se pasa todo a minúsculas, los nombres quedan mal
        // Con esto se pasa a mayúscula la primera letra
        String nombre = args[2].substring(0, 1).toUpperCase() + args[2].substring(1);

        // @formatter:off
        Avatar.TipoAvatar tipo;
        switch (args[3]) {
            case "c", "coche"    -> tipo = Avatar.TipoAvatar.Coche;
            case "p", "pelota"   -> tipo = Avatar.TipoAvatar.Pelota;
            default -> {
                Consola.error("\"%s\": No es un tipo válido de Avatar (prueba con c, p)".formatted(args[3]));
                return;
            }
        }
        // @formatter:on

        tablero.anadirJugador(nombre, tipo);
    }

    /**
     * Ejecuta el comando de describir
     * <pre>
     *     describir {casilla}
     *     describir jugador {jugador}
     *     describir avatar {avatar}
     * </pre>
     */
    private void cmdDescribir(String[] args) {
        if (args.length == 2) {
            tablero.describirCasilla(args[1]);
            return;
        }

        if (args.length == 3) {
            switch (args[1]) {
                case "jugador" -> tablero.describirJugador(args[2]);
                case "avatar" -> tablero.describirAvatar(args[2].charAt(0));
                default -> Consola.error("\"%s\": Argumento inválido".formatted(args[1]));
            }
        } else {
            Consola.error("Se esperaban 2 o 3 parámetros, se recibieron %d".formatted(args.length));
        }
    }

    /**
     * Ejecuta el comando de comprar
     * <pre>
     *     comprar {propiedad}
     * </pre>
     */
    private void cmdComprar(String[] args) {
        if (args.length != 2) {
            Consola.error("Se esperaban 1 parámetro, se recibieron %d".formatted(args.length - 1));
            return;
        }

        tablero.comprar(args[1]);
    }

    /**
     * Ejecuta el comando de mover
     * <pre>
     *     mover {dado 1} [dado2]
     * </pre>
     */
    private void cmdMover(String[] args) {
        if (args.length != 2 && args.length != 3) {
            Consola.error("Se esperaba 1 o 2 parámetros, se recibieron %d".formatted(args.length - 1));
            return;
        }

        tablero.moverAvatar(new Dado(Integer.parseInt(args[1]), args.length == 2 ? 0 : Integer.parseInt(args[2])));
    }

    /**
     * Ejecuta el comando de ejecutar un archivo
     * <pre>
     *     exec {nombre archivo}
     * </pre>
     */
    private void cmdExec(String[] args) {
        if (args.length != 2) {
            Consola.error("Se esperaba 1 parámetro, se recibieron %d".formatted(args.length - 1));
            return;
        }

        ejecutarArchivo(args[1]);
    }

    /**
     * A partir del String dado como parámetro, lo convierte a un tipo de edificio
     */
    private TipoEdificio convertirTipoEdificio(String str) {
        return switch (str) {
            case "c", "casa", "casas" -> TipoEdificio.Casa;
            case "h", "hotel", "hoteles" -> TipoEdificio.Hotel;
            case "p", "piscina", "piscinas" -> TipoEdificio.Piscina;
            case "d", "pista", "pistas", "deportes", "deporte", "pistasdeporte", "pistadeporte" ->
                    TipoEdificio.PistaDeporte;
            default -> {
                Consola.error("\"%s\": tipo de edificio desconocido (prueba con: casa, hotel, piscina, pista)".formatted(str));
                yield null;
            }
        };
    }

    /**
     * Ejecuta el comando de edificar:
     * <pre>
     *     edificar {tipo edificio} [cantidad]
     * </pre>
     */
    private void cmdEdificar(String[] args) {
        if (args.length != 2 && args.length != 3) {
            Consola.error("Se esperaban 1 o 2 parámetros, se recibieron %d".formatted(args.length - 1));
            return;
        }

        TipoEdificio tipoEdificio = convertirTipoEdificio(args[1]);
        if (tipoEdificio != null) {
            tablero.edificar(tipoEdificio, args.length == 2 ? 1 : Integer.parseInt(args[2]));
        }
    }

    /**
     * Ejecuta el comando de vender un edificio
     * <pre>
     *     vender {tipo edificio} {solar} [cantidad]
     * </pre>
     */
    private void cmdVender(String[] args) {
        if (args.length != 3 && args.length != 4) {
            Consola.error("Se esperaban 2 o 3 parámetros, se recibieron %d".formatted(args.length - 1));
            return;
        }

        TipoEdificio tipoEdificio = convertirTipoEdificio(args[1]);
        if (tipoEdificio != null) {
            tablero.vender(tipoEdificio, args[2], args.length == 3 ? 1 : Integer.parseInt(args[3]));
        }
    }

    /**
     * Ejecuta todos los comandos de listar
     * <pre>
     *     listar { casillas | jugadores | enventa | avatares }
     *     listar edificios { nombre grupo }
     * </pre>
     */
    private void cmdListar(String[] args) {
        if (args.length == 2) {
            // @formatter:off
            switch (args[1]) {
                case "casillas"  -> tablero.listarCasillas();
                case "jugadores" -> tablero.listarJugadores();
                case "enventa"   -> tablero.listarEnVenta();
                case "avatares"  -> tablero.listarAvatares();
                case "edificios" -> tablero.listarEdificios();
                default -> Consola.error("Listar \"%s\" no está soportado".formatted(args[1]));
            }
            // @formatter:on
            return;
        }

        if (args.length == 3 && args[1].equals("edificios")) {
            tablero.listarEdificiosGrupo(args[2]);
            return;
        }

        Consola.error("Se esperaban 2 o 3 parámetros, se recibieron %d.".formatted(args.length - 1));
    }

    /**
     * Ejecuta el comando de hipotecar
     * <pre>
     *      hipotecar {propiedad}
     * </pre>
     */
    private void cmdHipoteca(String[] args) {
        if (args.length != 2) {
            Consola.error("Se esperaba 1 parámetro, se recibieron %d".formatted(args.length - 1));
            return;
        }
        if (args[0].equals("hipotecar")) tablero.hipotecar(args[1]);
        else tablero.deshipotecar(args[1]);
    }

    private void cmdEstadisticas(String[] args) {
        if (args.length == 1) {
            tablero.mostrarEstadisticas();
            return;
        }

        if (args.length == 2) {
            tablero.mostrarEstadisticas(args[1]);
            return;
        }

        Consola.error("Se esperaba 0 o 1 parámetro, se recibieron %d".formatted(args.length - 1));
    }

    /**
     * Procesa y ejecuta el comando de fortuna (cobra o ingresa a un jugador) (debug only)
     * <pre>
     *     fortuna {jugador} {cantidad}
     * </pre>
     * Si cantidad > 0 se ingresa, si cantidad < 0 se cobra
     */
    private void cmdFortuna(String[] args) {
        if (args.length != 3) {
            Consola.error("Se esperaban 2 parámetros, se recibieron %d".formatted(args.length - 1));
            return;
        }

        // Obtener el jugador
        Jugador jugador = null;
        for (Jugador j : tablero.getJugadores()) {
            if (j.getNombre().equalsIgnoreCase(args[1])) {
                jugador = j;
            }
        }

        if (jugador == null) {
            Consola.error("No se ha encontrado al jugador \"%s\"".formatted(args[1]));
            return;
        }

        long cantidad = Long.parseLong(args[2]);

        // Aplicar la cantidad
        if (cantidad > 0) {
            jugador.ingresar(cantidad);
        } else if (cantidad < 0) {
            if (jugador.cobrar(-cantidad, true)) {
                System.out.printf("Se ha cobrado exitosamente %s a %s\n", Consola.num(-cantidad), jugador.getNombre());
            } else {
                System.out.printf("Se no se ha podido cobrar %s a %s. Ahora está endeudado\n", Consola.num(-cantidad), jugador.getNombre());
            }
        }

        jugador.describirTransaccion();
    }
}
