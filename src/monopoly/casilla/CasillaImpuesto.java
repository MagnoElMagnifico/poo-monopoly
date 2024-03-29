package monopoly.casilla;

import monopoly.Juego;
import monopoly.JuegoConsts;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Banca;
import monopoly.jugador.Jugador;
import monopoly.utils.Consola;
import monopoly.utils.Dado;

public class CasillaImpuesto extends Casilla {
    private static int nImpuestos = 0;
    private final Banca banca;
    private long impuestos;

    public CasillaImpuesto(int posicion, Banca banca) {
        super(posicion);
        this.banca = banca;
    }

    public void setImpuestos(long abonoSalida) {
        // El último impuesto valdrá 1/2 del abono de salida
        // El primer impuesto valdrá 2/2 = 1 abono de salida
        nImpuestos++;
        impuestos = nImpuestos * abonoSalida / 2;
    }

    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    importe: %s
                }
                """.formatted(getNombreFmt(), Juego.consola.num(impuestos));
    }

    @Override
    public String listar() {
        return '\n' + getNombreFmt() + '\n';
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) throws ErrorFatalLogico {
        jugadorTurno.cobrar(impuestos, banca);
        Juego.consola.imprimir("El jugador paga de impuestos: %s\n".formatted(Juego.consola.num(impuestos)));
        jugadorTurno.getEstadisticas().anadirTasa(impuestos);

        banca.ingresar(impuestos);
        Juego.consola.imprimir("Se han cobrado %s de impuestos a la banca\n".formatted(Juego.consola.num(impuestos)));
    }

    @Override
    public String getNombre() {
        return "Impuesto";
    }

    @Override
    public int codColorRepresentacion() {
        return JuegoConsts.COD_COLOR_ESPECIAL;
    }

    @Override
    public Consola.Estilo estiloRepresentacion() {
        return JuegoConsts.EST_ESPECIAL;
    }
}
