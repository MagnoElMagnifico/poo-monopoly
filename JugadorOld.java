package monopoly.jugador;

import trato.Trato;
import trato.TratoP_P;
import monopoly.error.ErrorComandoAvatar;
import monopoly.error.ErrorComandoFortuna;
import monopoly.utils.Consola;
import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.edificio.Edificio;
import monopoly.casilla.propiedad.Grupo;
import monopoly.casilla.propiedad.Propiedad;
import monopoly.utils.Dado;

import java.util.ArrayList;
import java.util.HashSet;

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
    private JugadorOld acreedor;
    private HashSet<Trato> tratos;

    public JugadorOld(String nombre, Avatar avatar, long fortuna) {
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
            Juego.consola.error("El jugador %s ya ha comprado la casilla %s.".formatted(nombre, p.getNombreFmt()));
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
            Juego.consola.error();
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
    }

    /**
     * Hace que el jugador page el alquiler correspondiente
     * al dueño de la casilla en donde se encuentra
     */
    public void pagarAlquiler(Propiedad p, Dado dado) {
        if (p.getPropietario() instanceof Banca || p.getPropietario().equals(this) || p.isHipotecada()) {
            return;
        }

        // Se multiplica el alquiler por el valor de los dados en caso de que sea un servicio
        long importe = p.getTipo() == Propiedad.TipoPropiedad.Servicio ? p.getAlquiler() * dado.getValor() : p.getAlquiler();

        // Se debe cobrar todo el importe, aunque el jugador no pueda pagarlo.
        // La cuenta se quedará en números negativos (es decir, está endeudado)
        p.getPropietario().ingresar(importe);

        if (!cobrar(importe, true)) {
            acreedor = p.getPropietario();
            Consola.error("El jugador no tiene suficientes fondos para pagar el alquiler");
            return;
        }

        System.out.printf("Se han pagado %s de alquiler a %s\n", Consola.num(importe), Consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));

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
    public void cobrar(long cantidad, JugadorOld acreedor) throws ErrorComandoFortuna {
        if (cantidad <= 0) {
            throw new ErrorComandoFortuna("[Jugador] Se intentó cobrar una cantidad nula o negativa a %s".formatted(nombre), this);
        }
    }


    public String listarTratos(){
        StringBuilder str = new StringBuilder();
        for(Trato t : tratos){
            str.append(t.toString());
        }
        return str.toString();
    }

    /**
     * Determina si
     */
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

    public JugadorOld getAcreedor() {
        return acreedor;
    }

}
