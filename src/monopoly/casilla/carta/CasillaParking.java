package monopoly.casilla.carta;

import monopoly.Juego;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Banca;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class CasillaParking extends CasillaAccion {
    private final Banca banca;

    public CasillaParking(int posicion, Banca banca) {
        super(posicion);
        this.banca = banca;
    }

    @Override
    public String toString() {
        return """
               {
                   nombre: %s
                   bote: %s
               }""".formatted(getNombreFmt(), Juego.consola.num(banca.getFortuna()));
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) throws ErrorFatalLogico, ErrorComandoFortuna {
        long bote = banca.getFortuna();
        jugadorTurno.ingresar(bote);
        jugadorTurno.getEstadisticas().anadirPremio(bote);
        banca.cobrar(bote);

        Juego.consola.imprimir("El jugador recibe el bote de la banca: %s\n".formatted(Juego.consola.num(bote)));
    }

    @Override
    public String getNombre() {
        return "Parking";
    }
}
