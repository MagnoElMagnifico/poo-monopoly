package monopoly.utilidades;

import monopoly.casillas.Edificio;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Jugador;

public class EstadisticasJugador {
    // @formatter:off
    private final Jugador jugador;

    private long inversiones;      /* Dinero invertido en la compra de propiedades y edificaciones */
    private long pagoTasas;        /* Pago de tasas y/o impuestos */
    private long cobroAlquileres;  /* Alquileres cobrados de otros jugadores */
    private long pagoAlquileres;   /* Alquileres pagados a otros jugadores */
    private long abonosSalida;     /* Cantidad total de los abonos recibidos al completar una vuelta */
    private long premios;          /* Dinero recibido por premios */
    private long gastos;           /* Dinero total gastado desde el inicio de la partida */

    private int vecesEncarcelado;
    private int nVueltas;
    private int nTiradas;
    // @formatter:on

    /**
     * Estadísticas para el jugador
     */
    public EstadisticasJugador(Jugador jugador) {
        this.jugador = jugador;

        inversiones = 0;
        pagoTasas = 0;
        cobroAlquileres = 0;
        pagoAlquileres = 0;
        abonosSalida = 0;
        premios = 0;
        gastos = 0;

        vecesEncarcelado = 0;
        nVueltas = 0;
        nTiradas = 0;
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
               {
                   jugador: %s
                   dinero invertido: %s
                   pago de tasas e impuestos: %s
                   cobro de alquileres: %s
                   pago de alquileres: %s
                   abono total de salida: %s
                   premios de inversiones o bote: %s
                   gastos: %s
                   veces en la cárcel: %d
                   número de vueltas: %s
                   número de tiradas: %s
               }
               """.formatted(jugador.getNombre(),
                             Consola.num(inversiones),
                             Consola.num(pagoTasas),
                             Consola.num(cobroAlquileres),
                             Consola.num(pagoAlquileres),
                             Consola.num(abonosSalida),
                             Consola.num(premios),
                             Consola.num(gastos),
                             vecesEncarcelado,
                             nVueltas,
                             nTiradas);
        // @formatter:on
    }

    public void anadirInversion(long cantidad) {
        inversiones += cantidad;
    }

    public void anadirTasa(long cantidad) {
        pagoTasas += cantidad;
    }

    public void anadirCobroAlquiler(long cantidad) {
        cobroAlquileres += cantidad;
    }

    public void anadirPagoAlquiler(long cantidad) {
        pagoAlquileres += cantidad;
    }

    public void anadirAbonoSalida(long cantidad) {
        abonosSalida += cantidad;
    }

    public void anadirPremio(long cantidad) {
        premios += cantidad;
    }

    public void anadirGastos(long cantidad) {
        gastos += cantidad;
    }

    public void anadirEstanciaCarcel() {
        vecesEncarcelado++;
    }

    public void anadirVuelta() {
        nVueltas++;
    }

    public void quitarVuelta() {
        nVueltas--;
    }

    public void anadirTirada() {
        nTiradas++;
    }

    public long getInversiones() {
        return inversiones;
    }

    public long getPagoTasas() {
        return pagoTasas;
    }

    public long getCobroAlquileres() {
        return cobroAlquileres;
    }

    public long getPagoAlquileres() {
        return pagoAlquileres;
    }

    public long getAbonosSalida() {
        return abonosSalida;
    }

    public long getPremios() {
        return premios;
    }

    public long getGastos() {
        return gastos;
    }

    public long getCapital() {
        long capital = jugador.getFortuna();

        for (Propiedad p : jugador.getPropiedades()) {
            capital += p.getPrecio();

            for (Edificio e : p.getEdificios()) {
                capital += e.getValor();
            }
        }

        return capital;
    }

    public int getVecesEncarcelado() {
        return vecesEncarcelado;
    }

    public int getVueltas() {
        return nVueltas;
    }

    public int getTiradas() {
        return nTiradas;
    }
}
