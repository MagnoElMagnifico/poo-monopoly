package monopoly;


import monopoly.Casilla;
import monopoly.Jugador;
import monopoly.Propiedad;
public class Calculadora {
    //Variables
    private static Jugador banca;


    //Contructores
    public Calculadora(Jugador banca) {
        this.banca =banca;
    }

    // Get y set

    // Otros metodos


    public static String pagarAlquiler(Propiedad solar, Jugador jugador){
        if(solar.getPropietario()==banca) return " ";
        if(solar.getPropietario()==jugador) return " ";
        else{
            jugador.setFortuna(jugador.getFortuna()-solar.getAlquiler());
            return "se han pagado %d de alquiler".formatted(solar.getAlquiler());
        }
    }
    public static String valoresPropiedad(Propiedad solar){
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
                (int)(solar.getPrecio()*0.6),(int)(solar.getPrecio()*0.6),(int)(solar.getPrecio()*0.4),(int)(solar.getPrecio()*1.25),
                solar.getPrecio()*5, solar.getPrecio()*15,solar.getPrecio()*30,solar.getPrecio()*70,solar.getPrecio()*25,solar.getPrecio()*25);
    }
}
