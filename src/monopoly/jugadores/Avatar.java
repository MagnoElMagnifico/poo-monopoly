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

    public boolean mover(Dado dado, Tablero tablero, Dado pelotaDado) {
        if (lanzamientosRestantes <= 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return false;
        }



        if (dado == null && encerrado) {
            Consola.error("No puedes usar el comando siguiente cuando estás encerrado");
            return false;
        }

        if (dado == null && doblesSeguidos != 0) {
            Consola.error("No puedes usar el comando siguiente después de obtener dados dobles. Prueba con lanzar.");
            return false;
        }



        lanzamientosRestantes--;

        // Mostrar la representación de los dados
        if (dado != null) {
            System.out.println(dado);

            // Cuando el dado no es null, es que se ha lanzado un nuevo dado.
            // De esta forma, el comando siguiente no se considera una tirada.
            jugador.getEstadisticas().anadirTirada();
        }

        if (encerrado) {
            moverEstandoCarcel(dado);
            return false;
        }

        // Obtener la nueva casilla
        int posNuevaCasilla;
        if (movimientoEspecial) {
            posNuevaCasilla = moverEspecial(dado,tablero.getCarcel());
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
            jugador.getEstadisticas().quitarVuelta();

            // No tiene sentido cobrar un abono que aún no ha recibido
            if (jugador.getEstadisticas().getVueltas() > 0) {
                // Si la casilla calculada es negativa, quiere decir que se pasa por la salida hacia atrás
                long abonoSalida = tablero.getCalculadora().calcularAbonoSalida();

                if (getJugador().cobrar(abonoSalida, true)) {
                    System.out.printf(
                            "El jugador %s paga %s por retroceder por la casilla de salida.\n",
                            Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                            Consola.num(abonoSalida));
                } else {
                    // En este caso, el avatar queda en la nueva casilla,
                    // pero no ha podido devolver el abono que recibió.
                    // Entonces, el jugador queda endeudado con la banca.
                    Consola.error("No puedes devolver el abono de la salida.");
                }

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
        nuevaCasilla.accion(jugador, pelotaDado == null ? dado : pelotaDado);
        return true;
    }


    public abstract int moverEspecial(Dado dado,Casilla carcel);

    public boolean irCarcelDadosDobles(Dado dado, Casilla carcel) {
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

    public void cambiarModo() {


        if (movimientoEspecial) {
            movimientoEspecial = false;
            System.out.printf("%s regresa al modo de movimiento básico\n", Consola.fmt(jugador.getNombre(), Consola.Color.Azul));
        } else {
            movimientoEspecial = true;
            System.out.printf("A partir de ahora %s (%s), de tipo %s, se moverá de modo avanzado\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(id), Consola.Color.Azul),
                    this.getClass());
        }


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

    public void setLanzamientosRestantes(int n) {lanzamientosRestantes=n;}

    public int getDoblesSeguidos() {
        return doblesSeguidos;
    }

    public void setDoblesSeguidos(int n) { doblesSeguidos=n;}

    public boolean isMovimientoEspecial() {
        return movimientoEspecial;
    }


    public ArrayList<Casilla> getHistorialCasillas() {
        return historialCasillas;
    }

    public  abstract boolean acabarTurno();


    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }

}
