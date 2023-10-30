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
    private boolean estadoCoche;
    private int nEstadoCoche;
    private int nLanzamientos;

    private int nDoblesSeguidos;

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
        this.nLanzamientos=0;
        this.nDoblesSeguidos=0;
        this.estadoCoche=false;
        this.nEstadoCoche=0;
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
        this.nLanzamientos=0;
        this.nDoblesSeguidos=0;
        this.estadoCoche=false;
        this.nEstadoCoche=0;
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

    public int getnLanzamientos() {return this.nLanzamientos;}

    public void setnLanzamientos() {
        if(!movimientoEspecial) this.nLanzamientos=1;
        else if (tipo==TipoAvatar.Coche){
            this.nLanzamientos=4;
        }
    }
    public void setnDoblesSeguidos() {this.nDoblesSeguidos=0;}
    public boolean getMovimientoEspecial() {return this.movimientoEspecial;}


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
        if(estadoCoche) {
            nEstadoCoche++;
            if(nEstadoCoche>2){
                nEstadoCoche=0;
                estadoCoche=false;
            }
            return "Aún no te puedes mover. Enfriando cocche";
        }
        Casilla actualCasilla = this.casilla;
        int nActual = casillas.indexOf(this.casilla);
        String accionAdicional= "";
        if(!this.movimientoEspecial){
            nLanzamientos--;
            if (this.estarEncerrado) {
                this.seguirEnCarcel();

                if (dado.isDoble()) {
                    accionAdicional += "Dados dobles! El jugador puede salir de %s\n".formatted(Formatear.casillaNombre(actualCasilla));
                    this.salirCarcel();
                } else if (this.estanciasCarcel >= 3) {
                    // @formatter:off
                    return """
                        %s con avatar %s no ha sacado dados dobles %s.
                        Ahora debe pagar obligatoriamente la fianza.
                        %s""".formatted(Formatear.con(jugador.getNombre(), Formatear.Color.Azul),
                            Formatear.con(Character.toString(this.id), Formatear.Color.Azul),
                            dado, this.salirCarcel());
                    // @formatter:on
                } else {
                    // @formatter:off
                    return """
                        %s con avatar %s no ha sacado dados dobles %s.
                        Puede pagar la fianza o permanecer encerrado.
                        """.formatted(Formatear.con(jugador.getNombre(), Formatear.Color.Azul),
                            Formatear.con(Character.toString(this.id), Formatear.Color.Azul),
                            dado);
                    // @formatter:on
                }
            } else if (dado.isDoble()) {
                accionAdicional += "Dados dobles! El jugador puede lanzar otra vez\n";
                nLanzamientos++;
                nDoblesSeguidos++;
                if (nDoblesSeguidos >= 3) {
                    // @formatter:off
                    return """
                        %s con avatar %s ha sacado %s.
                        Ya son 3 veces seguidas sacando dados dobles.
                        %s es arrestado por tener tanta suerte.
                        %s""".formatted(Formatear.con(jugador.getNombre(), Formatear.Color.Azul),
                            Formatear.con(Character.toString(this.id), Formatear.Color.Azul),
                            dado,
                            jugador.getNombre(),
                            this.irCarcel(casillas));
                    // @formatter:on
                }
            }

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
        else {
            return switch (this.tipo) {
                case Coche -> moverEspecialCoche(casillas,dado,calculadora,jugadores,banca);
                case Pelota -> moverEspecialPelota(casillas,dado,calculadora,jugadores,banca);
                case Sombrero -> moverEspecialSombrero(casillas,dado,calculadora,jugadores,banca);
                case Esfinge -> moverEspecialEsfinge(casillas,dado,calculadora,jugadores,banca);
            };
        }
    }

    private String moverEspecialCoche(ArrayList<Casilla> casillas, Dado dado,Calculadora calculadora,ArrayList<Jugador> jugadores, Jugador banca){
        int nActual = casillas.indexOf(this.casilla);
        String accionAdicional= "";
        int nNuevo;
        if(dado.getValor()<=4)  {
            nNuevo= nActual - dado.getValor()+ casillas.size();
            if (nNuevo >= casillas.size()) {
                nNuevo -= casillas.size();
            }
            estadoCoche=true;
            nLanzamientos=0;
        }
        else {
            nNuevo= nActual + dado.getValor();
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

    private String moverEspecialPelota(ArrayList<Casilla> casillas, Dado dado,Calculadora calculadora,ArrayList<Jugador> jugadores, Jugador banca){
        return"";
    }

    private String moverEspecialEsfinge(ArrayList<Casilla> casillas, Dado dado,Calculadora calculadora,ArrayList<Jugador> jugadores, Jugador banca){
        return"";
    }

    private String moverEspecialSombrero(ArrayList<Casilla> casillas, Dado dado,Calculadora calculadora,ArrayList<Jugador> jugadores, Jugador banca){
        return"";
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
        nLanzamientos=0;
        Casilla nuevaCasilla = casillas.get(casillas.indexOf(new Casilla(null, "Cárcel")));
        this.getCasilla().quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);


        return "El avatar se coloca en la Cárcel\n";
    }

    public String salirCarcel() {


        if (!this.estarEncerrado) {
            return Formatear.con("El jugador %s no está en la Cárcel".formatted(jugador.getNombre()), Formatear.Color.Rojo);
        }


        long importe = this.casilla.getPrecio();
        if(!jugador.cobrar(importe)) return "El jugador no tiene dinero no sale de la carel";

        estanciasCarcel = 0;
        estarEncerrado = false;

        //TODO: Arreglar la carcel parar da bancarotq
        return "El jugador %s paga %s para salir de la cárcel\n".formatted(Formatear.con(jugador.getNombre(), Formatear.Color.Azul), Formatear.num(importe));
    }
}
