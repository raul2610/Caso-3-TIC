import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public final int clientesEmisores;
    public final int mensajesPorCliente; // si es <=0, se usa aleatorio 20..100
    public final int filtrosSpam;
    public final int servidoresEntrega;
    public final int capacidadEntrada;
    public final int capacidadEntrega;

    public Config(int clientesEmisores, int mensajesPorCliente, int filtrosSpam,
                  int servidoresEntrega, int capacidadEntrada, int capacidadEntrega) {
        this.clientesEmisores = clientesEmisores;
        this.mensajesPorCliente = mensajesPorCliente;
        this.filtrosSpam = filtrosSpam;
        this.servidoresEntrega = servidoresEntrega;
        this.capacidadEntrada = capacidadEntrada;
        this.capacidadEntrega = capacidadEntrega;
    }

    public static Config load(String path) throws IOException {
        Map<String,String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                String[] kv = line.split("=", 2);
                map.put(kv[0].trim().toLowerCase(), kv[1].trim());
            }
        }
        int clientes = parse(map.get("clientes"), 3);
        int msgs = parse(map.get("mensajesporcliente"), -1);
        int filtros = parse(map.get("filtros"), 2);
        int servidores = parse(map.get("servidores"), 3);
        int capIn = parse(map.get("capacidadentrada"), 20);
        int capOut = parse(map.get("capacidadentrega"), 10);
        return new Config(clientes, msgs, filtros, servidores, capIn, capOut);
    }

    private static int parse(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    public static Config defaults() {
        return new Config(3, -1, 2, 3, 20, 10);
    }

    @Override
    public String toString() {
        return "clientes=" + clientesEmisores +
                ", mensajesPorCliente=" + mensajesPorCliente +
                ", filtros=" + filtrosSpam +
                ", servidores=" + servidoresEntrega +
                ", capacidadEntrada=" + capacidadEntrada +
                ", capacidadEntrega=" + capacidadEntrega;
    }
}