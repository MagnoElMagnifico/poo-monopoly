package monopoly.casilla.carta;

import monopoly.Juego;
import monopoly.casilla.edificio.Edificio;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.ErrorFatal;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;

/**
 * Es una clase final porque ya implementa todo lo necesario y no tiene sentido crear subtipos.
 */
public final class CartaSuerte extends Carta {
    private final Juego juego;

    public CartaSuerte(int id, String descripcion, Juego juego) {
        super(id, descripcion);
        this.juego = juego;
    }

    @Override
    public String toString() {
        return "Carta de Suerte: " + super.getDescripcion();
    }

    @Override
    public void accionCarta() throws ErrorFatal {
        Jugador jugadorTurno = juego.getJugadorTurno();

        // Se ingresa si la cantidad es positiva y se cobra si es negativa
        long cantidad = 0;

        switch (super.getId()) {
            case 3 -> cantidad = 500_000L;
            case 6 -> cantidad = 1_000_000L;
            case 7 -> cantidad = -1_500_000L;
            case 12 -> cantidad = -150_000L;
            case 8 -> {
                for (Propiedad p : jugadorTurno.getPropiedades()) {
                    if (p instanceof Solar) {
                        for (Edificio e : ((Solar) p).getEdificios()) {
                            cantidad -= switch (e.getClass().getName()) {
                                case "Casa" -> 4_000_000L;
                                case "Hotel" -> 1_500_000L;
                                case "Piscina" -> 200_000L;
                                case "PistaDeporte" -> 750_000L;
                                default -> throw new ErrorFatalLogico("Tipo de edificio no soportado");
                            };
                        }
                    }
                }

                if (cantidad == 0) {
                    Juego.consola.imprimir("El jugador no tiene edificios, por lo que no tiene que pagar nada.");
                    return;
                }
            }
            case 10 -> {
                long cantidadPorJugador = 250_000L;
                jugadorTurno.cobrar(cantidadPorJugador * juego.getJugadores().size(), juego.getBanca());

                // NOTA: el pago a otros jugadores no se considera una tasa.
                // No hace falta actualizar las estadísticas del jugadorTurno

                // NOTA: se está ingresando el dinero a cada jugador incluso si el jugador no puede pagarlo

                for (Jugador j : juego.getJugadores()) {
                    // No ingresar la cantidad a sí mismo
                    if (j.equals(jugadorTurno)) {
                        continue;
                    }

                    j.ingresar(cantidadPorJugador);
                    j.getEstadisticas().anadirPremio(cantidadPorJugador);
                }

                return;
            }

            default -> throw new ErrorFatal("ID no soportado");
        }

        aplicarCantidad(cantidad, jugadorTurno, juego.getBanca());
    }
}
