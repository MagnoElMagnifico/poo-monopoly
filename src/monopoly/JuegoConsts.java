package monopoly;

public interface JuegoConsts {
    long PRECIO_SOLAR1 = 1_000_000;

    int N_CASILLAS = 40;
    int N_GRUPOS = 10; // Hay 8 grupos de solares, 1 de transporte y 1 de servicios,
    int N_CARTAS_SUERTE = 6;
    int N_CARTAS_COMUNIDAD = 6;
    int N_IMPUESTOS = 2;

    String CONFIG_CASILLAS = "src/casillas.txt";
    String CONFIG_CARTAS = "src/cartas.txt";
    String CONFIG_AYUDA = "src/ayuda.txt";

    char[] AVATARES_ID = {'A', 'B', 'C', 'D', 'E', 'F'};
    int MAX_JUGADORES = AVATARES_ID.length;
    int MIN_JUGADORES = 2;

    int N_CASAS_SIN_MAX_HOTELES = 4;
    int N_CASAS_PARA_HOTEL = 4;
    int N_CASAS_PARA_PISCINA = 2;
    int N_HOTELES_PARA_PISCINA = 1;
    int N_HOTELES_PARA_PISTA = 2;

    // http://www.patorjk.com/software/taag/#p=display&f=Roman&t=Monopoly
    String MSG_INICIO = """
                                            BIENVENIDO AL JUEGO DEL
            ooo        ooooo                                                      oooo             \s
            `88.       .888'                                                      `888             \s
             888b     d'888   .ooooo.  ooo. .oo.    .ooooo.  oo.ooooo.   .ooooo.   888  oooo    ooo\s
             8 Y88. .P  888  d88' `88b `888P"Y88b  d88' `88b  888' `88b d88' `88b  888   `88.  .8' \s
             8  `888'   888  888   888  888   888  888   888  888   888 888   888  888    `88..8'  \s
             8    Y     888  888   888  888   888  888   888  888   888 888   888  888     `888'   \s
            o8o        o888o `Y8bod8P' o888o o888o `Y8bod8P'  888bod8P' `Y8bod8P' o888o     .8'    \s
                                                              888                       .o..P'     \s
                                                             o888o                      `Y8P'      \s
            """;
    // http://www.patorjk.com/software/taag/#p=display&f=Roman&t=A%20jugar!
    String MSG_JUGAR = """
                  
                  .o.                o8o                                             .o.\s
                 .888.               `"'                                             888\s
                .8"888.             oooo oooo  oooo   .oooooooo  .oooo.   oooo d8b   888\s
               .8' `888.            `888 `888  `888  888' `88b  `P  )88b  `888""8P   Y8P\s
              .88ooo8888.            888  888   888  888   888   .oP"888   888       `8'\s
             .8'     `888.           888  888   888  `88bod8P'  d8(  888   888       .o.\s
            o88o     o8888o          888  `V88V"V8P' `8oooooo.  `Y888""8o d888b      Y8P\s
                                     888             d"     YD                          \s
                                 .o. 88P             "Y88888P'                          \s
                                 `Y888P                                                 \s
            """;
    // http://www.patorjk.com/software/taag/#p=display&f=Roman&t=Fin%20de%20Partida
    String MSG_FIN = """
                        
            oooooooooooo  o8o                         .o8                 ooooooooo.                          .    o8o        .o8           \s
            `888'     `8  `"'                        "888                 `888   `Y88.                      .o8    `"'       "888           \s
             888         oooo  ooo. .oo.         .oooo888   .ooooo.        888   .d88'  .oooo.   oooo d8b .o888oo oooo   .oooo888   .oooo.  \s
             888oooo8    `888  `888P"Y88b       d88' `888  d88' `88b       888ooo88P'  `P  )88b  `888""8P   888   `888  d88' `888  `P  )88b \s
             888    "     888   888   888       888   888  888ooo888       888          .oP"888   888       888    888  888   888   .oP"888 \s
             888          888   888   888       888   888  888    .o       888         d8(  888   888       888 .  888  888   888  d8(  888 \s
            o888o        o888o o888o o888o      `Y8bod88P" `Y8bod8P'      o888o        `Y888""8o d888b      "888" o888o `Y8bod88P" `Y888""8o\s
            """;
}
