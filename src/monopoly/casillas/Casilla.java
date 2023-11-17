package monopoly.casillas;

import monopoly.jugadores.Avatar;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Consola.Estilo;
import monopoly.utilidades.Dado;
import monopoly.utilidades.EstadisticasCasilla;

import java.util.ArrayList;

/**
 * La clase Casilla representa una casilla del tablero, que pueden ser de dos tipos:
 *
 * <li> Propiedad: se puede comprar por los jugadores (Solares, Servicios, Transporte).
 * <li> Casilla especial: Cárcel, Salida, IrACárcel, Parking, Impuestos, CartaComunidad,
 * CartaSuerte.
 *
 * <p> Además, sabe si hay un avatar sobre la casilla (útil para dibujar el tablero).
 *
 * @see Propiedad
 * @see Avatar
 */
public class Casilla {
    private final int posicion;
    private final TipoCasilla tipo;
    private final Grupo grupo;
    // Si es una casilla especial, este campo está a `null`.
    private final Propiedad propiedad;
    private final ArrayList<Avatar> avatares;
    private final EstadisticasCasilla estadisticas;

    private long fianza;       /* Solo en Carcel: Valor pagado para salir de la cárcel */
    private long abonoSalida;  /* Solo en Salida: Valor recibido al pasar por la salida */
    private long impuestos;    /* Solo en Impuestos: Valor que se cobran por los impuestos */
    private Jugador banca;     /* Solo en Parking e Impuestos: banca.getFortuna() es el valor recibido */
    private Casilla carcel;    /* Solo en IrCarcel: casilla a donde se tiene que mover el avatar */
    private Mazo mazo;         /* Solo en Comunidad y Suerte: mazo que contiene las cartas */

    /**
     * Construye una nueva casilla de tipo Propiedad
     */
    public Casilla(int posicion, Grupo grupo, String nombre, Propiedad.TipoPropiedad tipoPropiedad) {
        this.posicion = posicion;
        this.tipo = TipoCasilla.Propiedad;
        this.propiedad = new Propiedad(nombre, this, tipoPropiedad);
        this.grupo = grupo;
        this.avatares = new ArrayList<>();
        this.estadisticas = new EstadisticasCasilla(this);

        // Los establece luego la Calculadora según el tipo
        fianza = -1;
        abonoSalida = -1;
        impuestos = -1;
        banca = null;
        carcel = null;
        mazo = null;
    }

    /**
     * Construye una nueva casilla de tipo especial
     */
    public Casilla(int posicion, Grupo grupo, TipoCasilla tipo) {
        this.posicion = posicion;
        this.tipo = tipo;
        this.propiedad = null;
        this.grupo = grupo;
        this.avatares = new ArrayList<>();
        this.estadisticas = new EstadisticasCasilla(this);

        // Los establece luego la Calculadora según el tipo
        fianza = -1;
        abonoSalida = -1;
        impuestos = -1;
        banca = null;
        carcel = null;
        mazo = null;
    }

    public void listar() {
        if (tipo == TipoCasilla.Propiedad) {
            propiedad.listar();
            return;
        }

        System.out.println(toString());
    }

    @Override
    public String toString() {
        return switch (tipo) {
            case Propiedad -> propiedad.toString();
            case Salida -> """
                    \n%s: Casilla de inicio del juego.
                    Cada vez que un jugador pase por esta casilla recibirá %s.
                    """.formatted(getNombreFmt(), Consola.num(abonoSalida));
            case Carcel -> """
                    {
                        nombre: %s
                        fianza: %s
                    }""".formatted(getNombreFmt(), Consola.num(fianza));
            case IrCarcel -> """
                    \n%s: Si un jugador cae en esta casilla, se le enviará directamente
                    a la casilla Cárcel.
                    """.formatted(getNombreFmt());
            case Comunidad, Suerte -> """
                    \n%s: Si un jugador cae en esta casilla, tendrá que escoger una carta y
                    se realizará su acción específica.
                    """.formatted(getNombreFmt());
            case Impuestos -> """
                    {
                        nombre: %s
                        importe: %s
                    }""".formatted(getNombreFmt(), Consola.num(impuestos));
            case Parking -> """
                    {
                        nombre: %s
                        bote: %s
                    }""".formatted(getNombreFmt(), Consola.num(banca.getFortuna()));
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Casilla && ((Casilla) obj).posicion == this.posicion;
    }

    /**
     * Realiza la acción de casilla específica cuando un avatar cae en ella
     */
    public void accion(Jugador jugadorTurno, Dado dado) {
        if (isPropiedad()) {
            jugadorTurno.pagarAlquiler(propiedad, dado);
            return;
        }

        switch (tipo) {
            case IrCarcel -> jugadorTurno.getAvatar().irCarcel(carcel);
            case Parking -> {
                long bote = banca.getFortuna();
                jugadorTurno.ingresar(bote);
                jugadorTurno.getEstadisticas().anadirPremio(bote);
                banca.cobrar(bote, false);

                System.out.printf("El jugador recibe el bote de la banca: %s\n", Consola.num(bote));
            }

            case Impuestos -> {
                if (!jugadorTurno.cobrar(impuestos, true)) {
                    Consola.error("El jugador no tiene suficientes fondos para pagar los impuestos");
                    return;
                } else {
                    System.out.printf("El jugador paga de impuestos: %s\n", Consola.num(impuestos));
                    jugadorTurno.getEstadisticas().anadirTasa(impuestos);
                }

                banca.ingresar(impuestos);
                System.out.printf("Se han cobrado %s de impuestos a la banca\n", Consola.num(impuestos));
            }

            case Carcel -> System.out.println("El avatar se coloca en la Cárcel. Solo está de visita");
            case Comunidad -> mazo.caerComunidad(jugadorTurno);
            case Suerte -> mazo.caerSuerte(jugadorTurno);
        }
    }

    public String getNombre() {
        if (isPropiedad()) {
            return propiedad.getNombre();
        }

        return tipo.toString();
    }

    /**
     * Obtiene el nombre formateado (con colores) de la casilla
     */
    public String getNombreFmt() {
        if (isPropiedad()) {
            Estilo estilo = switch (propiedad.getTipo()) {
                case Solar -> Estilo.Normal;
                case Servicio, Transporte -> Estilo.Cursiva;
            };

            return Consola.fmt("%s - %s".formatted(propiedad.getNombre(), grupo.getNombre()), grupo.getCodigoColor(), estilo);
        }

        return Consola.fmt(tipo.toString(), grupo.getCodigoColor(), Estilo.Negrita);
    }

    public int getPosicion() {
        return posicion;
    }

    public TipoCasilla getTipo() {
        return tipo;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public boolean isPropiedad() {
        return propiedad != null; // O bien: tipo == TipoCasilla.Propiedad
    }

    public ArrayList<Avatar> getAvatares() {
        return avatares;
    }

    public EstadisticasCasilla getEstadisticas() {
        return estadisticas;
    }

    public long getFianza() {
        if (tipo != TipoCasilla.Carcel) {
            Consola.error("[Casilla] No se puede obtener la fianza de %s".formatted(getNombreFmt()));
            return -1;
        }

        return fianza;
    }

    public void setFianza(long fianza) {
        if (tipo != TipoCasilla.Carcel) {
            Consola.error("[Casilla] No se puede asignar una fianza a %s".formatted(getNombreFmt()));
            return;
        }

        if (fianza <= 0) {
            Consola.error("[Casilla] La fianza no puede ser nula o negativa");
            return;
        }

        this.fianza = fianza;
    }

    public long getAbonoSalida() {
        if (tipo != TipoCasilla.Salida) {
            Consola.error("[Casilla] No se puede obtener el abono de salida %s".formatted(getNombreFmt()));
            return -1;
        }

        return abonoSalida;
    }

    public void setAbonoSalida(long abonoSalida) {
        if (tipo != TipoCasilla.Salida) {
            Consola.error("[Casilla] No se puede asignar el abono de salida a %s".formatted(getNombreFmt()));
            return;
        }

        if (abonoSalida <= 0) {
            Consola.error("[Casilla] El abono de salida no puede ser nulo o negativo");
            return;
        }

        this.abonoSalida = abonoSalida;
    }

    public long getImpuestos() {
        if (tipo != TipoCasilla.Impuestos) {
            Consola.error("[Casilla] No se puede obtener los impuestos de %s".formatted(getNombreFmt()));
            return -1;
        }

        return impuestos;
    }

    public void setImpuestos(long impuestos) {
        if (tipo != TipoCasilla.Impuestos) {
            Consola.error("[Casilla] No se puede asignar unos impuestos a %s".formatted(getNombreFmt()));
            return;
        }

        if (impuestos <= 0) {
            Consola.error("[Casilla] Los impuestos no pueden ser nulos o negativos");
            return;
        }
        this.impuestos = impuestos;
    }

    public void setBanca(Jugador banca) {
        if (tipo != TipoCasilla.Parking && tipo != TipoCasilla.Impuestos) {
            Consola.error("[Casilla] No se puede asignar la banca a %s".formatted(getNombreFmt()));
            return;
        }

        if (banca == null) {
            Consola.error("[Casilla] La banca recibida no puede ser nula");
            return;
        }

        this.banca = banca;
    }

    public Casilla getCarcel() {
        if (tipo != TipoCasilla.IrCarcel) {
            Consola.error("[Casilla] No se puede obtener la Cárcel a partir de %s".formatted(getNombreFmt()));
            return null;
        }

        return carcel;
    }

    public void setCarcel(Casilla carcel) {
        if (tipo != TipoCasilla.IrCarcel) {
            Consola.error("[Casilla] No se puede asignar la cárcel a %s".formatted(getNombreFmt()));
            return;
        }

        if (carcel == null) {
            Consola.error("[Casilla] La casilla de cárcel recibida no puede ser null");
            return;
        }

        this.carcel = carcel;
    }

    public Mazo getMazo() {
        if (tipo != TipoCasilla.Comunidad && tipo != TipoCasilla.Suerte) {
            Consola.error("[Casilla] No se puede obtener el mazo de %s".formatted(getNombreFmt()));
            return null;
        }

        return mazo;
    }

    public void setMazo(Mazo mazo) {
        if (tipo != TipoCasilla.Comunidad && tipo != TipoCasilla.Suerte) {
            Consola.error("[Casilla] No se puede asignar el mazo a %s".formatted(getNombreFmt()));
            return;
        }

        if (mazo == null) {
            Consola.error("[Casilla] El mazo recibido no puede ser null");
            return;
        }

        this.mazo = mazo;
    }

    public void anadirAvatar(Avatar avatar) {
        avatares.add(avatar);
    }

    public void quitarAvatar(Avatar avatar) {
        avatares.remove(avatar);
    }

    public enum TipoCasilla {
        Propiedad, Salida, Carcel, IrCarcel, Parking, Impuestos, Comunidad, Suerte
    }
}