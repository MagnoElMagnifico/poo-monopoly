package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Edificio;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Consola.Color;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Lector;
import monopoly.utilidades.PintorTablero;

import java.util.ArrayList;
import java.util.Random;

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
    private int turno;
    private boolean jugando;
    private int nCompras;


    /**
     * Crea un tablero por defecto
     */
    public Tablero() {
        // Entre 2 y 6 jugadores
        jugadores = new ArrayList<>(6);
        turno = 0;
        jugando = false;

        banca = new Jugador();
        // En lugar de añadir con código las casillas, se leen de
        // un archivo de configuración.
        //
        // NOTA: Esto es potencialmente un problema de seguridad,
        // dado que el usuario puede modificarlo sin reparos.
        casillas = Lector.leerCasillas("src/casillas.txt");
        // Creación de la calculadora
        calculadora = new Calculadora(casillas, banca, Lector.leerCartas("src/cartas.txt"));
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


        // Muestra el tablero si se ha movido el avatar con éxito
        if (getJugadorTurno().getAvatar().mover(dado, casillas, jugadores, calculadora)) {
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
        Avatar avatarTurno = jugadorTurno.getAvatar();

        if (jugadorTurno.getAvatar().getLanzamientos() > 0 && !jugadorTurno.getAvatar().isMovimientoEspecial()) {
            Consola.error("Al jugador %s le quedan %d tiros".formatted(jugadorTurno.getNombre(), avatarTurno.getLanzamientos()));
            return;
        }


        avatarTurno.resetDoblesSeguidos();
        avatarTurno.resetLanzamientos();
        avatarTurno.setPuedeComprar(true);
        turno = (turno + 1) % jugadores.size();

        System.out.printf("Se ha cambiado el turno.\nAhora le toca a %s.\n", Consola.fmt(getJugadorTurno().getNombre(), Color.Azul));
        System.out.print(this);
        System.out.print(jugadorTurno.describirTransaccion());
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

        Jugador j = getJugadorTurno();

        if (j.getAvatar().salirCarcelPagando()) {
            System.out.print(this);
            System.out.print(j.describirTransaccion());
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

        Jugador j = getJugadorTurno();

        if (!j.getAvatar().getCasilla().getNombre().equalsIgnoreCase(nombre)) {
            Consola.error("No se puede comprar otra casilla que no sea la actual");
            return;
        }

        Casilla c = j.getAvatar().getCasilla();

        if (!c.isPropiedad()) {
            Consola.error("No se puede comprar la casilla \"%s\"".formatted(c.getNombre()));
            return;
        }
        if(!j.getAvatar().isPuedeComprar()){
            Consola.error("No se puede comprar la casilla \"%s\"".formatted(c.getNombre()));
            return;
        }
        if (j.comprar(c.getPropiedad())) {
            if(j.getAvatar().isMovimientoEspecial() && j.getAvatar().getTipo()== Avatar.TipoAvatar.Pelota) {
                j.getAvatar().setPuedeComprar(false);
            }
            j.describirTransaccion();
        }
    }

    /**
     * Realiza las comprobaciones necesarias y llama al jugador para que edifique un nuevo edificio del tipo dado.
     */
    public void edificar(Edificio.TipoEdificio tipoEdificio) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador j = getJugadorTurno();
        Casilla c = j.getAvatar().getCasilla();

        if (!c.isPropiedad() || c.getPropiedad().getTipo() != Propiedad.TipoPropiedad.Solar) {
            Consola.error("No se puede edificar en una casilla que no sea un solar");
            return;
        }

        Propiedad p = c.getPropiedad();

        if (!p.getPropietario().equals(j)) {
            Consola.error("No se puede edificar en una propiedad que no te pertenece");
            return;
        }

        j.comprar(new Edificio(p.getEdificios().size(), tipoEdificio, p));
    }

    @Override
    public String toString() {
        return PintorTablero.pintarTablero(this);
    }

    /**
     * Obtiene los avatares de los jugadores
     */
    public ArrayList<Avatar> getAvatares() {
        ArrayList<Avatar> avatares = new ArrayList<>(jugadores.size());

        // NOTA: si `jugadores` está vacío, esto no se ejecuta y se devuelve un ArrayList vacío
        for (Jugador jugador : jugadores) {
            avatares.add(jugador.getAvatar());
        }

        return avatares;
    }

    /**
     * Obtiene las casillas que actualmente están en venta
     */
    public ArrayList<Propiedad> getEnVenta() {
        ArrayList<Propiedad> enVenta = new ArrayList<>(casillas.size());

        for (Casilla casilla : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (casilla.isPropiedad() && (casilla.getPropiedad().getPropietario() == null || casilla.getPropiedad().getPropietario() == banca)) {
                enVenta.add(casilla.getPropiedad());
            }
        }

        return enVenta;
    }

    /**
     * Obtiene las casillas de este tablero
     */
    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    /**
     * Obtiene los jugadores jugando actualmente en esta partida
     */
    public ArrayList<Jugador> getJugadores() {
        return jugadores;
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


}

