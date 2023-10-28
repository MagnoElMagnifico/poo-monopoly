package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
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
    private int nLanzamientos;
    private int nDoblesSeguidos;
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
        nLanzamientos = 1;
        nDoblesSeguidos = 0;
        jugando = false;

        banca = new Jugador();
        // En lugar de añadir con código las casillas, se leen de
        // un archivo de configuración.
        //
        // NOTA: Esto es potencialmente un problema de seguridad,
        // dado que el usuario puede modificarlo sin reparos.
        casillas = Lector.leerCasillas("casillas.txt");
        // Creación de la calculadora
        calculadora = new Calculadora(casillas, banca);
    }

    public String iniciar() {
        if (jugando) {
            return Formatear.error("La partida ya está iniciada\n");
        }

        if (jugadores.size() < 2) {
            return Formatear.error("No hay suficientes jugadores para empezar (mínimo 2)\n");
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
        Random rand = new Random();
        do {
            posibleId = (char) (rand.nextInt((int) 'Z' - (int) 'A' + 1) + (int) 'A');
        } while (!comprobarAvatarId(posibleId));
        return posibleId;
    }

    /**
     * Añade un jugador dado su nombre y tipo de avatar
     */
    public String anadirJugador(String nombre, Avatar.TipoAvatar tipo) {
        if (jugando) {
            return Formatear.error("No se pueden añadir jugadores en mitad de una partida\n");
        }

        if (jugadores.size() >= 6) {
            return Formatear.error("El máximo de jugadores es 6\n");
        }

        char avatar = generarAvatarId();
        jugadores.add(new Jugador(nombre, tipo, avatar, casillas.get(0), calculadora.calcularFortuna()));

        // @formatter:off
        return "El jugador %s con avatar %s se ha creado con éxito.\n"
                .formatted(Formatear.con(nombre, Color.Verde),
                           Formatear.con(Character.toString(avatar), Color.Verde));
        // @formatter:on
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
    public String moverJugador(Dado dado) {
        if (!jugando) {
            return Formatear.error("No se ha iniciado la partida\n");
        }

        if (nLanzamientos <= 0) {
            return Formatear.error("No se puede lanzar más veces. El jugador debe terminar su turno.\n");
        }
        nLanzamientos--;

        // TODO: tener en cuenta el tipo de avatar

        // Calcular la casilla siguiente
        Jugador jugador = getJugadorTurno();
        Avatar avatar = jugador.getAvatar();
        Casilla actualCasilla = avatar.getCasilla();

        String accionAdicional = "";
        if (avatar.isEstarEncerrado()) {
            avatar.seguirEnCarcel();

            if (dado.isDoble()) {
                accionAdicional += "Dados dobles! El jugador puede salir de %s\n".formatted(Formatear.casillaNombre(actualCasilla));
                avatar.salirCarcel();
            } else if (avatar.getEstanciasCarcel() >= 3) {
                // @formatter:off
                return """
                        %s con avatar %s no ha sacado dados dobles %s.
                        Ahora debe pagar obligatoriamente la fianza.
                        %s""".formatted(Formatear.con(jugador.getNombre(), Color.Azul),
                                        Formatear.con(Character.toString(avatar.getId()), Color.Azul),
                                        dado, salirCarcel());
                // @formatter:on
            } else {
                // @formatter:off
                return """
                        %s con avatar %s no ha sacado dados dobles %s.
                        Puede pagar la fianza o permanecer encerrado.
                        """.formatted(Formatear.con(jugador.getNombre(), Color.Azul),
                                      Formatear.con(Character.toString(avatar.getId()), Color.Azul),
                                      dado);
                // @formatter:on
            }
        } else if (dado.isDoble()) {
            accionAdicional += "Dados dobles! El jugador puede lanzar otra vez\n";
            nLanzamientos++;
            nDoblesSeguidos++;
            if (nDoblesSeguidos >= 3) {
                // @formatter:off
                return """
                        %s con avatar %s ha sacado %s.
                        Ya son 3 veces seguidas sacando dados dobles.
                        %s es arrestado por tener tanta suerte.
                        %s""".formatted(Formatear.con(jugador.getNombre(), Color.Azul),
                                        Formatear.con(Character.toString(avatar.getId()), Color.Azul),
                                        dado,
                                        jugador.getNombre(),
                                        irCarcel());
                // @formatter:on
            }
        }
        return accionAdicional + avatar.mover(casillas,dado,calculadora,jugadores,banca);
    }


    // TODO: accionCasilla de IrCarcel
    public String irCarcel() {
        Jugador jugador = getJugadorTurno();
        Avatar avatar = jugador.getAvatar();

        avatar.irCarcel();

        Casilla nuevaCasilla = casillas.get(casillas.indexOf(new Casilla(null, "Cárcel")));
        avatar.getCasilla().quitarAvatar(avatar);
        avatar.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(avatar);
        nLanzamientos = 0;

        return "El avatar se coloca en la Cárcel\n";
    }

    // TODO: accionCasilla de Carcel
    public String salirCarcel() {
        Jugador jugador = getJugadorTurno();
        Avatar avatar = jugador.getAvatar();

        if (!avatar.isEstarEncerrado()) {
            return Formatear.error("El jugador %s no está en la Cárcel".formatted(jugador.getNombre()));
        }

        long importe = avatar.getCasilla().getPrecio();
        avatar.salirCarcel();
        jugador.cobrar(importe);

        return "El jugador %s paga %s para salir de la cárcel\n".formatted(Formatear.con(jugador.getNombre(), Color.Azul), Formatear.num(importe));
    }

    /**
     * Termina el turno del jugador actual y calcula el siguiente
     */
    public String acabarTurno() {
        if (!jugando) {
            return Formatear.error("No se ha iniciado la partida\n");
        }

        if (nLanzamientos > 0) {
            return Formatear.error("Al jugador %s le quedan %d tiros\n".formatted(getJugadorTurno().getNombre(), nLanzamientos));
        }

        nDoblesSeguidos = 0;
        nLanzamientos = 1;
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

    public String describirCasilla(String nombre) {
        Casilla c = new Casilla(null, nombre);

        if (!casillas.contains(c)) {
            return Formatear.error("No es una casilla\n");
        }

        return casillas.get(casillas.indexOf(c)).toString() + '\n';
    }

    public String describirJugador(String nombre) {
        StringBuilder resultado = new StringBuilder();

        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombre)) {
                resultado.append(j);
            }
        }

        return resultado.isEmpty() ?
                Formatear.error("El jugador \"%s\" no existe\n".formatted(nombre)) :
                resultado.toString() + '\n';
    }

    public String describirAvatar(char id) {
        for (Jugador jugador : jugadores) {
            Avatar a = jugador.getAvatar();
            if (a.getId() == Character.toUpperCase(id)) {
                return a.toString() + '\n';
            }
        }

        return Formatear.error("No existe el avatar \"%s\"\n".formatted(id));
    }

    public String comprar(String nombre) {
        Casilla c = new Casilla(null, nombre);
        Jugador j = getJugadorTurno();

        if (!j.getAvatar().getCasilla().equals(c)) {
            return Formatear.error("No se puede comprar otra casilla que no sea la actual\n");
        }

        // Ahora nos referimos siempre a la casilla donde está el avatar
        c = j.getAvatar().getCasilla();

        if (!c.isPropiedad()) {
            return Formatear.error("No se puede comprar la casilla \"%s\"\n".formatted(c.getNombre()));
        }

        return calculadora.comprar(c.getPropiedad(), j);
    }

    public String cambiarModo(){
        getJugadorTurno().getAvatar().setMovimientoEspecial();
        return "A partir de ahora el %s, de tipo %s, se moverá de modo avanzado".formatted(Formatear.con(Character.toString(getJugadorTurno().getAvatar().getId()), Color.Azul),
                getJugadorTurno().getAvatar().getTipo());
    }

}
