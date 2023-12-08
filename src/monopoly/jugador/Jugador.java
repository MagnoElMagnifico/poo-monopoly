package monopoly.jugador;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.edificio.Edificio;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.casilla.propiedad.Solar;
import monopoly.error.*;
import monopoly.jugador.trato.*;
import monopoly.utils.Consola;
import monopoly.utils.Listable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Jugador implements Listable {
    private final String nombre;
    private final Avatar avatar;
    private final HashSet<Propiedad> propiedades;
    private final EstadisticasJugador estadisticas;
    private long fortuna;
    private Jugador acreedor;
    private HashSet<Trato> tratos;

    public Jugador(String nombre, Avatar avatar, long fortunaInicial) {
        this.nombre = nombre;
        this.avatar = avatar;
        if (avatar != null) {
            avatar.setJugador(this);
        }
        this.fortuna = fortunaInicial;

        propiedades = new HashSet<>();
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
        return this.toString();
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
                }""".formatted(
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
                    edificios:
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
            if (((AvatarCoche) avatar).isPuedeComprar()) {
                throw new ErrorComandoFortuna("El jugador ya ha realizado una compra en este turno", this);
            }
            ((AvatarCoche) avatar).noPuedeComprar();
        }

        cobrar(propiedad.getPrecio());
        estadisticas.anadirInversion(propiedad.getPrecio());
        anadirPropiedad(propiedad);
        propiedad.comprar(this);

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

    public void construir(Edificio edificio) throws ErrorComandoFortuna, ErrorFatalLogico, ErrorComandoEdificio {
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

        cobrar(edificio.getValor());
        edificio.getSolar().edificar(edificio);
        estadisticas.anadirInversion(edificio.getValor());

        Juego.consola.imprimir("""
                %s ha construido %d %s(s) en el solar %s por %s.
                Ahora tiene una fortuna de %s.
                """.formatted(nombre, edificio.getValor(), edificio.getClass().getName(), edificio.getSolar().getNombreFmt(), Juego.consola.num(edificio.getValor()), Juego.consola.num(fortuna)));
        describirTransaccion();
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
            if (edificios.get(ii).getClass().getName().equals(tipoEdificio)) {
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

    public void anadirPropiedad(Propiedad propiedad) {
        propiedades.add(propiedad);
    }

    public void quitarPropiedad(Propiedad propiedad) {
        propiedades.remove(propiedad);
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

    public String getNombre() {
        return nombre;
    }

    public EstadisticasJugador getEstadisticas() {
        return estadisticas;
    }

    /**
     * Intercambio de propiedades: p1 <--> p2
     */
    public void crearTrato(Jugador jugador, Propiedad p1, Propiedad p2) throws ErrorComandoJugador {
        if (!p1.perteneceAJugador(this) || !p2.perteneceAJugador(jugador)) {
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no os pertenecen", this);
        }

        TratoP_P trato = new TratoP_P(this, jugador, p1, p2);
        this.tratos.add(trato);
        jugador.tratos.add(trato);
    }

    /**
     * Vender propiedad: p <--> cantidad
     */
    public void crearTrato(Jugador jugador, Propiedad p, long cantidad) throws ErrorComandoJugador {
        if (!p.perteneceAJugador(this)) {
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no te pertenecen", this);
        }

        TratoP_C trato = new TratoP_C(this, jugador, p, cantidad);
        this.tratos.add(trato);
        jugador.tratos.add(trato);
    }

    /**
     * Comprar propiedad: cantidad <--> p
     */
    public void crearTrato(Jugador jugador, long cantidad, Propiedad p) throws ErrorComandoJugador {
        if (!p.perteneceAJugador(jugador)) {
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no os pertenecen", this);
        }

        if (this.fortuna < cantidad) {
            throw new ErrorComandoJugador("No tienes suficiente dinero para ofrecer el trato", this);
        }

        TratoC_P trato = new TratoC_P(this, jugador, cantidad, p);
        this.tratos.add(trato);
        jugador.tratos.add(trato);
    }

    /**
     * Intercambiar con compensación: p1 <--> p2 + cantidad
     */
    public void crearTrato(Jugador jugador, Propiedad p1, Propiedad p2, long cantidad) throws ErrorComandoJugador {
        if (!p1.perteneceAJugador(this) || !p2.perteneceAJugador(jugador)) {
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no os pertenecen.", this);
        }

        TratoP_PC trato = new TratoP_PC(this, jugador, p1, p2, cantidad);
        this.tratos.add(trato);
        jugador.tratos.add(trato);
    }

    /**
     * Intercambiar con compensación: p1 + cantidad <--> p2
     */
    public void crearTrato(String nombre, Jugador jugador, Propiedad p1, long cantidad, Propiedad p2) throws ErrorComandoJugador, ErrorComandoFortuna {
        if (!p1.perteneceAJugador(this) || !p2.perteneceAJugador(jugador)) {
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no te pertenecen.", this);
        }

        if (this.fortuna < cantidad) {
            throw new ErrorComandoFortuna("No tienes suficiente dinero para ofrecer el trato", this);
        }

        TratoPC_P trato = new TratoPC_P(nombre, this, jugador, p1, cantidad, p2);
        this.tratos.add(trato);
        jugador.tratos.add(trato);
    }

    public void aceptarTrato(String nombre) throws ErrorComandoFortuna, ErrorFatalLogico {
        for (Trato t : tratos) {
            if (t.getNombre().equalsIgnoreCase(nombre) && t.getAceptador().equals(this)) {
                t.aceptar();
                Juego.consola.imprimir("Aceptado:\n%s".formatted(t.toString()));
                break;
            }
        }
    }

    public void eliminarTrato(String nombre) {
        Iterator<Trato> itr = tratos.iterator();
        while (itr.hasNext()) {
            Trato trato = itr.next();
            if (trato.getNombre().equalsIgnoreCase(nombre) && trato.getInteresado().equals(this)) {
                trato.getAceptador().tratos.remove(trato);
                this.tratos.remove(trato);
                break;
            }
        }
    }
}
