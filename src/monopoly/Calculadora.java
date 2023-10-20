package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Grupo;
import monopoly.casillas.Propiedad;
import monopoly.casillas.Propiedad.TipoPropiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;

import java.util.ArrayList;

/**
 * Clase encargada de poner los precios iniciales de
 * cada propiedad, calcular la fortuna de los jugadores,
 * el alquiler, etc.
 * <p>
 * Además, se encarga de realizar la operación de compra
 * y de pago del alquiler.
 *
 * @see Propiedad
 * @see Jugador
 */
public class Calculadora {
    public static final long PRECIO_GRUPO1 = 1_000_000;
    private long sumaSolares;
    private long nSolares;
    private final Jugador banca;

    public Calculadora(ArrayList<Casilla> casillas, Jugador banca) {
        this.banca = banca;
        sumaSolares = 0;
        nSolares = 0;

        for (Casilla c : casillas) {
            if (c.isPropiedad()) {
                // Asignar propiedades a la Banca
                Propiedad p = c.getPropiedad();
                banca.anadirPropiedad(p);
                p.setPropietario(banca);

                // Contar los solares y su precio total
                if (p.getTipo() == TipoPropiedad.Solar) {
                    sumaSolares += calcularPrecio(c.getPropiedad());
                    nSolares++;
                }
            }
        }

        // Ahora hay que asignar los precios a cada casilla
        for (Casilla c : casillas) {
            c.setPrecio(calcularPrecio(c));
        }
    }

    public static long calcularAlquiler(Propiedad p) {
        return p.getPrecio() / 10;
    }

    public long calcularAbonoSalida() {
        return sumaSolares / nSolares;
    }

    public long calcularFortuna() {
        return sumaSolares / 3;
    }

    public long calcularPrecio(Casilla c) {
        if (c.isPropiedad()) {
            return calcularPrecio(c.getPropiedad());
        }

        return switch (c.getNombre()) {
            case "Salida" -> calcularAbonoSalida(); // Cantidad que recibe al pasar por Salida
            case "Impuesto1" -> calcularAbonoSalida();
            case "Impuesto2" -> calcularAbonoSalida() / 2;
            case "Parking" -> banca.getFortuna(); // La fortuna de la banca representa este bote
            case "Cárcel" -> calcularAbonoSalida() / 4;
            default -> -1;
        };
    }
    // TODO: calcularPrecio(Edificacion)
    // TODO: calcularHipoteca(Propiedad)

    public long calcularPrecio(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();
        long precioGrupo = switch (p.getTipo()) {
            case Solar -> (long) (0.3 * g.getNumeroSolar() * PRECIO_GRUPO1 + PRECIO_GRUPO1);
            case Transporte -> calcularAbonoSalida();
            case Servicio -> (long) (0.75 * calcularAbonoSalida());
        };

        return precioGrupo / g.getNumeroCasillas();
    }

    /** Hace que el jugador compre la propiedad a la banca */
    public String comprar(Propiedad solar, Jugador jugador) {
        // Comprobar que el jugador no haya comprado ya la casilla
        if (jugador.getPropiedades().contains(solar)) {
            return Formatear.con("El jugador %s ya ha comprado la casilla %s.\n".formatted(jugador.getNombre(), solar.getCasilla().getNombre()), Color.Rojo);
        }

        // Comprobar que no sea propiedad de otro jugador
        if (solar.getPropietario() != banca) {
            return """
                   %s
                   %s pertenece a %s
                   """.formatted(Formatear.con("No se pueden comprar propiedades de otro jugador\n", Color.Rojo),
                                 Formatear.casillaNombre(solar.getCasilla()),
                                 Formatear.con(jugador.getNombre(), Color.Azul));
        }

        // Comprobar que el jugador tiene fortuna suficiente
        if (solar.getPrecio() > jugador.getFortuna()) {
            return Formatear.con("%s no dispone de suficiente dinero para comprar %s\n"
                    .formatted(jugador.getNombre(), solar.getCasilla().getNombre()), Color.Rojo);
        }

        jugador.cobrar(solar.getPrecio());
        jugador.anadirPropiedad(solar);
        banca.quitarPropiedad(solar);
        solar.setPropietario(jugador);

        return "El jugador %s ha comprado la casilla %s por %s\n"
                .formatted(jugador.getNombre(), Formatear.casillaNombre(solar.getCasilla()), Formatear.num(solar.getPrecio()));
    }

    /**
     * Hace que el jugador page el alquiler correspondiente
     * al dueño de la casilla en donde se encuentra
     */
    public String pagarAlquiler(Propiedad p, Jugador jugador, Dado dado) {
        if (p.getPropietario() == banca) return "";
        if (p.getPropietario() == jugador) return "";

        // TODO: comprobar si está hipotecado
        // TODO: precio servicios y transportes incrementado

        long importe = switch (p.getTipo()) {
            case Solar, Transporte -> p.getAlquiler(); // TODO: Tranporte con monopolio
            case Servicio -> p.getAlquiler() * dado.getValor() * 4;
        };

        if (importe > jugador.getFortuna()) {
            return Formatear.con("El jugador no tiene suficientes fondos para pagar el alquiler\n", Color.Rojo);
        }

        jugador.cobrar(importe);
        p.getPropietario().ingresar(importe);
        return "Se han pagado %s de alquiler a %s\n"
                .formatted(Formatear.num(p.getAlquiler()), Formatear.con(p.getPropietario().getNombre(), Color.Azul));
    }

    /**
     * Aumenta el precio de todos los solares que
     * aún no se han vendido al cabo de 4 vueltas.
     */
    public String aumentarPrecio(ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores){
        for(Jugador jugador : jugadores){
            if(jugador.getVueltas()<4) return "";
        }

        for (Casilla casilla : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (casilla.isPropiedad() && (casilla.getPropiedad().getPropietario() == null || casilla.getPropiedad().getPropietario() == banca)) {
                casilla.getPropiedad().aumentarPrecio();
            }
        }
        for(Jugador jugador: jugadores) {
            jugador.resetVuelta();
        }

        return "Se ha aumentado el precio de todas las casillas en venta\n";
    }
    /*
    No es para esta entrega:
    public static String valoresPropiedad(Propiedad solar){
        return """
                        Nombre:%s
                        propietario: %s
                        Precio: %d
                        alquiler: %d
                        valor hotel: %d
                        valor casa: %d
                        valor piscina: %d
                        valor pista de deporte: %d
                        alquiler una casa: %d
                        alquiler dos casas: %d
                        alquiler tres casas: %d
                        alquiler hotel: %d
                        alquiler piscina: %d
                        alquiler pista de deporte: %d
                """.formatted(solar.getCasilla().getNombre(),solar.getPropietario(),solar.getPrecio(),solar.getAlquiler(),
                (int)(solar.getPrecio()*0.6),(int)(solar.getPrecio()*0.6),(int)(solar.getPrecio()*0.4),(int)(solar.getPrecio()*1.25),
                solar.getPrecio()*5, solar.getPrecio()*15,solar.getPrecio()*30,solar.getPrecio()*70,solar.getPrecio()*25,solar.getPrecio()*25);
    }
    */
}
