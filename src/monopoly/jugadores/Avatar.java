package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Consola.Color;
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
    // @formatter:off
    // Propiedades
    private final TipoAvatar tipo;
    private final char id;
    private final Jugador jugador;
    // Historial
    private final ArrayList<Casilla> historialCasillas;
    private Casilla casilla;
    // Estado
    private boolean encerrado;
    private boolean movimientoEspecial;
    private boolean pelotaMovimiento;
    private boolean puedeComprar;
    private int dadoEspera;
    private int doblesSeguidos;
    private int turnosEnCarcel;
    private int lanzamientosEnTurno;
    private int lanzamientosEspeciales;
    private int penalizacion;
    // @formatter:on

    /**
     * Crea un avatar dado su tipo, id y el jugador al que hace referencia
     */
    public Avatar(TipoAvatar tipo, char id, Jugador jugador, Casilla casillaInicial) {
        this.tipo = tipo;
        this.id = id;
        this.jugador = jugador;
        this.casilla = casillaInicial;
        casillaInicial.anadirAvatar(this);
        casillaInicial.getEstadisticas().anadirEstancia();

        this.historialCasillas = new ArrayList<>();

        this.encerrado = false;
        this.movimientoEspecial = false;
        this.pelotaMovimiento = false;
        this.puedeComprar = true;

        this.dadoEspera = 0;
        this.doblesSeguidos = 0;
        this.turnosEnCarcel = 0;
        this.lanzamientosEnTurno = 1;
        this.lanzamientosEspeciales = 0;
        this.penalizacion = 0;
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

    /**
     * Mueve el avatar las posiciones que indique el dado y según su estado (movimiento básico o avanzado)
     *
     * @return True si se ha movido con éxito, false si ha habido un error.
     */
    public boolean mover(Dado dado, ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores, Calculadora calculadora) {
        if (lanzamientosEnTurno <= 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return false;
        }

        lanzamientosEnTurno--;
        jugador.getEstadisticas().anadirTirada();

        if (penalizacion != 0) {
            penalizacion--;
            Consola.error("Restaurando avatar: espera para poder moverte");
            return false;
        }

        if (encerrado) {
            moverEstandoCarcel(dado);
            return false;
        }

        // Obtener la nueva casilla
        int posNuevaCasilla;
        if (movimientoEspecial) {
            posNuevaCasilla = switch (tipo) {
                case Coche -> moverEspecialCoche(dado, casillas.size());
                case Esfinge -> moverEspecialEsfinge();
                case Sombrero -> moverEspecialSombrero();
                case Pelota -> moverEspecialPelota(dado, casillas.size());
            };
        } else {
            posNuevaCasilla = moverBasico(dado);
        }

        // Los métodos anteriores devuelven -1 cuando ellos mismos
        // ya han movido el avatar; por tanto, no hay que hacer nada.
        if (posNuevaCasilla <= 0) {
            return true;
        }

        // Comprobación de si pasa por la casilla de salida
        if (posNuevaCasilla >= casillas.size()) {
            posNuevaCasilla -= casillas.size();

            long abonoSalida = calculadora.calcularAbonoSalida();
            jugador.ingresar(abonoSalida);
            jugador.getEstadisticas().anadirAbonoSalida(abonoSalida);
            jugador.getEstadisticas().anadirVuelta();

            // Aumentar los precios en caso de que el avatar pasase por la salida
            calculadora.aumentarPrecio(casillas, jugadores);

            System.out.printf("Como el avatar pasa por la casilla de Salida, %s recibe %s\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.num(calculadora.calcularAbonoSalida()));
        }

        Casilla anteriorCasilla = this.casilla;
        Casilla nuevaCasilla = casillas.get(posNuevaCasilla);

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        // Añadir la nueva casilla al historial
        historialCasillas.add(nuevaCasilla);
        nuevaCasilla.getEstadisticas().anadirEstancia();

        // Mostrar información
        System.out.printf("%s con avatar %s, avanza %s posiciones.\nAvanza desde %s hasta %s.\n",
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                Consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                dado,
                anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());

        // Aumentar los precios en caso de que el avatar pasase por la salida
        calculadora.aumentarPrecio(casillas, jugadores);

        // Realizar la acción de la casilla
        nuevaCasilla.accion(jugador, dado);
        return true;
    }

    /**
     * Función de ayuda que calcula la casilla siguiente cuando se usa el modo básico
     */
    private int moverBasico(Dado dado) {
        if (dado.isDoble()) {
            doblesSeguidos++;

            if (doblesSeguidos >= 3) {
                System.out.printf("""
                                %s con avatar %s ha sacado %s.
                                Ya son 3 veces seguidas sacando dados dobles.
                                %s es arrestado por tener tanta suerte.
                                """,
                        Consola.fmt(jugador.getNombre(), Color.Azul),
                        Consola.fmt(Character.toString(id), Color.Azul),
                        dado, jugador.getNombre());
                irCarcel(); // TODO: no funciona: NullPointer porque cárcel no está definido para todas las casillas
                return -1;
            } else {
                lanzamientosEnTurno++;
                System.out.println("Dados dobles! El jugador puede tirar otra vez");
            }
        }

        return this.casilla.getPosicion() + dado.getValor();
    }

    /**
     * Realiza una tirada de dados cuando está en la cárcel
     */
    private void moverEstandoCarcel(Dado dado) {
        turnosEnCarcel++;

        if (dado.isDoble()) {
            System.out.println("Dados dobles! El jugador puede salir de la Cárcel");
            lanzamientosEnTurno++;
            encerrado = false;
            turnosEnCarcel = 0;
        } else if (turnosEnCarcel >= 3) {
            System.out.printf("%s con avatar %s no ha sacado dados dobles %s.\nAhora debe pagar obligatoriamente la fianza.\n",
                    Consola.fmt(jugador.getNombre(), Color.Azul),
                    Consola.fmt(Character.toString(id), Color.Azul),
                    dado);
            salirCarcelPagando();
        } else {
            System.out.printf("%s con avatar %s no ha sacado dados dobles %s.\nPuede pagar la fianza o permanecer encerrado.\n",
                    Consola.fmt(jugador.getNombre(), Color.Azul),
                    Consola.fmt(Character.toString(id), Color.Azul),
                    dado);
        }
    }

    /**
     * Pone el Avatar en el estado encerrado y lo mueve a la cárcel
     */
    public void irCarcel() {
        encerrado = true;
        turnosEnCarcel = 0;
        lanzamientosEnTurno = 0;
        lanzamientosEspeciales = 0;
        jugador.getEstadisticas().anadirEstanciaCarcel();

        Casilla nuevaCasilla = this.casilla.getCarcel();
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        System.out.println("Por tanto, el avatar termina en la Cárcel");
    }

    /**
     * Saca el avatar de la cárcel pagando la fianza.
     *
     * @return True si la operación ha sido exitosa, false en otro caso.
     */
    public boolean salirCarcelPagando() {
        if (!encerrado) {
            Consola.error("El jugador no está en la Cárcel");
            return false;
        }

        if (!jugador.cobrar(casilla.getFianza())) {
            Consola.error("El jugador no tiene dinero suficiente para pagar la fianza");
            return false;
        }

        encerrado = false;
        turnosEnCarcel = 0;

        System.out.printf("El jugador %s paga %s para salir de la cárcel\n", jugador.getNombre(), Consola.num(casilla.getFianza()));
        return true;
    }

    private int moverEspecialCoche(Dado dado, int nCasillas) {
        if (lanzamientosEnTurno == 0) {
            lanzamientosEnTurno = 2;
            lanzamientosEspeciales = 4;
        }

        if (lanzamientosEspeciales == 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return -1;
        }

        lanzamientosEspeciales--;

        if (dado.getValor() < 4) {
            lanzamientosEspeciales = 0;
            penalizacion = 2;
            lanzamientosEnTurno = 0;
            return (this.casilla.getPosicion() - dado.getValor() + nCasillas) % nCasillas;
        }

        lanzamientosEnTurno++;

        if (lanzamientosEspeciales == 0) {
            lanzamientosEnTurno = 0;
            if (dado.isDoble()) {
                doblesSeguidos++;
                if (doblesSeguidos >= 3) {
                    System.out.printf("""
                                    %s con avatar %s ha sacado %s.
                                    Ya son 3 veces seguidas sacando dados dobles.
                                    %s es arrestado por tener tanta suerte.
                                    """,
                            Consola.fmt(jugador.getNombre(), Color.Azul),
                            Consola.fmt(Character.toString(id), Color.Azul),
                            dado, jugador.getNombre());
                    irCarcel();
                    return -1;
                }
                lanzamientosEspeciales++;
                lanzamientosEnTurno++;
            }
            return this.casilla.getPosicion() + dado.getValor();
        }

        return this.casilla.getPosicion() + dado.getValor();
    }

    private int moverEspecialEsfinge() {
        // TODO
        return -1;
    }

    private int moverEspecialSombrero() {
        // TODO
        return -1;
    }

    private int moverEspecialPelota(Dado dado, int nCasillas) {
        if (!pelotaMovimiento) {
            if (dado.getValor() <= 4) return (this.casilla.getPosicion() - dado.getValor() + nCasillas) % nCasillas;
            if (dado.getValor() == 5) return this.casilla.getPosicion() + dado.getValor();
        }

        if (lanzamientosEnTurno == 0) {
            lanzamientosEnTurno = 2;
            dadoEspera = dado.getValor() - 5;
            pelotaMovimiento = true;
            return this.casilla.getPosicion() + 5;
        }

        if (pelotaMovimiento) {
            if (dadoEspera <= 2) {
                lanzamientosEnTurno = 0;
                pelotaMovimiento = false;
                return this.casilla.getPosicion() + dadoEspera;
            }
            lanzamientosEnTurno++;
            dadoEspera -= 2;
            return this.casilla.getPosicion() + 2;
        }

        return -1;
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

    public void cambiarModo() {
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

    public int getTurnosEnCarcel() {
        return turnosEnCarcel;
    }

    public boolean isEncerrado() {
        return encerrado;
    }

    /** Devuelve el número de lanzamientos restantes. No confundir con EstadisticasJugador.nTiradas */
    public int getLanzamientosEnTurno() {
        return lanzamientosEnTurno;
    }

    public void resetLanzamientos() {
        lanzamientosEnTurno = 1;
    }

    public int getDoblesSeguidos() {
        return doblesSeguidos;
    }

    public void resetDoblesSeguidos() {
        doblesSeguidos = 0;
    }

    public boolean isMovimientoEspecial() {
        return movimientoEspecial;
    }

    public ArrayList<Casilla> getHistorialCasillas() {
        return historialCasillas;
    }

    public boolean isPuedeComprar() {
        return puedeComprar;
    }

    public void setPuedeComprar(boolean puedeComprar) {
        this.puedeComprar = puedeComprar;
    }

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}
