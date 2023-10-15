package monopoly;

/**
 * Clase que representa un Avatar. Esta es la parte del jugador que está en
 * el tablero, es decir, está en una casilla concreta.
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see monopoly.Jugador
 */
public class Avatar {
    private final TipoAvatar tipo;
    private final char id;
    private final Jugador jugador;
    private Casilla casilla;

    /**
     * Crea un avatar dado su tipo, id y el jugador al que hace referencia
     */
    public Avatar(TipoAvatar tipo, char id, Jugador jugador, Casilla casillaInicial) {
        this.tipo = tipo;
        this.id = id;
        this.casilla = casillaInicial;
        casillaInicial.anadirAvatar(this);
        this.jugador = jugador;
    }

    public Avatar(char id){
        this.tipo=null;
        this.id=id;
        this.jugador =null;
        this.casilla=null;
    }
    @Override
    public String toString() {
        return """
                {
                    id: %c
                    tipo: %s,
                    casilla: %s
                    jugador: %s
                }""".formatted(id, tipo, casilla.getNombre(), jugador.getNombre());
    }

    public boolean equals(Object obj) {
        return obj instanceof Avatar && String.valueOf(((Avatar) obj).getId()).equalsIgnoreCase(String.valueOf(this.getId()));

    }
    public char getId() {
        return id;
    }

    public TipoAvatar getTipo() {
        return tipo;
    }

    public Casilla getCasilla() {
        return casilla;
    }

    public void setCasilla(Casilla casilla) {
        this.casilla = casilla;
    }

    public Jugador getJugador() {
        return jugador;
    }

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}
