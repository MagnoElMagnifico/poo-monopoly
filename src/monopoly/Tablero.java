package monopoly;

import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.LectorCasillas;
import monopoly.utilidades.PintorTablero;
import monopoly.Calculadora;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Clase que representa el tablero del juego.
 * Contiene a los jugadores y a las casillas. Tira el dado y gestiona el turno actual.
 *
 * @see monopoly.Jugador
 * @see monopoly.Casilla
 */
public class Tablero {
    private final ArrayList<Jugador> jugadores;
    private Jugador banca;
    private Calculadora calc;
    private int turno;
    private ArrayList<Casilla> casillas;
    private final Dado dado;

    /**
     * Crea un tablero por defecto
     */
    public Tablero() {
        jugadores = new ArrayList<>();
        turno = 0;

        // En lugar de añadir con código las casillas, se leen de
        // un archivo de configuración.
        //
        // NOTA: Esto es potencialmente un problema de seguridad,
        // dado que el usuario puede modificarlo sin reparos.
        try {
            casillas = LectorCasillas.leerCasillas("casillas.txt");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        }

        banca=new Jugador();

        dado = new Dado();

        calc =new Calculadora(banca);
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
        jugadores.add(new Jugador(nombre, tipo, generarAvatarId(), casillas.get(0)));
        return """
                Jugador: %s
                Avatar: %s
                """.formatted(nombre,jugadores.get(jugadores.size()-1).getAvatar().getId());
    }

    /**
     * Obtiene el jugador de turno. Si no hay jugadores devuelve `Optional.empty()`
     */
    public Optional<Jugador> getJugadorTurno() {
        return jugadores.isEmpty() ? Optional.empty() : Optional.of(jugadores.get(turno));
    }

    /**
     * Mueve el jugador un determinado número de casillas
     */
    public String moverJugador(int nCasillas) {
        if (getJugadorTurno().isEmpty()) {
            return Formatear.con("No hay jugadores\n", Color.Rojo);
        }

        Avatar avatar = getJugadorTurno().get().getAvatar();
        Casilla actualCasilla = avatar.getCasilla();
        int nActual = casillas.indexOf(actualCasilla);
        // TODO: tener en cuenta el tipo de avatar
        Casilla nuevaCasilla = casillas.get((nActual + nCasillas) % casillas.size());

        avatar.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(avatar);
        actualCasilla.quitarAvatar(avatar);
        String s = calc.pagarAlquiler(nuevaCasilla.getPropiedad(), jugadores.get(turno));
        return """
               El jugador %s avanzó %d posiciones.
               Ahora se encuentra en %s.
               %s
               """.formatted(Formatear.con(avatar.getJugador().getNombre(), Color.Azul), nCasillas, Formatear.con(nuevaCasilla.getNombre(), Color.Cian),Formatear.con(s,Color.Azul));
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
        if (jugadores.isEmpty()) {
            return Formatear.con("No hay jugadores\n", Color.Rojo);
        }

        turno = (turno + 1) % jugadores.size();
        return """
                Se ha cambiado el turno.
                Ahora le toca a %s.
                """.formatted(Formatear.con(getJugadorTurno().orElseThrow().getNombre(), Formatear.Color.Azul));
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
    public ArrayList<Casilla> getEnVenta() {
        ArrayList<Casilla> enVenta = new ArrayList<>(casillas.size());

        for (Casilla casilla : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (casilla.getPropiedad().getPropietario()==banca) {
                enVenta.add(casilla);
            }
        }

        return enVenta;
    }

    @Override
    public String toString() {
        return PintorTablero.pintarTablero(casillas);
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public String describirCasilla(String nombre){
        Casilla c=new Casilla(nombre,1);
        if(!casillas.contains(c)) return "No existe la casilla\n";
        else {
            return Calculadora.valoresPropiedad(casillas.get(casillas.lastIndexOf(c)).getPropiedad());
        }
    }

}
