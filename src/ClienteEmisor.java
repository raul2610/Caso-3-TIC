import java.util.Random;

public class ClienteEmisor extends Thread {
    private final String idCliente;
    private final int cantidadMensajes;
    private final BuzonEntrada entrada;
    private final Random rnd = new Random();

    public ClienteEmisor(String idCliente, int cantidadMensajes, BuzonEntrada entrada) {
        super("ClienteEmisor-" + idCliente);
        this.idCliente = idCliente;
        this.cantidadMensajes = cantidadMensajes;
        this.entrada = entrada;
    }

    @Override
    public void run() {
        try {
            entrada.depositar(Mensaje.inicio(idCliente));
            for (int i = 1; i <= cantidadMensajes; i++) {
                boolean spam = rnd.nextBoolean();
                Mensaje m = Mensaje.data(idCliente, i, spam);
                entrada.depositar(m);
            }
            entrada.depositar(Mensaje.finCliente(idCliente));
        } catch (InterruptedException e) {
        }
    }
}