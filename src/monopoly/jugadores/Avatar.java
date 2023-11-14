package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.Tablero;
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
    private int doblesSeguidos;
    private int lanzamientosEnTurno; /* Si es 0 se debe terminar el turno */
    private int turnosEnCarcel;

    // Estado movimiento especial
    private boolean movimientoEspecial;
    private boolean puedeComprar;       /* Solo para el coche: no permite comprar más una vez por turno */
    private int lanzamientosEspeciales; /* Solo para el coche: almacena los movimientos que quedan en modo especial */
    private int penalizacion;           /* Solo para el coche: no puede tirar en los dos siguientes turnos si saca < 4 */
    private boolean pelotaMovimiento;   /* Solo para la pelota: se comprueba si está en movimiento */
    private int casillasRestantes;      /* Solo para la pelota: almacena el dado del turno */
    private Dado pelotaDado;            /* Solo para la pelota: guarda el dado entre distintos movimientos */
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
        this.doblesSeguidos = 0;
        this.lanzamientosEnTurno = 1;
        this.turnosEnCarcel = 0;

        this.movimientoEspecial = false;
        this.puedeComprar = true;
        this.lanzamientosEspeciales = 0;
        this.penalizacion = 0;
        this.pelotaMovimiento = false;
        this.casillasRestantes = 0;
        this.pelotaDado = null;
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
    public boolean mover(Dado dado, Tablero tablero) {
        if (lanzamientosEnTurno <= 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return false;
        }

        lanzamientosEnTurno--;
        jugador.getEstadisticas().anadirTirada();

        if (encerrado) {
            moverEstandoCarcel(dado);
            return false;
        }

        if (penalizacion != 0) {
            penalizacion--;
            Consola.error("Restaurando avatar: espera para poder moverte");
            return false;
        }

        // Obtener la nueva casilla
        int posNuevaCasilla;
        if (movimientoEspecial) {
            posNuevaCasilla = switch (tipo) {
                case Coche -> moverEspecialCoche(dado, tablero.getCalculadora(), tablero.getCarcel(), tablero.getCasillas().size());
                case Pelota -> moverEspecialPelota(dado, tablero.getCalculadora(), tablero.getCarcel(), tablero.getCasillas().size());
                default -> -1; // No implementado
            };
        } else {
            posNuevaCasilla = moverBasico(dado, tablero.getCarcel());
        }

        // Los métodos anteriores devuelven -1 cuando ellos mismos
        // ya han movido el avatar; por tanto, no hay que hacer nada.
        if (posNuevaCasilla < 0) {
            return true;
        }

        // Comprobación de si pasa por la casilla de salida
        if (posNuevaCasilla >= tablero.getCasillas().size()) {
            posNuevaCasilla -= tablero.getCasillas().size();

            long abonoSalida = tablero.getCalculadora().calcularAbonoSalida();
            jugador.ingresar(abonoSalida);
            jugador.getEstadisticas().anadirAbonoSalida(abonoSalida);
            jugador.getEstadisticas().anadirVuelta();

            // Aumentar los precios en caso de que todos los avatares pasasen por la salida
            tablero.getCalculadora().aumentarPrecio(tablero.getCasillas(), tablero.getJugadores());

            System.out.printf("Como el avatar pasa por la casilla de Salida, %s recibe %s\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.num(tablero.getCalculadora().calcularAbonoSalida()));
        }

        Casilla anteriorCasilla = this.casilla;
        Casilla nuevaCasilla = tablero.getCasillas().get(posNuevaCasilla);

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        // Añadir la nueva casilla al historial
        historialCasillas.add(nuevaCasilla);
        nuevaCasilla.getEstadisticas().anadirEstancia();

        // Mostrar información
        if (movimientoEspecial && tipo == TipoAvatar.Pelota && pelotaDado != null) {
            System.out.printf("%s con avatar %s, avanza %s posiciones. Quedan %d.\nAvanza desde %s hasta %s.\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                    pelotaDado, casillasRestantes,
                    anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());

            if (casillasRestantes == 0) {
                pelotaDado = null;
            }
        } else {
            System.out.printf("%s con avatar %s, avanza %s posiciones.\nAvanza desde %s hasta %s.\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                    dado,
                    anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());
        }

        // Realizar la acción de la casilla
        if (movimientoEspecial && tipo == TipoAvatar.Pelota && pelotaDado != null) {
            nuevaCasilla.accion(jugador, pelotaDado);
        } else {
            nuevaCasilla.accion(jugador, dado);
        }

        return true;
    }

    private boolean dadosDoblesSeguidos(Dado dado, Casilla carcel) {
        if (doblesSeguidos >= 3) {
            System.out.printf("""
                            %s con avatar %s ha sacado %s.
                            Ya son 3 veces seguidas sacando dados dobles.
                            %s es arrestado por tener tanta suerte.
                            """,
                    Consola.fmt(jugador.getNombre(), Color.Azul),
                    Consola.fmt(Character.toString(id), Color.Azul),
                    dado, jugador.getNombre());
            irCarcel(carcel);
            return true;
        }

        return false;
    }

    /**
     * Función de ayuda que calcula la casilla siguiente cuando se usa el modo básico
     */
    private int moverBasico(Dado dado, Casilla carcel) {
        if (dado.isDoble()) {
            doblesSeguidos++;

            if (dadosDoblesSeguidos(dado, carcel)) {
                return -1;
            }

            lanzamientosEnTurno++;
            System.out.println("Dados dobles! El jugador puede tirar otra vez");
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
            salirCarcelPagando(true);
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
    public void irCarcel(Casilla carcel) {
        if (carcel.getTipo() != Casilla.TipoCasilla.Carcel) {
            Consola.error("[Avatar] irCarcel() requiere la casilla de cárcel, recibió %s".formatted(carcel.getTipo()));
            return;
        }

        encerrado = true;
        turnosEnCarcel = 0;
        lanzamientosEnTurno = 0;
        lanzamientosEspeciales = 0;
        jugador.getEstadisticas().anadirEstanciaCarcel();

        this.casilla.quitarAvatar(this);
        this.setCasilla(carcel);
        carcel.anadirAvatar(this);

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

        System.out.printf("El jugador %s paga %s para salir de la cárcel\n", jugador.getNombre(), Consola.num(casilla.getFianza()));
        return true;
    }

    private int moverEspecialCoche(Dado dado, Calculadora calculadora, Casilla carcel, int nCasillas) {
        // Primera tirada
        if (lanzamientosEnTurno == 0) {
            lanzamientosEnTurno = 2;
            lanzamientosEspeciales = 4;
        }

        // Lanzamos el error de que no quedan lanzamientos
        if (lanzamientosEspeciales == 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return -1;
        }

        // 4 tiradas especiales
        lanzamientosEspeciales--;

        // Ultima(s) tirada con comportamiento normal
        if (lanzamientosEspeciales == 0) {
            lanzamientosEnTurno = 0;
            lanzamientosEspeciales++;
            return moverBasico(dado, carcel);
        }

        lanzamientosEnTurno++;

        // Penalización por sacar menos de un 4
        if (dado.getValor() < 4) {
            lanzamientosEspeciales = 0;
            penalizacion = 2;
            lanzamientosEnTurno = 0;

            int cantidad = this.casilla.getPosicion() - dado.getValor();

            // Si la casilla calculada es negativa, quiere decir que se pasa
            // por la salida
            if (cantidad < 0) {
                cantidad += nCasillas;

                if (!getJugador().cobrar(calculadora.calcularAbonoSalida(), true)) {
                    Consola.error("No puedes devolver el abono de la cárcel.");
                    return -1;
                }

                System.out.printf(
                        "El jugador %s paga %s por retroceder por la casilla de salida.\n",
                        Consola.fmt(jugador.getNombre(), Color.Azul),
                        Consola.num(calculadora.calcularAbonoSalida()));
            }

            return cantidad;
        }

        // En caso de que dado >= 4, se mueve como siempre
        return this.casilla.getPosicion() + dado.getValor();
    }

    private int moverEspecialPelota(Dado dado, Calculadora calculadora, Casilla carcel, int nCasillas) {
        // Comportamieto normal
        if (!pelotaMovimiento) {
            if (dado.getValor() <= 4) {
                int cantidad = this.casilla.getPosicion() - dado.getValor();
                if (cantidad < 0) {
                    if (!getJugador().cobrar(calculadora.calcularAbonoSalida(), true)) {
                        Consola.error("No puedes devolver el abono de la carcel.");
                    } else {
                        System.out.printf(
                                "El jugador %s paga %s por retroceder por la casillas de salida.\n%n", Consola.fmt(jugador.getNombre(), Color.Azul),
                                Consola.num(calculadora.calcularAbonoSalida()));
                    }
                    cantidad += nCasillas;
                }
                return cantidad;
            }
            if (dado.getValor() == 5) return this.casilla.getPosicion() + dado.getValor();
        }

        // Hay que mover más de una vez. 1º vez
        if (lanzamientosEnTurno == 0) {
            lanzamientosEnTurno = 2;
            pelotaDado = dado;
            casillasRestantes = dado.getValor() - 5;
            pelotaMovimiento = true;
            return this.casilla.getPosicion() + 5;
        }

        // Resto de veces
        if (pelotaMovimiento) {
            // Ultima vez
            if (casillasRestantes <= 2) {
                lanzamientosEnTurno = 0;
                int i = casillasRestantes;
                casillasRestantes = 0;
                pelotaMovimiento = false;
                if (pelotaDado.isDoble()) {
                    lanzamientosEnTurno++;
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
                        irCarcel(carcel);
                        return -1;
                    }
                    System.out.println("Dados dobles! El jugador puede tirar otra vez");
                }
                return this.casilla.getPosicion() + i;
            }

            // Resto de veces
            casillasRestantes -= 2;
            lanzamientosEnTurno++;
            if (casillasRestantes == 0) {
                pelotaMovimiento = false;
                lanzamientosEnTurno = 0;
                if (pelotaDado.isDoble()) {
                    lanzamientosEnTurno++;
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
                        irCarcel(carcel);
                        return -1;
                    }
                    System.out.println("Dados dobles! El jugador puede tirar otra vez");
                }
            }
            return this.casilla.getPosicion() + 2;
        }
        // Salio algo mal
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
        if (lanzamientosEnTurno >= 2) {
            Consola.error("No puedes cambiar de modo en mitad de un movimiento");
            return;
        }

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

    /**
     * Devuelve el número de lanzamientos restantes. No confundir con EstadisticasJugador.nTiradas
     */
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

    public boolean isPelotaMovimiento() {
        return pelotaMovimiento;
    }

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}
