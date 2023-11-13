package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Edificio;
import monopoly.casillas.Grupo;
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
    private final ArrayList<Grupo> grupos;
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

        if (getJugadorTurno().isEndeudado()) {
            Consola.error("Estas endeudado paga la deuda o declárate en bancarrota");
            return;
        }


        // Muestra el tablero si se ha movido el avatar con éxito
        if (getJugadorTurno().getAvatar().mover(dado, casillas, jugadores, calculadora)) {
            System.out.print(this);
        }
    }

    public void moverAvatar(){
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }
        if(getJugadorTurno().getAvatar().getTipo()!=Avatar.TipoAvatar.Pelota){
            Consola.error("No puedes usar este comando no eres una pelota.");
            return;
        }
        if(!getJugadorTurno().getAvatar().isMovimientoEspecial() || !getJugadorTurno().getAvatar().isPelotaMovimiento()){
            Consola.error("No puedes usar esto ahora vuelve más tarde.");
            return;
        }
        if (getJugadorTurno().isEndeudado()) {
            Consola.error("Estas endeudado paga la deuda o declárate en bancarrota");
            return;
        }
        if (getJugadorTurno().getAvatar().mover(null, casillas, jugadores, calculadora)) {
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
        if (jugadorTurno.getAvatar().getTipo() == Avatar.TipoAvatar.Pelota && jugadorTurno.getAvatar().getLanzamientos() > 0) {
            Consola.error("Al jugador %s le quedan  tiros".formatted(jugadorTurno.getNombre()));
            return;
        }
        if (jugadorTurno.isEndeudado()) {
            Consola.error("El jugador %s está enduedado paga la deuda o declárate en bancarrota".formatted(jugadorTurno.getNombre()));
            return;
        }


        avatarTurno.resetDoblesSeguidos();
        avatarTurno.resetLanzamientos();
        avatarTurno.setPuedeComprar(true);

        turno = (turno + 1) % jugadores.size();

        System.out.printf("Se ha cambiado el turno.\nAhora le toca a %s.\n", Consola.fmt(getJugadorTurno().getNombre(), Color.Azul));
        System.out.print(this);
        jugadorTurno.describirTransaccion();
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
            j.describirTransaccion();
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
        // Especifico para el coche que solo puede comprar una vez por turno en modo especial
        if (!j.getAvatar().isPuedeComprar()) {

            Consola.error("No se puede comprar la casilla \"%s\"".formatted(c.getNombre()));
            return;
        }

        if (j.comprar(c.getPropiedad())) {
            if (j.getAvatar().isMovimientoEspecial() && j.getAvatar().getTipo() == Avatar.TipoAvatar.Pelota) {
                j.getAvatar().setPuedeComprar(false);
            }
            j.describirTransaccion();
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

        if (getJugadorTurno().comprar(tipoEdificio, cantidad)) {
            getJugadorTurno().describirTransaccion();
        }
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
            }
        }

        if (solar == null) {
            Consola.error("No existe el solar \"%s\"".formatted(nombreSolar));
            return;
        }

        if (getJugadorTurno().vender(tipoEdificio, solar, cantidad)) {
            getJugadorTurno().describirTransaccion();
        }
    }

    public void hipotecar(String nombre) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador j = getJugadorTurno();
        for (Casilla c : casillas) {
            if (c.getNombre().equalsIgnoreCase(nombre) && c.isPropiedad()) {
                j.hipotecar(c.getPropiedad());
            }
        }
    }

    public void deshipotecar(String nombre) {
        if (!jugando) {
            Consola.error("No se ha iniciado la partida");
            return;
        }

        Jugador j = getJugadorTurno();
        for (Casilla c : casillas) {
            if (c.getNombre().equalsIgnoreCase(nombre) && c.isPropiedad()) {
                j.deshipotecar(c.getPropiedad());
            }
        }
    }

    public void pagarDeuda() {
        Jugador j = getJugadorTurno();
        Avatar a = j.getAvatar();
        j.pagarDeuda(banca);
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

        Jugador j = getJugadorTurno();
        if (j.setBancarrota(banca)) {
            System.out.printf("El jugador: %s se declara en bancarrota\n%n", Consola.fmt(j.getNombre(), Color.Azul));
            jugadores.remove(j);
            turno--;
            if (turno < 0) turno = jugadores.size() - 1;
        }
        if (jugadores.size() == 1) {
            j = jugadores.get(0);
            System.out.println(Consola.fmt("Felicidades %s, has ganado la partida".formatted(j.getNombre()), Color.Amarillo));
            jugando = false;
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

        // TODO: información sobre los edificios aún edificables

    }


}
