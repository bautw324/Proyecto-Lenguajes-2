public abstract class clsArbolBinario {
    protected clsNodoArbolBinario raiz;
    public clsArbolBinario(){
        this.raiz = null;
    }
    public clsNodoArbolBinario getRaiz() {
        return raiz;
    }
    public int getHojas(){
        return getHojasRecursivo(this.raiz);
    }
    private int getHojasRecursivo(clsNodoArbolBinario nodo){
        if (nodo==null){
            return 0;
        }
        if (nodo.getHijoIzquierdo() == null && nodo.getHijoDerecho()==null){
            return 1;
        }
        return getHojasRecursivo(nodo.getHijoIzquierdo()) + getHojasRecursivo(nodo.getHijoDerecho());
    }
    public int getHijos(){
        return getHijosRecursivo(this.raiz);
    }
    private int getHijosRecursivo(clsNodoArbolBinario nodo){
        if (nodo==null){
            return 0;
        }
        return 1 + getHijosRecursivo(nodo.getHijoIzquierdo()) + getHijosRecursivo(nodo.getHijoDerecho());
    }

    public int getAltura(){
        return getAlturaRecursivo(this.raiz);
    }
    private int getAlturaRecursivo(clsNodoArbolBinario nodo){
        if (nodo==null){
            return -1;
        }
        int alturaIzquierda = getAlturaRecursivo(nodo.getHijoIzquierdo());
        int alturaDerecha = getAlturaRecursivo(nodo.getHijoDerecho());
        return 1 + Math.max(alturaIzquierda,alturaDerecha);
    }

    public int getDiferencia(clsNodoArbolBinario nodo){
        if (nodo == null){
            return 0;
        }
        return (getAlturaRecursivo(nodo.getHijoIzquierdo())-getAlturaRecursivo(nodo.getHijoDerecho()));
    }

    public void postOrden(){
        System.out.print("Recorrido Post-Orden: ");
        postOrdenRecursivo(this.raiz);
        System.out.println();
    }
    private void postOrdenRecursivo(clsNodoArbolBinario nodo){
        if (nodo != null){
            postOrdenRecursivo(nodo.getHijoIzquierdo());
            postOrdenRecursivo(nodo.getHijoDerecho());
            System.out.print(nodo.getNodoInfo() + " ");
        }
    }

    public abstract void meter(Comparable dato);
}
