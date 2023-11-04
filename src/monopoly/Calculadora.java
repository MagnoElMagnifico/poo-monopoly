package monopoly;

import monopoly.casillas.Casilla;
import monopoly.casillas.Grupo;
import monopoly.casillas.Mazo;
import monopoly.casillas.Propiedad;
import monopoly.casillas.Propiedad.TipoPropiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Lector;

import java.util.ArrayList;

/**
 * Clase encargada de poner los precios iniciales de
 * cada propiedad, calcular la fortuna de los jugadores,
 * el alquiler, etc.
 *
 * @see Propiedad
 * @see Jugador
 */
public class Calculadora {
    public static final long PRECIO_GRUPO1 = 1_000_000;
    private long sumaSolares;
    private long nSolares;

    public Calculadora(ArrayList<Casilla> casillas, Jugador banca, Mazo mazo) {
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

        // Ahora hay que asignar los precios y otros atributos a cada casilla
        // Para establecer una referencia a la cárcel en IrCárcel, se deben
        // recorrer las casillas al revés, dado que la Cárcel aparece después
        // de IrCárcel.
        // También se asigna el mazo a las casillas de carta.
        Casilla carcel = null;
        for (int ii = casillas.size() - 1; ii >= 0; ii--) {
            Casilla c = casillas.get(ii);

            switch (c.getTipo()) {
                case Propiedad -> {
                    Propiedad p = c.getPropiedad();
                    p.setPrecio(calcularPrecio(p));
                    p.setAlquiler(calcularAlquiler(p));
                }
                case Salida -> c.setAbonoSalida(calcularAbonoSalida());
                case Impuestos -> c.setImpuestos(calcularAbonoSalida()); // TODO: uno debe valer la mitad
                case Carcel -> {
                    c.setFianza(calcularAbonoSalida() / 4);
                    carcel = c;
                }
                case IrCarcel -> c.setCarcel(carcel);
                case Parking -> c.setBanca(banca);
                case Comunidad, Suerte -> c.setMazo(mazo);
            }
        }
    }

    /** Comprueba si el dueño de la propiedad tiene todo el grupo */
    public static boolean tieneGrupo(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();
        Jugador propietario = p.getPropietario();

        for (Casilla c : g.getCasillas()) {
            if (!propietario.getPropiedades().contains(c.getPropiedad())) {
                return false;
            }
        }

        return true;
    }

    public static long calcularAlquiler(Propiedad p) {
        long alquiler = p.getPrecio() / 10;

        if (tieneGrupo(p)) {
            alquiler *= 2;
        }

        // TODO: recorrer edificios

        return alquiler;
    }

    /**
     * Aumenta el precio de todos los solares que
     * aún no se han vendido al cabo de 4 vueltas.
     */
    public static void aumentarPrecio(ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores) {
        for (Jugador jugador : jugadores) {
            if (jugador.getAvatar().getVueltas() < 4) {
                return;
            }
        }

        for (Casilla c : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (c.isPropiedad() && (c.getPropiedad().getPropietario() == null || c.getPropiedad().getPropietario().isBanca())) {
                Propiedad p = c.getPropiedad();
                p.setPrecio((long) (p.getPrecio() * 1.05));
            }
        }

        for (Jugador jugador : jugadores) {
            jugador.getAvatar().resetVuelta();
        }

        System.out.println("Se ha aumentado el precio de todas las casillas en venta\n");
    }

    public long calcularAbonoSalida() {
        return sumaSolares / nSolares;
    }

    // TODO: calcularPrecio(Edificacion)
    // TODO: calcularHipoteca(Propiedad)

    public long calcularFortuna() {
        return sumaSolares / 3;
    }

    public long calcularPrecio(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();

        // @formatter:off
        long precioGrupo = switch (p.getTipo()) {
            case Solar      -> (long) (0.3 * g.getNumeroSolar() * PRECIO_GRUPO1 + PRECIO_GRUPO1);
            case Transporte -> calcularAbonoSalida();
            case Servicio   -> (long) (0.75 * calcularAbonoSalida());
        };
        // @formatter:on

        return precioGrupo / g.getNumeroCasillas();
    }
}
