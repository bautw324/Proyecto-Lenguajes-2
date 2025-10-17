public class clsABB extends clsArbolBinario{
    public clsABB(){
        super();
    }
    @Override
    public void meter(Comparable dato) {
        this.raiz = meterRecursivo(this.raiz, dato);
    }
    private clsNodoArbolBinario meterRecursivo(clsNodoArbolBinario nodo, Comparable dato){
        if (nodo==null){
            return new clsNodoArbolBinario(dato);
        }
        if (dato.compareTo(nodo.getNodoInfo()) < 0){
            nodo.setHijoIzquierdo(meterRecursivo(nodo.getHijoIzquierdo(),dato));
        } else if (dato.compareTo(nodo.getNodoInfo()) > 0){
            nodo.setHijoDerecho(meterRecursivo(nodo.getHijoDerecho(),dato));
        }
        return nodo;
    }
}
