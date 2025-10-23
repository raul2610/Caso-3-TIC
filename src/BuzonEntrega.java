import java.util.LinkedList;
import java.util.Queue;

public class BuzonEntrega {
    private final int capacidad;
    private final int servidores;
    private final Queue<Mensaje> cola = new LinkedList<>();
    private boolean finRecibido = false;
    private int finServidoresRestantes = 0;
    private boolean cerrado = false;

    public BuzonEntrega(int capacidad, int servidores) {
        this.capacidad = capacidad;
        this.servidores = servidores;
            }

    // Insercion semiactiva: FIN no ocupa espacio en cola
    public boolean offerSemiactiva(Mensaje m) {
        while (true) {
            synchronized (this) {
                if (cerrado) return false;

                if (m.tipo == Mensaje.Tipo.FIN) {
                    finRecibido = true;
                    finServidoresRestantes = servidores;
                    System.out.println("FIN recibido en buz?n de entrega. Distribuyendo a " + servidores + " servidores");
                    return true;
                }

                if (cola.size() < capacidad) {
                    cola.add(m);
                    return true;
                }
            }
            Thread.yield();
        }
    }

    // Lectura en espera activa
    public Mensaje pollActiva() {
        synchronized (this) {
            Mensaje m = cola.poll();
            if (m != null) return m;

            if (finRecibido && finServidoresRestantes > 0) {
                finServidoresRestantes--;
                System.out.println("Distribuyendo FIN a servidor (restantes: " + finServidoresRestantes + ")");
                return Mensaje.finSistema();
            }
            if (finRecibido && finServidoresRestantes == 0) {
                cerrado = true;
            }
            return null;
        }
    }

    public synchronized boolean estaVacio() {
        return cola.isEmpty() && finServidoresRestantes == 0;
    }

    public synchronized boolean finCompletamenteDistribuido() {
        return finRecibido && finServidoresRestantes == 0;
    }

    public synchronized int getFinServidoresRestantes() {
        return finServidoresRestantes; 
    }
}