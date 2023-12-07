package monopoly.jugador;


import monopoly.Tratos.*;
import monopoly.error.ErrorComandoAvatar;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorComandoJugador;
import monopoly.utils.Consola;
import monopoly.Juego;
import monopoly.casilla.edificio.Edificio;
import monopoly.casilla.propiedad.Propiedad;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 *
 * @see Avatar
 */
public class Jugador {
    private final String nombre;
    private final Avatar avatar;
    private final HashSet<Propiedad> propiedades;
    private final EstadisticasJugador estadisticas;
    private long fortuna;
    private Jugador acreedor;
    private HashSet<Trato> tratos;

    /**
     * Crea el jugador especial Banca
     */
    public Jugador() {
        this.nombre = "Banca";
        this.avatar = null;
        this.fortuna = 0;
        this.propiedades = new HashSet<>(28);
        this.acreedor = null;
        this.estadisticas = new EstadisticasJugador(this);
    }

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */

    public Jugador(String nombre, Avatar avatar, long fortuna) {

        this.nombre = nombre;
        this.avatar = avatar;
        this.fortuna = fortuna;
        this.propiedades = new HashSet<>();
        this.acreedor = null;
        this.estadisticas = new EstadisticasJugador(this);
        this.tratos =new HashSet<>();
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
                               Juego.consola.listar(propiedades.iterator(), (p) -> p.isHipoetcada()? null : p.getNombreFmt()),
                               Juego.consola.listar(propiedades.iterator(), (p) -> p.isHipotecada()? p.getNombreFmt() : null),
                               listarEdificios());
        // @formatter:on
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
                     Juego.consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()),
                     listarEdificios()));
        // @formatter:on
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Jugador && ((Jugador) obj).getAvatar().equals(avatar);
    }

    public boolean comprar(Propiedad p) throws ErrorComandoFortuna {

        if (isEndeudado()) {
            Juego.consola.error("No puedes comprar nada si estas endeudado");
            return false;
        }

        // Comprobar que el jugador no haya comprado ya la casilla
        if (propiedades.contains(p)) {
            Juego.consola.error("El jugador %s ya ha comprado la casilla %s.".formatted(nombre, p.getNombreFmt()));
            return false;
        }

        // Comprobar que no sea propiedad de otro jugador
        if (!(p.getPropietario() instanceof Banca)) {
            Juego.consola.error("No se pueden comprar propiedades de otro jugador");
            System.out.printf("%s pertenece a %s\n", p.getNombreFmt(), Juego.consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));
            return false;
        }

        // Comprobar que el jugador tiene fortuna suficiente
        cobrar(p.getPrecio());

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

        Juego.consola.imprimir("""
                El jugador %s ha comprado la casilla %s por %s
                Ahora tiene una fortuna de %s
                """.formatted( nombre,p.getNombreFmt(), Juego.consola.num(p.getPrecio()), Juego.consola.num(fortuna)));

        // Actualizar los precios de los alquileres si se acaba de
        // completar un Monopolio
        //TODO: Actualizar el nombre de los precios si tiene monopolio

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
                %s ha vendido %s(s) del solar %s por %s.
                Ahora tiene una fortuna de %s.
                """, nombre, cantidad, solar.getNombre(), Juego.consola.num(importeRecuperado), Juego.consola.num(fortuna));

        // Actualizar el estado TODO No se si hace falta esto
        //solar.actualizarAlquiler();

        describirTransaccion();
        return true;
    }

    /**
     * Hace que el jugador page el alquiler correspondiente
     * al dueño de la casilla en donde se encuentra
     */

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

    public void crearTrato(String nombre, Jugador jugador, Propiedad p1, Propiedad p2) throws ErrorComandoJugador {
        if(!this.propiedades.contains(p1) || !jugador.propiedades.contains(p2)){
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no teneis.",this);
        }
        TratoP_P t1= new TratoP_P(nombre, this,jugador,p1,p2);
        this.tratos.add(t1);
        jugador.tratos.add(t1);
    }
    public void crearTrato(String nombre, Jugador jugador, Propiedad p1, long cantidad) throws ErrorComandoJugador {
        if(!this.propiedades.contains(p1)){
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no te pertencen.",this);
        }
        TratoP_C t1 = new TratoP_C(nombre,this,jugador,p1,cantidad);
        this.tratos.add(t1);
        jugador.tratos.add(t1);
    }
    public void crearTrato(String nombre, Jugador jugador, long cantidad, Propiedad p2) throws ErrorComandoJugador {
        if(!jugador.propiedades.contains(p2)){
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no teneis.",this);
        }
        if(this.fortuna<cantidad){
            throw new ErrorComandoJugador("No tienes suficiente dinero para ofrecer el trato",this);
        }
        TratoC_P t1= new TratoC_P(nombre, this,jugador,cantidad,p2);
        this.tratos.add(t1);
        jugador.tratos.add(t1);
    }

    public void crearTrato(String nombre, Jugador jugador, Propiedad p1, Propiedad p2, long cantidad) throws ErrorComandoJugador {
        if(!this.propiedades.contains(p1) || !jugador.propiedades.contains(p2)){
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no teneis.",this);
        }
        TratoP_PC t1= new TratoP_PC(nombre, this,jugador,p1,p2, cantidad);
        this.tratos.add(t1);
        jugador.tratos.add(t1);
    }

    public void crearTrato(String nombre, Jugador jugador, Propiedad p1, long cantidad ,Propiedad p2) throws ErrorComandoJugador, ErrorComandoFortuna {
        if(!this.propiedades.contains(p1) || !jugador.propiedades.contains(p2)){
            throw new ErrorComandoJugador("No puedes ofrecer un trato con propiedades que no te pertencen.",this);
        }
        if(this.fortuna<cantidad){
            throw new ErrorComandoFortuna("No tienes suficiente dinero para ofrecer el trato",this);
        }
        TratoPC_P t1= new TratoPC_P(nombre, this,jugador,p1, cantidad,p2);
        this.tratos.add(t1);
        jugador.tratos.add(t1);
    }

    public void aceptarTrato(String nombre) throws ErrorComandoFortuna {
        for(Trato t : tratos){
            if(t.getNombre().equalsIgnoreCase(nombre)){
                if(t.getAceptador()==this) {
                    t.aceptar();
                    Juego.consola.imprimir("Aceptado:\n%s".formatted(t.toString()));
                }
            }
        }
    }

    public void eliminarTrato(String nombre) {
        Iterator<Trato> itr = tratos.iterator();
        while(itr.hasNext()){
            Trato t= itr.next();
            if(t.getNombre().equalsIgnoreCase(nombre) && t.getInteresado() == this){
                t.getAceptador().tratos.remove(t);
                this.tratos.remove(t);
            }
        }
    }
    /**
     * Determina si
     */
    public void acabarTurno() throws ErrorComandoAvatar {
        if (isEndeudado()) {
            Juego.consola.error("El jugador %s está endeudado: paga la deuda o declárate en bancarrota para poder avanzar".formatted(nombre));
            return;
        }

        avatar.acabarTurno();
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
