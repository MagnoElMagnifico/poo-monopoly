package monopoly;


import monopoly.Casilla;
import monopoly.Jugador;
import monopoly.Propiedad;
public class Calculadora {
    //Variables



    //Contructores


    // Get y set



    // Otros metodos

    public int pagarAlquiler(Propiedad solar){
        //TODO: añadir propiedades cuando sean computables

        return solar.getAlquiler();
    }

    public String valorespropiedad(Propiedad solar){
        return """
                        Nombre:%s
                        propietario: %s
                        Precio: %d
                        alquiler: %d
                        valor hotel: %d
                        valor casa: %d
                        valor piscina: %d
                        valor pista de deporte: %d
                        alquiler una casa: %d
                        alquiler dos casas: %d
                        alquiler tres casas: %d
                        alquiler hotel: %d
                        alquiler piscina: %d
                        alquiler pista de deporte: %d
                """.formatted(solar.getCasilla().getNombre(),solar.getPropietario(),solar.getPrecio(),solar.getAlquiler(),
                solar.getPrecio()*)
    }
}
