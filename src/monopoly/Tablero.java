package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;
import monopoly.utilidades.LectorCasillas;
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
        casillas = LectorCasillas.leerCasillas("casillas.txt");
        // Creación de la calculadora
        calculadora = new Calculadora(casillas, banca);
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
            return Formatear.con("No se pueden añadir jugadores en mitad de una partida\n", Color.Rojo);
        }

        if (jugadores.size() >= 6) {
            return Formatear.con("El máximo de jugadores es 6\n", Color.Rojo);
        }

        char avatar = generarAvatarId();
        jugadores.add(new Jugador(nombre, tipo, avatar, casillas.get(0), calculadora.calcularFortuna()));

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
    public String moverJugador(Dado dado) {
        if (!jugando) {
            return Formatear.con("No se ha iniciado la partida\n", Color.Rojo);
        }

        if (nLanzamientos <= 0) {
            return Formatear.con("No se puede lanzar más veces. El jugador debe terminar su turno.\n", Color.Rojo);
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
                return """
                        %s con avatar %s no ha sacado dados dobles %s.
                        Ahora debe pagar obligatoriamente la fianza.
                        %s""".formatted(Formatear.con(jugador.getNombre(), Color.Azul), Formatear.con(Character.toString(avatar.getId()), Color.Azul), dado, salirCarcel());
            } else {
                return """
                        %s con avatar %s no ha sacado dados dobles %s.
                        Puede pagar la fianza o permanecer encerrado.
                        """.formatted(Formatear.con(jugador.getNombre(), Color.Azul), Formatear.con(Character.toString(avatar.getId()), Color.Azul), dado);
            }
        } else if (dado.isDoble()) {
            accionAdicional += "Dados dobles! El jugador puede lanzar otra vez\n";
            nLanzamientos++;
            nDoblesSeguidos++;
            if (nDoblesSeguidos >= 3) {
                return """
                        %s con avatar %s ha sacado %s.
                        Ya son 3 veces seguidas sacando dados dobles.
                        %s es arrestado por tener tanta suerte.
                        %s""".formatted(Formatear.con(jugador.getNombre(), Color.Azul), Formatear.con(Character.toString(avatar.getId()), Color.Azul), dado, jugador.getNombre(), irCarcel());
            }
        }

        int nActual = casillas.indexOf(actualCasilla);

        int nNuevo = nActual + dado.getValor();
        if (nNuevo >= casillas.size()) {
            nNuevo -= casillas.size();

            accionAdicional += "Como el avatar pasa por la casilla de Salida, %s recibe %s\n"
                    .formatted(Formatear.con(jugador.getNombre(), Color.Azul), Formatear.num(calculadora.calcularAbonoSalida()));
            jugador.ingresar(calculadora.calcularAbonoSalida());
        }

        Casilla nuevaCasilla = casillas.get(nNuevo);

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        avatar.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(avatar);
        actualCasilla.quitarAvatar(avatar);

        return """
                %s con avatar %s, avanza %s posiciones.
                Avanza desde %s hasta %s.
                %s%s""".formatted(Formatear.con(avatar.getJugador().getNombre(), Color.Azul),
                Formatear.con(Character.toString(avatar.getId()), Color.Azul),
                dado,
                Formatear.casillaNombre(actualCasilla),
                Formatear.casillaNombre(nuevaCasilla),
                accionCasilla(nuevaCasilla, dado), accionAdicional);
    }

    public String accionCasilla(Casilla casilla, Dado dado) {
        if (casilla.isPropiedad()) {
            return calculadora.pagarAlquiler(casilla.getPropiedad(), getJugadorTurno(), dado);
        }

        return switch (casilla.getNombre()) {
            case "IrCárcel" -> irCarcel();
            case "Comunidad" -> "* Acción de Carta de Comunidad *\n";
            case "Suerte" -> "* Acción de Carta de Suerte *\n";
            case "Parking" -> {
                Jugador jugador = getJugadorTurno();
                long bote = banca.getFortuna();
                jugador.ingresar(bote);
                banca.cobrar(bote); // Poner a 0 el bote
                yield "El jugador recibe el bote de la banca: %s\n".formatted(Formatear.num(bote));

            }
            case "Impuesto1", "Impuesto2" -> {
                Jugador jugador = getJugadorTurno();
                long importe = casilla.getPrecio();

                if (importe > jugador.getFortuna()) {
                    yield Formatear.con("El jugador no tiene suficientes fondos para pagar el alquiler\n", Color.Rojo);
                }

                jugador.cobrar(importe);
                banca.ingresar(importe);

                yield "Se han pagado %s de impuestos a la banca.\n".formatted(Formatear.num(importe));
            }
            case "Cárcel" -> "El jugador está solo de visita.\n";
            default -> "";
        };
    }

    public String irCarcel() {
        Jugador jugador = getJugadorTurno();
        Avatar avatar = jugador.getAvatar();

        avatar.irCarcel();

        Casilla nuevaCasilla = casillas.get(casillas.indexOf(new Casilla(null, "Cárcel")));
        avatar.getCasilla().quitarAvatar(avatar);
        avatar.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(avatar);
        nLanzamientos=0;

        return "El avatar se coloca en la Cárcel\n";
    }

    public String salirCarcel() {
        Jugador jugador = getJugadorTurno();
        Avatar avatar = jugador.getAvatar();

        if (!avatar.isEstarEncerrado()) {
            return Formatear.con("El jugador %s no está en la Cárcel".formatted(jugador.getNombre()), Color.Rojo);
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
            return Formatear.con("No se ha iniciado la partida\n", Color.Rojo);
        }

        if (nLanzamientos > 0) {
            return Formatear.con("Al jugador %s le quedan %d tiros\n".formatted(getJugadorTurno().getNombre(), nLanzamientos), Color.Rojo);
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
            return Formatear.con("No es una casilla\n", Color.Rojo);
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
                Formatear.con("El jugador \"%s\" no existe\n".formatted(nombre), Color.Rojo) :
                resultado.toString() + '\n';
    }

    public String describirAvatar(char id) {
        for (Jugador jugador : jugadores) {
            Avatar a = jugador.getAvatar();
            if (a.getId() == Character.toUpperCase(id)) {
                return a.toString() + '\n';
            }
        }

        return Formatear.con("No existe el avatar \"%s\"\n".formatted(id), Color.Rojo);
    }

    public String comprar(String nombre) {
        Casilla c = new Casilla(null, nombre);
        Jugador j = getJugadorTurno();

        if (!j.getAvatar().getCasilla().equals(c)) {
            return Formatear.con("No se puede comprar otra casilla que no sea la actual\n", Color.Rojo);
        }

        // Ahora nos referimos siempre a la casilla donde está el avatar
        c = j.getAvatar().getCasilla();

        if (!c.isPropiedad()) {
            return Formatear.con("No se puede comprar la casilla \"%s\"\n".formatted(c.getNombre()), Color.Rojo);
        }

        return calculadora.comprar(c.getPropiedad(), j);
    }
}
