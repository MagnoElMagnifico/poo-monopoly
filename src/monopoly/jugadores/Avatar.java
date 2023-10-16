package monopoly.jugadores;

import monopoly.casillas.Casilla;

/**
 * Clase que representa un Avatar. Esta es la parte del jugador que está en
 * el tablero, es decir, está en una casilla concreta.
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see Jugador
 */
public class Avatar {
    private final TipoAvatar tipo;
    private final char id;
    private final Jugador jugador;
    private Casilla casilla;
    private int estanciasCarcel;
    private boolean estarEncerrado;

    /**
     * Crea un avatar dado su tipo, id y el jugador al que hace referencia
     */
    public Avatar(TipoAvatar tipo, char id, Jugador jugador, Casilla casillaInicial) {
        this.tipo = tipo;
        this.id = id;
        this.casilla = casillaInicial;
        casillaInicial.anadirAvatar(this);
        this.jugador = jugador;
        this.estanciasCarcel = 0;
        this.estarEncerrado = false;
    }

    /**
     * Crear un avatar temporal dado su ID. Útil para el comando `describir`.
     */
    public Avatar(char id) {
        this.tipo = null;
        this.id = id;
        this.casilla = null;
        this.jugador = null;
        this.estanciasCarcel = 0;
        this.estarEncerrado = false;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Avatar && ((Avatar) obj).getId() == this.id;
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

    public void irCarcel() {
        estarEncerrado = true;
        estanciasCarcel = 0;
    }

    public int getEstanciasCarcel() {
        return estanciasCarcel;
    }

    public void seguirEnCarcel() {
        if (estarEncerrado) {
            estanciasCarcel++;
        }
        // TODO: Error, no se puede seguir en la Cárcel si no estabas dentro inicialmente
    }

    public void salirCarcel() {
        estanciasCarcel = 0;
        estarEncerrado = false;
    }

    public boolean isEstarEncerrado() {
        return estarEncerrado;
    }

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}
