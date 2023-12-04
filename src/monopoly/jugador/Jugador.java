package monopoly.jugador;


import monopoly.Tratos.Trato;
import monopoly.Tratos.TratoP_P;
import monopoly.error.ErrorComandoFormato;
import monopoly.error.ErrorComandoFortuna;
import monopoly.utils.Consola;
import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.edificio.Edificio;
import monopoly.casilla.edificio.Edificio.TipoEdificio;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.utils.Dado;
import monopoly.utils.Listable;


import java.util.ArrayList;
import java.util.HashSet;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 *
 * @see Avatar
 */
public class Jugador implements Listable {
    private final String nombre;
    private final Avatar avatar;
    private final HashSet<Propiedad> propiedades;
    private final EstadisticasJugador estadisticas;
    private long fortuna;
    private Jugador acreedor;
    private HashSet<Trato> tratos;

    public Jugador(String nombre, Avatar avatar, long fortuna) {
        this.nombre = nombre;
        this.avatar = avatar;

        this.fortuna = fortuna;
        this.propiedades = new HashSet<>();
        this.acreedor = null;
        this.estadisticas = new EstadisticasJugador(this);
        this.tratos =new HashSet<>();
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
                        Juego.consola.error("No se pueden edificar más de 4 casas en un solar cuando no hay el máximo de hoteles");
                        return false;
                    }
                } else if (grupo.contarEdificios(TipoEdificio.Casa) + cantidad > maxEdificios) {
                    Juego.consola.error("No se pueden edificar más de %d casas en un grupo cuando hay el número máximo de hoteles".formatted(maxEdificios));
                    return false;
                }
            }

            case Hotel -> {
                if (grupo.contarEdificios(TipoEdificio.Hotel) + cantidad > maxEdificios) {
                    Juego.consola.error("No se pueden edificar más de %d hoteles en este grupo".formatted(maxEdificios));
                    return false;
                }

                if (solar.contarEdificios(TipoEdificio.Casa) < 4 * cantidad) {
                    Juego.consola.error("Se necesitan 4 casas en el solar para edificar un hotel");
                    return false;
                }
            }

            case Piscina -> {
                if (grupo.contarEdificios(TipoEdificio.Piscina) + cantidad > maxEdificios) {
                    Juego.consola.error("No se pueden edificar más de %d piscinas en este grupo".formatted(maxEdificios));
                    return false;
                }

                if (grupo.contarEdificios(TipoEdificio.Hotel) < 1 || grupo.contarEdificios(Edificio.TipoEdificio.Casa) < 2) {
                    Juego.consola.error("Se necesita 1 hotel y 2 casas en el grupo para edificar una piscina");
                    return false;
                }
            }

            case PistaDeporte -> {
                if (grupo.contarEdificios(TipoEdificio.PistaDeporte) + cantidad >= maxEdificios) {
                    Juego.consola.error("No se pueden edificar más de %d pistas de deporte en este grupo".formatted(maxEdificios));
                    return false;
                }

                if (grupo.contarEdificios(TipoEdificio.Hotel) < 2) {
                    Juego.consola.error("Se necesitan 2 hoteles en el grupo para construir una pista de deporte");
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
                               Juego.consola.fmt(Juego.consola.num(fortuna), fortuna < 0? Juego.consola.Color.Rojo : Juego.consola.Color.Verde),
                               Juego.consola.listar(propiedades.iterator(), (p) -> p.isHipotecada()? null : p.getCasilla().getNombreFmt()),
                               Juego.consola.listar(propiedades.iterator(), (p) -> p.isHipotecada()? p.getCasilla().getNombreFmt() : null),
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
                """, Juego.consola.fmt(Juego.consola.num(fortuna), fortuna < 0? Consola.Color.Rojo : Consola.Color.Verde),
                     Juego.consola.num(estadisticas.getGastos()),
                     Juego.consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()),
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

        if (isEndeudado()) {
            Juego.consola.error("No puedes comprar nada si estas endeudado");
            return false;
        }

        // Comprobar que el jugador no haya comprado ya la casilla
        if (propiedades.contains(p)) {
            Juego.consola.error("El jugador %s ya ha comprado la casilla %s.".formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }

        // Comprobar que no sea propiedad de otro jugador
        if (!p.getPropietario().isBanca()) {
            Juego.consola.error("No se pueden comprar propiedades de otro jugador");
            System.out.printf("%s pertenece a %s\n", p.getCasilla().getNombreFmt(), Juego.consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));
            return false;
        }

        // Comprobar que el jugador tiene fortuna suficiente
        if (!cobrar(p.getPrecio(), false)) {
            Juego.consola.error("%s no dispone de suficiente dinero para comprar %s".formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }

        AvatarCoche avatarCoche;
        if(avatar instanceof AvatarCoche){
            avatarCoche = (AvatarCoche) avatar;
            // Movimientos especiales del avatar: Comprobar que no se haya comprado ya en este turno
            if (!avatarCoche.isPuedeComprar()) {
                Juego.consola.error("El jugador %s ya ha realizado una compra en este turno".formatted(nombre));
                return false;
            }
            else{
                avatarCoche.noPuedeComprar();
            }
        }
        estadisticas.anadirInversion(p.getPrecio());

        p.getPropietario().quitarPropiedad(p);
        anadirPropiedad(p);
        p.setPropietario(this);

        System.out.printf("""
                El jugador %s ha comprado la casilla %s por %s
                Ahora tiene una fortuna de %s
                """, nombre, p.getCasilla().getNombreFmt(), Juego.consola.num(p.getPrecio()), Juego.consola.num(fortuna));

        // Actualizar los precios de los alquileres si se acaba de
        // completar un Monopolio
        switch (p.getTipo()) {
            case Solar -> {
                if (Calculadora.tieneGrupo(p)) {
                    for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
                        c.getPropiedad().actualizarAlquiler();
                    }

                    Grupo g = p.getCasilla().getGrupo();
                    System.out.printf("""
                    Con esta casilla, %s completa el Monopolio de %s!
                    Ahora los alquileres de ese grupo valen el doble.
                    """, Juego.consola.fmt(nombre, Consola.Color.Azul), Juego.consola.fmt(g.getNombre(), g.getCodigoColor()));
                }
            }
            case Servicio, Transporte -> {
                for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
                    c.getPropiedad().actualizarAlquiler();
                }
            }
        }

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

        if (isEndeudado()) {
            Juego.consola.error("No puedes edificar si estas endeudado");
            return false;
        }

        if (!casilla.isPropiedad() || casilla.getPropiedad().getTipo() != Propiedad.TipoPropiedad.Solar) {
            Juego.consola.error("No se puede edificar en una casilla que no sea un solar");
            return false;
        }

        Propiedad solar = casilla.getPropiedad();

        if (solar.isHipotecada()) {
            Juego.consola.error("No puedes edificar sobre una propiedad hipotecada");
            return false;
        }

        if (!solar.getPropietario().equals(this)) {
            Juego.consola.error("No se puede edificar en una propiedad que no te pertenece");
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
            Juego.consola.error("El jugador tiene que tener el Monopolio o haber pasado más de 2 veces por la casilla para poder edificar");
            return false;
        }

        if (!edificable(solar, tipoEdificio, cantidad)) {
            return false;
        }

        Edificio e = new Edificio(tipoEdificio, solar);

        // Comprobar que tiene el dinero
        if (!cobrar(cantidad * e.getValor(), false)) {
            Juego.consola.error("El jugador no tiene los fondos suficientes para edificar.\nNecesita %s.".formatted(Juego.consola.num(cantidad * e.getValor())));
            return false;
        }
        estadisticas.anadirInversion(cantidad * e.getValor());

        System.out.printf("""
                %s ha construido %d %s(s) en el solar %s por %s.
                Ahora tiene una fortuna de %s.
                """, nombre, cantidad, e.getTipo(), casilla.getNombreFmt(), Juego.consola.num(cantidad * e.getValor()), Juego.consola.num(fortuna));

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
            Juego.consola.error("No se puede vender un edificio de otro jugador: %s pertenece a %s".formatted(solar.getNombre(), solar.getPropietario().getNombre()));
            return false;
        }

        int nEdificios = solar.contarEdificios(tipoEdificio);
        if (nEdificios < cantidad) {
            Juego.consola.error("No se pueden vender %d %s(s) dado que solo hay %d".formatted(cantidad, tipoEdificio, nEdificios));
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
                """, nombre, cantidad, tipoEdificio, solar.getNombre(), Juego.consola.num(importeRecuperado), Juego.consola.num(fortuna));

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
    }

    /**
     * Cobra al jugador una cantidad de dinero
     *
     * @param cantidad Dinero a ingresar
     * @return True si la operación es correcta, false en otro caso
     */
    public void cobrar(long cantidad) throws ErrorComandoFortuna {
        if (cantidad <= 0) {
            throw new ErrorComandoFortuna("[Jugador] Se intentó cobrar una cantidad nula o negativa a %s".formatted(nombre), this);
        }
        if (fortuna < cantidad){
            throw new ErrorComandoFortuna("[Jugador] %s no tiene dinero suficiente.".formatted(nombre),this);
        }
        // Si hay suficientes fondos, no hay problema
        else  {
            fortuna -= cantidad;
            estadisticas.anadirGastos(cantidad);
        }
    }
    public void cobrar(long cantidad, Jugador acreedor) throws ErrorComandoFortuna {
        if (cantidad <= 0) {
            throw new ErrorComandoFortuna("[Jugador] Se intentó cobrar una cantidad nula o negativa a %s".formatted(nombre), this);
        }
        // Si no hay suficientes fondos y se quiere endeudar al jugador,
        // entonces se resta igualmente para conseguir una fortuna negativa.
        if (fortuna < cantidad) {
            fortuna -= cantidad;
            estadisticas.anadirGastos(cantidad);
            this.acreedor = acreedor;
            throw new ErrorComandoFortuna("[Jugador] %s no tiene dinero suficiente.".formatted(nombre), acreedor);
        }
        // Si hay suficientes fondos, no hay problema
        else {
            fortuna -= cantidad;
            estadisticas.anadirGastos(cantidad);
        }
    }

    /**
     * Ingresa una cantidad de dinero al jugador
     */
    public void ingresar(long cantidad) {
        if (cantidad < 0) {
            Juego.consola.error("[Jugador] No se puede ingresar una cantidad negativa o nula");
            return;
        }

        fortuna += cantidad;
    }

    public String listarTratos(){
        StringBuilder str = new StringBuilder();
        for(Trato t : tratos){
            str.append(t.toString());
        }
        return str.toString();
    }

    public void crearTrato(String nombre, Jugador jugador, Propiedad p1, Propiedad p2){
        TratoP_P t1= new TratoP_P(nombre, this,jugador,p1,p2);
        tratos.add(t1);
    }
    public void aceptarTrato(String nombre){
        for(Trato t :tratos){
            if(t.getNombre().equalsIgnoreCase(nombre)){
                t.aceptar();

            }
        }
    }

    /**
     * Determina si
     */
    public boolean acabarTurno() {
        if (isEndeudado()) {
            Juego.consola.error("El jugador %s está endeudado: paga la deuda o declárate en bancarrota para poder avanzar".formatted(nombre));
            return false;
        }

        return avatar.acabarTurno();
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


    @Override
    public String listar() {
        return null;
    }
}
