package monopoly;

/**
 * Clase para Almacenar informacion de las casillas
 *
 * @autor Gabriel otero Pombo
 */
public class Casillas {
    //Atributos
    /** Información para el nombre de la casilla y el dueño
     * en caso de le las casillas especiales propietario = nombre*/
    private String nombre, propietario;
    /**
     * identificador del tipo de casilla
     * tipo = -1 cailla vacia (por si acaso necesitamo generar un casilla sin nada)
     * tipo = 0 solar
     * tipo = 1 transporte
     * tipo = 2 servicios
     * tipo = 3 suerte/comunidad
     * tipo = 4 impuesto
     * tipo = 5 especiles(Cárcel, Parking, Salida e Ir a la Cárcel)
     */
    private int tipo;
    /**Grupo al que pertence un solar, tambien determina el color  va del 1 al 8
     * para el resto de casillas su valor sera 0
     * */
    private  int grupo;
    /**Conjunto de valores de los solares los alquileres
     * Para la casilla de impuesto su precio a pagar se guarda en alquiler
     * Para la casilla parking el bote se guarda en precio
     * Para la casilla carcel el precio de salida se guarda en alquiler
     * */
    private int precio, hipoteca, valorCasa, valorHotel, valorPiscina, valorPista;
    private int alquiler, alquilerCasa, alquilerHotel, alquilerPiscina, alquilerPista;
    /** jugadores actuales en la casilla*/
    private String jugadores;
    //Constructores
    /**Constructor para la casilla vacia tipo -1*/
    public Casillas(){
        this.tipo=-1;
        this.grupo=0;
        this.nombre=new String("");
        this.propietario=new String("");
        this.jugadores=new String("");
        this.alquiler=0;
        this.alquilerCasa=0;
        this.alquilerHotel=0;
        this.alquilerPiscina=0;
        this.alquilerPista=0;
        this.precio=0;
        this.hipoteca=0;
        this.valorCasa=0;
        this.valorHotel=0;
        this.valorPiscina=0;
        this.valorPista=0;
    }

    /**Constructor para solares*/
    public Casillas(String nombre,int alquiler, int alquilerCasa, int alquilerHotel, int alquilerPiscina, int alquilerPista, int precio,
                    int hipoteca, int valorCasa, int valorHotel, int valorPiscina, int valorPista, int grupo){
        this.tipo=0;
        if(0<grupo && grupo <=8) {
            this.grupo = grupo;
        }
        else{
            this.grupo=0;
            System.out.println("Grupo no asignado\n");
        }
        this.nombre=new String(nombre);
        this.propietario=new String("Banco");
        this.jugadores=new String("");
        if(alquiler>0) {
            this.alquiler = alquiler;
        }
        else{
            this.alquiler=0;
            System.out.println("alquiler no asignado\n");
        }
        if(alquilerCasa>0) {
            this.alquilerCasa = alquilerCasa;
        }
        else{
            this.alquilerCasa=0;
            System.out.println("alquilerCasa no asignado\n");
        }
        if(alquilerHotel>0) {
            this.alquilerHotel = alquilerHotel;
        }
        else{
            this.alquilerHotel=0;
            System.out.println("alquilerHotel no asignado\n");
        }
        if(alquilerPiscina>0) {
            this.alquilerPiscina = alquilerPiscina;
        }
        else{
            this.alquilerPiscina=0;
            System.out.println("alquilerPiscina no asignado\n");
        }
        if(alquilerPista>0) {
            this.alquilerPista = alquilerPista;
        }
        else{
            this.alquilerPista=0;
            System.out.println("alquilerPista no asignado\n");
        }
        if(precio>0) {
            this.precio = precio;
        }
        else{
            this.precio=0;
            System.out.println("precio no asignado\n");
        }
        if(hipoteca>0) {
            this.hipoteca = hipoteca;
        }
        else{
            this.hipoteca=0;
            System.out.println("Hipoteca no asignado\n");
        }
        if(valorCasa>0) {
            this.valorCasa = valorCasa;
        }
        else{
            this.valorCasa=0;
            System.out.println("ValorCasa no asignado\n");
        }
        if(valorHotel>0) {
            this.valorHotel = valorHotel;
        }
        else{
            this.valorHotel=0;
            System.out.println("ValorHotel no asignado\n");
        }
        if(valorPiscina>0) {
            this.valorPiscina = valorPiscina;
        }
        else{
            this.valorPiscina=0;
            System.out.println("ValorPiscina no asignado\n");
        }
        if(valorPista>0) {
            this.valorPista = valorPista;
        }
        else{
            this.valorPista=0;
            System.out.println("ValorPista no asignado\n");
        }
    }

    /**Constructor para transporte y servicios*/
    public Casillas(String nombre, int alquiler, int precio, int tipo){
        if(tipo==1) {
            this.tipo = 1;
            this.grupo = 0;
            this.nombre = new String(nombre);
            this.propietario = new String("Banca");
            this.jugadores = new String("");
            if (alquiler > 0) {
                this.alquiler = alquiler;
            }
            else {
                this.alquiler = 0;
                System.out.println("alquiler no asignado\n");
            }
            this.alquilerCasa = 0;
            this.alquilerHotel = 0;
            this.alquilerPiscina = 0;
            this.alquilerPista = 0;
            if (precio > 0) {
                this.precio = precio;
            }
            else {
                this.precio = 0;
                System.out.println("precio no asignado\n");
            }
            this.hipoteca = 0;
            this.valorCasa = 0;
            this.valorHotel = 0;
            this.valorPiscina = 0;
            this.valorPista = 0;
        }
        if(tipo==2){
            this.tipo=2;
            this.grupo=0;
            this.nombre=new String(nombre);
            this.propietario=new String("Banca");
            this.jugadores=new String("");
            if(alquiler>0) {
                this.alquiler = alquiler;
            }
            else{
                this.alquiler=0;
                System.out.println("alquiler no asignado\n");
            }
            this.alquilerCasa=0;
            this.alquilerHotel=0;
            this.alquilerPiscina=0;
            this.alquilerPista=0;
            if(precio>0) {
                this.precio = precio;
            }
            else{
                this.precio=0;
                System.out.println("precio no asignado\n");
            }
            this.hipoteca=0;
            this.valorCasa=0;
            this.valorHotel=0;
            this.valorPiscina=0;
            this.valorPista=0;
        }
    }

    /**Contructor para suerte comunidad*/
    public Casillas(String nombre){
        this.tipo=3;
        this.grupo=0;
        this.nombre=new String(nombre);
        this.propietario=new String("Banca");
        this.jugadores=new String("");
        this.alquiler=0;
        this.alquilerCasa=0;
        this.alquilerHotel=0;
        this.alquilerPiscina=0;
        this.alquilerPista=0;
        this.precio=0;
        this.hipoteca=0;
        this.valorCasa=0;
        this.valorHotel=0;
        this.valorPiscina=0;
        this.valorPista=0;
    }

    /** Contructor para los impuestos y las casillas especiales*/
    public Casillas(String nombre, int alquiler, int tipo){
        if(tipo==4) {
            this.tipo = 4;
            this.grupo = 0;
            this.nombre = new String(nombre);
            this.propietario = new String("Banca");
            this.jugadores = new String("");
            if (alquiler > 0) {
                this.alquiler = alquiler;
            }
            else {
                this.alquiler = 0;
                System.out.println("alquiler no asignado\n");
            }
            this.alquilerCasa = 0;
            this.alquilerHotel = 0;
            this.alquilerPiscina = 0;
            this.alquilerPista = 0;
            this.hipoteca = 0;
            this.valorCasa = 0;
            this.valorHotel = 0;
            this.valorPiscina = 0;
            this.valorPista = 0;
        }
        if(tipo==5){
            this.tipo = 5;
            this.grupo = 0;
            this.nombre = new String(nombre);
            this.propietario = new String("Banca");
            this.jugadores = new String("");
            if (alquiler >= 0) {
                this.alquiler = alquiler;
            }
            else {
                this.alquiler = 0;
                System.out.println("alquiler no asignado\n");
            }
            this.alquilerCasa = 0;
            this.alquilerHotel = 0;
            this.alquilerPiscina = 0;
            this.alquilerPista = 0;
            this.hipoteca = 0;
            this.valorCasa = 0;
            this.valorHotel = 0;
            this.valorPiscina = 0;
            this.valorPista = 0;
        }
    }



    // Metodos de lectura y escritura
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        if(tipo<=5 && tipo>=-1) {
            this.tipo = tipo;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getGrupo() {
        return grupo;
    }

    public void setGrupo(int grupo) {
        if(grupo<=8 && grupo>=0) {
            this.grupo = grupo;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        if(precio>0) {
            this.precio = precio;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getHipoteca() {
        return hipoteca;
    }

    public void setHipoteca(int hipoteca) {
        if(hipoteca>0) {
            this.hipoteca = hipoteca;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getValorCasa() {
        return valorCasa;
    }

    public void setValorCasa(int valorCasa) {
        if(valorCasa>0) {
            this.valorCasa = valorCasa;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getValorHotel() {
        return valorHotel;
    }

    public void setValorHotel(int valorHotel) {
        if(precio>0) {
            this.valorHotel = valorHotel;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getValorPiscina() {
        return valorPiscina;
    }

    public void setValorPiscina(int valorPiscina) {
        if(valorPiscina>0) {
            this.valorPiscina = valorPiscina;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getValorPista() {
        return valorPista;
    }

    public void setValorPista(int valorPista) {
        if(valorPista>0) {
            this.valorPista = valorPista;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getAlquiler() {
        return alquiler;
    }

    public void setAlquiler(int alquiler) {
        if(alquiler>0) {
            this.alquiler = alquiler;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getAlquilerCasa() {
        return alquilerCasa;
    }

    public void setAlquilerCasa(int alquilerCasa) {
        if(alquilerCasa>0) {
            this.alquilerCasa = alquilerCasa;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getAlquilerHotel() {
        return alquilerHotel;
    }

    public void setAlquilerHotel(int alquilerHotel) {
        if(alquilerHotel>0) {
            this.alquilerHotel = alquilerHotel;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getAlquilerPiscina() {
        return alquilerPiscina;
    }

    public void setAlquilerPiscina(int alquilerPiscina) {
        if(alquilerPiscina>0) {
            this.alquilerPiscina = alquilerPiscina;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public int getAlquilerPista() {
        return alquilerPista;
    }

    public void setAlquilerPista(int alquilerPista) {
        if(alquilerPista>0) {
            this.alquilerPista = alquilerPista;
        }
        else{
            System.out.println("Valor no valido\n");
        }
    }

    public String getJugadores() {
        return jugadores;
    }

    public void setJugadores(String jugadores) {
        this.jugadores = jugadores;
    }

    // Otros Metodos


}
