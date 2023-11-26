package monopoly.jugadores;

import monopoly.Tablero;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Dado;

import java.util.ArrayList;

public abstract class Avatar {
    // @formatter:off
    // Propiedades
    private final char id;
    private final Jugador jugador;

    // Historial
    private final ArrayList<Casilla> historialCasillas;
    private Casilla casilla;

    // Estado
    private boolean encerrado;
    private int doblesSeguidos;
    private int lanzamientosRestantes; /* Si es 0 se debe terminar el turno */
    private int turnosEnCarcel;

    private boolean movimientoEspecial;

    public Avatar(char id, Jugador jugador, Casilla casillaInicial) {
        this.id = id;
        this.jugador = jugador;
        this.casilla = casillaInicial;
        casillaInicial.anadirAvatar(this);
        casillaInicial.getEstadisticas().anadirEstancia();
        this.historialCasillas = new ArrayList<>();
        this.encerrado = false;
        this.doblesSeguidos = 0;
        this.lanzamientosRestantes = 1;
        this.turnosEnCarcel = 0;
        this.movimientoEspecial=false;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // Aquí se puede usar == dado que char es primitivo
        return obj instanceof Avatar && ((Avatar) obj).getId() == this.id;
    }

    public abstract boolean mover(Dado dado,Tablero tablero);

    public abstract Casilla moverEspecial(Dado dado,Casilla carcel);

    private boolean irCarcelDadosDobles(Dado dado, Casilla carcel) {
        if (dado.isDoble()) {
            doblesSeguidos++;

            if (doblesSeguidos >= 3) {
                System.out.printf("""
                        Ya son 3 veces seguidas sacando dados dobles.
                        %s es arrestado por tener tanta suerte.
                        """, jugador.getNombre());
                irCarcel(carcel);
                return true;
            }

            lanzamientosRestantes++;
            System.out.println("Dados dobles! El jugador puede tirar otra vez");
        }

        return false;
    }

    /**
     * Realiza una tirada de dados cuando está en la cárcel
     */
    private void moverEstandoCarcel(Dado dado) {
        turnosEnCarcel++;

        if (dado.isDoble()) {
            System.out.println("Dados dobles! El jugador puede salir de la Cárcel");
            lanzamientosRestantes = 1;
            encerrado = false;
            turnosEnCarcel = 0;
        } else if (turnosEnCarcel >= 3) {
            System.out.printf("%s con avatar %s no ha sacado dados dobles.\nAhora debe pagar obligatoriamente la fianza.\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(id), Consola.Color.Azul));
            salirCarcelPagando(true);
        } else {
            System.out.printf("%s con avatar %s no ha sacado dados dobles.\nPuede pagar la fianza o permanecer encerrado.\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(id), Consola.Color.Azul));
        }
    }

    /**
     * Pone el Avatar en el estado encerrado y lo mueve a la cárcel
     */
    public void irCarcel(Casilla carcel) {
        if (carcel.getTipo() != Casilla.TipoCasilla.Carcel) {
            Consola.error("[Avatar] irCarcel() requiere la casilla de cárcel, recibió %s".formatted(carcel.getTipo()));
            return;
        }

        encerrado = true;
        turnosEnCarcel = 0;
        lanzamientosRestantes = 0;
        jugador.getEstadisticas().anadirEstanciaCarcel();

        casilla.quitarAvatar(this);
        casilla = carcel;
        carcel.anadirAvatar(this);

        historialCasillas.add(carcel);
        carcel.getEstadisticas().anadirEstancia();

        System.out.println("Por tanto, el avatar termina en la Cárcel");
    }

    /**
     * Saca el avatar de la cárcel pagando la fianza.
     *
     * @param obligado True si obligatoriamente debe salir.
     * @return True si la operación ha sido exitosa, false en otro caso.
     */
    public boolean salirCarcelPagando(boolean obligado) {
        if (!encerrado) {
            Consola.error("El jugador no está en la Cárcel");
            return false;
        }

        if (!jugador.cobrar(casilla.getFianza(), obligado)) {
            Consola.error("El jugador no tiene dinero suficiente para pagar la fianza");
            return false;
        }

        encerrado = false;
        turnosEnCarcel = 0;
        lanzamientosRestantes = 1;

        System.out.printf("El jugador %s paga %s para salir de la cárcel\n", jugador.getNombre(), Consola.num(casilla.getFianza()));
        return true;
    }

    public char getId() {
        return id;
    }

    public Casilla getCasilla() {
        return casilla;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public abstract void cambiarModo();

    public int getTurnosEnCarcel() {
        return turnosEnCarcel;
    }

    public boolean isEncerrado() {
        return encerrado;
    }

    /**
     * Devuelve el número de lanzamientos restantes. No confundir con EstadisticasJugador.nTiradas
     */
    public int getLanzamientosRestantes() {
        return lanzamientosRestantes;
    }

    public int getDoblesSeguidos() {
        return doblesSeguidos;
    }

    public boolean isMovimientoEspecial() {
        return movimientoEspecial;
    }

    public ArrayList<Casilla> getHistorialCasillas() {
        return historialCasillas;
    }

}
