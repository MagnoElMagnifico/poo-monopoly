package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Propiedad;
import monopoly.jugador.Avatar;
import monopoly.jugador.Jugador;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.LectorCasillas;
import monopoly.utilidades.PintorTablero;

import java.util.ArrayList;

/**
 * Clase que representa el tablero del juego.
 * Contiene a los jugadores y a las casillas. Tira el dado y gestiona el turno actual.
 *
 * @see Jugador
 * @see Casilla
 */
public class Tablero {
    // Objetos de ayuda
    private final Dado dado;
    private final Calculadora calculadora;

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
     * True si ya se lanzaron dados en el turno actual: solo se puede lanzar una vez
     */
    private boolean yaLanzoDados;

    /**
     * Crea un tablero por defecto
     */
    public Tablero() {
        // Entre 2 y 6 jugadores
        jugadores = new ArrayList<>(6);
        turno = 0;
        jugando = false;
        yaLanzoDados = false;

        // En lugar de añadir con código las casillas, se leen de
        // un archivo de configuración.
        //
        // NOTA: Esto es potencialmente un problema de seguridad,
        // dado que el usuario puede modificarlo sin reparos.
        casillas = LectorCasillas.leerCasillas("casillas.txt");
        // Creación de la calculadora
        calculadora = new Calculadora(casillas);
        // Ahora hay que asignar los precios a cada casilla
        for (Casilla c : casillas) {
            c.setPrecio(calculadora.calcularPrecio(c));
        }

        // TODO: Agregar el jugador de la banca
        dado = new Dado();
    }

    public String iniciar() {
        if (jugando) {
            return Formatear.con("La partida ya está iniciada\n", Color.Rojo);
        }

        if (jugadores.size() < 2) {
            return Formatear.con("No hay suficientes jugadores para empezar (mínimo 2)\n", Color.Rojo);
        }

        jugando = true;
        return Formatear.con("Se ha iniciado la partida.\nA JUGAR!\n", Color.Verde);
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
        do {
            posibleId = dado.letraAleatoria();
        } while (!comprobarAvatarId(posibleId));
        return posibleId;
    }

    /**
     * Añade un jugador dado su nombre y tipo de avatar
     */
    public String anadirJugador(String nombre, Avatar.TipoAvatar tipo) {
        if (jugando) {
            return Formatear.con("No se pueden añadir jugadores en mitad de una partida\n", Color.Rojo);
        }

        if (jugadores.size() >= 6) {
            return Formatear.con("El máximo de jugadores es 6\n", Color.Rojo);
        }

        char avatar = generarAvatarId();
        jugadores.add(new Jugador(nombre, tipo, avatar, casillas.get(0)));

        return "El jugador %s con avatar %s se ha creado con éxito.\n"
                .formatted(Formatear.con(nombre, Color.Verde), Formatear.con(Character.toString(avatar), Color.Verde));
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
    public String moverJugador(int nCasillas) {
        if (!jugando) {
            return Formatear.con("No se ha iniciado la partida\n", Color.Rojo);
        }

        if (yaLanzoDados) {
            return Formatear.con("No se puede lanzar dos veces en el mismo turno\n", Color.Rojo);
        }
        yaLanzoDados = true;

        // TODO: tener en cuenta el tipo de avatar
        // TODO: cobrar el alquiler y otras acciones de casilla

        // Calcular la casilla siguiente
        Avatar avatar = getJugadorTurno().getAvatar();
        Casilla actualCasilla = avatar.getCasilla();
        int nActual = casillas.indexOf(actualCasilla);
        Casilla nuevaCasilla = casillas.get((nActual + nCasillas) % casillas.size());

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        avatar.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(avatar);
        actualCasilla.quitarAvatar(avatar);

        return """
                %s con avatar %s, avanza %d posiciones.
                Avanza desde %s hasta %s.
                %s
                """.formatted(Formatear.con(avatar.getJugador().getNombre(), Color.Azul),
                Formatear.con(Character.toString(avatar.getId()), Color.Azul),
                nCasillas,
                Formatear.casillaNombre(actualCasilla),
                Formatear.casillaNombre(nuevaCasilla),
                accionCasilla(actualCasilla));
    }

    public String accionCasilla(Casilla casilla) {
        // Si es una propiedad y tiene dueño, se debe cobrar un alquiler
        if (casilla.isPropiedad() && casilla.getPropiedad().getPropietario() != null) {
            int cantidad = 10; // TODO: calcular cantidad
            getJugadorTurno().cobrar(cantidad);
            return "Se ha cobrado";
        }
        return "TODO";
    }

    /**
     * Lanza 2 dados y mueve el jugador con el turno actual a la casilla que le toca
     */
    public String lanzarDados() {
        return moverJugador(dado.lanzar2Dados());
    }

    /**
     * Termina el turno del jugador actual y calcula el siguiente
     */
    public String acabarTurno() {
        if (!jugando) {
            return Formatear.con("No se ha iniciado la partida\n", Color.Rojo);
        }

        yaLanzoDados = false;
        turno = (turno + 1) % jugadores.size();
        return """
                Se ha cambiado el turno.
                Ahora le toca a %s.
                """.formatted(Formatear.con(getJugadorTurno().getNombre(), Formatear.Color.Azul));
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
            if (casilla.isPropiedad() && casilla.getPropiedad().getPropietario() == null) {
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

}
