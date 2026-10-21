package com.inventario;

/**
 * Punto de entrada del .jar empaquetado. Java no arranca un .jar con JavaFX en el
 * classpath si la clase principal extiende Application, por eso se usa esta clase.
 */
public class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}
