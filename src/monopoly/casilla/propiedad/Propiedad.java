package monopoly.casilla.propiedad;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Banca;
import monopoly.jugador.Jugador;
import monopoly.utils.Consola;
import monopoly.utils.Dado;

/**
 * Representa una casilla que se puede comprar por un jugador.
 * <p>
 * Existen tres tipos:
 *
 * <li>Solares</li>
 * <li>Servicios</li>
 * <li>Transporte</li>
 *
 * Se trata de una clase abstracta porque hay ciertas funcionalidades que dependen de cada
 * tipo de Propiedad, como el alquiler y el precio.
 *
 * @see Casilla
 * @see Jugador
 */
public abstract class Propiedad extends Casilla {
    private final String nombre;
    private final Grupo grupo;
    private Jugador propietario;
    private boolean hipotecada;

    public Propiedad(int posicion, Grupo grupo, String nombre, Jugador propietario) {
        super(posicion);
        this.grupo = grupo;
        this.nombre = nombre;

        this.propietario = propietario;
        hipotecada = false;
    }

    /** <b>NOTA</b>: requerida por la especificación de la entrega 3. */
    public abstract long getPrecio();

    /** <b>NOTA</b>: requerida por la especificación de la entrega 3. */
    public abstract long getAlquiler();

    /** Multiplica el precio actual por el factor dado para aumentarlo o disminuirlo */
    public abstract void factorPrecio(float factor) throws ErrorFatalLogico;

    public abstract long getCosteHipoteca();

    public abstract long getCosteDeshipoteca();

    /** Para las estadísticas */
    public abstract long getAlquilerTotalCobrado();

    @Override
    public String listar() {
        return """
                {
                    nombre: %s
                    tipo: Propiedad
                    precio: %s
                }
                """.formatted(getNombreFmt(), Juego.consola.num(getPrecio()));
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
               {
                   tipo: Propiedad
                   nombre: %s
                   precio: %s
                   alquiler: %s
                   propietario: %s
                   hipotecada?: %s
               }""".formatted(
                    nombre,
                    Juego.consola.num(getPrecio()),
                    Juego.consola.num(getAlquiler()),
                    propietario.getNombre(),
                    hipotecada? "Sí" : "No");
        // @formatter:on
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) {
        if (propietario instanceof Banca || propietario.equals(jugadorTurno) || hipotecada) {
            return;
        }

        // Se multiplica el alquiler por el valor de los dados en caso de que sea un servicio
        long importe = p.getTipo() == Propiedad.TipoPropiedad.Servicio ? p.getAlquiler() * dado.getValor() : p.getAlquiler();

        // Se debe cobrar todo el importe, aunque el jugador no pueda pagarlo.
        // La cuenta se quedará en números negativos (es decir, está endeudado)
        propietario.ingresar(importe);

        if (!cobrar(importe, true)) {
            acreedor = p.getPropietario();
            Juego.consola.error("El jugador no tiene suficientes fondos para pagar el alquiler");
            return;
        }

        Juego.consola.imprimir("Se han pagado %s de alquiler a %s\n".formatted(Juego.consola.num(importe), Juego.consola.fmt(propietario.getNombre(), Consola.Color.Azul)));

        getAlquilerTotalCobrado();
        jugadorTurno.getEstadisticas().anadirPagoAlquiler(importe);
        propietario.getEstadisticas().anadirCobroAlquiler(importe);
    }

    public void setPropietario(Jugador jugador) {
        propietario = jugador;
    }

    /** <b>NOTA</b>: requerida por la especificación de la entrega 3. */
    public void comprar(Jugador jugador) {
        // TODO
    }

    /** <b>NOTA</b>: requerida por la especificación de la entrega 3. */
    public boolean perteneceAJugador(Jugador jugador) {
        return propietario.equals(jugador);
    }

    public void hipotecar() {
        if (propietario == null || propietario.isBanca()) {
            Juego.consola.error("No se puede hipotecar una propiedad sin dueño");
            return;
        }

        if (hipotecada) {
            Juego.consola.error("No se puede hipotecar, ya está hipotecada");
            return;
        }

        // TODO
        /*
        if (tipo == TipoPropiedad.Solar && !edificios.isEmpty()) {
            Juego.consola.error("No se puede hipotecar una propiedad con edificios");
            return;
        }
        */

        hipotecada = true;
        long cantidad = getCosteHipoteca();
        propietario.ingresar(cantidad);

        // NOTA: esta cantidad no se tiene en cuenta para las estadísticas

        Juego.consola.imprimir("Se ha hipotecado %s por %s\n".formatted(getNombreFmt(), Juego.consola.num(cantidad)));
        propietario.describirTransaccion();
    }

    public void deshipotecar() {
        if (!hipotecada) {
            Juego.consola.error("No se puede deshipotecar, no está hipotecada");
            return;
        }

        long cantidad = getCosteDeshipoteca();

        if (!propietario.cobrar(cantidad, false)) {
            Juego.consola.error("No tienes suficientes fondos para deshipotecar esa propiedad");
            return;
        }

        hipotecada = false;
        Juego.consola.imprimir("Se ha deshipotecado %s por %s\n".formatted(getNombreFmt(), Juego.consola.num(cantidad)));
        propietario.describirTransaccion();
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public boolean isHipotecada() {
        return hipotecada;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public Jugador getPropietario() {
        return propietario;
    }
}
