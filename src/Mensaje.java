import java.util.concurrent.atomic.AtomicLong;

public class Mensaje {
    public enum Tipo { INICIO, DATA, FIN }

    private static final AtomicLong SEQGEN = new AtomicLong(0);

    public final long internoId; // único global para trazas
    public final Tipo tipo;
    public final String clienteId; // puede ser null en FIN del sistema
    public final int secuencial; // por cliente (DATA), -1 en otros
    public final boolean spam; // solo relevante para DATA
    public int cuarentenaContador; // usado por cuarentena

    public Mensaje(Tipo tipo, String clienteId, int secuencial, boolean spam) {
        this.internoId = SEQGEN.incrementAndGet();
        this.tipo = tipo;
        this.clienteId = clienteId;
        this.secuencial = secuencial;
        this.spam = spam;
        this.cuarentenaContador = 0;
    }

    public static Mensaje inicio(String clienteId) {
        return new Mensaje(Tipo.INICIO, clienteId, -1, false);
    }
    public static Mensaje finCliente(String clienteId) {
        return new Mensaje(Tipo.FIN, clienteId, -1, false);
    }
    public static Mensaje finSistema() {
        return new Mensaje(Tipo.FIN, null, -1, false);
    }
    public static Mensaje data(String clienteId, int secuencial, boolean spam) {
        return new Mensaje(Tipo.DATA, clienteId, secuencial, spam);
    }

    @Override
    public String toString() {
        String base = "[" + internoId + "]" + tipo +
                (clienteId != null ? ("(" + clienteId + (secuencial>=0?"-"+secuencial:"") + ")") : "") +
                (tipo==Tipo.DATA? (spam?"{SPAM}":"{OK}") : "");
        return base;
    }
}

