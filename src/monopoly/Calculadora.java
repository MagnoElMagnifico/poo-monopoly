package monopoly;

import monopoly.casillas.*;
import monopoly.casillas.Edificio.TipoEdificio;
import monopoly.casillas.Propiedad.TipoPropiedad;
import monopoly.jugadores.Jugador;
import monopoly.utilidades.Consola;

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
    private int nAumentosPrecio;

    public Calculadora(ArrayList<Casilla> casillas) {
        sumaSolares = 0;
        nSolares = 0;
        nAumentosPrecio = 1;

        // Contar los solares y su precio total
        for (Casilla c : casillas) {
            if (c.isPropiedad()) {
                if (c.getPropiedad().getTipo() == TipoPropiedad.Solar) {
                    sumaSolares += precio(c.getPropiedad());
                    nSolares++;
                }
            }
        }
    }

    public static long alquilerEdificio(Propiedad p, TipoEdificio tipo, int cantidad) {
        long alquilerSolar = p.getAlquiler();
        return switch (tipo) {
            case Casa -> switch (cantidad) {
                case 0 -> 0;
                case 1 -> 5 * alquilerSolar;
                case 2 -> 15 * alquilerSolar;
                case 3 -> 35 * alquilerSolar;
                default -> 50 * alquilerSolar;
            };
            case Hotel -> 70 * alquilerSolar * cantidad;
            case Piscina, PistaDeporte -> 25 * alquilerSolar * cantidad;
        };
    }

    /**
     * Calcula y devuelve el precio del alquiler de una propiedad
     */
    public static long calcularAlquiler(Propiedad p) {
        long alquilerPropiedad = switch (p.getTipo()) {
            case Solar -> p.getPrecio() / 10;
            case Transporte -> {
                // Contar el número de transportes que posee el jugador
                int nTransportes = 0;

                Grupo g = p.getCasilla().getGrupo();
                Jugador propietario = p.getPropietario();

                for (Casilla c : g.getCasillas()) {
                    if (c.isPropiedad() && c.getPropiedad().getTipo() == TipoPropiedad.Transporte && propietario.getPropiedades().contains(c.getPropiedad())) {
                        nTransportes++;
                    }
                }

                // Finalmente, el alquiler es el factor de transporte por el
                // porcentaje que el jugador posea.
                yield (long) ((float) p.getPrecio() * (float) nTransportes / (float) g.getNumeroCasillas());
            }

            case Servicio -> {
                // Contar el número de servicios
                int nServicios = 0;

                Grupo g = p.getCasilla().getGrupo();
                Jugador propietario = p.getPropietario();

                for (Casilla c : g.getCasillas()) {
                    if (c.isPropiedad() && c.getPropiedad().getTipo() == TipoPropiedad.Servicio && propietario.getPropiedades().contains(c.getPropiedad())) {
                        nServicios++;
                    }
                }

                // Finalmente, si posee solo 1 servicio, se multiplica por 4. Si tiene los dos, se multiplica por 10.
                yield switch (nServicios) {
                    case 1 -> (long) ((p.getPrecio() * 2.85) / 200) * 4;
                    case 2 -> (long) ((p.getPrecio() * 2.85) / 200) * 10;
                    default -> {
                        Consola.error("[Calculadora] Estado imposible: nServicios es no es 1 ni 2");
                        yield 0;
                    }
                };
                // El factor de servicio será 200 veces inferior a la cantidad que recibirá el jugador cada vez que completa
                // una vuelta al tablero
            }
        };

        // Calcular el precio incluyendo los edificios (solo si es solar)
        long alquilerEdificio = 0;

        if (p.getTipo() == TipoPropiedad.Solar) {
            int nCasas = 0;
            int nHoteles = 0;
            int nPiscinas = 0;
            int nPistas = 0;

            // Contar el número de edificios de cada tipo.
            // No se usa Propiedad.contarEdificios porque se iteraría 4 veces
            // sobre los mismos datos. De esta forma solo se itera 1 vez.
            for (Edificio e : p.getEdificios()) {
                switch (e.getTipo()) {
                    case Hotel -> nHoteles++;
                    case Piscina -> nPiscinas++;
                    case PistaDeporte -> nPistas++;
                    case Casa -> nCasas++;
                }
            }

            alquilerEdificio += alquilerEdificio(p, TipoEdificio.Casa, nCasas);
            alquilerEdificio += alquilerEdificio(p, TipoEdificio.Hotel, nHoteles);
            alquilerEdificio += alquilerEdificio(p, TipoEdificio.Piscina, nPiscinas);
            alquilerEdificio += alquilerEdificio(p, TipoEdificio.PistaDeporte, nPistas);
        }

        // Si el dueño tiene el monopolio, el alquiler se duplica
        if (p.getTipo() == TipoPropiedad.Solar && tieneGrupo(p)) {
            alquilerPropiedad *= 2;
        }

        return alquilerPropiedad + alquilerEdificio;
    }

    /**
     * Comprueba si el dueño de la propiedad tiene todo el grupo
     */
    public static boolean tieneGrupo(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();
        Jugador propietario = p.getPropietario();

        // A la banca no se le aplican los monopolios
        if (propietario.isBanca()) {
            return false;
        }

        for (Casilla c : g.getCasillas()) {
            if (!propietario.getPropiedades().contains(c.getPropiedad())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calcula y devuelve el precio de edificar un edificio en la propiedad dada
     */
    public static long precio(Edificio.TipoEdificio tipo, Propiedad solar) {
        long precioSolar = solar.getPrecio();
        // @formatter:off
        return switch (tipo) {
            case Casa, Hotel  -> (long) (0.6 * precioSolar);
            case Piscina      -> (long) (0.4 * precioSolar);
            case PistaDeporte -> (long) (1.25 * precioSolar);
        };
        // @formatter:on
    }

    public static long calcularHipoteca(Propiedad propiedad) {
        return propiedad.getPrecio() / 2;
    }

    public static long calcularDeshipoteca(Propiedad propiedad) {
        return (long) (calcularHipoteca(propiedad) * 1.1);
    }

    /**
     * Aumenta el precio de todos los solares que
     * aún no se han vendido al cabo de 4 vueltas.
     */
    public void aumentarPrecio(ArrayList<Casilla> casillas, ArrayList<Jugador> jugadores) {
        for (Jugador jugador : jugadores) {
            if (jugador.getEstadisticas().getVueltas() - 4 * nAumentosPrecio < 0) {
                return;
            }
        }

        nAumentosPrecio++;

        for (Casilla c : casillas) {
            // Si la casilla se puede comprar y no tiene dueño, es que está en venta
            if (c.isPropiedad() && (c.getPropiedad().getPropietario() == null || c.getPropiedad().getPropietario().isBanca())) {
                Propiedad p = c.getPropiedad();
                p.setPrecio((long) (p.getPrecio() * 1.05));
            }
        }

        System.out.println("Se ha aumentado el precio de todas las casillas en venta");
    }

    /**
     * Asigna los precios y otros atributos a cada casilla según su tipo.
     * <p>
     * Además, pone a la banca como propietario de todas las propiedades.
     * <p>
     * También se asigna el mazo a las casillas de carta.
     */
    public void asignarValores(ArrayList<Casilla> casillas, Jugador banca, Mazo mazo) {
        // Para establecer una referencia a la cárcel en IrCárcel, se deben
        // recorrer las casillas al revés, dado que la Cárcel aparece después
        // de IrCárcel.
        Casilla carcel = null;
        int nImpuestos = 0;
        for (int ii = casillas.size() - 1; ii >= 0; ii--) {
            Casilla c = casillas.get(ii);

            switch (c.getTipo()) {
                case Propiedad -> {
                    Propiedad p = c.getPropiedad();

                    // Asignar propiedades a la Banca
                    banca.anadirPropiedad(p);
                    p.setPropietario(banca);

                    p.setPrecio(precio(p));
                    p.actualizarAlquiler();
                }
                case Salida -> c.setAbonoSalida(calcularAbonoSalida());
                case Impuestos -> {
                    c.setBanca(banca);
                    // El último impuesto valdrá 1/2 del abono de salida
                    // El primer impuesto valdrá 2/2 = 1 abono de salida
                    nImpuestos++;
                    c.setImpuestos(nImpuestos * calcularAbonoSalida() / 2);
                }
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

    /**
     * Devuelve el abono que reciben los jugadores cuando su avatar pasa por la casilla de salida.
     */
    public long calcularAbonoSalida() {
        return sumaSolares / nSolares;
    }

    /**
     * Calcula y devuelve la fortuna inicial que poseeran los jugadores
     */
    public long calcularFortuna() {
        return sumaSolares / 3;
    }

    /**
     * Calcula y devuelve el precio de una propiedad
     */
    public long precio(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();

        // @formatter:off
        long precioGrupo = switch (p.getTipo()) {
            case Transporte -> calcularAbonoSalida();
            case Servicio   -> (long) (0.75 * calcularAbonoSalida());
            case Solar      -> (long) (0.3 * g.getNumeroSolar() * PRECIO_GRUPO1 + PRECIO_GRUPO1);
        };
        // @formatter:on

        return precioGrupo / g.getNumeroCasillas();
    }
}
