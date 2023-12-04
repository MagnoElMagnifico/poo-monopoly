package monopoly;

import monopoly.casilla.Lector;
import monopoly.casilla.especial.CasillaCarcel;
import monopoly.casilla.especial.CasillaSalida;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.*;
import monopoly.jugador.*;
import monopoly.casilla.Casilla;
import monopoly.casilla.propiedad.Grupo;
import monopoly.utils.Consola.Color;
import monopoly.utils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Clase principal del juego del Monopoly.
 * <p>
 * Se encarga de interpretar los comandos y ejecutarlos.
 * </p><p>
 * Además contiene a los jugadores, las casillas y los grupos.
 * Por eso gestiona el turno actual, el estado de la partida, etc.
 * </p>
 */
public class Juego implements Comando {
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

    // TODO: poner en privado y añadir un getter
    public static Consola consola;

    private final String msgAyuda;

    // Atributos
    private final ArrayList<Jugador> jugadores;
    private final ArrayList<Casilla> casillas;
    private final ArrayList<Grupo> grupos;
    private final Banca banca;

    // Estado
    private int turno;
    private int nAumentosPrecio;
    private boolean jugando;

    // Información relevante
    private final long fortunaInicial;
    private final CasillaCarcel carcel;
    private final CasillaSalida salida;

    public Juego() throws ErrorFatalConfig {
        try {
            consola = new ConsolaNormal();
            msgAyuda = Files.readString(Path.of(JuegoConsts.CONFIG_AYUDA));

            banca = new Banca();
            jugadores = new ArrayList<>(JuegoConsts.MAX_JUGADORES);
            turno = 0;
            jugando = false;

            Lector lector = new Lector(this);
            casillas = lector.getCasillas();
            grupos = lector.getGrupos();
            carcel = lector.getCarcel();
            salida = lector.getSalida();
            fortunaInicial = lector.getFortunaInicial();
        } catch (IOException e) {
            throw new ErrorFatalConfig("Error abriendo el archivo de ayuda: " + e, JuegoConsts.CONFIG_AYUDA, 0);
        }
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * <p>
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        consola.imprimir(consola.fmt(MSG_INICIO, Color.Amarillo));
        consola.imprimir("Puedes usar el comando \"%s\" para ver las opciones disponibles\n".formatted(consola.fmt("ayuda", Color.Verde)));

        boolean cerrar = false;
        while (!cerrar) {
            // TODO: ¿Cómo tratarlas de forma diferente?
            try {
                cerrar = ejecutarComando(consola.leer("$> "));
            } catch (ErrorComando | ErrorFatal e) {
                e.imprimirMsg();
            }
        }
    }

    /**
     * Procesa el comando dado y realiza las llamadas pertinentes para ejecutarlo
     *
     * @return True en caso de que deba seguir procesando comandos. False cuando se ha ejecuta el comando de salir.
     */
    private boolean ejecutarComando(String cmd) throws ErrorComando, ErrorFatal {
        // Ignorar comandos en blanco o con solo espacios
        if (cmd.isBlank() || cmd.stripLeading().startsWith("#")) {
            return true;
        }

        // Normalizar:
        //   - Eliminar espacio al inicio y al final
        //   - Eliminar más de dos espacios seguidos
        //   - Convertir a minúsculas
        cmd = cmd.strip().replaceAll(" +", " ").toLowerCase();

        if (cmd.equals("salir") || cmd.equals("quit") || cmd.equals("exit")) {
            return false;
        }

        // Comandos sin parámetros
        boolean cmdProcesado = true;
        switch (cmd) {
            case "ayuda", "help" -> ayuda();
            case "iniciar", "start" -> iniciar();
            case "ver tablero", "tablero", "show" -> verTablero();
            case "jugador", "turno", "player" -> jugador();
            case "salir carcel" -> salirCarcel();
            case "cambiar modo" -> cambiarModo();
            case "lanzar dados", "lanzar", "throw" -> lanzar();
            case "siguiente", "sig", "next" -> siguiente();
            case "acabar turno", "fin", "end" -> acabarTurno();
            case "bancarrota" -> bancarrota();
            // TODO: listar
            default -> cmdProcesado = false;
        }

        if (cmdProcesado) {
            return true;
        }

        String[] args = cmd.split(" ");

        // Comandos con parámetros
        cmdProcesado = true;
        switch (args[0]) {
            case "crear" -> crearJugador(args);
            case "comprar" -> comprar(args);
            case "edificar" -> edificar(args);
            case "vender" -> vender(args);
            case "hipotecar" -> hipotecar(args);
            case "deshipotecar" -> deshipotecar(args);
            case "estadisticas" -> estadisticas(args);
            // ------------------------------------------------------------
            case "exec" -> ejecutarArchivo(args);
            case "mover" -> mover(args);
            case "fortuna" -> fortuna(args);
            default -> cmdProcesado = false;
        }

        if (cmdProcesado) {
            return true;
        }

        throw new ErrorComandoFormato("\"%s\": comando no válido".formatted(args[0]));
    }

    // ================================================================================

    @Override
    public String toString() {
        Avatar avatar = getJugadorTurno() == null? null : getJugadorTurno().getAvatar() ;
        return PintorTablero.pintarTablero(casillas, avatar);
    }

    /**
     * Obtiene el jugador de turno. Si no hay jugadores devuelve `null`
     */
    public Jugador getJugadorTurno() {
        return jugadores.isEmpty() ? null : jugadores.get(turno);
    }

    public Banca getBanca() {
        return banca;
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public CasillaCarcel getCarcel() {
        return carcel;
    }

    public CasillaSalida getSalida() {
        return salida;
    }

    /**
     * Aumenta el precio de todos los solares que
     * aún no se han vendido al cabo de 4 vueltas.
     */
    public void aumentarPrecio() {
        try {
            for (Jugador jugador : jugadores) {
                if (jugador.getEstadisticas().getVueltas() - 4 * nAumentosPrecio < 0) {
                    return;
                }
            }

            nAumentosPrecio++;

            for (Casilla c : casillas) {
                // Si la casilla se puede comprar y no tiene dueño, es que está en venta
                if (c instanceof Propiedad && !(((Propiedad) c).getPropietario() instanceof Banca)) {
                    ((Propiedad) c).factorPrecio(1.05f);
                }
            }

            consola.imprimir("Se ha aumentado el precio de todas las casillas en venta");
        } catch (ErrorFatalLogico e) {
            e.imprimirMsg();
            e.abortar();
        }
    }

    // ================================================================================

    @Override
    public void ayuda() {
        consola.imprimir(msgAyuda);
    }

    @Override
    public void iniciar() throws ErrorComandoEstadoPartida {
        if (jugando) {
            throw new ErrorComandoEstadoPartida("La partida ya está iniciada");
        }

        if (jugadores.size() < JuegoConsts.MIN_JUGADORES) {
            throw new ErrorComandoEstadoPartida("No hay suficientes jugadores para empezar (mínimo %d)".formatted(JuegoConsts.MIN_JUGADORES));
        }

        jugando = true;
        consola.imprimir(consola.fmt("Se ha iniciado la partida\n", Color.Amarillo));
        consola.imprimir(consola.fmt(MSG_JUGAR, Color.Amarillo));
    }

    @Override
    public void verTablero() {
        consola.imprimir(toString());
    }

    @Override
    public void jugador() throws ErrorComandoEstadoPartida {
        Jugador j = getJugadorTurno();

        if (j == null) {
            throw new ErrorComandoEstadoPartida("No hay jugadores");
        }

        consola.imprimir(j.toString());
    }

    @Override
    public void salirCarcel() throws ErrorComandoEstadoPartida, ErrorComandoAvatar {
        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        Jugador jugadorTurno = getJugadorTurno();

        jugadorTurno.getAvatar().salirCarcelPagando(false);
        verTablero();
        jugadorTurno.describirTransaccion();
    }

    @Override
    public void cambiarModo() throws ErrorComandoEstadoPartida {
        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        getJugadorTurno().getAvatar().cambiarModo();
    }

    @Override
    public void lanzar() throws ErrorComandoEstadoPartida, ErrorComandoFortuna, ErrorComandoAvatar, ErrorFatal {
        moverComun(new Dado());
    }

    @Override
    public void siguiente() throws ErrorComandoEstadoPartida, ErrorComandoFortuna, ErrorComandoAvatar, ErrorFatal {
        moverComun(null);
    }

    private void moverComun(Dado dado) throws ErrorComandoEstadoPartida, ErrorComandoFortuna, ErrorComandoAvatar, ErrorFatal {
        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        Jugador jugadorTurno = getJugadorTurno();

        if (jugadorTurno.isEndeudado()) {
            throw new ErrorComandoFortuna("Estas endeudado: paga la deuda o declárate en bancarrota para poder avanzar", jugadorTurno);
        }

        // Muestra el tablero si se ha movido el avatar con éxito
        jugadorTurno.getAvatar().mover(this, dado);
        verTablero();
    }

    @Override
    public void acabarTurno() throws ErrorComandoEstadoPartida {
        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        Jugador jugadorTurno = getJugadorTurno();
        jugadorTurno.acabarTurno();

        // Mostrar los cambios
        jugadorTurno.describirTransaccion();

        // Finalmente seleccionar el nuevo jugador
        turno = (turno + 1) % jugadores.size();

        // Mostrar el tablero para el nuevo turno
        verTablero();
        consola.imprimir("Se ha cambiado el turno.\nAhora le toca a %s.\n".formatted(consola.fmt(getJugadorTurno().getNombre(), Color.Azul)));
    }

    @Override
    public void bancarrota() {
        // Pedir confirmación
        String respuesta = consola.leer("Esta seguro de que quiere abandonar la partida? (y/N): ");
        if (respuesta.isBlank() || Character.toLowerCase(respuesta.trim().charAt(0)) != 'y') {
            consola.imprimir("Operación cancelada");
            return;
        }

        Jugador deudor = getJugadorTurno();
        Jugador acreedor = deudor.getAcreedor();
        if (acreedor == null) {
            acreedor = banca;
        }

        // Dar sus propiedades al jugador que se debe el dinero
        for (Propiedad p : deudor.getPropiedades()) {
            p.setPropietario(acreedor);
            acreedor.anadirPropiedad(p);
        }

        // Se borra el jugador
        deudor.getAvatar().getCasilla().quitarAvatar(deudor.getAvatar());
        jugadores.remove(deudor);
        consola.imprimir("El jugador %s se declara en bancarrota y abandona la partida\n".formatted(consola.fmt(deudor.getNombre(), Color.Azul)));

        if (jugadores.size() != 1) {
            consola.imprimir("Ahora le toca a %s\n".formatted(consola.fmt(getJugadorTurno().getNombre(), Color.Azul)));
            return;
        }

        // Fin de la partida
        consola.imprimir(consola.fmt("Felicidades %s, has ganado la partida".formatted(jugadores.get(0).getNombre()), Color.Amarillo));
        jugando = false;

        consola.imprimir(consola.fmt(MSG_FIN, Color.Amarillo));
        consola.imprimir("\nReseteando tablero...\n");
        // TODO: resetear tablero
    }

    // ================================================================================

    @Override
    public void crearJugador(String[] args) throws ErrorComandoFormato, ErrorComandoEstadoPartida {
        if (args.length != 4) {
            throw new ErrorComandoFormato(3, args.length - 1);
        }

        if (!args[1].equals("jugador")) {
            throw new ErrorComandoFormato("\"%s\": subcomando no válido".formatted(args[1]));
        }

        if (jugando) {
            throw new ErrorComandoEstadoPartida("No se pueden añadir jugadores en mitad de una partida");
        }

        if (jugadores.size() >= JuegoConsts.MAX_JUGADORES) {
            throw new ErrorComandoEstadoPartida("El máximo de jugadores es %d".formatted(JuegoConsts.MAX_JUGADORES));
        }

        // Como se pasa todo a minúsculas, los nombres quedan mal
        // Con esto se pasa a mayúscula la primera letra
        String nombre = args[2].substring(0, 1).toUpperCase() + args[2].substring(1);

        Avatar avatar = switch (args[3]) {
            case "c", "coche" -> new AvatarCoche(JuegoConsts.AVATARES_ID[jugadores.size()], salida);
            case "p", "pelota" -> new AvatarPelota(JuegoConsts.AVATARES_ID[jugadores.size()], salida);
            default -> throw new ErrorComandoFormato("\"%s\": No es un tipo válido de Avatar (prueba con c, p)".formatted(args[3]));
        };

        jugadores.add(new Jugador(nombre, avatar, fortunaInicial));

        consola.imprimir("El jugador %s con avatar %s se ha creado con éxito.\n".formatted(
                consola.fmt(nombre, Color.Verde),
                consola.fmt(Character.toString(avatar.getId()), Color.Verde)));

    }

    @Override
    public void comprar(String[] args) throws ErrorComando {
        if (args.length != 2) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        Jugador jugadorTurno = getJugadorTurno();
        Avatar avatarTurno = jugadorTurno.getAvatar();

        if (!avatarTurno.getCasilla().getNombre().equalsIgnoreCase(args[1])) {
            throw new ErrorComando("No se puede comprar otra casilla que no sea la actual");
        }

        Casilla casillaActual = avatarTurno.getCasilla();

        if (!(casillaActual instanceof Propiedad)) {
            throw new ErrorComandoFortuna("No se puede comprar la casilla \"%s\"".formatted(casillaActual.getNombre()), jugadorTurno);
        }

        // Especifico para el coche que solo puede comprar una vez por turno en modo especial
        // TODO
        /*if (!avatarTurno.isPuedeComprar()) {
            consola.error("El jugador ya ha comprado una vez en este turno");
            return;
        }*/

        jugadorTurno.comprar((Propiedad) casillaActual);
    }

    @Override
    public void edificar(String[] args) throws ErrorComandoFormato, ErrorComandoEstadoPartida {
        if (args.length != 2 && args.length != 3) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        // TODO:
        //Edificio edificio = convertirTipoEdificio(args[1]);
        //getJugadorTurno().comprar(tipoEdificio, args.length == 2 ? 1 : Integer.parseInt(args[2]));
    }

    private <T> T buscar(Collection<T> elementos, Function<T, Boolean> funcion) throws ErrorComando {
        for (T e : elementos) {
            if (funcion.apply(e)) {
                return e;
            }
        }

        throw new ErrorComando("No se ha encontrado");
    }

    @Override
    public void vender(String[] args) throws ErrorComandoFormato, ErrorComandoEstadoPartida {
        /*
        if (args.length != 3 && args.length != 4) {
            throw new ErrorComandoFormato(2, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        // TODO: usar buscar()
        // Buscar el solar
        Propiedad solar = null;
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getPropiedad().getTipo() == Propiedad.TipoPropiedad.Solar && c.getPropiedad().getNombre().equalsIgnoreCase(nombreSolar)) {
                solar = c.getPropiedad();
                break;
            }
        }

        if (solar == null) {
            consola.error("No existe el solar \"%s\"".formatted(args[2]));
            return;
        }

        // TODO: obtener tipo edificio
        //getJugadorTurno().vender(tipoEdificio, buscar(solar, ()), cantidad);
        */
    }

    @Override
    public void hipotecar(String[] args) throws ErrorComando {
        if (args.length != 2) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        // Buscar la propiedad
        Propiedad p = (Propiedad) buscar(casillas, (c) -> c instanceof Propiedad && c.getNombre().equalsIgnoreCase(args[1]));

        if (!getJugadorTurno().getPropiedades().contains(p)) {
            throw new ErrorComandoFortuna("No puedes hipotecar una propiedad que no te pertenece", getJugadorTurno());
        }

        p.hipotecar();
    }

    @Override
    public void deshipotecar(String[] args) throws ErrorComando {
        if (args.length != 2) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        // Buscar la propiedad
        Propiedad p = (Propiedad) buscar(casillas, (c) -> c instanceof Propiedad && c.getNombre().equalsIgnoreCase(args[1]));

        if (!getJugadorTurno().getPropiedades().contains(p)) {
            throw new ErrorComandoFortuna("No puedes deshipotecar una propiedad que no te pertenece", getJugadorTurno());
        }

        p.deshipotecar();
    }

    @Override
    public void describir(String[] args) throws ErrorComandoFormato {
        if (args.length == 2) {
            consola.describir(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[1]));
            return;
        }

        if (args.length == 3) {
            switch (args[1]) {
                case "jugador" -> consola.describir(jugadores, (j) -> j.getNombre().equalsIgnoreCase(args[2]));
                case "avatar" -> consola.describir(jugadores, (j) -> j.getAvatar().getId() == Character.toUpperCase(args[2].charAt(0)));
                default -> throw new ErrorComandoFormato("\"%s\": Argumento inválido".formatted(args[1]));
            }
        } else {
            throw new ErrorComandoFormato(2, args.length - 1);
        }
    }

    @Override
    public void listar(String[] args) {
        // TODO
        // TODO: Listable no está implementado
        /*
        if (args.length == 2) {
            // @formatter:off
            switch (args[1]) {
                case "casillas"  -> consola.listar(casillas);
                case "jugadores" -> consola.listar(jugadores);
                case "enventa"   -> consola.listar(casillas, (e) -> null);
                case "avatares"  -> consola.listar(jugadores, (j) -> j.getAvatar().listar());
                case "edificios" -> consola.listar(casillas, (c) -> c instanceof Solar? ((Solar) c).)
                default -> consola.error("Listar \"%s\" no está soportado".formatted(args[1]));
            }
            // @formatter:on
            return;
        }


        // En venta
        for (Casilla casilla : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (casilla.isPropiedad() && (casilla.getPropiedad().getPropietario() == null || casilla.getPropiedad().getPropietario() == banca)) {
                casilla.getPropiedad().listar();
            }
        }

        // Edificios
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getPropiedad().getTipo() == Propiedad.TipoPropiedad.Solar) {
                for (Edificio e : c.getPropiedad().getEdificios()) {
                    System.out.println(e);
                }
            }
        }


        if (args.length == 3 && args[1].equals("edificios")) {
            tablero.listarEdificiosGrupo(args[2]);
            return;
        }

        consola.error("Se esperaban 2 o 3 parámetros, se recibieron %d.".formatted(args.length - 1));
        */
    }

    /*
    public void listarEdificiosGrupo(String nombreGrupo) {
        Grupo grupo = buscar(grupos, (g) -> g.getNombre().equalsIgnoreCase(nombreGrupo));
        grupo.listarEdificios();

        // Mostrar cuantos edificios más se pueden construir
        // @formatter:off
        int nCasillas = grupo.getNumeroCasillas();
        int nHoteles  = nCasillas - grupo.contarEdificios(Edificio.TipoEdificio.Hotel);
        int nPiscinas = nCasillas - grupo.contarEdificios(Edificio.TipoEdificio.Piscina);
        int nPistas   = nCasillas - grupo.contarEdificios(Edificio.TipoEdificio.PistaDeporte);
        int nCasas    = (nHoteles == 0? nCasillas : 4) - grupo.contarEdificios(Edificio.TipoEdificio.Casa);
        // @formatter:on

        if (nCasas == 0 && nHoteles == 0 && nPiscinas == 0 && nPistas == 0) {
            System.out.printf("\nYa no se pueden construir más edificios en %s\n", grupo.getNombre());
            return;
        }

        // @formatter:off
        System.out.println("\nAún se pueden edificar:");
        if (nCasas != 0)    System.out.printf("  - %d casa(s)\n", nCasas);
        if (nHoteles != 0)  System.out.printf("  - %d hotel(es)\n", nHoteles);
        if (nPiscinas != 0) System.out.printf("  - %d piscina(s)\n", nPiscinas);
        if (nPistas != 0)   System.out.printf("  - %d pistas(s) de deporte\n", nPistas);
        // @formatter:on
    }
    */

    @Override
    public void estadisticas(String[] args) throws ErrorComando {
        if (args.length == 2) {
            Jugador jugador = buscar(jugadores, (j) -> j.getNombre().equalsIgnoreCase(args[1]));
            consola.imprimir(jugador.getEstadisticas().toString());
            return;
        }

        if (args.length == 1) {
            estadisticas();
            return;
        }

        consola.error("Se esperaba 0 o 1 parámetro, se recibieron %d".formatted(args.length - 1));
    }

    private void estadisticas() {
        if (!jugando) {
            consola.error("No se ha iniciado la partida");
            return;
        }

        Casilla casillaMasRentable = null;
        long maxAlquilerCobrado = -1;

        Casilla masFrecuentada = null;
        int maxEstancias = -1;

        // Analizar todas las casillas
        for (Casilla c : casillas) {
            // Encontrar el máximo del alquiler cobrado
            if (c instanceof Propiedad && ((Propiedad) c).getAlquilerTotalCobrado() > maxAlquilerCobrado) {
                maxAlquilerCobrado = ((Propiedad) c).getAlquilerTotalCobrado();
                casillaMasRentable = c;
            }

            // Encontrar el máximo de estancias
            if (c.frecuenciaVisita() > maxEstancias) {
                maxEstancias = c.frecuenciaVisita();
                masFrecuentada = c;
            }
        }

        // Analizar todos los grupos
        Grupo grupoMasRentable = null;
        long maxGrupoAlquiler = -1;

        for (Grupo g : grupos) {
            long alquilerTotalGrupo = 0;
            for (Propiedad p : g.getPropiedades()) {
                alquilerTotalGrupo += p.getAlquilerTotalCobrado();
            }

            if (alquilerTotalGrupo > maxGrupoAlquiler) {
                maxGrupoAlquiler = alquilerTotalGrupo;
                grupoMasRentable = g;
            }
        }

        // Analizar todos los jugadores
        Jugador masVueltas = null;
        int maxVueltas = -1;

        Jugador masTiradas = null;
        int maxTiradas = -1;

        Jugador enCabeza = null;
        long maxCapital = -1;

        for (Jugador j : jugadores) {
            EstadisticasJugador ej = j.getEstadisticas();

            if (ej.getVueltas() > maxVueltas) {
                maxVueltas = ej.getVueltas();
                masVueltas = j;
            }

            if (ej.getTiradas() > maxTiradas) {
                maxTiradas = ej.getTiradas();
                masTiradas = j;
            }

            if (ej.getCapital() > maxCapital) {
                maxCapital = ej.getCapital();
                enCabeza = j;
            }
        }

        // @formatter:off
        consola.imprimir(
                """
                {
                    casilla más rentable: %s (%s)
                    grupo más rentable: %s (%s)
                    casilla más frecuentada: %s (%d)
                    jugador con más vueltas: %s (%d)
                    jugador con más tiradas: %s (%d)
                    jugador en cabeza: %s (%s)
                }
                """.formatted(
                casillaMasRentable.getNombreFmt(), consola.num(maxAlquilerCobrado),
                consola.fmt(grupoMasRentable.getNombre(), grupoMasRentable.getCodigoColor()), consola.num(maxGrupoAlquiler),
                masFrecuentada.getNombreFmt(), maxEstancias,
                masVueltas.getNombre(), maxVueltas,
                masTiradas.getNombre(), maxTiradas,
                enCabeza.getNombre(), consola.num(maxCapital)));
        // @formatter:on
    }

    @Override
    public void trato(String[] args) {
        // TODO
    }

    @Override
    public void aceptar(String[] args) {
        // TODO
    }

    @Override
    public void eliminar(String[] args) {
        // TODO
    }

    // ================================================================================

    @Override
    public void ejecutarArchivo(String[] args) throws ErrorFatal, ErrorComando {
        if (args.length != 2) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        try (Scanner scanner = new Scanner(new File(args[1]))) {
            // Se procesa línea a línea, ejecutando cada comando
            while (scanner.hasNextLine()) {
                if (!ejecutarComando(scanner.nextLine())) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            throw new ErrorComando("\"%s\" no encontrado: ".formatted(args[1]));
        }
    }

    @Override
    public void fortuna(String[] args) throws ErrorComando {
        if (args.length != 3) {
            throw new ErrorComandoFormato(2, args.length - 1);
        }

        try {
            // Obtener el jugador
            Jugador jugador = buscar(jugadores, (j) -> j.getNombre().equalsIgnoreCase(args[1]));

            long cantidad = Long.parseLong(args[2]);

            // Aplicar la cantidad
            if (cantidad > 0) {
                jugador.ingresar(cantidad);
            } else if (cantidad < 0) {
                jugador.cobrar(-cantidad, true);
                consola.imprimir("Se ha cobrado exitosamente %s a %s\n".formatted(consola.num(-cantidad), jugador.getNombre()));
            }

            jugador.describirTransaccion();
        } catch (NumberFormatException e) {
            throw new ErrorComandoFormato("\"%s\": no es un número válido".formatted(args[2]));
        }
    }

    @Override
    public void mover(String[] args) throws ErrorComandoFormato, ErrorComandoEstadoPartida, ErrorFatal, ErrorComandoFortuna, ErrorComandoAvatar {
        if (args.length != 2 && args.length != 3) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        try {
            moverComun(new Dado(Integer.parseInt(args[1]), args.length == 2 ? 0 : Integer.parseInt(args[2])));
        } catch (NumberFormatException e) {
            throw new ErrorComandoFormato("Número no válido");
        }
    }
}
