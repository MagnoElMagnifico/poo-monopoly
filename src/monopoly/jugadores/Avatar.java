package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Consola;
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
    /**
     * Determina si el avatar está en la Cárcel o no
     */
    private boolean encerrado;
    /**
     * Número de turnos que se han pasado en la Cárcel
     */
    private int estanciasCarcel;
    private int vueltas;
    private boolean movimientoEspecial;

    private int nLanzamientos;

    private int nDoblesSeguidos;

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

        this.movimientoEspecial = false;
        this.nLanzamientos = 0;
        this.nDoblesSeguidos = 0;
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

        this.movimientoEspecial=false;
        this.nLanzamientos = 0;
        this.nDoblesSeguidos = 0;
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

    public void mover(Dado dado, ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores, Calculadora calculadora) {
        int nActual = this.casilla.getPosicion();

        if (movimientoEspecial) {
            moverEspecial();
            return;
        }

        int nNuevo = nActual + dado.getValor();

        if (nNuevo >= casillas.size()) {
            nNuevo -= casillas.size();

            this.anadirVuelta();
            jugador.ingresar(calculadora.calcularAbonoSalida());

            System.out.printf("Como el avatar pasa por la casilla de Salida, %s recibe %s\n",
                    Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                    Consola.num(calculadora.calcularAbonoSalida()));
            Calculadora.aumentarPrecio(casillas, jugadores);
        }

        Casilla anteriorCasilla = this.casilla;
        Casilla nuevaCasilla = casillas.get(nNuevo);

        // Quitar el avatar de la casilla actual y añadirlo a la nueva
        this.casilla.quitarAvatar(this);
        this.setCasilla(nuevaCasilla);
        nuevaCasilla.anadirAvatar(this);

        System.out.printf("%s con avatar %s, avanza %s posiciones.\nAvanza desde %s hasta %s.\n",
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                Consola.fmt(jugador.getNombre(), Consola.Color.Azul),
                dado,
                anteriorCasilla.getNombreFmt(), nuevaCasilla.getNombreFmt());

        nuevaCasilla.accion(jugador, dado);
    }

    private void moverEspecial() {
        // TODO
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

        System.out.println("El avatar se coloca en la Cárcel");
    }

    public void salirCarcel() {
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

    public int getnLanzamientos() {
        return this.nLanzamientos;
    }

    public void setnLanzamientos() {
        this.nLanzamientos = 1;
    }
  
    public void setnDoblesSeguidos() {
        nDoblesSeguidos = 0;
    }

    /**
     * Pone el Avatar en el estado encerrado
     */

// > main

    /**
     * Se notifica al Avatar de que pasa otro turno en la Cárcel
     */
    public void seguirEnCarcel() {
        if (!encerrado) {
            Consola.error("[Avatar] No está encerrado, entonces no puede seguir en la Cárcel");
            return;
        }

// < Marcos
        estanciasCarcel++;
    }

    public boolean isEncerrado() {
        return encerrado;
    }
// =
    /**
     * Saca el Avatar del estado encerrado
     */

// > main

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
// < Marcos
// =

    public String mover(ArrayList<Casilla> casillas, Dado dado,Calculadora calculadora,ArrayList<Jugador> jugadores, Jugador banca){
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
        return "";
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
        estanciasCarcel = 0;
        estarEncerrado = false;
        jugador.cobrar(importe);

        return "El jugador %s paga %s para salir de la cárcel\n".formatted(Formatear.con(jugador.getNombre(), Formatear.Color.Azul), Formatear.num(importe));
    }
//  main
}
