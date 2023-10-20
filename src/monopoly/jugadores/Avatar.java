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
    /**
     * Determina si el avatar está en la Cárcel o no
     */
    private boolean estarEncerrado;
    /**
     * Número de turnos que se han pasado en la Cárcel
     */
    private int estanciasCarcel;
    private int vueltas;

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
        this.vueltas = 0;
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
        this.vueltas = 0;
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
        if (this == obj) {
            return true;
        }

        // Aquí se puede usar == dado que char es primitivo
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

    /**
     * Pone el Avatar en el estado encerrado
     */
    public void irCarcel() {
        estarEncerrado = true;
        estanciasCarcel = 0;
    }

    public int getEstanciasCarcel() {
        return estanciasCarcel;
    }

    /**
     * Se notifica al Avatar de que pasa otro turno en la Cárcel
     */
    public void seguirEnCarcel() {
        if (estarEncerrado) {
            estanciasCarcel++;
        }
        // TODO: Error, no se puede seguir en la Cárcel si no estabas dentro inicialmente
    }

    /**
     * Saca el Avatar del estado encerrado
     */
    public void salirCarcel() {
        estanciasCarcel = 0;
        estarEncerrado = false;
    }

    public boolean isEstarEncerrado() {
        return estarEncerrado;
    }

    public int getVueltas() {
        return vueltas;
    }

    public void anadirVuelta() {
        this.vueltas++;
    }

    public void resetVuelta() {
        this.vueltas = 0;
    }

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}
