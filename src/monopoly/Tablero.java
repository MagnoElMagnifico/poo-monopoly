package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Edificio;
import monopoly.casillas.Grupo;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.*;
import monopoly.utilidades.Consola.Color;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Clase que representa el tablero del juego.
 * Contiene a los jugadores y a las casillas; gestiona el
 * turno actual, el estado de la partida, etc.
 *
 * @see Jugador
 * @see Casilla
 */
public class Tablero {
    private final Calculadora calculadora;
    private final Jugador banca;
    private final ArrayList<Jugador> jugadores;
    private final ArrayList<Casilla> casillas;
    private final ArrayList<Grupo> grupos;
    private Casilla carcel;
    private int turno;
    private boolean jugando;

    /**
     * Crea un tablero por defecto
     */
    public Tablero(ArrayList<Casilla> casillas, ArrayList<Grupo> grupos) {
        // Entre 2 y 6 jugadores
        this.jugadores = new ArrayList<>(6);
        this.turno = 0;
        this.jugando = false;

        this.banca = new Jugador();
        this.casillas = casillas;
        this.grupos = grupos;

        this.calculadora = new Calculadora(casillas);
        this.calculadora.asignarValores(casillas, banca, Lector.leerCartas("src/cartas.txt", this));

        // Buscar la casilla de cárcel
        // Se necesita tener esta referencia para poder enviar rápidamente el avatar
        // a la cárcel en caso de que saque dados dobles o caiga en IrCarcel.
        carcel = null;
        for (Casilla c : casillas) {
            if (c.getTipo() == Casilla.TipoCasilla.Carcel) {
                carcel = c;
                break;
            }
        }
    }

    /**
     * Iniciar la partida: a partir de ahora se pueden lanzar
     * los dados, pero no se pueden añadir más jugadores.
     */
    public void iniciar() {
        if (jugando) {
            Consola.error("La partida ya está iniciada");
            return;
        }

        if (jugadores.size() < 2) {
            Consola.error("No hay suficientes jugadores para empezar (mínimo 2)");
            return;
        }

        jugando = true;
        System.out.print(Consola.fmt("Se ha iniciado la partida.\nA JUGAR!\n", Color.Verde));
    }

    /**
     * Función de ayuda que comprueba si un ID de avatar es único
     */
    private boolean comprobarAvatarId(char id) {
        for (Jugador jugador : jugadores) {
            if (jugador.getAvatar().getId() == id) {
                return false;
            }
        }
        return true;
    }

    /**
     * Función de ayuda que genera un ID para el avatar aleatoriamente
     */
    private char generarAvatarId() {
        char posibleId;
        Random rand = new Random();

        do {
            posibleId = (char) (rand.nextInt((int) 'Z' - (int) 'A' + 1) + (int) 'A');
        } while (!comprobarAvatarId(posibleId));

        return posibleId;
    }

    /**
     * Añade un jugador dado su nombre y tipo de avatar
     */
    public void anadirJugador(String nombre, Avatar.TipoAvatar tipo) {
        if (jugando) {
            Consola.error("No se pueden añadir jugadores en mitad de una partida");
            return;
        }

        if (jugadores.size() >= 6) {
            Consola.error("El máximo de jugadores es 6");
            return;
        }

        char avatar = generarAvatarId();
        jugadores.add(new Jugador(nombre, tipo, avatar, casillas.get(0), calculadora.calcularFortuna()));

        System.out.printf("El jugador %s con avatar %s se ha creado con éxito.\n",
                Consola.fmt(nombre, Color.Verde),
                Consola.fmt(Character.toString(avatar), Color.Verde));
    }

    /**
     * Obtiene el jugador de turno. Si no hay jugadores devuelve `null`
     */
    public Jugador getJugadorTurno() {
        return jugadores.isEmpty() ? null : jugadores.get(turno);
    }

    /**
     * Mueve el avatar del jugador actual las
     * casillas correspondientes según la tirada
     * del dado.
     */
    public void moverAvatar(Dado dado) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador jugadorTurno = getJugadorTurno();

        if (jugadorTurno.isEndeudado()) {
            Consola.error("Estas endeudado: paga la deuda o declárate en bancarrota para poder avanzar");
            return;
        }

        // Muestra el tablero si se ha movido el avatar con éxito
        if (jugadorTurno.getAvatar().mover(dado, this)) {
            System.out.print(this);
            jugadorTurno.describirTransaccion();
        }
    }

    public void siguienteMovimiento() {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        if (getJugadorTurno().isEndeudado()) {
            Consola.error("Estas endeudado: paga la deuda o declárate en bancarrota para poder avanzar");
            return;
        }

        Avatar avatarTurno = getJugadorTurno().getAvatar();

        if (avatarTurno.getTipo() != Avatar.TipoAvatar.Pelota) {
            Consola.error("No puedes usar este comando no eres una pelota.");
            return;
        }

        if (!avatarTurno.isMovimientoEspecial() || !avatarTurno.isPelotaMovimiento()) {
            Consola.error("No puedes usar esto ahora. Vuelve más tarde.");
            return;
        }

        if (avatarTurno.mover(null, this)) {
            System.out.print(this);
        }
    }

    /**
     * Termina el turno del jugador actual y calcula el siguiente
     */
    public void acabarTurno() {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador jugadorTurno = getJugadorTurno();

        if (jugadorTurno.isEndeudado()) {
            Consola.error("El jugador %s está endeudado: paga la deuda o declárate en bancarrota para poder avanzar".formatted(jugadorTurno.getNombre()));
            return;
        }

        Avatar avatarTurno = jugadorTurno.getAvatar();

        if (avatarTurno.getLanzamientosEnTurno() > 0 && !jugadorTurno.getAvatar().isMovimientoEspecial()) {
            Consola.error("Al jugador %s le quedan %d tiros".formatted(jugadorTurno.getNombre(), avatarTurno.getLanzamientosEnTurno()));
            return;
        }

        if (avatarTurno.getTipo() == Avatar.TipoAvatar.Pelota && avatarTurno.getLanzamientosEnTurno() > 0) {
            Consola.error("Al jugador %s aún le quedan tiros especiales".formatted(jugadorTurno.getNombre()));
            return;
        }

        avatarTurno.resetDoblesSeguidos();
        avatarTurno.resetLanzamientos();
        avatarTurno.setPuedeComprar(true);

        turno = (turno + 1) % jugadores.size();

        System.out.printf("Se ha cambiado el turno.\nAhora le toca a %s.\n", Consola.fmt(getJugadorTurno().getNombre(), Color.Azul));
        jugadorTurno.describirTransaccion();
        System.out.print(this);
    }

    /**
     * Realiza las comprobaciones necesarias y llama al avatar para que cambie de modo de movimiento
     */
    public void cambiarModo() {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        getJugadorTurno().getAvatar().cambiarModo();
    }

    /**
     * Realiza las comprobaciones necesarias y llama al avatar para que salga de la cárcel pagando la fianza.
     */
    public void salirCarcel() {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador jugadorTurno = getJugadorTurno();

        if (jugadorTurno.getAvatar().salirCarcelPagando()) {
            System.out.print(this);
            jugadorTurno.describirTransaccion();
        }
    }

    /**
     * Realiza las comprobaciones necesarias y llama al jugador para que compre la
     * propiedad en la que se encuentra.
     *
     * @param nombre Nombre de la propiedad a comprar recibida desde el comando
     */
    public void comprar(String nombre) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador jugadorTurno = getJugadorTurno();
        Avatar avatarTurno = jugadorTurno.getAvatar();

        if (!avatarTurno.getCasilla().getNombre().equalsIgnoreCase(nombre)) {
            Consola.error("No se puede comprar otra casilla que no sea la actual");
            return;
        }

        Casilla casillaActual = avatarTurno.getCasilla();

        if (!casillaActual.isPropiedad()) {
            Consola.error("No se puede comprar la casilla \"%s\"".formatted(casillaActual.getNombre()));
            return;
        }

        // Especifico para el coche que solo puede comprar una vez por turno en modo especial
        if (!avatarTurno.isPuedeComprar()) {
            Consola.error("El jugador ya ha comprado una vez en este turno");
            return;
        }

        if (jugadorTurno.comprar(casillaActual.getPropiedad())) {
            // Si se realizó la compra correctamente y se usa el avatar tipo Pelota,
            // se debe evitar que se compre otra vez en este mismo turno.
            if (avatarTurno.isMovimientoEspecial() && avatarTurno.getTipo() == Avatar.TipoAvatar.Pelota) {
                avatarTurno.setPuedeComprar(false);
            }
        }
    }

    /**
     * Realiza las comprobaciones necesarias y llama al jugador para que edifique un nuevo edificio del tipo dado.
     */
    public void edificar(Edificio.TipoEdificio tipoEdificio, int cantidad) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        getJugadorTurno().comprar(tipoEdificio, cantidad);
    }

    /**
     * Realiza las comprobaciones necesarias y llama al jugador para que venda una edificación
     */
    public void vender(Edificio.TipoEdificio tipoEdificio, String nombreSolar, int cantidad) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        // Buscar el solar
        Propiedad solar = null;
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getPropiedad().getTipo() == Propiedad.TipoPropiedad.Solar && c.getPropiedad().getNombre().equalsIgnoreCase(nombreSolar)) {
                solar = c.getPropiedad();
                break;
            }
        }

        if (solar == null) {
            Consola.error("No existe el solar \"%s\"".formatted(nombreSolar));
            return;
        }

        getJugadorTurno().vender(tipoEdificio, solar, cantidad);
    }

    public void hipotecar(String nombre) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        // Buscar la propiedad
        Propiedad p = null;
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getNombre().equalsIgnoreCase(nombre)) {
                p = c.getPropiedad();
                break;
            }
        }

        if (p == null) {
            Consola.error("No se ha encontrado la propiedad \"%s\"".formatted(nombre));
            return;
        }

        getJugadorTurno().hipotecar(p);
    }

    public void deshipotecar(String nombre) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        // Buscar la propiedad
        Propiedad p = null;
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getNombre().equalsIgnoreCase(nombre)) {
                p = c.getPropiedad();
                break;
            }
        }

        if (p == null) {
            Consola.error("No se ha encontrado la propiedad \"%s\"".formatted(nombre));
            return;
        }

        getJugadorTurno().deshipotecar(p);
    }

    @Override
    public String toString() {
        return PintorTablero.pintarTablero(this);
    }

    /**
     * Obtiene las casillas de este tablero
     */
    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public void listarCasillas() {
        for (Casilla c : casillas) {
            System.out.println(c);
        }
    }

    public void listarJugadores() {
        for (Jugador j : jugadores) {
            System.out.println(j);
        }
    }

    /**
     * Obtiene los grupos en los que se dividen las casillas de este tablero
     */
    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    /**
     * Obtiene los jugadores jugando actualmente en esta partida
     */
    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public Jugador getBanca() {
        return banca;
    }

    /**
     * Imprime por pantalla la información de la casilla dado su nombre
     */
    public void describirCasilla(String nombre) {
        for (Casilla c : casillas) {
            if (c.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println(c);
            }
        }
    }

    /**
     * Imprime por pantalla la información del jugador dado su nombre
     */
    public void describirJugador(String nombre) {
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println(j);
            }
        }
    }

    /**
     * Imprime por pantalla la información del avatar dado su ID
     */
    public void describirAvatar(char id) {
        for (Jugador jugador : jugadores) {
            Avatar a = jugador.getAvatar();
            if (a.getId() == Character.toUpperCase(id)) {
                System.out.println(a);
                return;
            }
        }
    }

    /**
     * Muestra los avatares de los jugadores
     */
    public void listarAvatares() {
        for (Jugador jugador : jugadores) {
            System.out.println(jugador.getAvatar());
        }
    }

    public void bancarrota() {
        // Pedir confirmación
        Scanner scanner = new Scanner(System.in);
        System.out.print("Esta seguro de que quiere abandonar la partida? (y/N): ");
        if (Character.toLowerCase(scanner.nextLine().trim().charAt(0)) != 'y') {
            System.out.println("Operación cancelada");
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
            p.setHipotecada(false);
        }

        // Se borra el jugador
        jugadores.remove(deudor);
        System.out.printf("El jugador %s se declara en bancarrota y abandona la partida\n", Consola.fmt(deudor.getNombre(), Color.Azul));

        if (jugadores.size() == 1) {
            // Fin de la partida
            System.out.println(Consola.fmt("Felicidades %s, has ganado la partida".formatted(jugadores.get(0).getNombre()), Color.Amarillo));
            jugando = false;
        } else {
            System.out.printf("Ahora le toca a %s\n", Consola.fmt(getJugadorTurno().getNombre(), Color.Azul));
        }
    }

    /**
     * Muestra las casillas que actualmente están en venta
     */
    public void listarEnVenta() {
        for (Casilla casilla : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (casilla.isPropiedad() && (casilla.getPropiedad().getPropietario() == null || casilla.getPropiedad().getPropietario() == banca)) {
                System.out.println(casilla.getPropiedad());
            }
        }
    }

    /**
     * Muestra todos los edificios construidos
     */
    public void listarEdificios() {
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getPropiedad().getTipo() == Propiedad.TipoPropiedad.Solar) {
                for (Edificio e : c.getPropiedad().getEdificios()) {
                    System.out.println(e);
                }
            }
        }
    }

    /**
     * Obtiene información sobre los edificios del grupo dado su nombre, junto con el número de edificios que se pueden construir
     */
    public void listarEdificiosGrupo(String nombreGrupo) {
        Grupo grupo = null;
        for (Grupo g : grupos) {
            if (g.getNombre().equalsIgnoreCase(nombreGrupo)) {
                grupo = g;
                break;
            }
        }

        if (grupo == null) {
            Consola.error("No existe un grupo con el nombre \"%s\"".formatted(nombreGrupo));
            return;
        }

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

    /**
     * Muestra las estadísticas generales sobre la evolución del juego
     */
    public void mostrarEstadisticas() {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Casilla casillaMasRentable = null;
        long maxAlquilerCobrado = -1;

        Casilla masFrecuentada = null;
        int maxEstancias = -1;

        // Analizar todas las casillas
        for (Casilla c : casillas) {
            EstadisticasCasilla ec = c.getEstadisticas();

            // Encontrar el máximo del alquiler cobrado
            if (ec.getAlquilerTotalCobrado() > maxAlquilerCobrado) {
                maxAlquilerCobrado = ec.getAlquilerTotalCobrado();
                casillaMasRentable = c;
            }

            // Encontrar el máximo de estancias
            if (ec.getEstancias() > maxEstancias) {
                maxEstancias = ec.getEstancias();
                masFrecuentada = c;
            }
        }

        // Analizar todos los grupos
        Grupo grupoMasRentable = null;
        long maxGrupoAlquiler = -1;

        for (Grupo g : grupos) {

            long alquilerTotalGrupo = 0;
            for (Casilla c : g.getCasillas()) {
                alquilerTotalGrupo += c.getEstadisticas().getAlquilerTotalCobrado();
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
        System.out.printf(
                """
                {
                    casilla más rentable: %s (%s)
                    grupo más rentable: %s (%s)
                    casilla más frecuentada: %s (%d)
                    jugador con más vueltas: %s (%d)
                    jugador con más tiradas: %s (%d)
                    jugador en cabeza: %s (%s)
                }
                """,
                casillaMasRentable.getNombreFmt(), Consola.num(maxAlquilerCobrado),
                grupoMasRentable.getNombre(), Consola.num(maxGrupoAlquiler),
                masFrecuentada.getNombreFmt(), maxEstancias,
                masVueltas.getNombre(), maxVueltas,
                masTiradas.getNombre(), maxTiradas,
                enCabeza.getNombre(), Consola.num(maxCapital));
        // @formatter:on
    }

    /**
     * Muestra las estadísticas de un jugador en concreto
     */
    public void mostrarEstadisticas(String nombreJugador) {
        Jugador jugador = null;
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombreJugador)) {
                jugador = j;
                break;
            }
        }

        if (jugador == null) {
            Consola.error("No existe el jugador \"%s\"".formatted(nombreJugador));
            return;
        }

        System.out.print(jugador.getEstadisticas());
    }

    public Casilla getCarcel() {
        return carcel;
    }

    public Calculadora getCalculadora() {
        return calculadora;
    }
}
