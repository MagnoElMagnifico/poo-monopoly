package monopoly;

import monopoly.casillas.Edificio.TipoEdificio;
import monopoly.jugadores.Avatar;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Consola.Color;
import monopoly.utilidades.Consola.Estilo;
import monopoly.utilidades.Dado;

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
    private final Scanner scanner;
    private final Tablero tablero;
    private String msgAyuda;

    public Monopoly() {
        scanner = new Scanner(System.in);
        tablero = new Tablero();

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
            case "salir", "quit"    -> System.exit(0);
            case "ayuda", "help"    -> System.out.print(msgAyuda);
            case "iniciar", "start" -> tablero.iniciar();

            // Comandos de información
            case "ver tablero", "tablero", "show" -> System.out.print(tablero);
            case "listar casillas"  -> System.out.println(tablero.getCasillas());
            case "listar enventa"   -> System.out.println(tablero.getEnVenta());
            case "listar jugadores" -> System.out.println(tablero.getJugadores());
            case "listar avatares"  -> System.out.println(tablero.getAvatares());

            // Acciones de jugadores
            case "jugador", "turno", "player" -> {
                if (tablero.getJugadorTurno() == null) {
                    Consola.error("No hay jugadores");
                } else {
                    System.out.println(tablero.getJugadorTurno());
                }
            }
            //case "siguiente", "sig" -> tablero.
            case "salir carcel"               -> tablero.salirCarcel();
            case "cambiar modo"               -> tablero.cambiarModo();
            case "lanzar", "lanzar dados", "siguiente", "sig"-> tablero.moverAvatar(new Dado());
            case "acabar turno", "fin", "end" -> tablero.acabarTurno();
            case "bancarrota" -> tablero.bancarrota();
            case "pagar deuda" -> tablero.pagarDeuda();

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
            case "crear"     -> cmdCrear(args);
            case "comprar"   -> cmdComprar(args);
            case "describir" -> cmdDescribir(args);
            case "mover"     -> cmdMover(args);
            case "exec"      -> cmdExec(args);
            case "edificar"  -> cmdEdificar(args);
            case "hipotecar","deshipotecar" -> cmdHipoteca(args);
            default          -> Consola.error("\"%s\": Comando no válido".formatted(args[0]));
        }
        // @formatter:on
    }

    /**
     * Ejecuta el comando crear jugador
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
            case "e", "esfinge"  -> tipo = Avatar.TipoAvatar.Esfinge;
            case "s", "sombrero" -> tipo = Avatar.TipoAvatar.Sombrero;
            case "p", "pelota"   -> tipo = Avatar.TipoAvatar.Pelota;
            default -> {
                Consola.error("\"%s\": No es un tipo válido de Avatar (prueba con c, e, s, p)".formatted(args[3]));
                return;
            }
        }
        // @formatter:on

        tablero.anadirJugador(nombre, tipo);
    }

    /**
     * Ejecuta el comando de describir
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
     */
    private void cmdMover(String[] args) {
        if (args.length != 3) {
            Consola.error("Se esperaban 2 parámetros, se recibieron %d".formatted(args.length - 1));
            return;
        }

        tablero.moverAvatar(new Dado(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
    }

    /**
     * Ejecuta el comando de ejecutar un archivo
     */
    private void cmdExec(String[] args) {
        if (args.length != 2) {
            Consola.error("Se esperaba 1 parámetro, se recibieron %d".formatted(args.length - 1));
            return;
        }

        ejecutarArchivo(args[1]);
    }


    /**
     * Ejecuta el comando de edificar
     */
    private void cmdEdificar(String[] args) {
        if (args.length != 2) {
            Consola.error("Se esperaba 1 parámetro, se recibieron %d".formatted(args.length - 1));
            return;
        }

        TipoEdificio tipoEdificio;
        switch (args[1]) {
            case "casa", "c" -> tipoEdificio = TipoEdificio.Casa;
            case "hotel", "h" -> tipoEdificio = TipoEdificio.Hotel;
            case "piscina", "p" -> tipoEdificio = TipoEdificio.Piscina;
            case "pista", "pistadeporte", "deporte", "d" -> tipoEdificio = TipoEdificio.PistaDeporte;
            default -> {
                Consola.error("\"%s\": tipo de edificio desconocido (prueba con: casa, hotel, piscina, pista)".formatted(args[1]));
                return;
            }
        }

        tablero.edificar(tipoEdificio);
    }

    private void cmdHipoteca(String[] args) {
        if (args.length != 2) {
            Consola.error("Se esperaba 1 parámetro, se recibieron %d".formatted(args.length - 1));
            return;
        }
        if(args[0].equals("hipotecar")) tablero.hipotecar(args[1]);
        else tablero.deshipotecar(args[1]);
    }
}
