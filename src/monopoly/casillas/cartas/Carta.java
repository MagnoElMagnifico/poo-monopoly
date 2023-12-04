
import monopoly.Tablero;
import monopoly.jugadores.Jugador;



/**
 * La clase abstracta Carta representa una carta en el juego de Monopoly.
 * Contiene información sobre el identificador, descripción, tablero y jugador asociados a la carta.
 */
public abstract class Carta {
    private int id;
    private String descripcion;
    private Tablero tablero;
    private Jugador jugador;

    /**
     * Constructor de la clase Carta.
     * 
     * @param tablero el tablero del juego
     * @param id el identificador de la carta
     * @param descripcion la descripción de la carta
     * @param jugador el jugador asociado a la carta
     */
    public Carta(Tablero tablero, int id, String descripcion, Jugador jugador) {
        this.id = id;
        this.descripcion = descripcion;
        this.tablero = tablero;
        this.jugador = jugador;
    }

    /**
     * Constructor de la clase Carta.
     * 
     * @param id el identificador de la carta
     */
    public Carta(int id) {
        this.id = id;
    }

    /**
     * Método abstracto que define la acción de la carta.
     */
    public abstract void accion();

    /**
     * Devuelve una representación en cadena de la carta.
     * 
     * @return la representación en cadena de la carta
     */
    //@Override
    /*public String toString() {
        // TODO?: Mejorar presentación de la carta
        return "%s: %s\n".formatted(tipo == TipoCarta.Suerte ? "Carta de Suerte" : "Carta de Comunidad", descripcion);
    }*/

    /**
     * Compara si la carta es igual a otro objeto.
     * 
     * @param obj el objeto a comparar
     * @return true si la carta es igual al objeto, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Carta carta)) {
            return false;
        }

        return carta.id == this.id;
    }

    /**
     * Devuelve el identificador de la carta.
     * 
     * @return el identificador de la carta
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve la descripción de la carta.
     * 
     * @return la descripción de la carta
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Devuelve el tablero del juego.
     * 
     * @return el tablero del juego
     */
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Devuelve el jugador asociado a la carta.
     * 
     * @return el jugador asociado a la carta
     */
    public Jugador getJugador() {
        return jugador;
    }

    /**
     * Establece la descripción de la carta.
     * 
     * @param descripcion la descripción de la carta
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Establece el tablero del juego.
     * 
     * @param tablero el tablero del juego
     */
    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    /**
     * Establece el jugador asociado a la carta.
     * 
     * @param jugador el jugador asociado a la carta
     */
    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }
}
