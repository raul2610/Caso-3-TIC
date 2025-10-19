import java.util.LinkedList;
import java.util.Queue;

/**
 * Buzón de entrada con capacidad limitada. Productores y consumidores usan espera pasiva.
 */
public class BuzonEntrada {
    private final int capacidad;
    private final Queue<Mensaje> cola = new LinkedList<>();
    private final ControlEstado control;
    private boolean cerrado = false;

    public BuzonEntrada(int capacidad, ControlEstado control) {
        this.capacidad = capacidad;
        this.control = control;
        // Se registra luego el control.setBuzones desde App
    }

    public synchronized void setControlReferences() {
        // placeholder para coherencia
    }

    public synchronized void depositar(Mensaje m) throws InterruptedException {
        while (cola.size() >= capacidad) {
            wait(); // espera pasiva mientras está lleno
        }
        cola.add(m);
        notifyAll(); // despierta filtros
    }

    /**
     * Extrae un mensaje esperando pasivamente. Devuelve null cuando ya no habrá más producción
     * (todos los FIN de clientes recibidos) y la cola está vacía.
     */
    public synchronized Mensaje extraer() throws InterruptedException {
        while (cola.isEmpty() && !cerrado) {
            // permitir salida limpia cuando ya no habrá más mensajes
            if (control.produccionTerminadaYVacios()) {
                cerrado = true;
                break;
            }
            wait();
        }
        if (cola.isEmpty()) return null;
        Mensaje m = cola.remove();
        notifyAll(); // despertar posibles productores
        return m;
    }

    public synchronized boolean estaVacio() { return cola.isEmpty(); }

    public synchronized int size() { return cola.size(); }
}

