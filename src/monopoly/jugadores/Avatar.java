package monopoly.jugadores;

import monopoly.casillas.Casilla;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.Calculadora;
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

    private boolean movimientoEspecial;

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
        this.movimientoEspecial=false;
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
        this.movimientoEspecial=false;
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
    public void setMovimientoEspecial() {this.movimientoEspecial= !this.movimientoEspecial;}

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

    public String mover(ArrayList<Casilla> casillas, Dado dado,Calculadora calculadora,ArrayList<Jugador> jugadores, Jugador banca){
        int nActual = casillas.indexOf(this.casilla);
        String accionAdicional= "";
        if(!this.movimientoEspecial){
            int nNuevo = nActual + dado.getValor();
            if (nNuevo >= casillas.size()) {
                nNuevo -= casillas.size();

                this.anadirVuelta();
                jugador.ingresar(calculadora.calcularAbonoSalida());

                // @formatter:off
                accionAdicional += "Como el avatar pasa por la casilla de Salida, %s recibe %s\n%s"
                        .formatted(Formatear.con(jugador.getNombre(), Formatear.Color.Azul),
                                Formatear.num(calculadora.calcularAbonoSalida()),
                                calculadora.aumentarPrecio(casillas,jugadores));
                // @formatter:on
            }
            Casilla actualCasilla = this.casilla;
            Casilla nuevaCasilla = casillas.get(nNuevo);

            // Quitar el avatar de la casilla actual y añadirlo a la nueva
            actualCasilla.quitarAvatar(this);
            this.setCasilla(nuevaCasilla);
            nuevaCasilla.anadirAvatar(this);
            return """
                %s con avatar %s, avanza %s posiciones.
                Avanza desde %s hasta %s.
                %s%s""".formatted(Formatear.con(this.jugador.getNombre(), Formatear.Color.Azul),
                    Formatear.con(Character.toString(this.getId()), Formatear.Color.Azul),
                    dado,
                    Formatear.casillaNombre(actualCasilla),
                    Formatear.casillaNombre(nuevaCasilla),
                    accionCasilla(nuevaCasilla, dado, calculadora, banca, casillas), accionAdicional);
        }
        switch(this.tipo){
            case Coche: break;
            case Pelota: break;
            case Esfinge: break;
            case Sombrero: break;
        }
        return accionAdicional;
    }

    private String accionCasilla(Casilla casilla, Dado dado, Calculadora calculadora, Jugador banca,ArrayList<Casilla> casillas) {
        if (casilla.isPropiedad()) {
            return calculadora.pagarAlquiler(casilla.getPropiedad(), this.jugador, dado);
        }

        return switch (casilla.getNombre()) {
            case "IrCárcel" -> irCarcel(casillas);
            case "Comunidad1", "Comunidad2", "Comunidad3" -> "* Acción de Carta de Comunidad *\n";
            case "Suerte1", "Suerte2", "Suerte3" -> "* Acción de Carta de Suerte *\n";
            case "Parking" -> {
                Jugador jugador = this.jugador;

                long bote = banca.getFortuna();
                jugador.ingresar(bote);
                banca.cobrar(bote); // Poner a 0 el bote

                yield "El jugador recibe el bote de la banca: %s\n".formatted(Formatear.num(bote));

            }

            case "Impuesto1", "Impuesto2" -> {
                Jugador jugador = this.jugador;
                long importe = casilla.getPrecio();

                if (importe > jugador.getFortuna()) {
                    yield Formatear.con("El jugador no tiene suficientes fondos para pagar el alquiler\n", Formatear.Color.Rojo);
                }

                jugador.cobrar(importe);
                banca.ingresar(importe);

                yield "Se han pagado %s de impuestos a la banca.\n".formatted(Formatear.num(importe));
            }

            case "Cárcel" -> "El jugador está solo de visita.\n";
            default -> "";
        };
    }



    private String irCarcel(ArrayList<Casilla> casillas) {

        estarEncerrado = true;
        estanciasCarcel = 0;

        Casilla nuevaCasilla = casillas.get(casillas.indexOf(new Casilla(null, "Cárcel")));
        this.getCasilla().quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);


        return "El avatar se coloca en la Cárcel\n";
    }
}
