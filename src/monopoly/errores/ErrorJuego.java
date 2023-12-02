package monopoly.errores;

import monopoly.utilidades.Consola;

public class ErrorJuego extends Exception{
    public ErrorJuego(String mensaje){
        super(mensaje);
    }

    public void imprimirMsg(){
        Consola.error(getMessage());
    }
}
