package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Dado;

import java.util.ArrayList;

/**
 * Clase que representa un Avatar. Esta es la parte del jugador que está en
 * el tablero, es decir, está en una casilla concreta.
 *
 * @author Marcos Granja Grille
 * @date 2-10-2023
 * @see Jugador
 */
public class Avatar {
    // Propiedades
    private final TipoAvatar tipo;
    private final char id;
    private final Jugador jugador;
    private Casilla casilla;

    // Estado
    /**
     * Determina si el avatar está en la Cárcel o no
     */
    private boolean encerrado;
    /**
     * Número de turnos que se han pasado en la Cárcel
     */
    private int estanciasCarcel;
    private int vueltas;
    private boolean movimientoEspecial;

    /**
     * Crea un avatar dado su tipo, id y el jugador al que hace referencia
     */
    public Avatar(TipoAvatar tipo, char id, Jugador jugador, Casilla casillaInicial) {
        this.tipo = tipo;
        this.id = id;
        this.jugador = jugador;
        this.casilla = casillaInicial;
        casillaInicial.anadirAvatar(this);

        this.encerrado = false;
        this.estanciasCarcel = 0;
        this.vueltas = 0;
        this.movimientoEspecial = false;
    }

    /**
     * Crear un avatar temporal dado su ID. Útil para el comando `describir`.
     */
    public Avatar(char id) {
        this.tipo = null;
        this.id = id;
        this.jugador = null;
        this.casilla = null;

        this.encerrado = false;
        this.estanciasCarcel = 0;
        this.vueltas = 0;
        this.movimientoEspecial = false;
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

    public void mover(Dado dado, ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores, Calculadora calculadora) {
        int nActual = this.casilla.getPosicion();

        if (movimientoEspecial) {
            moverEspecial();
            return;
        }

        int nNuevo = nActual + dado.getValor();

        if (nNuevo >= casillas.size()) {
            nNuevo -= casillas.size();

            this.anadirVuelta();
            jugador.ingresar(calculadora.calcularAbonoSalida());

            System.out.printf("Como el avatar pasa por la casilla de Salida, %s recibe %s\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.num(calculadora.calcularAbonoSalida()));
            Calculadora.aumentarPrecio(casillas, jugadores);
        }

        Casilla anteriorCasilla = this.casilla;
        Casilla nuevaCasilla = casillas.get(nNuevo);

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        System.out.printf("%s con avatar %s, avanza %s posiciones.\nAvanza desde %s hasta %s.\n",
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                dado,
                anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());

        nuevaCasilla.accion(jugador, dado);
    }

    private void moverEspecial() {
        // TODO
    }

    /**
     * Pone el Avatar en el estado encerrado y lo mueve a la cárcel
     */
    public void irCarcel() {
        encerrado = true;
        estanciasCarcel = 0;

        Casilla nuevaCasilla = this.casilla.getCarcel();
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        System.out.println("El avatar se coloca en la Cárcel");
    }

    public void salirCarcel() {
        if (!encerrado) {
            Consola.error("El jugador no está en la Cárcel");
            return;
        }

        if (!jugador.cobrar(casilla.getFianza())) {
            Consola.error("El jugador no tiene dinero suficiente para pagar la fianza");
            return;
        }

        encerrado = false;
        estanciasCarcel = 0;

        System.out.printf("El jugador %s paga %s para salir de la cárcel\n", jugador.getNombre(), Consola.num(casilla.getFianza()));
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

    public void setMovimientoEspecial() {
        if (movimientoEspecial) {
            movimientoEspecial = false;
            System.out.printf("%s regresa al modo de movimiento básico\n", Consola.fmt(jugador.getNombre(), Consola.Color.Azul));
        } else {
            movimientoEspecial = true;
            System.out.printf("A partir de ahora %s (%s), de tipo %s, se moverá de modo avanzado\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(id), Consola.Color.Azul),
                    tipo);
        }
    }

    public int getEstanciasCarcel() {
        return estanciasCarcel;
    }

    /**
     * Se notifica al Avatar de que pasa otro turno en la Cárcel
     */
    public void seguirEnCarcel() {
        if (!encerrado) {
            Consola.error("[Avatar] No está encerrado, entonces no puede seguir en la Cárcel");
            return;
        }

        estanciasCarcel++;
    }

    public boolean isEncerrado() {
        return encerrado;
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
