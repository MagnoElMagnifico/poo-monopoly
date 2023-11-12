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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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
    private long fortuna;
    private long gastos;
    private boolean endeudado;
    private long cantidadDeuda;
    private Jugador jug; /* Solo par al deuda jugador a quien le debes dinero*/
    private boolean bancarrota;



    /**
     * Crea el jugador especial Banca
     */
    public Jugador() {
        this.nombre = "Banca";
        this.avatar = null;
        this.banca = true;
        this.fortuna = 0;
        this.gastos = 0;
        this.propiedades = new HashSet<>(28);
        this.bancarrota=false;
        this.jug =null;
        this.endeudado=false;
        this.cantidadDeuda=0;

    }

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */
    public Jugador(String nombre, TipoAvatar tipo, char id, Casilla casillaInicial, long fortuna) {
        this.avatar = new Avatar(tipo, id, this, casillaInicial);
        this.nombre = nombre;
        this.banca = false;
        this.fortuna = fortuna;
        this.gastos = 0;
        this.propiedades = new HashSet<>();
        this.bancarrota=false;
        this.jug =null;
        this.endeudado=false;
        this.cantidadDeuda=0;
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

        // TODO: hipotecas
        // @formatter:off
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                    edificios: %s
                }""".formatted(nombre,
                               avatar.getId(),
                               Consola.num(fortuna),
                               Consola.num(gastos),
                               Consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()),
                               listarEdificios());
        // @formatter:on
    }

    /**
     * Devuelve un String con información sobre la fortuna, gastos y propiedades del jugador
     */
    public void describirTransaccion() {
        System.out.printf("""
                {
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                }
                """, Consola.num(fortuna), Consola.num(gastos), Consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()));
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
        if (!cobrar(p.getPrecio())) {
            cantidadDeuda=0;
            endeudado=false;
            Consola.error("%s no dispone de suficiente dinero para comprar %s"
                    .formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }

        p.getPropietario().quitarPropiedad(p);
        anadirPropiedad(p);
        p.setPropietario(this);

        System.out.printf("""
                El jugador %s ha comprado la casilla %s por %s
                Ahora tiene una fortuna de %s
                """, nombre, p.getCasilla().getNombreFmt(), Consola.num(p.getPrecio()), Consola.num(fortuna));

        // Actualizar los precios de los alquileres si se acaba de
        // completar un Monopolio
        boolean tenerMonopolio = true;
        for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
            // Si se encuentra una propiedad cuyo propietario no es este jugador,
            // es que no tiene el monopolio
            if (!c.getPropiedad().getPropietario().equals(this)) {
                tenerMonopolio = false;
                break;
            }
        }

        if (tenerMonopolio) {
            for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
                Propiedad propiedad = c.getPropiedad();
                propiedad.actualizarAlquiler();
            }

            Grupo g = p.getCasilla().getGrupo();
            System.out.printf("""
                    Con esta casilla, %s completa el Monopolio de %s!
                    Ahora los alquileres de ese grupo valen el doble.
                    """, Consola.fmt(nombre, Consola.Color.Azul), Consola.fmt(g.getNombre(), g.getCodigoColor()));
        }

        return true;
    }

    /**
     * Comprueba las restricciones de construcción
     */
    private boolean edificable(Propiedad solar, Edificio.TipoEdificio tipo, int cantidad) {
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

                // En caso de que se borren casas y haya alguna piscina, tienen que quedar al menos 2 casas
                if (grupo.contarEdificios(TipoEdificio.Piscina) >= 1 && grupo.contarEdificios(TipoEdificio.Casa) + cantidad < 2) {
                    Consola.error("Se necesitan al menos 2 casas para tener una piscina");
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

                // Si se borran hoteles y hay alguna piscina, tiene que quedar al menos 1 hotel
                if (grupo.contarEdificios(TipoEdificio.Piscina) >= 1 && grupo.contarEdificios(TipoEdificio.Hotel) + cantidad < 1) {
                    Consola.error("Se necesita al menos 1 hotel para tener una piscina");
                    return false;
                }

                // Si se borran hoteles y hay alguna pista de deporte, tienen que quedar al menos 2 hoteles
                if (grupo.contarEdificios(TipoEdificio.PistaDeporte) >= 1 && grupo.contarEdificios(TipoEdificio.Hotel) + cantidad < 2) {
                    Consola.error("Se necesitan al menos 2 hoteles para tener una pista de deporte");
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
        if (!cobrar(cantidad * e.getValor())) {
            endeudado=false;
            cantidadDeuda=0;
            Consola.error("El jugador no tiene los fondos suficientes para edificar.\nNecesita %s.".formatted(Consola.num(cantidad * e.getValor())));
            return false;
        }

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

        // Comprobar si el estado es válido después de quitar el número de edificios dado
        if (!edificable(solar, tipoEdificio, -cantidad)) {
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

        System.out.printf("""
                %s ha vendido %d %s(s) del solar %s por %s.
                Ahora tiene una fortuna de %s.
                """, nombre, cantidad, tipoEdificio, solar.getNombre(), Consola.num(importeRecuperado), Consola.num(fortuna));

        // Acualizar el estado
        solar.actualizarAlquiler();

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

        long importe = switch (p.getTipo()) {
            case Solar, Transporte -> p.getAlquiler();
            case Servicio -> p.getAlquiler() * dado.getValor() * 4;
        };

        if (!cobrar(importe)) {
            jug=p.getPropietario();
            Consola.error("El jugador no tiene suficientes fondos para pagar el alquiler");
            return;
        }

        p.getPropietario().ingresar(importe);
        System.out.printf("Se han pagado %s de alquiler a %s\n", Consola.num(p.getAlquiler()), Consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));
    }

    /**
     * Cobra al jugador una cantidad de dinero
     *
     * @param cantidad Dinero a ingresar
     * @return True si la operación es correcta, false en otro caso
     */
    public boolean cobrar(long cantidad) {
        if (cantidad <= 0 ) {
            return false;
        }
        if (cantidad > fortuna) {
            endeudado = true;
            cantidadDeuda = cantidad;
            return false;
        }
        fortuna -= cantidad;
        gastos += cantidad;
        return true;
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
        if (propiedad.isHipotecada()) {
            Consola.error("No se puede hipotecar, ya está hipotecada");
            return;
        }

        propiedad.setHipotecada(true);
        long cantidad = Calculadora.calcularHipoteca(propiedad);
        fortuna += cantidad;
        System.out.printf("Se ha hipotecado %s por %s\n%n", propiedad.getCasilla().getNombreFmt(), Consola.num(cantidad));
    }

    public void deshipotecar(Propiedad propiedad) {
        if (!propiedad.isHipotecada()) {
            Consola.error("No se puede deshipotecar, no está hipotecada");
            return;
        }
        long cantidad = Calculadora.calculardeshipoteca(propiedad);
        propiedad.setHipotecada(false);
        fortuna -= cantidad;
        gastos += cantidad;
        System.out.printf("Se ha deshipotecado %s por %s\n%n", propiedad.getCasilla().getNombreFmt(), Consola.num(cantidad));
    }
    public void pagarDeuda(Jugador banca) {
        if (!endeudado) {
            Consola.error("El jugador %s no está endeudado.".formatted(nombre));
            return;
        }
        if(jug==null){
            if(cobrar(cantidadDeuda)) {
                System.out.printf("El jugador %s ha pagado %s de deuda\n%n", Consola.fmt(nombre, Consola.Color.Azul),Consola.num(cantidadDeuda));
                banca.ingresar(cantidadDeuda);
                endeudado = false;
                cantidadDeuda = 0;
            }
        }
        else {
            if(cobrar(cantidadDeuda)){
                System.out.printf("Se han pagado %s de alquiler a %s\n", Consola.num(cantidadDeuda), Consola.fmt(jug.getNombre(), Consola.Color.Azul));
                jug.ingresar(cantidadDeuda);
                endeudado=false;
                jug=null;
                cantidadDeuda=0;
            }
        }
    }

    public boolean setBancarrota(Jugador banca){
        if(bancarrota){
            Consola.error("Ya estas en BancaRota");
            return false;
        }
        bancarrota=true;
        if(jug==null){

            Iterator<Propiedad> iter = propiedades.iterator();

            while (iter.hasNext()) {
                 Propiedad c = iter.next();
                 c.setPropietario(banca);
                 c.setHipotecada(false);
                 iter.remove();
                 banca.anadirPropiedad(c);
            }
        }
        else {
            Iterator<Propiedad> iter = propiedades.iterator();

            while (iter.hasNext()) {
                Propiedad c = iter.next();
                c.setPropietario(jug);
                c.setHipotecada(false);
                iter.remove();
                jug.anadirPropiedad(c);
            }
        }

        return true;
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

    public long getGastos() {
        return gastos;
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
        return endeudado;
    }

}
