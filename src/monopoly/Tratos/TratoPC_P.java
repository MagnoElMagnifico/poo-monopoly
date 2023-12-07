package monopoly.Tratos;

import monopoly.Juego;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.error.ErrorComando;
import monopoly.error.ErrorComandoFortuna;
import monopoly.jugador.Jugador;

public class TratoPC_P extends Trato{
    private final Propiedad inter;
    private final Propiedad acept;
    private final long cantidad;
    public TratoPC_P(String nombre, Jugador interesado, Jugador benefactor, Propiedad inter, long cantidad, Propiedad acept) {
        super(nombre, interesado, benefactor);
        this.inter=inter;
        this.acept=acept;
        this.cantidad=cantidad;
    }

    @Override
    public String toString() {
        return """
                %s
                Cambiar %s y %s por %s
                
                """.formatted(
                        super.toString(),
                        inter.getNombreFmt(), Juego.consola.num(cantidad), acept.getNombreFmt());
    }

    @Override
    public void aceptar() throws ErrorComandoFortuna {
        Jugador j1 = getInteresado();
        Jugador j2 = getAceptador();
        j1.cobrar(cantidad);
        j1.anadirPropiedad(acept);
        j2.anadirPropiedad(inter);
        j1.quitarPropiedad(inter);
        j2.quitarPropiedad(acept);
        j2.ingresar(cantidad);
        super.aceptar();
    }
}
