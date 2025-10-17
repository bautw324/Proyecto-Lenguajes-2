public class MetodoArbolBinario {
    private static int suma (clsNodoArbolBinario nodo, int n1, int n2, int nivel) {
        if (nodo == null || nivel > Math.max(n1,n2)){
            return 0;
        }
        int sumaParcial = 0;
        int valorNodo = (Integer) nodo.getNodoInfo();
        if (nivel==n1){
            sumaParcial+=valorNodo;
        }
        if (nivel==n2) {
            sumaParcial-=valorNodo;
        }
        int sumaIzquierda = suma(nodo.getHijoIzquierdo(),n1,n2,nivel+1);
        int sumaDerecha = suma(nodo.getHijoDerecho(),n1,n2,nivel+1);
        return sumaParcial + sumaIzquierda + sumaDerecha;
    }
    public static boolean comprobarSumaClavesDosNiveles(clsArbolBinario arbol, int n1, int n2){
        if (arbol.getRaiz()==null) return false;
        // Si los niveles son iguales, entonces: n1 - n2 = 0
        return suma(arbol.getRaiz(),n1,n2,1) == 0;
    }
}