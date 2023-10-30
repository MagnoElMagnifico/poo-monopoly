package monopoly;

import monopoly.casillas.Casilla;
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
 * Contiene a los jugadores y a las casillas. Tira el dado y gestiona el turno actual.
 *
 * @see Jugador
 * @see Casilla
 */
public class Tablero {
    private final Calculadora calculadora;
    private final Jugador banca;
    private final ArrayList<Jugador> jugadores;
    /**
     * Casillas del tablero
     */
    private final ArrayList<Casilla> casillas;

    private int turno;
    /**
     * True si la partida ha comenzado: ya no se pueden añadir más jugadores
     */
    private boolean jugando;

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
        calculadora = new Calculadora(casillas, banca);
    }

    public void iniciar() {
        if (jugando) {
            Consola.error("La partida ya está iniciada\n");
            return;
        }

        if (jugadores.size() < 2) {
            Consola.error("No hay suficientes jugadores para empezar (mínimo 2)\n");
            return;
        }

        jugando = true;
        System.out.printf(Consola.fmt("Se ha iniciado la partida.\nA JUGAR!\n", Color.Verde));
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
            Consola.error("No se pueden añadir jugadores en mitad de una partida\n");
            return;
        }

        if (jugadores.size() >= 6) {
            Consola.error("El máximo de jugadores es 6\n");
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
     * Mueve el jugador un determinado número de casillas
     */
    public void moverJugador(Dado dado) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida\n");
            return;
        }

        if (nLanzamientos <= 0) {
            Consola.error("No se puede lanzar más veces. El jugador debe terminar su turno.\n");
            return;
        }
        nLanzamientos--;

        // Calcular la casilla siguiente
        Jugador jugador = getJugadorTurno();
        Avatar avatar = jugador.getAvatar();

        if (avatar.isEncerrado()) {
            avatar.seguirEnCarcel();

            if (dado.isDoble()) {
                System.out.println("Dados dobles! El jugador puede salir de la Cárcel");
                avatar.salirCarcel();
            } else if (avatar.getEstanciasCarcel() >= 3) {
                System.out.printf("%s con avatar %s no ha sacado dados dobles %s.\nAhora debe pagar obligatoriamente la fianza.\n",
                        Consola.fmt(jugador.getNombre(), Color.Azul),
                        Consola.fmt(Character.toString(avatar.getId()), Color.Azul),
                        dado);
                avatar.salirCarcel();
            } else {
                System.out.printf("%s con avatar %s no ha sacado dados dobles %s.\nPuede pagar la fianza o permanecer encerrado.\n",
                        Consola.fmt(jugador.getNombre(), Color.Azul),
                        Consola.fmt(Character.toString(avatar.getId()), Color.Azul),
                        dado);
            }
        } else if (dado.isDoble()) {
            nLanzamientos++;
            nDoblesSeguidos++;
            if (nDoblesSeguidos >= 3) {
                System.out.printf("""
                        %s con avatar %s ha sacado %s.
                        Ya son 3 veces seguidas sacando dados dobles.
                        %s es arrestado por tener tanta suerte.
                        """,
                        Consola.fmt(jugador.getNombre(), Color.Azul),
                        Consola.fmt(Character.toString(avatar.getId()), Color.Azul),
                        dado, jugador.getNombre());
                avatar.irCarcel();
            }

            System.out.printf("%s. El jugador puede tirar otra vez.\n", Consola.fmt("Dados dobles!", Color.Azul));
            avatar.mover(dado, casillas, jugadores, calculadora);
        }
    }

    /**
     * Termina el turno del jugador actual y calcula el siguiente
     */
    public void acabarTurno() {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida\n");
            return;
        }

        if (nLanzamientos > 0) {
            Consola.error("Al jugador %s le quedan %d tiros\n".formatted(getJugadorTurno().getNombre(), nLanzamientos));
            return;
        }
          
        jugadores.get(turno).getAvatar().setnDoblesSeguidos();
        turno = (turno + 1) % jugadores.size();

        System.out.printf("Se ha cambiado el turno.\nAhora le toca a %s.\n", Consola.fmt(getJugadorTurno().getNombre(), Color.Azul));
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

    @Override
    public String toString() {
        return PintorTablero.pintarTablero(this);
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public void describirCasilla(String nombre) {
        for (Casilla c : casillas) {
            if (c.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println(c);
            }
        }
    }

    public void describirJugador(String nombre) {
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println(j);
            }
        }
    }

    public void describirAvatar(char id) {
        for (Jugador jugador : jugadores) {
            Avatar a = jugador.getAvatar();
            if (a.getId() == Character.toUpperCase(id)) {
                System.out.println(a);
                return;
            }
        }
    }

    public void comprar(String nombre) {
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

        j.comprar(c.getPropiedad());
    }

    public void cambiarModo() {
        getJugadorTurno().getAvatar().setMovimientoEspecial();
    }
}
