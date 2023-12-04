package monopoly.casillas.cartas;

import monopoly.Tablero;
import monopoly.jugadores.Jugador;
import monopoly.casillas.Propiedad
import monopoly.error.*;


public class CartaSuerte extends Carta {
    // Modificar cuando se cree la clase juego y solar

    public CartaSuerte() {
        super();
    }

    @Override
    public void accion() {
        Jugador jugadorTurno = tablero.getJugadorTurno();

        // Se ingresa si la cantidad es positiva y se cobra si es negativa
        long cantidad = 0;

        switch (get(id)) {
            case 3 -> cantidad = 500_000L;
            case 6 -> cantidad = 1_000_000L;
            case 7 -> cantidad = -1_500_000L;
            case 12 -> cantidad = -150_000L;
            case 8 -> {
                for (Propiedad p : jugadorTurno.getPropiedades()) {
                    if (p.getTipo() == Propiedad.TipoPropiedad.Solar) {
                        for (Edificio e : p.getEdificios()) {
                            cantidad -= switch (e.getTipo()) {
                                case Casa -> 4_000_000L;
                                case Hotel -> 1_500_000L;
                                case Piscina -> 200_000L;
                                case PistaDeporte -> 750_000L;
                            };
                        }
                    }
                }

                if (cantidad == 0) {
                    System.out.println("El jugador no tiene edificios, por lo que no tiene que pagar nada.");
                    // ErrorComando();
                    return;
                }
            }
            case 10 -> {
                long cantidadPorJugador = 250_000L;
                if (!jugadorTurno.cobrar(cantidadPorJugador * tablero.getJugadores().size(), true)) {
                    Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
                    // SinFortuna();
                }

                // NOTA: el pago a otros jugadores no se considera una tasa.
                // No hace falta actualizar las estadísticas del jugadorTurno

                // NOTA: se está ingresando el dinero a cada jugador incluso si el jugador no puede pagarlo

                for (Jugador j : tablero.getJugadores()) {
                    // No ingresar la cantidad a sí mismo
                    if (j.equals(jugadorTurno)) {
                        continue;
                    }

                    j.ingresar(cantidadPorJugador);
                    j.getEstadisticas().anadirPremio(cantidadPorJugador);
                }

                return;
            }

            default -> Consola.error("[Carta] ID no soportado para realizar acción");
            // default -> ErrorLogico();
        }

         if (cantidad > 0) {
            jugadorTurno.ingresar(cantidad);
            jugadorTurno.getEstadisticas().anadirPremio(cantidad);
        } else {
            if (jugadorTurno.cobrar(-cantidad, true)) {
                jugadorTurno.getEstadisticas().anadirTasa(-cantidad);
                tablero.getBanca().ingresar(-cantidad);
            } else {
                Consola.error("El jugador no tiene suficiente dinero para completar su fortuna");
                // SinFortuna();
            }
        }
    }
}