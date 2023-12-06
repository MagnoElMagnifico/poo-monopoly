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
    public abstract long getPrecio() throws ErrorFatalLogico;

    /**
     * Alquiler base de la propiedad.
     * <br>
     * <b>NOTA</b>: requerida por la especificación de la entrega 3.
     */
    public abstract long getAlquiler() throws ErrorFatalLogico;

    /** Devuelve el importe real que se cobra al jugador que cae en esta propiedad */
    public abstract long getAlquiler(Jugador jugador, Dado dado) throws ErrorFatalLogico;

    public long getCosteHipoteca() throws ErrorFatalLogico {
        return getPrecio() / 2;
    }

    public long getCosteDeshipoteca() throws ErrorFatalLogico {
        return (long) (1.1 * (float) getCosteHipoteca());
    }

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
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getNombreFmt() {
        return Juego.consola.fmt("%s - %s".formatted(getNombre(), grupo.getNombre()), grupo.getCodigoColor());
    }

    @Override
    public void accion(Jugador jugadorTurno, Dado dado) {
        if (propietario instanceof Banca || propietario.equals(jugadorTurno) || hipotecada) {
            return;
        }

        // Se multiplica el alquiler por el valor de los dados en caso de que sea un servicio
        long importe = getAlquiler(jugadorTurno, dado);

        // Se debe cobrar todo el importe, aunque el jugador no pueda pagarlo.
        // La cuenta se quedará en números negativos (es decir, está endeudado)
        propietario.ingresar(importe);

        jugadorTurno.cobrar(importe);

        Juego.consola.imprimir("Se han pagado %s de alquiler a %s\n".formatted(Juego.consola.num(importe), Juego.consola.fmt(propietario.getNombre(), Consola.Color.Azul)));

        getAlquilerTotalCobrado();
        jugadorTurno.getEstadisticas().anadirPagoAlquiler(importe);
        propietario.getEstadisticas().anadirCobroAlquiler(importe);
    }

    /** <b>NOTA</b>: requerida por la especificación de la entrega 3. */
    public void comprar(Jugador jugador) {
        propietario = jugador;
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

    public boolean isHipotecada() {
        return hipotecada;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador jugador) {
        propietario = jugador;
    }
}
