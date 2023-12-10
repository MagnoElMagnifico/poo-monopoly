package monopoly.jugador;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.edificio.Edificio;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.*;
import monopoly.jugador.trato.*;
import monopoly.utils.Buscar;
import monopoly.utils.Consola;
import monopoly.utils.Listable;

import java.util.ArrayList;
import java.util.HashSet;

public class Jugador implements Listable, Buscar {
    private final String nombre;
    private final Avatar avatar;
    private final HashSet<Propiedad> propiedades;
    private final HashSet<Trato> tratos;
    private final EstadisticasJugador estadisticas;
    private long fortuna;
    private Jugador acreedor;

    public Jugador(String nombre, Avatar avatar, long fortunaInicial) {
        this.nombre = nombre;
        this.avatar = avatar;

        if (avatar != null) {
            avatar.setJugador(this);
        }
        this.fortuna = fortunaInicial;

        propiedades = new HashSet<>();
        tratos = new HashSet<>();
        estadisticas = new EstadisticasJugador(this);
    }

    private String listarEdificios() {
        StringBuilder edificios = new StringBuilder();
        edificios.append('[');

        // Si es el primer elemento que se añade a la lista,
        // no se añade coma; pero sí en el resto.
        boolean primero = true;
        for (Propiedad p : propiedades) {
            if (p instanceof Solar) {
                for (Edificio e : ((Solar) p).getEdificios()) {
                    if (primero) {
                        primero = false;
                    } else {
                        edificios.append(", ");
                    }

                    edificios.append(e.getNombreFmt());
                }
            }
        }

        edificios.append(']');
        return edificios.toString();
    }

    @Override
    public String listar() {
        // En la especificación de la entrega 1,
        // no hay ninguna diferencia entre listar
        // y describir jugadores.
        String str = this.toString();
        return str.substring(0, str.length() - 1); // eliminar el \n
    }

    @Override
    public String toString() {
        // @formatter:off
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    propiedades: %s
                    hipotecas: %s
                    edificios: %s
                }
                """.formatted(
                        nombre,
                        avatar.getId(),
                        Juego.consola.fmt(Juego.consola.num(fortuna), fortuna < 0? Consola.Color.Rojo : Consola.Color.Verde),
                        Juego.consola.listar(propiedades, (p) -> p.isHipotecada()? null : p.getNombreFmt()),
                        Juego.consola.listar(propiedades, (p) -> !p.isHipotecada()? null : p.getNombreFmt()),
                        listarEdificios());
        // @formatter:on
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Jugador && ((Jugador) obj).nombre.equalsIgnoreCase(this.nombre);
    }

    /**
     * Devuelve un String con información sobre la fortuna, gastos y propiedades del jugador
     */
    public void describirTransaccion() {
        // @formatter:off
        Juego.consola.imprimir("""
                {
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                    edificios: %s
                }
                """.formatted(Juego.consola.fmt(Juego.consola.num(fortuna), fortuna < 0? Consola.Color.Rojo : Consola.Color.Verde),
                Juego.consola.num(estadisticas.getGastos()),
                Juego.consola.listar(propiedades, Propiedad::getNombreFmt),
                listarEdificios()));
        // @formatter:on
    }

    public void comprar(Propiedad propiedad) throws ErrorComandoFortuna, ErrorFatalLogico {
        if (isEndeudado()) {
            throw new ErrorComandoFortuna("No puedes comprar nada si estás endeudado", this);
        }

        if (propiedades.contains(propiedad)) {
            throw new ErrorComandoFortuna("El jugador ya ha comprado esta casilla", this);
        }

        if (!(propiedad.getPropietario() instanceof Banca)) {
            throw new ErrorComandoFortuna("No se pueden comprar propiedades a otro jugador", this);
        }

        // Avatar especial
        if (avatar instanceof AvatarCoche) {
            if (!((AvatarCoche) avatar).isPuedeComprar()) {
                throw new ErrorComandoFortuna("El jugador ya ha realizado una compra en este turno", this);
            }
        }

        cobrar(propiedad.getPrecio());
        estadisticas.anadirInversion(propiedad.getPrecio());
        anadirPropiedad(propiedad);
        propiedad.comprar(this);

        if (avatar instanceof AvatarCoche) {
            ((AvatarCoche) avatar).noPuedeComprar();
        }

        Juego.consola.imprimir("""
                El jugador %s ha comprado la casilla %s por %s
                Ahora tiene una fortuna de %s
                """.formatted(nombre, propiedad.getNombreFmt(), Juego.consola.num(propiedad.getPrecio()), Juego.consola.num(fortuna)));

        if (propiedad.getGrupo().isMonopolio(this)) {
            Juego.consola.imprimir("""
                    Con esta casilla, %s completa el Monopolio de %s!
                    Ahora los alquileres de ese grupo valen el doble.
                    """.formatted(Juego.consola.fmt(nombre, Consola.Color.Azul), propiedad.getGrupo().getNombreFmt()));
        }

        describirTransaccion();
    }

    public void construir(Edificio edificio, int cantidad) throws ErrorComando, ErrorFatalLogico {
        if (isEndeudado()) {
            throw new ErrorComandoFortuna("No puedes edificar si estás endeudado", this);
        }

        if (!edificio.getSolar().perteneceAJugador(this)) {
            throw new ErrorFatalLogico("No se puede construir un edificio de otro jugador");
        }

        // Calcula el número de estancias del avatar en el solar
        int nEstanciasCasilla = 0;
        for (Casilla c : avatar.getHistorialCasillas()) {
            if (c.equals(edificio.getSolar())) {
                nEstanciasCasilla++;
            }
        }

        if (!edificio.getSolar().getGrupo().isMonopolio(this) && nEstanciasCasilla <= 2) {
            throw new ErrorComandoEdificio("El jugador tiene que tener el Monopolio o haber pasado más de 2 veces por la casilla para poder edificar");
        }

        int i = 0;
        try {
            for (i = 0; i < cantidad; i++) {
                if (i == 0) {
                    edificio.getSolar().edificar(edificio);
                } else {
                    edificio.getSolar().edificar(edificio.clonar());
                }
                cobrar(edificio.getValor());
                estadisticas.anadirInversion(edificio.getValor());
            }
        } finally {
            Juego.consola.imprimir("""
                %s ha construido %d %s(s) en el solar %s por %s.
                Ahora tiene una fortuna de %s.
                """.formatted(nombre, i, edificio.getClass().getSimpleName(), edificio.getSolar().getNombreFmt(), Juego.consola.num(edificio.getValor()), Juego.consola.num(fortuna)));
            describirTransaccion();
        }
    }

    public void vender(Solar solar, String tipoEdificio, int cantidad) throws ErrorComandoEdificio, ErrorFatalLogico {
        if (!solar.perteneceAJugador(this)) {
            throw new ErrorComandoEdificio("No se puede vender un edificio de otro jugador");
        }

        // Borrar los edificios en cuestión e ingresar la mitad de su valor
        ArrayList<Edificio> edificios = solar.getEdificios();
        long importeRecuperado = 0;
        int nBorrados = 0;

        for (int ii = 0; ii < edificios.size(); ii++) {
            if (edificios.get(ii).getClass().getSimpleName().equals(tipoEdificio)) {
                importeRecuperado += edificios.get(ii).getValor() / 2;
                edificios.remove(ii);
                nBorrados++;

                // Cuando se borra el elemento ii, el elemento siguiente
                // (ii + 1) pasará a estar en la posición ii; pero en la
                // siguiente iteración se irá a ii+1 (ii+2 antes de borrar).
                // Por tanto, nos estamos saltando un elemento.
                ii--;
            }

            if (nBorrados >= cantidad) {
                break;
            }
        }

        // NOTA: no se considera este importe recuperado para las estadísticas
        ingresar(importeRecuperado);

        Juego.consola.imprimir("""
                %s ha vendido %d %s(s) del solar %s por %s.
                Ahora tiene una fortuna de %s.
                """.formatted(nombre, cantidad, tipoEdificio, solar.getNombre(), Juego.consola.num(importeRecuperado), Juego.consola.num(fortuna)));
        describirTransaccion();
    }

    /**
     * Cobra una cantidad positiva no nula al jugador, y en caso de que
     * no pueda pagarlo se endeuda con el otro jugador dado.
     */
    public void cobrar(long cantidad, Jugador acreedor) throws ErrorFatalLogico {
        if (cantidad <= 0) {
            throw new ErrorFatalLogico("Se intentó cobrar una cantidad negativa");
        }

        fortuna -= cantidad;
        estadisticas.anadirGastos(cantidad);

        // Si no hay suficientes fondos y se quiere endeudar al jugador,
        // entonces se resta igualmente para conseguir una fortuna negativa.
        if (isEndeudado()) {
            this.acreedor = acreedor;
            Juego.consola.imprimir("No tienes suficientes fondos. Ahora estás endeudado con %s\n".formatted(acreedor.getNombre()));
        }
    }

    /**
     * Cobra al jugador una cantidad positiva no nula dada.
     * En caso de que no tenga dinero suficiente, se cancela lanzando una excepción.
     */
    public void cobrar(long cantidad) throws ErrorFatalLogico, ErrorComandoFortuna {
        if (cantidad <= 0) {
            throw new ErrorFatalLogico("Se intentó cobrar una cantidad negativa");
        }

        if (fortuna < cantidad) {
            throw new ErrorComandoFortuna("No tienes suficiente dinero", this);
        }

        fortuna -= cantidad;
        estadisticas.anadirGastos(cantidad);
    }

    public void ingresar(long cantidad) throws ErrorFatalLogico {
        if (cantidad <= 0) {
            throw new ErrorFatalLogico("Se intentó ingresar una cantidad negativa");
        }

        fortuna += cantidad;
    }

    public void acabarTurno() throws ErrorComandoEstadoPartida, ErrorComandoAvatar {
        if (isEndeudado()) {
            throw new ErrorComandoEstadoPartida("El jugador está endeudado: para la deuda o declárate en bancarrota para avanzar");
        }

        avatar.acabarTurno();
    }

    public void anadirPropiedad(Propiedad p) throws ErrorFatalLogico {
        if (!propiedades.add(p)) {
            throw new ErrorFatalLogico("El jugador ya tenía la propiedad %s".formatted(p.getNombreFmt()));
        }
    }

    public void quitarPropiedad(Propiedad p) throws ErrorFatalLogico {
        if (!propiedades.remove(p)) {
            throw new ErrorFatalLogico("El jugador no poseía la propiedad %s".formatted(p.getNombreFmt()));
        }
    }

    public boolean isEndeudado() {
        return fortuna < 0;
    }

    public long getFortuna() {
        return fortuna;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public HashSet<Propiedad> getPropiedades() {
        return propiedades;
    }

    public Jugador getAcreedor() {
        return acreedor;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public EstadisticasJugador getEstadisticas() {
        return estadisticas;
    }

    public HashSet<Trato> getTratos() {
        return tratos;
    }

    public void crearTrato(Jugador jugAcepta, Trato trato) {
        this.tratos.add(trato);
        jugAcepta.tratos.add(trato);
        Juego.consola.imprimir(trato.toString() + '\n');
    }

    public void aceptarTrato(String nombre) throws ErrorComandoFortuna, ErrorFatalLogico, ErrorComandoNoEncontrado, ErrorComandoTrato {
        Trato trato = Buscar.porNombre(nombre, tratos);

        if (!trato.getJugadorAcepta().equals(this)) {
            // La lista de tratos es compartida entre los tratos que he propuesto
            // y los tratos que me han propuesto. Entonces, si no soy el que debe
            // aceptar, es que yo he propuesto el trato.
            throw new ErrorComandoTrato("No puedes aceptar un trato que tú has propuesto", this);
        }

        if (trato.isAceptado()) {
            throw new ErrorComandoTrato("El trato ya había sido aceptado", this);
        }

        trato.aceptar();
        Juego.consola.imprimir("Aceptado:\n%s\n".formatted(trato.toString()));
    }

    public void eliminarTrato(String nombre) throws ErrorComandoNoEncontrado, ErrorComandoTrato {
        Trato trato = Buscar.porNombre(nombre, tratos);

        if (!trato.getJugadorPropone().equals(this)) {
            throw new ErrorComandoTrato("No puedes eliminar un trato que no hayas propuesto", this);
        }

        if (trato.isAceptado()) {
            throw new ErrorComandoTrato("No puedes eliminar un trato que ha sido aceptado", this);
        }

        tratos.remove(trato);
        trato.getJugadorAcepta().tratos.remove(trato);
        Juego.consola.imprimir("Se ha eliminado el trato %s\n".formatted(trato.getNombre()));
    }
}
