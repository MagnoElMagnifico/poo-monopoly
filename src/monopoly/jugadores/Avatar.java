package monopoly.jugadores;

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
    private int lanzamientosRestantes; /* Si es 0 se debe terminar el turno */
    private int turnosEnCarcel;

    // Estado movimiento especial
    private boolean movimientoEspecial;
    private boolean puedeComprar;       /* Solo para el coche: no permite comprar más una vez por turno */
    private int penalizacion;           /* Solo para el coche: no puede tirar en los dos siguientes turnos si saca < 4 */
    private Dado pelotaDado;            /* Solo para la pelota: guarda el dado usado en el tiro inicial (solo para calcular el alquiler de los transportes) */
    private int pelotaPosFinal;         /* Solo para la pelota: guarda la posición final a la que se tiene que llegar */
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
        this.lanzamientosRestantes = 1;
        this.turnosEnCarcel = 0;

        this.movimientoEspecial = false;
        this.puedeComprar = true;
        this.penalizacion = 0;
        this.pelotaDado = null;
        this.pelotaPosFinal = 0;
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
        if (lanzamientosRestantes <= 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return false;
        }

        if (dado != null && pelotaDado != null) {
            Consola.error("No puedes lanzar más dados. Prueba con el comando siguiente");
            return false;
        }

        if (dado == null && encerrado) {
            Consola.error("No puedes usar el comando siguiente cuando estás encerrado");
            return false;
        }

        if (dado == null && (!movimientoEspecial || tipo != TipoAvatar.Pelota)) {
            Consola.error("No puedes usar el comando siguiente si no estás usando el avatar Pelota");
            return false;
        }

        if (dado == null && doblesSeguidos != 0) {
            Consola.error("No puedes usar el comando siguiente después de obtener dados dobles. Prueba con lanzar.");
            return false;
        }

        // Penalización de turnos para el coche
        // TODO: probar penalización + IrCarcel
        if (penalizacion != 0) {
            Consola.error("Restaurando avatar: espera %d turno(s) para poder moverte".formatted(penalizacion));
            return false;
        }

        lanzamientosRestantes--;
        jugador.getEstadisticas().anadirTirada();

        // Mostrar la representación de los dados
        if (dado != null) {
            System.out.println(dado);
        }

        if (encerrado) {
            moverEstandoCarcel(dado);
            return false;
        }

        // Obtener la nueva casilla
        int posNuevaCasilla;
        if (movimientoEspecial) {
            posNuevaCasilla = switch (tipo) {
                case Coche -> moverEspecialCoche(dado, tablero.getCarcel());
                case Pelota -> moverEspecialPelota(dado, tablero.getCarcel());
                default -> Integer.MAX_VALUE; // No implementado
            };
        } else {
            // Movimiento básico
            if (irCarcelDadosDobles(dado, tablero.getCarcel())) {
                posNuevaCasilla = Integer.MAX_VALUE;
            } else {
                posNuevaCasilla = casilla.getPosicion() + dado.getValor();
            }
        }

        // Los métodos anteriores devuelven MAX_INT cuando ellos mismos
        // ya han movido el avatar (se ha enviado directamente a la cárcel);
        // por tanto, no hay que hacer nada.
        if (posNuevaCasilla == Integer.MAX_VALUE) {
            return true;
        }

        int movimientoDelta = posNuevaCasilla - casilla.getPosicion();
        Casilla nuevaCasilla = tablero.getCasillas().get(Math.floorMod(posNuevaCasilla, tablero.getCasillas().size()));

        // Mostrar información
        System.out.printf("""
                        %s, con avatar %s, %s %d posiciones.
                        Viaja desde %s hasta %s.
                        """,
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                Consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                movimientoDelta > 0 ? "avanza" : "retrocede",
                Math.abs(movimientoDelta),
                casilla.getNombreFmt(),
                nuevaCasilla.getNombreFmt());

        // Se pasa por la casilla de salida
        if (posNuevaCasilla >= tablero.getCasillas().size()) {
            long abonoSalida = tablero.getCalculadora().calcularAbonoSalida();
            jugador.ingresar(abonoSalida);
            jugador.getEstadisticas().anadirAbonoSalida(abonoSalida);
            jugador.getEstadisticas().anadirVuelta();

            // Aumentar los precios en caso de que todos los avatares pasasen por la salida
            tablero.getCalculadora().aumentarPrecio(tablero.getCasillas(), tablero.getJugadores());

            System.out.printf("Como el avatar pasa por la casilla de Salida, %s recibe %s\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.num(tablero.getCalculadora().calcularAbonoSalida()));

        } else if (posNuevaCasilla < 0) {
            // Si la casilla calculada es negativa, quiere decir que se pasa por la salida hacia atrás
            long abonoSalida = tablero.getCalculadora().calcularAbonoSalida();
            jugador.getEstadisticas().quitarVuelta();

            if (getJugador().cobrar(abonoSalida, true)) {
                System.out.printf(
                        "El jugador %s paga %s por retroceder por la casilla de salida.\n",
                        Consola.fmt(jugador.getNombre(), Color.Azul),
                        Consola.num(abonoSalida));
            } else {
                // En este caso, el avatar queda en la nueva casilla,
                // pero no ha podido devolver el abono que recibió.
                // Entonces, el jugador queda endeudado con la banca.
                Consola.error("No puedes devolver el abono de la salida.");
            }
        }

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        casilla.quitarAvatar(this);
        casilla = nuevaCasilla;
        nuevaCasilla.anadirAvatar(this);

        // Añadir la nueva casilla al historial
        historialCasillas.add(nuevaCasilla);
        nuevaCasilla.getEstadisticas().anadirEstancia();

        // Realizar la acción de la casilla
        nuevaCasilla.accion(jugador, dado == null ? pelotaDado : dado);
        return true;
    }

    private boolean irCarcelDadosDobles(Dado dado, Casilla carcel) {
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
            lanzamientosRestantes++;
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
        lanzamientosRestantes = 0;
        jugador.getEstadisticas().anadirEstanciaCarcel();

        casilla.quitarAvatar(this);
        casilla = carcel;
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

    private int moverEspecialCoche(Dado dado, Casilla carcel) {
        // Si ha salido un dado doble, se mueve de forma básica
        if (doblesSeguidos != 0) {
            return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() + dado.getValor();
        }

        // Penalización por sacar menos de un 4
        if (dado.getValor() < 4) {
            // Dos turnos de penalización en los que no se puede lanzar
            // (se restará 1 al cambiar de turno).
            penalizacion = 3;
            // Se ponen los lanzamientos restantes a 0, indicando que debe terminar el turno
            lanzamientosRestantes = 0;

            System.out.printf("Se aplica una penalización de %s por sacar un valor tan bajo.\n", Consola.fmt("2 turnos", Color.Azul));

            // Se retrocede el valor de los dados
            // Aunque sea la última tirada no se tienen en cuenta los dados dobles
            // porque se ha aplicado una penalización
            return casilla.getPosicion() - dado.getValor();
        }

        // En caso de que sea la última tirada, se tiene en cuenta los dados dobles
        // Cuando se tire otra vez, también pasará por aquí
        if (lanzamientosRestantes == 0 && irCarcelDadosDobles(dado, carcel)) {
            return Integer.MAX_VALUE;
        }

        return casilla.getPosicion() + dado.getValor();
    }

    private int moverEspecialPelota(Dado dado, Casilla carcel) {
        // Si ha salido un dado doble, se mueve de forma básica
        if (doblesSeguidos != 0) {
            return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() + dado.getValor();
        }

        // Primera tirada
        if (dado != null) {
            int valorDado = dado.getValor();

            // Si es menor que 4 se mueve hacia atrás
            if (valorDado < 4) {
                // Como es la última tirada, se tiene en cuenta si los dados han sido dobles
                return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() - valorDado;
            }

            // Si el resultado es 4 o 5, como no hay impares de por medio
            // se realiza un movimiento normal (no hay que hacer ningún salto)
            if (valorDado <= 5) {
                // Como es la última tirada, se tiene en cuenta si los dados han sido dobles
                return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() + valorDado;
            }

            // En otro caso, guardamos la posición final que marcan los dados
            pelotaPosFinal = casilla.getPosicion() + valorDado;
            pelotaDado = dado;

            // Movemos 5 posiciones dado que ese siempre será el primer salto
            lanzamientosRestantes = 1;
            return casilla.getPosicion() + 5;
        }

        // La cantidad de casillas que me tengo que mover es 2 o 1.
        // Esto funciona porque el primer salto cae en una casilla impar
        // y luego se va sumando 2 (o 1) hasta llegar a la casilla final.
        int paso = Math.min(pelotaPosFinal - casilla.getPosicion(), 2);

        if (paso <= 0) {
            Consola.error("[Avatar Pelota] Paso negativo o nulo");
            return 0;
        }

        // Si me muevo según el paso calculado y termino en la casilla
        // que me interesa, debo terminar el turno.
        // Como es la última tirada, hay que tener en cuenta si los dados
        // fueron dobles, por eso uso movimientoBasico()
        if (casilla.getPosicion() + paso == pelotaPosFinal) {
            lanzamientosRestantes = 0;

            // Como es la última tirada, se tiene en cuenta si los dados han sido dobles
            if (irCarcelDadosDobles(pelotaDado, carcel)) {
                return Integer.MAX_VALUE;
            }

            // Se borra el dado del turno anterior para que se pueda volver a usar lanzar
            pelotaDado = null;

            return casilla.getPosicion() + paso;
        }

        lanzamientosRestantes = 1;
        return casilla.getPosicion() + paso;
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

    public Jugador getJugador() {
        return jugador;
    }

    public void cambiarModo() {
        // Se puede cambiar el modo de Coche a básico si todavía no se ha lanzado
        // (4 es la cantidad de lanzamientos inicial) o si ya se ha terminado de
        // lanzar (0, no quedan lanzamientos).
        if (movimientoEspecial && tipo == TipoAvatar.Coche && lanzamientosRestantes != 4 && lanzamientosRestantes != 0) {
            Consola.error("No puedes cambiar de modo en mitad de un movimiento especial");
            return;
        }

        // TODO: para pelota no funciona
        // Mismo razonamiento que antes pero con la pelota: no se puede cambiar al
        // modo básico si todavía no se lanzó (pelotaDado es null, todavía no se
        // asignó) o si no quedan lanzamientos.
        if (movimientoEspecial && tipo == TipoAvatar.Pelota && (pelotaDado != null || lanzamientosRestantes != 0)) {
            Consola.error("No puedes cambiar de modo en mitad de un movimiento especial");
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

            // Si todavía no se tiró y se quiere usar el avatar de coche,
            // hay que poner los lanzamientos a 4, dado que en todos los
            // otros casos es 1.
            if (tipo == TipoAvatar.Coche && lanzamientosRestantes == 1) {
                lanzamientosRestantes = 4;
            }
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

    public boolean isPuedeComprar() {
        return puedeComprar;
    }

    public void noPuedeComprar() {
        if (movimientoEspecial && tipo == TipoAvatar.Coche) {
            this.puedeComprar = false;
        }
    }

    /**
     * Se cambia el turno, por tanto se tiene que resetear el estado. Devuelve false en caso de que no se pueda terminar aún, porque quedan acciones que realizar
     */
    public boolean acabarTurno() {
        if (lanzamientosRestantes > 0 && penalizacion == 0) {
            Consola.error("A %s aún le quedan %d tiros".formatted(jugador.getNombre(), lanzamientosRestantes));
            return false;
        }

        if (movimientoEspecial && tipo == TipoAvatar.Coche) {
            lanzamientosRestantes = 4;
        } else {
            lanzamientosRestantes = 1;
        }

        if (penalizacion != 0) {
            penalizacion--;
        }

        doblesSeguidos = 0;
        puedeComprar = true;
        pelotaDado = null;
        pelotaPosFinal = 0;

        return true;
    }

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}
