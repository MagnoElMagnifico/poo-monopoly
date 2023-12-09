package monopoly;

import monopoly.casilla.Casilla;
import monopoly.casilla.Lector;
import monopoly.casilla.edificio.*;
import monopoly.casilla.especial.CasillaCarcel;
import monopoly.casilla.especial.CasillaSalida;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.*;
import monopoly.jugador.*;
import monopoly.utils.Consola;
import monopoly.utils.Consola.Color;
import monopoly.utils.ConsolaNormal;
import monopoly.utils.Dado;
import monopoly.utils.PintorTablero;

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

    public static Consola consola;
    private final String msgAyuda;

    // Atributos
    private final ArrayList<Jugador> jugadores;
    private final ArrayList<Casilla> casillas;
    private final ArrayList<Grupo> grupos;
    private final Banca banca;
    // Información relevante
    private final long fortunaInicial;
    private final CasillaCarcel carcel;
    private final CasillaSalida salida;
    // Estado
    private int turno;
    private int nAumentosPrecio;
    private boolean jugando;

    public Juego() throws ErrorFatalConfig, ErrorFatalLogico {
        try {
            consola = new ConsolaNormal();
            msgAyuda = Files.readString(Path.of(JuegoConsts.CONFIG_AYUDA));

            banca = new Banca();
            jugadores = new ArrayList<>(JuegoConsts.MAX_JUGADORES);
            turno = 0;
            jugando = false;
            nAumentosPrecio = 1;

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
        consola.imprimir(consola.fmt(JuegoConsts.MSG_INICIO, Color.Amarillo));
        consola.imprimir("Puedes usar el comando \"%s\" para ver las opciones disponibles\n".formatted(consola.fmt("ayuda", Color.Verde)));

        boolean ejecutar = true;
        while (ejecutar) {
            try {
                ejecutar = ejecutarComando(consola.leer(JuegoConsts.PROMPT));
            } catch (ErrorComando e) {
                e.imprimirMsg();
            } catch (ErrorFatal e) {
                e.imprimirMsg();
                e.abortar();
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
        //   - Eliminar los ':' (tratos)
        //   - Convertir a minúsculas
        cmd = cmd.strip().replaceAll(" +", " ").replaceAll(":", "").toLowerCase();

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
            case "listar" -> listar(args);
            case "describir" -> describir(args);
            case "trato" -> trato(args);
            case "aceptar" -> aceptar(args);
            case "eliminar" -> eliminar(args);
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
        return PintorTablero.pintarTablero(casillas);
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
                if (c instanceof Solar && !(((Solar) c).getPropietario() instanceof Banca)) {
                    ((Solar) c).factorPrecio(1.05f);
                }
            }

            consola.imprimir("Se ha aumentado el precio de todos los solares a la venta\n");
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
        consola.imprimir(consola.fmt(JuegoConsts.MSG_JUGAR, Color.Amarillo));
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
    public void salirCarcel() throws ErrorComandoEstadoPartida, ErrorComandoAvatar, ErrorFatalLogico {
        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        Jugador jugadorTurno = getJugadorTurno();

        jugadorTurno.getAvatar().salirCarcelPagando(banca);
        verTablero();
        jugadorTurno.describirTransaccion();
    }

    @Override
    public void cambiarModo() throws ErrorComandoEstadoPartida, ErrorComandoAvatar {
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
    public void siguiente() throws ErrorComandoEstadoPartida, ErrorComandoFortuna, ErrorComandoAvatar, ErrorFatal, ErrorComandoFormato {
        if(getJugadorTurno().getAvatar() instanceof AvatarPelota){
            moverComun(null);
        }
        else {
            throw new ErrorComandoFormato("No puedes usar este comando si no eres una pelota");
        }
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
    public void acabarTurno() throws ErrorComandoEstadoPartida, ErrorComandoAvatar {
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
        consola.imprimir(getJugadorTurno().getTratos().toString());
    }

    @Override
    public void bancarrota() {
        // Pedir confirmación
        String respuesta = consola.leer("Esta seguro de que quiere abandonar la partida? (y/N): ");
        if (respuesta.isBlank() || Character.toLowerCase(respuesta.trim().charAt(0)) != 'y') {
            consola.imprimir("Operación cancelada\n");
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
        jugando = false;
        consola.imprimir(consola.fmt("\nFelicidades %s, has ganado la partida\n".formatted(jugadores.get(0).getNombre()), Color.Amarillo));
        consola.imprimir(consola.fmt(JuegoConsts.MSG_FIN, Color.Amarillo));
        System.exit(0);
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

        // Comprobar que es único
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombre)) {
                throw new ErrorComandoFormato("No puede haber dos jugadores con el mismo nombre");
            }
        }

        Avatar avatar = switch (args[3]) {
            case "c", "coche" -> new AvatarCoche(JuegoConsts.AVATARES_ID[jugadores.size()], salida);
            case "p", "pelota" -> new AvatarPelota(JuegoConsts.AVATARES_ID[jugadores.size()], salida);
            default ->
                    throw new ErrorComandoFormato("\"%s\": No es un tipo válido de Avatar (prueba con c, p)".formatted(args[3]));
        };

        jugadores.add(new Jugador(nombre, avatar, fortunaInicial));

        consola.imprimir("El jugador %s con avatar %s se ha creado con éxito.\n".formatted(
                consola.fmt(nombre, Color.Verde),
                consola.fmt(Character.toString(avatar.getId()), Color.Verde)));
    }

    @Override
    public void comprar(String[] args) throws ErrorComando, ErrorFatalLogico {
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

        jugadorTurno.comprar((Propiedad) casillaActual);
    }

    @Override
    public void edificar(String[] args) throws ErrorComando, ErrorFatalLogico {
        if (args.length != 2 && args.length != 3) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        if (!(getJugadorTurno().getAvatar().getCasilla() instanceof Solar)) {
            throw new ErrorComandoEdificio("No se puede edificar en una casilla que no sea un Solar");
        }

        try {
            Solar solar = (Solar) getJugadorTurno().getAvatar().getCasilla();

            for (int i = 0; i < (args.length == 2 ? 1 : Integer.parseInt(args[2])); i++) {
                Edificio edificio = switch (args[1]) {
                    case "c", "casa", "casas" -> new Casa(solar);
                    case "h", "hotel", "hoteles" -> new Hotel(solar);
                    case "p", "piscina", "piscinas" -> new Piscina(solar);
                    case "d", "pd", "pista", "pistas", "pistadeporte", "pistasdeporte" -> new PistaDeporte(solar);
                    default ->
                            throw new ErrorComandoFormato("\"%s\": no es un tipo de edificio válido".formatted(args[1]));
                };

                getJugadorTurno().construir(edificio);
            }
        } catch (NumberFormatException e) {
            throw new ErrorComandoFormato("\"%s\": no es un número válido".formatted(args[2]));
        }
    }

    private <T> T buscar(Collection<T> elementos, Function<T, Boolean> funcion) throws ErrorComando {
        for (T e : elementos) {
            if (funcion.apply(e)) {
                return e;
            }
        }

        throw new ErrorComando("No encontrado");
    }

    @Override
    public void vender(String[] args) throws ErrorComando, ErrorFatalLogico {
        if (args.length != 3 && args.length != 4) {
            throw new ErrorComandoFormato(2, args.length - 1);
        }

        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
        }

        Solar solar = (Solar) buscar(casillas, (c) -> c instanceof Solar);
        String tipoEdificio = switch (args[1]) {
            case "c", "casa", "casas" -> "Casa";
            case "h", "hotel", "hoteles" -> "Hotel";
            case "p", "piscina", "piscinas" -> "Piscina";
            case "d", "pd", "pista", "pistas", "pistadeporte", "pistasdeporte" -> "PistaDeporte";
            default -> throw new ErrorComandoFormato("\"%s\": no es un tipo de edificio válido".formatted(args[1]));
        };

        getJugadorTurno().vender(solar, tipoEdificio, args.length == 3 ? 1 : Integer.parseInt(args[2]));
    }

    @Override
    public void hipotecar(String[] args) throws ErrorComando, ErrorFatalLogico {
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
    public void deshipotecar(String[] args) throws ErrorComando, ErrorFatalLogico {
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
                case "avatar" ->
                        consola.describir(jugadores, (j) -> j.getAvatar().getId() == Character.toUpperCase(args[2].charAt(0)));
                default -> throw new ErrorComandoFormato("\"%s\": Argumento inválido".formatted(args[1]));
            }
        } else {
            throw new ErrorComandoFormato(2, args.length - 1);
        }
    }

    @Override
    public void listar(String[] args) throws ErrorComando, ErrorFatalLogico {
        if (args.length == 2) {
            // @formatter:off
            switch (args[1]) {
                case "jugadores" -> consola.imprimirLista(jugadores);
                case "avatares"  -> consola.imprimir(consola.listar(jugadores, (j) -> j.getAvatar().listar()) + '\n');
                case "casillas"  -> consola.imprimirLista(casillas);
                case "enventa"   -> consola.imprimir(consola.listar(casillas, (c) -> c instanceof Propiedad && ((Propiedad) c).getPropietario() instanceof Banca ? c.listar() : null) + '\n');
                case "tratos"    -> consola.imprimir(getJugadorTurno().getTratos().toString());
            }
            // @formatter:on

            if (args[1].equalsIgnoreCase("edificios")) {
                for (Casilla c : casillas) {
                    if (c instanceof Solar) {
                        for (Edificio e : ((Solar) c).getEdificios()) {
                            consola.imprimir(e.listar());
                        }
                    }
                }
            }
            return;
        }

        if (args.length == 3 && args[1].equals("edificios")) {
            Grupo grupo = buscar(grupos, (g) -> g.getNombre().equalsIgnoreCase(args[2]));
            grupo.listarEdificios();
            return;
        }

        throw new ErrorComandoFormato(2, args.length - 1);
    }

    @Override
    public void estadisticas(String[] args) throws ErrorComando, ErrorFatalLogico {
        if (args.length == 2) {
            Jugador jugador = buscar(jugadores, (j) -> j.getNombre().equalsIgnoreCase(args[1]));
            consola.imprimir(jugador.getEstadisticas().toString());
            return;
        }

        if (args.length == 1) {
            estadisticas();
            return;
        }

        throw new ErrorComandoFormato(0, args.length - 1);
    }

    private void estadisticas() throws ErrorComandoEstadoPartida, ErrorFatalLogico {
        if (!jugando) {
            throw new ErrorComandoEstadoPartida("No se ha iniciado la partida");
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

    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void trato(String[] args) throws ErrorComando {
        if(args.length<6){
            throw new ErrorComandoFormato(6,args.length);
        }

        Jugador jugador = buscar(jugadores, (j) -> j.getNombre().equalsIgnoreCase(args[1]));

        if (!args[2].equalsIgnoreCase("cambiar")) {
            throw new ErrorComandoFormato("El uso de la palabra \"cambiar\" es obligatorio");
        }

        if (getJugadorTurno().equals(jugador)) {
            throw new ErrorComando("No puedes hacer un trato contigo mismo");
        }

        // trato nombre cambiar X por Y
        if (args.length == 6) {
            if (!args[4].equalsIgnoreCase("por")) {
                throw new ErrorComandoFormato("El uso de la palabra \"por\" es obligatorio");
            }

            // trato nombre cambiar CANTIDAD por PROPIEDAD
            if (isNumeric(args[3])) {
                Propiedad p = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[5]));
                getJugadorTurno().crearTrato(jugador, Long.parseLong(args[3]), p);
                return;
            }

            // trato nombre cambiar PROPIEDAD por CANTIDAD
            if (isNumeric(args[5])) {
                Propiedad p = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[3]));
                getJugadorTurno().crearTrato(jugador, p, Long.parseLong(args[5]));
                return;
            }

            // trato nombre cambiar PROPIEDAD por PROPIEDAD
            Propiedad p1 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[3]));
            Propiedad p2 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[5]));
            getJugadorTurno().crearTrato(jugador, p1, p2);
            return;
        }

        // trato nombre cambiar X por Y y Z
        // trato nombre cambiar X y Y por Z
        if (args.length == 8) {
            // trato nombre cambiar PROPIEDAD por Y y Z
            if (args[4].equalsIgnoreCase("por") && args[6].equalsIgnoreCase("y")) {
                Propiedad p1 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[3]));

                // trato nombre cambiar PROPIEDAD por CANTIDAD y PROPIEDAD
                if (isNumeric(args[5])) {
                    // Intercambiar args[5] y args[7]
                    String temp = args[5];
                    args[5] = args[7];
                    args[7] = temp;
                }

                // trato nombre cambiar PROPIEDAD por PROPIEDAD y CANTIDAD
                if (isNumeric(args[7])) {
                    Propiedad p2 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[5]));
                    getJugadorTurno().crearTrato(jugador, p1, p2, Long.parseLong(args[7]));
                    return;
                }
            }

            // trato nombre cambiar X y Y por PROPIEDAD
            if (args[4].equalsIgnoreCase("y") && args[6].equalsIgnoreCase("por")) {

                // trato nombre cambiar CANTIDAD y PROPIEDAD por PROPIEDAD
                if (isNumeric(args[3])) {
                    // Intercambiar args[3] y args[5]
                    String temp = args[5];
                    args[5] = args[3];
                    args[3] = temp;
                }
                Propiedad p2 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[3]));

                // trato nombre cambiar PROPIEDAD y CANTIDAD por PROPIEDAD
                if (isNumeric(args[5])) {
                    Propiedad p1 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[3]));
                    getJugadorTurno().crearTrato(jugador, p1, Long.parseLong(args[5]), p2);
                    return;
                }
            }
        }

        // trato nombre cambiar PROPIEDAD por PROPIEDAD y noalquiler PROPIEDAD durante N
        if (args.length == 11
                && args[4].equalsIgnoreCase("por")
                && args[6].equalsIgnoreCase("y")
                && args[7].equalsIgnoreCase("noalquiler")
                && args[9].equalsIgnoreCase("durante")
                && isNumeric(args[10])
        ) {
            Propiedad p1 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[3]));
            Propiedad p2 = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[5]));
            Propiedad na = (Propiedad) buscar(casillas, (c) -> c.getNombre().equalsIgnoreCase(args[8]));
            if(!isNumeric(args[10])){
                throw new ErrorComandoFormato("Indica el número de turnos que no se pagará el alquiler.");
            }
            int nTurnos = Integer.parseInt(args[10]);
            getJugadorTurno().crearTrato(jugador, p1, p2, na, nTurnos);
        }

        throw new ErrorComandoFormato("Formato de comando incorrecto. Consulta la ayuda para más información.");
    }

    @Override
    public void aceptar(String[] args) throws ErrorComandoFormato, ErrorComandoFortuna, ErrorFatalLogico {
        if (args.length != 2) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }
        getJugadorTurno().aceptarTrato(args[1]);
    }

    @Override
    public void eliminar(String[] args) throws ErrorComandoFormato {
        if (args.length != 2) {
            throw new ErrorComandoFormato(1, args.length - 1);
        }
        getJugadorTurno().eliminarTrato(args[1]);
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
    public void fortuna(String[] args) throws ErrorComando, ErrorFatalLogico {
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
                jugador.cobrar(-cantidad, banca);
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
