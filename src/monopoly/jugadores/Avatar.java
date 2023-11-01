package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.Monopoly;
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
    // Propiedades
    private final TipoAvatar tipo;
    private final char id;
    private final Jugador jugador;
    private Casilla casilla;

    // Estado
    private boolean encerrado;
    private int estanciasCarcel;
    private int vueltas;
    private int lanzamientos;
    private int lanzamientosEspeciales;
    private int penalizacion;
    private int doblesSeguidos;
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
        this.lanzamientos = 1;
        this.doblesSeguidos = 0;
        this.movimientoEspecial = false;
        this.lanzamientosEspeciales=0;
        this.penalizacion=0;
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
        this.lanzamientos = 1;
        this.doblesSeguidos = 0;
        this.movimientoEspecial = false;
        this.lanzamientosEspeciales=0;
        this.penalizacion=0;
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
     */
    public void mover(Dado dado, ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores, Calculadora calculadora) {
        if (lanzamientos <= 0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno\n");
            return;
        }

        lanzamientos--;

        if(penalizacion!=0){
            penalizacion--;
            Consola.error("Restaurando avatar espera para poder moverte\n");
            return;
        }
        if (encerrado) {
            moverEstandoCarcel(dado);
            return;
        }

        // Obtener la nueva casilla
        int posNuevaCasilla;
        if (movimientoEspecial) {
            posNuevaCasilla = switch (tipo) {
                case Coche -> moverEspecialCoche(dado);
                case Esfinge -> moverEspecialEsfinge();
                case Sombrero -> moverEspecialSombrero();
                case Pelota -> moverEspecialPelota(dado);
            };
        } else {
            posNuevaCasilla = moverBasico(dado);
        }
        // Los métodos anteriores devuelven -1 cuando ellos mismos
        // ya han movido el avatar; por tanto, no hay que hacer nada.
        if (posNuevaCasilla <= 0) {
            return;
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

        // Mostrar información
        System.out.printf("%s con avatar %s, avanza %s posiciones.\nAvanza desde %s hasta %s.\n",
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                dado,
                anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());



        // Realizar la acción de la casilla
        nuevaCasilla.accion(jugador, dado);
    }

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
                irCarcel();
                return -1;
            }
        }

        return this.casilla.getPosicion() + dado.getValor();
    }

    private void moverEstandoCarcel(Dado dado) {
        estanciasCarcel++;

        if (dado.isDoble()) {
            System.out.println("Dados dobles! El jugador puede salir de la Cárcel");

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

        Casilla nuevaCasilla = this.casilla.getCarcel();
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        nuevaCasilla.accion(jugador, null); // La casilla Carcel no usa el dado
    }

    public void salirCarcelPagando() {
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

    private int moverEspecialCoche(Dado dado) {
        if(lanzamientos==0) {
            lanzamientos =2;
            lanzamientosEspeciales=4;
        }
        if(lanzamientosEspeciales==0) {
            Consola.error("No quedan lanzamientos. El jugador debe terminar el turno");
            return -1;
        }
        lanzamientosEspeciales--;
        if(dado.getValor()<4){
            lanzamientosEspeciales=0;
            penalizacion=2;
            lanzamientos=0;
            return (this.casilla.getPosicion()- dado.getValor()+40)%40;
        }else {
            lanzamientos++;
            if(lanzamientosEspeciales==0) {
                lanzamientos=0;
                movimientoEspecial=false;
                return moverBasico(dado);
            }

            return this.casilla.getPosicion() + dado.getValor();
        }
    }

    private int moverEspecialEsfinge() {
        // TODO
        return -1;
    }

    private int moverEspecialSombrero() {
        // TODO
        return -1;
    }

    private int moverEspecialPelota(Dado dado) {
        movimientoEspecial=false;
        if(dado.getValor() <=4) return (this.casilla.getPosicion()- dado.getValor()+40)%40;
        if(dado.getValor() ==5) return this.casilla.getPosicion() +dado.getValor();
        return this.casilla.getPosicion() +dado.getValor();
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

    public boolean getMovimientoEspecial(){return this.movimientoEspecial;}
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

    /**
     * Tipos de avatares posibles
     */
    public enum TipoAvatar {
        Coche, Esfinge, Sombrero, Pelota
    }
}