package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.casillas.Casilla;
import monopoly.casillas.Edificio;
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
    private final ArrayList<Edificio> edificios;
    private long fortuna;
    private long gastos;
    private boolean endeudado;
    private long cantidadDeuda;
    private Jugador jug;
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
        this.edificios = null;
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
        this.edificios = new ArrayList<>();
        this.bancarrota=false;
        this.jug =null;
        this.endeudado=false;
        this.cantidadDeuda=0;
    }

    /**
     * Función de ayuda para listar los nombres de las propiedades en un String
     */
    private String listaPropiedades() {
        StringBuilder propiedadesStr = new StringBuilder();

        propiedadesStr.append('[');
        Iterator<Propiedad> iter = propiedades.iterator();

        while (iter.hasNext()) {
            Casilla c = iter.next().getCasilla();
            propiedadesStr.append(c.getNombreFmt());

            if (iter.hasNext()) {
                propiedadesStr.append(", ");
            }
        }

        propiedadesStr.append(']');

        return propiedadesStr.toString();
    }

    /**
     * Función de ayuda para listar los nombres de los Edificios en un String
     */
    private String listaEdificios() {
        StringBuilder edificiosStr = new StringBuilder();

        edificiosStr.append('[');
        Iterator<Edificio> iter = edificios.iterator();

        while (iter.hasNext()) {
            edificiosStr.append(iter.next().getNombreFmt());

            if (iter.hasNext()) {
                edificiosStr.append(", ");
            }
        }

        edificiosStr.append(']');

        return edificiosStr.toString();
    }

    @Override
    public String toString() {
        if (banca) {
            return "Jugador Especial: Banca\n";
        }

        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                    edificios: %s
                }""".formatted(nombre, avatar.getId(), Consola.num(fortuna), Consola.num(gastos), listaPropiedades(), listaEdificios());
    }

    /**
     * Devuelve un String con información sobre la fortuna, gastos y propiedades del jugador
     */
    public String describirTransaccion() {
        return """
                {
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                }
                """.formatted(Consola.num(fortuna), Consola.num(gastos), listaPropiedades());
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

        System.out.printf("El jugador %s ha comprado la casilla %s por %s\n", nombre, p.getCasilla().getNombreFmt(), Consola.num(p.getPrecio()));

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
                propiedad.setAlquiler(2 * propiedad.getAlquiler());
            }

            Grupo g = p.getCasilla().getGrupo();
            System.out.printf("""
                    Con esta casilla el %s completa el Monopolio de %s!
                    Ahora los alquileres de ese grupo valen el doble.
                    """, Consola.fmt(nombre, Consola.Color.Azul), Consola.fmt(g.getNombre(), g.getCodigoColor()));
        }

        return true;
    }

    /**
     * Realiza la compra del edificio dado.
     *
     * @return True si la operación es exitosa y false en otro caso.
     */
    public boolean comprar(Edificio e) {
        // TODO
        return false;
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
     */
    public boolean cobrar(long cantidad) {
        if (cantidad <= 0 ) {
            return false;
        }
        if (cantidad > fortuna) {
            endeudado=true;
            cantidadDeuda=cantidad;
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

    public void hipotecar(Propiedad propiedad){
        if(propiedad.isHipotecada()){
            Consola.error("No se puede hipotecar, ya está hipotecada");
            return;
        }

        propiedad.setHipotecada(true);
        long cantidad = Calculadora.calcularHipoteca(propiedad);
        fortuna += cantidad;
        System.out.printf("Se ha hipotecado %s por %s\n%n", propiedad.getCasilla().getNombreFmt(),Consola.num(cantidad));
    }

    public void deshipotecar(Propiedad propiedad){
        if(!propiedad.isHipotecada()){
            Consola.error("No se puede deshipotecar, no está hipotecada");
            return;
        }
        long cantidad = Calculadora.calcularHipoteca(propiedad);
        propiedad.setHipotecada(false);
        fortuna -= cantidad;
        gastos += cantidad;
        System.out.printf("Se ha deshipotecado %s por %s\n%n", propiedad.getCasilla().getNombreFmt(),Consola.num(cantidad));
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
    public void pagarDeuda(Propiedad p){

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
                 propiedades.remove(c);
                 banca.anadirPropiedad(c);
            }
        }
        else {
            Iterator<Propiedad> iter = propiedades.iterator();

            while (iter.hasNext()) {
                Propiedad c = iter.next();
                c.setPropietario(jug);
                c.setHipotecada(false);
                propiedades.remove(c);
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
