package monopoly;

import java.util.ArrayList;
import monopoly.Casillas;
import monopoly.Jugador;

public class Tablero {
    /** Lista de las 40 casillas */
    ArrayList<Casillas>  casilla;
    /** Lista de los jugadores*/
    ArrayList<Jugador> jugadores;

    //Contructores
    /**Constructor para la clase tablero vacia*/
    public Tablero(){
        casilla =new ArrayList<Casillas>(40);
        jugadores = new ArrayList<Jugador>(4);
    }

    /**Contructor para el tablero bajo normas monopoloy*/
    public Tablero(int precioInit){
        int grupo,precio, i=1,salida=0;
        precio=precioInit;
        String nombre;
        casilla =new ArrayList<Casillas>(40);
        for(grupo=1;grupo<=8;grupo++){
            if(grupo==1 || grupo==8){
                for(int k=0;k<2;k++) {
                    nombre = new String("Solar" + i);
                    Casillas solar = new Casillas(nombre, precio/2, 0, grupo);
                    casilla.add(solar);
                    i +=1;
                }
            }
            else {
                for (int k = 0; k < 3; k++) {
                    nombre = new String("Solar" + i);
                    Casillas solar = new Casillas(nombre, precio/3, 0, grupo);
                    casilla.add(solar);
                    i +=1;
                }

            }
            salida+=precio;
            precio =(int) (1.3*precio);
        }
        //salida = (int) (salida/22);
        for(int k=0;k<4;k++){
            nombre=new String("Transporte"+(char) k);
            Casillas solar = new Casillas(nombre, salida, 1, grupo);
            casilla.add(solar);
        }
        nombre=new String("Servivio Electricidad");
        Casillas servicio1 = new Casillas(nombre,(int) ((int) salida*0.75), 2, grupo);
        casilla.add(servicio1);
        nombre=new String("Servivio agua");
        Casillas solar = new Casillas(nombre,(int) ((int) salida*0.75), 2, grupo);
        casilla.add(solar);
        for(int k=0;k<6;k++){

            if(k<3){
                nombre=new String("Suerte");
                Casillas suerte = new Casillas(nombre,0, 3, grupo);
                casilla.add(suerte);
            }
            else if (k<6) {
                nombre=new String("Caja comunidad");
                Casillas suerte = new Casillas(nombre,0, 3, grupo);
                casilla.add(suerte);
            }

        }
        nombre=new String("Impuesto1");
        Casillas imp1 = new Casillas(nombre,(int) (salida *0.5), 4, grupo);
        casilla.add(imp1);
        nombre=new String("Impuesto2");
        Casillas imp2 = new Casillas(nombre,salida, 4, grupo);
        casilla.add(imp2);
        nombre=new String("Carcel");
        Casillas carcel = new Casillas(nombre,salida, 5, grupo);
        casilla.add(carcel);
        nombre=new String("Parking");
        Casillas parking = new Casillas(nombre,salida, 5, grupo);
        casilla.add(parking);
        nombre=new String("Ir a la carcel");
        Casillas ircarcel = new Casillas(nombre,salida, 5, grupo);
        casilla.add(ircarcel);
        nombre=new String("Salida");
        Casillas start = new Casillas(nombre,salida, 5, grupo);
        casilla.add(start);


    }


    //Getters y setters


    //Otros metodos
}