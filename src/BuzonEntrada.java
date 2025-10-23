import java.util.LinkedList;
import java.util.Queue;

public class BuzonEntrada {
    private final int capacidad;
    private final Queue<Mensaje> cola = new LinkedList<>();
    private final ControlEstado control;
    private boolean cerrado = false;

    public BuzonEntrada(int capacidad, ControlEstado control) {
        this.capacidad = capacidad;
        this.control = control;
    }

    public synchronized void depositar(Mensaje m) throws InterruptedException {
        while (cola.size() >= capacidad) {
            wait();
        }
        cola.add(m);
        notifyAll();
    }

    public synchronized Mensaje extraer() throws InterruptedException {
        while (cola.isEmpty() && !cerrado) {
            if (control.produccionTerminadaYVacios()) {
                cerrado = true;
                break;
            }
            wait();
        }
        if (cola.isEmpty()) return null;
        Mensaje m = cola.remove();
        notifyAll();
        return m;
    }

    public synchronized boolean estaVacio() { return cola.isEmpty(); }

    public synchronized int size() { return cola.size(); }
}