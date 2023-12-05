package monopoly.jugador;


import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatal;
import monopoly.utils.Consola;
import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.especial.CasillaCarcel;
import monopoly.casilla.especial.CasillaSalida;
import monopoly.error.ErrorComandoAvatar;
import monopoly.utils.Dado;


public abstract class Avatar {
    // @formatter:off
    // Propiedades
    private final char id;
    private Jugador jugador;

    // Historial
    private final ArrayList<Casilla> historialCasillas;
    private Casilla casilla;

    // Estado
    private boolean encerrado;
    private int doblesSeguidos;
    private int lanzamientosRestantes; /* Si es 0 se debe terminar el turno */
    private int turnosEnCarcel;

    private boolean movimientoEspecial;
    // @formatter:on

    public Avatar(char id, CasillaSalida salida) {
        this.id = id;
        this.casilla = salida;
        salida.anadirAvatar(this);

        this.historialCasillas = new ArrayList<>();
        this.encerrado = false;
        this.doblesSeguidos = 0;
        this.lanzamientosRestantes = 1;
        this.turnosEnCarcel = 0;
        this.movimientoEspecial = false;
    }

    public abstract boolean acabarTurno() throws ErrorComandoAvatar;

    public abstract int moverEspecial(Dado dado, CasillaCarcel carcel) throws ErrorComandoAvatar;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // Aquí se puede usar == dado que char es primitivo
        return obj instanceof Avatar && ((Avatar) obj).getId() == this.id;
    }

    public void mover(Juego juego, Dado dado) throws ErrorComandoAvatar, ErrorFatal, ErrorComandoFortuna {
        if (lanzamientosRestantes <= 0) {
            throw new ErrorComandoAvatar("No quedan lanzamientos. El jugador debe terminar el turno", this);
        }

        if (dado == null && encerrado) {
            throw new ErrorComandoAvatar("No puedes usar el comando siguiente cuando estás encerrado", this);
        }

        if (dado == null && doblesSeguidos != 0) {
            throw new ErrorComandoAvatar("No puedes usar el comando siguiente después de obtener dados dobles. Prueba con lanzar.", this);
        }

        lanzamientosRestantes--;

        // Mostrar la representación de los dados
        if (dado != null) {
            Juego.consola.imprimir(dado.toString());

            // Cuando el dado no es null, es que se ha lanzado un nuevo dado.
            // De esta forma, el comando siguiente no se considera una tirada.
            jugador.getEstadisticas().anadirTirada();
        }

        if (encerrado) {
            moverEstandoCarcel(dado, juego.getBanca());
            return;
        }

        // Obtener la nueva casilla
        int posNuevaCasilla;
        if (movimientoEspecial) {
            posNuevaCasilla = moverEspecial(dado, juego.getCarcel());
        } else {
            // Movimiento básico
            if (irCarcelDadosDobles(dado, juego.getCarcel())) {
                return;
            }

            posNuevaCasilla = casilla.getPosicion() + dado.getValor();
        }

        // Los métodos anteriores devuelven MAX_INT cuando ellos mismos
        // ya han movido el avatar (se ha enviado directamente a la cárcel);
        // por tanto, no hay que hacer nada.
        if (posNuevaCasilla == Integer.MAX_VALUE) {
            return;
        }

        ArrayList<Casilla> casillas = juego.getCasillas();
        int movimientoDelta = posNuevaCasilla - casilla.getPosicion();
        Casilla nuevaCasilla = casillas.get(Math.floorMod(posNuevaCasilla, casillas.size()));

        // Mostrar información
        Juego.consola.imprimir("""
                        %s, con avatar %s, %s %d posiciones.
                        Viaja desde %s hasta %s.
                        """.formatted(
                Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                Juego.consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                movimientoDelta > 0 ? "avanza" : "retrocede",
                Math.abs(movimientoDelta),
                casilla.getNombreFmt(),
                nuevaCasilla.getNombreFmt()));

        long abonoSalida = juego.getSalida().getAbonoSalida();
        
        // Se pasa por la casilla de salida
        if (posNuevaCasilla >= casillas.size()) {
            jugador.ingresar(abonoSalida);
            jugador.getEstadisticas().anadirAbonoSalida(abonoSalida);
            jugador.getEstadisticas().anadirVuelta();

            // Aumentar los precios en caso de que todos los avatares pasasen por la salida
            juego.aumentarPrecio();

            Juego.consola.imprimir("Como el avatar pasa por la casilla de Salida, %s recibe %s\n".formatted(
                    Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Juego.consola.num(abonoSalida)));

        } else if (posNuevaCasilla < 0) {
            jugador.getEstadisticas().quitarVuelta();

            // No tiene sentido cobrar un abono que aún no ha recibido
            if (jugador.getEstadisticas().getVueltas() > 0) {
                // Si la casilla calculada es negativa, quiere decir que se pasa por la salida hacia atrás
                getJugador().cobrar(abonoSalida, juego.getBanca());
                Juego.consola.imprimir(
                        "El jugador %s paga %s por retroceder por la casilla de salida.\n".formatted(
                        Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                        Juego.consola.num(abonoSalida)));
            }
        }

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        casilla.quitarAvatar(this);
        casilla = nuevaCasilla;
        nuevaCasilla.anadirAvatar(this);

        // Añadir la nueva casilla al historial
        historialCasillas.add(nuevaCasilla);

        // Realizar la acción de la casilla
        nuevaCasilla.accion(jugador, dado);
    }


    public boolean irCarcelDadosDobles(Dado dado, CasillaCarcel carcel) {
        if (dado.isDoble()) {
            doblesSeguidos++;

            if (doblesSeguidos >= 3) {
                Juego.consola.imprimir("""
                        Ya son 3 veces seguidas sacando dados dobles.
                        %s es arrestado por tener tanta suerte.
                        """.formatted(jugador.getNombre()));
                irCarcel(carcel);
                return true;
            }

            lanzamientosRestantes++;
            Juego.consola.imprimir("Dados dobles! El jugador puede tirar otra vez");
        }

        return false;
    }

    /**
     * Realiza una tirada de dados cuando está en la cárcel
     */
    private void moverEstandoCarcel(Dado dado, Banca banca) throws ErrorComandoAvatar, ErrorComandoFortuna {
        turnosEnCarcel++;

        if (dado.isDoble()) {
            Juego.consola.imprimir("Dados dobles! El jugador puede salir de la Cárcel");
            lanzamientosRestantes = 1;
            encerrado = false;
            turnosEnCarcel = 0;
        } else if (turnosEnCarcel >= 3) {
            Juego.consola.imprimir("%s con avatar %s no ha sacado dados dobles.\nAhora debe pagar obligatoriamente la fianza.\n".formatted(
                    Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Juego.consola.fmt(Character.toString(id), Consola.Color.Azul)));
            salirCarcelPagando(banca);
        } else {
            Juego.consola.imprimir("%s con avatar %s no ha sacado dados dobles.\nPuede pagar la fianza o permanecer encerrado.\n".formatted(
                    Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Juego.consola.fmt(Character.toString(id), Consola.Color.Azul)));
        }
    }

    /**
     * Pone el Avatar en el estado encerrado y lo mueve a la cárcel
     */
    public void irCarcel(CasillaCarcel carcel) {
        encerrado = true;
        turnosEnCarcel = 0;
        lanzamientosRestantes = 0;

        casilla.quitarAvatar(this);
        casilla = carcel;
        carcel.anadirAvatar(this);
        historialCasillas.add(carcel);

        Juego.consola.imprimir("Por tanto, el avatar termina en la Cárcel");
    }

    /**
     * Saca el avatar de la cárcel pagando la fianza.
     *
     * @param banca La banca para endeudar al jugador si no tinee dinero.
     */
    public void salirCarcelPagando(Banca banca) throws ErrorComandoAvatar, ErrorComandoFortuna {
        if (!encerrado && !(casilla instanceof CasillaCarcel)) {
            throw new ErrorComandoAvatar("El jugador no está en la Cárcel", this);
        }

        long fianza = ((CasillaCarcel) casilla).getFianza();
        jugador.cobrar(fianza, banca);

        encerrado = false;
        turnosEnCarcel = 0;
        lanzamientosRestantes = 1;

        Juego.consola.imprimir("El jugador %s paga %s para salir de la cárcel\n".formatted(jugador.getNombre(), Juego.consola.num(fianza)));
    }

    public void cambiarModo() throws ErrorComandoAvatar {
        if (movimientoEspecial) {
            movimientoEspecial = false;
            Juego.consola.imprimir("%s regresa al modo de movimiento básico\n".formatted(Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul)));
        } else {
            movimientoEspecial = true;
            Juego.consola.imprimir("A partir de ahora %s (%s), de tipo %s, se moverá de modo avanzado\n".formatted(
                    Juego.consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Juego.consola.fmt(Character.toString(id), Consola.Color.Azul),
                    this.getClass()));
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
}
