public class Test {

    public static void main(String[] args) {
        clsABB arbol = new clsABB();
        int[] valores = {8, 3, 10, 1, 6, 14, 4, 7, 13};
        for (int valor : valores) {
            arbol.meter(valor);
        }

        System.out.println("----------------------------------------");
        System.out.println("El árbol resultante tiene esta estructura:");
        System.out.println("      8        <-- Nivel 1 (Suma = 8)");
        System.out.println("     / \\");
        System.out.println("    3   10     <-- Nivel 2 (Suma = 3 + 10 = 13)");
        System.out.println("   / \\    \\");
        System.out.println("  1   6    14  <-- Nivel 3 (Suma = 1 + 6 + 14 = 21)");
        System.out.println("     / \\   /");
        System.out.println("    4   7 13   <-- Nivel 4 (Suma = 4 + 7 + 13 = 24)");
        System.out.println("----------------------------------------");

        int nivelA = 2;
        int nivelB = 3;
        System.out.println("Prueba 1: Comparamos Nivel " + nivelA + " (Suma=13) con Nivel " + nivelB + " (Suma=21)");

        boolean resultado1 = MetodoArbolBinario.comprobarSumaClavesDosNiveles(arbol, nivelA, nivelB);
        System.out.println(">>> El resultado es: " + resultado1 + " (Correcto, porque 13 != 21)");
        System.out.println("----------------------------------------");

        int nivelC = 4;
        System.out.println("Prueba 2: Comparamos Nivel " + nivelC + " (Suma=24) consigo mismo");

        boolean resultado2 = MetodoArbolBinario.comprobarSumaClavesDosNiveles(arbol, nivelC, nivelC);
        System.out.println(">>> El resultado es: " + resultado2 + " (Correcto, porque 24 == 24)");
    }
}