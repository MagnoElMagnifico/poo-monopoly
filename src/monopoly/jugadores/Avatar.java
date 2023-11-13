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
    private boolean pelotaMovimiento;  /*Solo para la pelota se comprueba si esta en movimento*/
    private boolean puedeComprar; /* Solo para el coche no permite permite comprar solo una vez*/
    private int casillasRestantes; /*Solo para la pelota almacena el dado del turno*/
    private Dado pelotaDado; /*Solo pafra la pelota guarda el dado */
    private int doblesSeguidos;
    private int estanciasCarcel;
    private int lanzamientos;
    private int lanzamientosEspeciales; /*Solo para el coche alamcena los movimentos que quedan en modo especial*/
    private int penalizacion; /*Solo para el coche penalizacion si saca menos de 4 */
    private int vueltas;
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

        this.historialCasillas = new ArrayList<>();

        this.encerrado = false;
        this.movimientoEspecial = false;
        this.pelotaMovimiento = false;
        this.puedeComprar = true;

        this.casillasRestantes = 0;
        this.doblesSeguidos = 0;
        this.estanciasCarcel = 0;
        this.lanzamientos = 1;
        this.lanzamientosEspeciales = 0;
        this.penalizacion = 0;
        this.vueltas = 0;
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
    public boolean mover(Dado dado, ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores, Calculadora calculadora) {
        if (lanzamientos <= 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return false;
        }

        lanzamientos--;

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
                case Coche -> moverEspecialCoche(dado, calculadora, casillas.size());
                case Esfinge -> moverEspecialEsfinge();
                case Sombrero -> moverEspecialSombrero();
                case Pelota -> moverEspecialPelota(dado, calculadora,casillas.size());
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

            this.anadirVuelta();
            jugador.ingresar(calculadora.calcularAbonoSalida());

            // Aumentar los precios en caso de que el avatar pasase por la salida
            Calculadora.aumentarPrecio(casillas, jugadores);

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


        // Mostrar informacion de la lepota si estáen movimiento especial
        if (movimientoEspecial && tipo == TipoAvatar.Pelota && pelotaDado != null) {
            System.out.printf("%s con avatar %s, avanza %s posiciones. Quedan %s\nAvanza desde %s hasta %s.\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                    pelotaDado, Consola.num(casillasRestantes),
                    anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());
            if (casillasRestantes == 0) pelotaDado = null;
        } else {
            // Mostrar información
            System.out.printf("%s con avatar %s, avanza %s posiciones.\nAvanza desde %s hasta %s.\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.fmt(Character.toString(jugador.getAvatar().getId()), Consola.Color.Azul),
                    dado,
                    anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());
        }
        // Aumentar los precios en caso de que el avatar pasase por la salida
        Calculadora.aumentarPrecio(casillas, jugadores);

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
                lanzamientos++;
                System.out.println("Dados dobles! El jugador puede tirar otra vez");
            }
        }

        return this.casilla.getPosicion() + dado.getValor();
    }

    /**
     * Realiza una tirada de dados cuando está en la cárcel
     */
    private void moverEstandoCarcel(Dado dado) {
        estanciasCarcel++;

        if (dado.isDoble()) {
            System.out.println("Dados dobles! El jugador puede salir de la Cárcel");
            lanzamientos++;
            encerrado = false;
            estanciasCarcel = 0;
        } else if (estanciasCarcel >= 3) {
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
        estanciasCarcel = 0;
        lanzamientos = 0;
        lanzamientosEspeciales = 0;

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
        estanciasCarcel = 0;

        System.out.printf("El jugador %s paga %s para salir de la cárcel\n", jugador.getNombre(), Consola.num(casilla.getFianza()));
        return true;
    }

    private int moverEspecialCoche(Dado dado, Calculadora calculadora, int nCasillas) {
        // Primera tirada
        if (lanzamientos == 0) {
            lanzamientos = 2;
            lanzamientosEspeciales = 4;
        }
        // Lanzamos el error de que no quedan lanzamientos
        if (lanzamientosEspeciales == 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return -1;
        }

        // 4 tiradas especiales
        lanzamientosEspeciales--;
        lanzamientos++;
        // Ultima(s) tirada con comportamiento normal
        if (lanzamientosEspeciales == 0) {
            lanzamientos = 0;
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
                System.out.println("Dados dobles! El jugador puede tirar otra vez");
                lanzamientosEspeciales++;
                lanzamientos++;
            }
            return this.casilla.getPosicion() + dado.getValor();
        }
        //Condicion para romper el coche penalizacion
        if (dado.getValor() < 4) {
            lanzamientosEspeciales = 0;
            penalizacion = 2;
            lanzamientos = 0;
            int cantidad = this.casilla.getPosicion() - dado.getValor();
            if(cantidad<0){
                if(getJugador().cobrar(calculadora.calcularAbonoSalida())) {
                    Consola.error("No puedes devolver el abono de la carcel.");
                }
                cantidad+=nCasillas;
            }
            return cantidad;
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

    private int moverEspecialPelota(Dado dado, Calculadora calculadora, int nCasillas) {
        // Comportamieto normal
        if (!pelotaMovimiento) {
            if (dado.getValor() <= 4) {
                int cantidad = this.casilla.getPosicion() - dado.getValor();
                if(cantidad<0){
                    if(getJugador().cobrar(calculadora.calcularAbonoSalida())) {
                        Consola.error("No puedes devolver el abono de la carcel.");
                    }
                    cantidad+=nCasillas;
                }
                return cantidad;
            }
            if (dado.getValor() == 5) return this.casilla.getPosicion() + dado.getValor();
        }
        // Hay que mover más de una vez. 1º vez
        if (lanzamientos == 0) {
            lanzamientos = 2;
            pelotaDado = dado;
            casillasRestantes = dado.getValor() - 5;
            pelotaMovimiento = true;
            return this.casilla.getPosicion() + 5;
        }
        // Resto de veces
        if (pelotaMovimiento) {

            // Ultima vez
            if (casillasRestantes <= 2) {
                lanzamientos = 0;
                int i = casillasRestantes;
                casillasRestantes = 0;
                pelotaMovimiento = false;
                if (pelotaDado.isDoble()) {
                    lanzamientos++;
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
                    System.out.println("Dados dobles! El jugador puede tirar otra vez");
                }
                return this.casilla.getPosicion() + i;
            }
            // Resto de veces
            casillasRestantes -= 2;
            lanzamientos++;
            if (casillasRestantes == 0) {
                pelotaMovimiento = false;
                lanzamientos = 0;
                if (pelotaDado.isDoble()) {
                    lanzamientos++;
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
        if (lanzamientos >= 2) {
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

    public int getEstanciasCarcel() {
        return estanciasCarcel;
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

    public int getLanzamientos() {
        return lanzamientos;
    }

    public void resetLanzamientos() {
        lanzamientos = 1;
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
