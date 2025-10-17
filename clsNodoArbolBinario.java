public class clsNodoArbolBinario {
    private Object nodoInfo;
    private clsNodoArbolBinario hijoIzquierdo, hijoDerecho;

     clsNodoArbolBinario(Object nodeInfo) {
        this.setNodoInfo(nodeInfo);
        this.setHijoIzquierdo(null);
        this.setHijoDerecho(null);
    }

    public void setNodoInfo(Object nodoInfo) {
        this.nodoInfo = nodoInfo;
    }

    public Object getNodoInfo() {
        return nodoInfo;
    }

    public void setHijoDerecho(clsNodoArbolBinario hijoDerecho) {
        this.hijoDerecho = hijoDerecho;
    }
    public clsNodoArbolBinario getHijoDerecho() {
        return hijoDerecho;
    }

    public void setHijoIzquierdo(clsNodoArbolBinario hijoIzquierdo) {
        this.hijoIzquierdo = hijoIzquierdo;
    }
    public clsNodoArbolBinario getHijoIzquierdo() {
        return hijoIzquierdo;
    }
}
