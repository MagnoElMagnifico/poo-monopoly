package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.casillas.Casilla;
import monopoly.casillas.Edificio;
import monopoly.casillas.Edificio.TipoEdificio;
import monopoly.casillas.Grupo;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar.TipoAvatar;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Dado;
import monopoly.utilidades.EstadisticasJugador;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 *
 * @date 2-10-2023
 * @see Avatar
 */
public class Jugador {
    private final String nombre;
    private final Avatar avatar;
    private final boolean banca;
    private final HashSet<Propiedad> propiedades;
    private final EstadisticasJugador estadisticas;
    private long fortuna;
    private Jugador acreedor;

    /**
     * Crea el jugador especial Banca
     */
    public Jugador() {
        this.nombre = "Banca";
        this.avatar = null;
        this.banca = true;
        this.fortuna = 0;
        this.propiedades = new HashSet<>(28);
        this.acreedor = null;
        this.estadisticas = null;
    }

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */
    public Jugador(String nombre, TipoAvatar tipo, char id, Casilla casillaInicial, long fortuna) {
        this.nombre = nombre;
        this.avatar = new Avatar(tipo, id, this, casillaInicial);
        this.banca = false;
        this.fortuna = fortuna;
        this.propiedades = new HashSet<>();
        this.acreedor = null;
        this.estadisticas = new EstadisticasJugador(this);
    }

    /**
     * Comprueba las restricciones de construcción
     */
    private static boolean edificable(Propiedad solar, Edificio.TipoEdificio tipo, int cantidad) {
        Grupo grupo = solar.getCasilla().getGrupo();
        final int maxEdificios = grupo.getNumeroCasillas();

        switch (tipo) {
            case Casa -> {
                // Si no hay el máximo de edificios, se puede tener hasta 4 casas.
                // Sino, solo hasta maxEdificios.
                if (grupo.contarEdificios(TipoEdificio.Hotel) < maxEdificios) {
                    if (solar.contarEdificios(TipoEdificio.Casa) + cantidad > 4) {
                        Consola.error("No se pueden edificar más de 4 casas en un solar cuando no hay el máximo de hoteles");
                        return false;
                    }
                } else if (grupo.contarEdificios(TipoEdificio.Casa) + cantidad > maxEdificios) {
                    Consola.error("No se pueden edificar más de %d casas en un grupo cuando hay el número máximo de hoteles".formatted(maxEdificios));
                    return false;
                }
            }

            case Hotel -> {
                if (grupo.contarEdificios(TipoEdificio.Hotel) + cantidad > maxEdificios) {
                    Consola.error("No se pueden edificar más de %d hoteles en este grupo".formatted(maxEdificios));
                    return false;
                }

                if (solar.contarEdificios(TipoEdificio.Casa) < 4 * cantidad) {
                    Consola.error("Se necesitan 4 casas en el solar para edificar un hotel");
                    return false;
                }
            }

            case Piscina -> {
                if (grupo.contarEdificios(TipoEdificio.Piscina) + cantidad > maxEdificios) {
                    Consola.error("No se pueden edificar más de %d piscinas en este grupo".formatted(maxEdificios));
                    return false;
                }

                if (grupo.contarEdificios(TipoEdificio.Hotel) < 1 || grupo.contarEdificios(Edificio.TipoEdificio.Casa) < 2) {
                    Consola.error("Se necesita 1 hotel y 2 casas en el grupo para edificar una piscina");
                    return false;
                }
            }

            case PistaDeporte -> {
                if (grupo.contarEdificios(TipoEdificio.PistaDeporte) + cantidad >= maxEdificios) {
                    Consola.error("No se pueden edificar más de %d pistas de deporte en este grupo".formatted(maxEdificios));
                    return false;
                }

                if (grupo.contarEdificios(TipoEdificio.Hotel) < 2) {
                    Consola.error("Se necesitan 2 hoteles en el grupo para construir una pista de deporte");
                    return false;
                }
            }
        }

        return true;
    }

    private String listarEdificios() {
        StringBuilder edificios = new StringBuilder();
        edificios.append('[');

        // Si es el primer elemento que se añade a la lista,
        // no se añade coma; pero sí en el resto.
        boolean primero = true;
        for (Propiedad p : propiedades) {
            if (p.getTipo() == Propiedad.TipoPropiedad.Solar) {
                for (Edificio e : p.getEdificios()) {
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
    public String toString() {
        if (banca) {
            return "Jugador Especial: Banca\n";
        }

        // @formatter:off
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    propiedades: %s
                    hipotecas: %s
                    edificios: %s
                }""".formatted(nombre,
                               avatar.getId(),
                               Consola.num(fortuna),
                               Consola.listar(propiedades.iterator(), (p) -> p.isHipotecada()? null : p.getCasilla().getNombreFmt()),
                               Consola.listar(propiedades.iterator(), (p) -> p.isHipotecada()? p.getCasilla().getNombreFmt() : null),
                               listarEdificios());
        // @formatter:on
    }

    /**
     * Devuelve un String con información sobre la fortuna, gastos y propiedades del jugador
     */
    public void describirTransaccion() {
        // @formatter:off
        System.out.printf("""
                {
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                    edificios: %s
                }
                """, Consola.num(fortuna),
                     Consola.num(estadisticas.getGastos()),
                     Consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()),
                     listarEdificios());
        // @formatter:on
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Jugador && ((Jugador) obj).getAvatar().equals(avatar);
    }

    /**
     * Hace que el jugador compre la propiedad a la banca
     *
     * @return True cuando la operación resultó exitosa, false en otro caso.
     */
    public boolean comprar(Propiedad p) {
        // Movimientos especiales del avatar: Comprobar que no se haya comprado ya en este turno
        if (!avatar.isPuedeComprar()) {
            Consola.error("El jugador %s ya ha realizado una compra en este turno".formatted(nombre));
            return false;
        }

        // Comprobar que el jugador no haya comprado ya la casilla
        if (propiedades.contains(p)) {
            Consola.error("El jugador %s ya ha comprado la casilla %s.".formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }

        // Comprobar que no sea propiedad de otro jugador
        if (!p.getPropietario().isBanca()) {
            Consola.error("No se pueden comprar propiedades de otro jugador");
            System.out.printf("%s pertenece a %s\n", p.getCasilla().getNombreFmt(), Consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));
            return false;
        }

        // Comprobar que el jugador tiene fortuna suficiente
        if (!cobrar(p.getPrecio(), false)) {
            Consola.error("%s no dispone de suficiente dinero para comprar %s".formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }
        estadisticas.anadirInversion(p.getPrecio());

        p.getPropietario().quitarPropiedad(p);
        anadirPropiedad(p);
        p.setPropietario(this);

        System.out.printf("""
                El jugador %s ha comprado la casilla %s por %s
                Ahora tiene una fortuna de %s
                """, nombre, p.getCasilla().getNombreFmt(), Consola.num(p.getPrecio()), Consola.num(fortuna));

        // Actualizar los precios de los alquileres si se acaba de
        // completar un Monopolio
        if (Calculadora.tieneGrupo(p)) {
            for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
                c.getPropiedad().actualizarAlquiler();
            }

            Grupo g = p.getCasilla().getGrupo();
            System.out.printf("""
                    Con esta casilla, %s completa el Monopolio de %s!
                    Ahora los alquileres de ese grupo valen el doble.
                    """, Consola.fmt(nombre, Consola.Color.Azul), Consola.fmt(g.getNombre(), g.getCodigoColor()));
        }

        avatar.noPuedeComprar();
        describirTransaccion();
        return true;
    }

    /**
     * Realiza la compra del edificio y lo construye en el solar dado.
     *
     * @return True si la operación es exitosa y false en otro caso.
     */
    public boolean comprar(TipoEdificio tipoEdificio, int cantidad) {
        Casilla casilla = avatar.getCasilla();

        if (!casilla.isPropiedad() || casilla.getPropiedad().getTipo() != Propiedad.TipoPropiedad.Solar) {
            Consola.error("No se puede edificar en una casilla que no sea un solar");
            return false;
        }

        Propiedad solar = casilla.getPropiedad();

        if (!solar.getPropietario().equals(this)) {
            Consola.error("No se puede edificar en una propiedad que no te pertenece");
            return false;
        }

        // Calcula el número de estancias del avatar en el solar
        int nEstanciasCasilla = 0;
        for (Casilla c : avatar.getHistorialCasillas()) {
            if (c.equals(casilla)) {
                nEstanciasCasilla++;
            }
        }

        // Comprobar que el jugador tiene el monopolio o ha caído dos veces en la casilla
        if (!Calculadora.tieneGrupo(solar) && nEstanciasCasilla <= 2) {
            Consola.error("El jugador tiene que tener el Monopolio o haber pasado más de 2 veces por la casilla para poder edificar");
            return false;
        }

        if (!edificable(solar, tipoEdificio, cantidad)) {
            return false;
        }

        Edificio e = new Edificio(tipoEdificio, solar);

        // Comprobar que tiene el dinero
        if (!cobrar(cantidad * e.getValor(), false)) {
            Consola.error("El jugador no tiene los fondos suficientes para edificar.\nNecesita %s.".formatted(Consola.num(cantidad * e.getValor())));
            return false;
        }
        estadisticas.anadirInversion(cantidad * e.getValor());

        System.out.printf("""
                %s ha construido %d %s(s) en el solar %s por %s.
                Ahora tiene una fortuna de %s.
                """, nombre, cantidad, e.getTipo(), casilla.getNombreFmt(), Consola.num(cantidad * e.getValor()), Consola.num(fortuna));

        // Actualizar el solar
        solar.anadirEdificio(e);
        for (int ii = 1; ii < cantidad; ii++) {
            solar.anadirEdificio(new Edificio(tipoEdificio, solar));
        }

        solar.actualizarAlquiler();

        // Quitar las 4 casas requeridas por el hotel
        if (tipoEdificio == TipoEdificio.Hotel) {
            for (int ii = 0; ii < 4 * cantidad; ii++) {
                solar.quitarEdificio(TipoEdificio.Casa);
            }
        }

        describirTransaccion();
        return true;
    }

    public boolean vender(TipoEdificio tipoEdificio, Propiedad solar, int cantidad) {
        if (!solar.getPropietario().equals(this)) {
            Consola.error("No se puede vender un edificio de otro jugador: %s pertenece a %s".formatted(solar.getNombre(), solar.getPropietario().getNombre()));
            return false;
        }

        int nEdificios = solar.contarEdificios(tipoEdificio);
        if (nEdificios < cantidad) {
            Consola.error("No se pueden vender %d %s(s) dado que solo hay %d".formatted(cantidad, tipoEdificio, nEdificios));
            return false;
        }

        // Borrar los edificios en cuestión e ingresar la mitad de su valor
        ArrayList<Edificio> edificios = solar.getEdificios();
        int nBorrados = 0;
        long importeRecuperado = 0;

        for (int ii = 0; ii < edificios.size(); ii++) {
            if (edificios.get(ii).getTipo() == tipoEdificio) {
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

        ingresar(importeRecuperado);

        // NOTA: no se considera este importe recuperado para las estadísticas

        System.out.printf("""
                %s ha vendido %d %s(s) del solar %s por %s.
                Ahora tiene una fortuna de %s.
                """, nombre, cantidad, tipoEdificio, solar.getNombre(), Consola.num(importeRecuperado), Consola.num(fortuna));

        // Actualizar el estado
        solar.actualizarAlquiler();

        describirTransaccion();
        return true;
    }

    /**
     * Hace que el jugador page el alquiler correspondiente
     * al dueño de la casilla en donde se encuentra
     */
    public void pagarAlquiler(Propiedad p, Dado dado) {
        if (p.getPropietario().isBanca() || p.getPropietario().equals(this) || p.isHipotecada()) {
            return;
        }

        // TODO: alquiler servicio y operacion transporte
        long importe = switch (p.getTipo()) {
            case Solar, Transporte -> p.getAlquiler();
            case Servicio -> p.getAlquiler() * dado.getValor() * 4;
        };

        // Se debe cobrar todo el importe, aunque el jugador no pueda pagarlo.
        // La cuenta se quedará en números negativos (es decir, está endeudado)
        p.getPropietario().ingresar(importe);

        if (!cobrar(importe, true)) {
            acreedor = p.getPropietario();
            Consola.error("El jugador no tiene suficientes fondos para pagar el alquiler");
            return;
        }

        System.out.printf("Se han pagado %s de alquiler a %s\n", Consola.num(p.getAlquiler()), Consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));

        estadisticas.anadirPagoAlquiler(importe);
        p.getCasilla().getEstadisticas().anadirCobroAlquiler(importe);
        p.getPropietario().getEstadisticas().anadirCobroAlquiler(importe);
    }

    /**
     * Cobra al jugador una cantidad de dinero
     *
     * @param cantidad Dinero a ingresar
     * @return True si la operación es correcta, false en otro caso
     */
    public boolean cobrar(long cantidad, boolean endeudar) {
        if (cantidad <= 0) {
            Consola.error("[Jugador] Se intentó cobrar una cantidad nula o negativa a %s".formatted(nombre));
            return false;
        }

        // Si hay suficientes fondos, no hay problema
        if (fortuna >= cantidad) {
            fortuna -= cantidad;
            estadisticas.anadirGastos(cantidad);
            return true;
        }

        // Si no hay suficientes fondos y se quiere endeudar al jugador,
        // entonces se resta igualmente para conseguir una fortuna negativa.
        if (endeudar) {
            fortuna -= cantidad;
            estadisticas.anadirGastos(cantidad);
        }

        // Si no se quiere endeudar, no se realiza el cobro.

        return false;
    }

    /**
     * Ingresa una cantidad de dinero al jugador
     */
    public void ingresar(long cantidad) {
        if (cantidad < 0) {
            Consola.error("[Jugador] No se puede ingresar una cantidad negativa o nula");
            return;
        }

        fortuna += cantidad;
    }

    public void hipotecar(Propiedad propiedad) {
        if (!propiedades.contains(propiedad)) {
            Consola.error("No se puede hipotecar una propiedad que no te pertenece");
            return;
        }

        if (propiedad.isHipotecada()) {
            Consola.error("No se puede hipotecar, ya está hipotecada");
            return;
        }

        propiedad.setHipotecada(true);
        long cantidad = Calculadora.calcularHipoteca(propiedad);
        ingresar(cantidad);

        // NOTA: esta cantidad no se tiene en cuenta para las estadísticas

        System.out.printf("Se ha hipotecado %s por %s\n", propiedad.getCasilla().getNombreFmt(), Consola.num(cantidad));
        describirTransaccion();
    }

    public void deshipotecar(Propiedad propiedad) {
        if (!propiedades.contains(propiedad)) {
            Consola.error("No se puede hipotecar una propiedad que no te pertenece");
            return;
        }

        if (!propiedad.isHipotecada()) {
            Consola.error("No se puede deshipotecar, no está hipotecada");
            return;
        }

        long cantidad = Calculadora.calcularHipoteca(propiedad);

        if (!cobrar(cantidad, false)) {
            Consola.error("No tienes suficientes fondos para deshipotecar esa propiedad");
            return;
        }

        propiedad.setHipotecada(false);
        System.out.printf("Se ha deshipotecado %s por %s\n", propiedad.getCasilla().getNombreFmt(), Consola.num(cantidad));
    }

    /**
     * Determina si
     */
    public boolean acabarTurno() {
        if (isEndeudado()) {
            Consola.error("El jugador %s está endeudado: paga la deuda o declárate en bancarrota para poder avanzar".formatted(nombre));
            return false;
        }

        return avatar.acabarTurno();
    }

    public boolean isBanca() {
        return banca;
    }

    public String getNombre() {
        return nombre;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public long getFortuna() {
        return fortuna;
    }

    public EstadisticasJugador getEstadisticas() {
        return estadisticas;
    }

    public HashSet<Propiedad> getPropiedades() {
        return propiedades;
    }

    public void anadirPropiedad(Propiedad p) {
        propiedades.add(p);
    }

    public void quitarPropiedad(Propiedad p) {
        propiedades.remove(p);
    }

    public boolean isEndeudado() {
        return fortuna < 0;
    }

    public Jugador getAcreedor() {
        return acreedor;
    }
}
