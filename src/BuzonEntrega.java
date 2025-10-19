import java.util.LinkedList;
import java.util.Queue;

/**
 * Buzón de entrega (capacidad limitada). Envíos en semiactiva, servidores consumen en activa.
 */
public class BuzonEntrega {
    private final int capacidad;
    private final int servidores;
    private final Queue<Mensaje> cola = new LinkedList<>();
    private final ControlEstado control;

    private boolean finArmado = false; // ya no habrá nuevos mensajes
    private int finRestantes = 0; // copias de FIN por repartir

    public BuzonEntrega(int capacidad, int servidores, ControlEstado control) {
        this.capacidad = capacidad;
        this.servidores = servidores;
        this.control = control;
    }

    public boolean offerSemiactiva(Mensaje m) {
        while (true) {
            synchronized (this) {
                if (finArmado) return false; // cerrado para nuevos
                if (cola.size() < capacidad) {
                    cola.add(m);
                    if (m.tipo == Mensaje.Tipo.FIN) {
                        finArmado = true;
                        finRestantes = servidores; // una copia por servidor
                    }
                    return true;
                }
            }
            Thread.yield(); // semiactiva: cede CPU y reintenta
        }
    }

    /**
     * Lectura en espera activa: no bloquea; devuelve null si no hay nada disponible.
     * Si ya se armó FIN y la cola está vacía, devuelve una copia de FIN (hasta completar servidores).
     */
    public Mensaje pollActiva() {
        synchronized (this) {
            Mensaje m = cola.poll();
            if (m != null) return m;
            if (finArmado && finRestantes > 0) {
                finRestantes--;
                return Mensaje.finSistema();
            }
            return null;
        }
    }

    public synchronized boolean estaVacio() {
        return cola.isEmpty();
    }
}

