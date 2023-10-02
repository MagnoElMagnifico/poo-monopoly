package monopoly;

/**
 * Clase que representa un Avatar. Esta es la parte del jugador que está en
 * el tablero, es decir, está en una casilla concreta.
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see monopoly.Jugador
 */
public class Avatar {
    /** Tipos de avatares posibles */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }

    private TipoAvatar tipo;
    private char id;
    private String casilla;
    private Jugador jugador;

    /** Crea un avatar dado su tipo, id y el jugador al que hace referencia */
    public Avatar(TipoAvatar tipo, char id, Jugador jugador) {
        this.tipo = tipo;
        this.id = id;
        this.casilla = "inicial";
        this.jugador = jugador;
    }

    /** Mueve el avatar un determinado número de casillas */
    public void mover(int nCasillas) {
        casilla = "no inicial";
    }

    @Override
    public String toString() {
        return """
                {
                    id: %c
                    tipo: %s,
                    casilla: %s
                    jugador: %s
                }
                """.formatted(id, tipo, casilla, jugador.getNombre());
    }

    public char getId() {
        return id;
    }

    public TipoAvatar getTipo() {
        return tipo;
    }

    public String getCasilla() {
        return casilla;
    }

    public Jugador getJugador() {
        return jugador;
    }
}
