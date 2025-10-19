import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        String configPath = args != null && args.length > 0 ? args[0] : "config.txt";
        Config config;
        try {
            config = Config.load(configPath);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de configuración '" + configPath + "'. Usando valores por defecto.");
            config = Config.defaults();
        }

        System.out.println("===============================================");
        System.out.println("    SIMULADOR DE CENTRO DE MENSAJERÍA - CASO 3");
        System.out.println("===============================================");
        System.out.println("Configuración: " + config);
        System.out.println("===============================================");

        ControlEstado control = new ControlEstado(config);

        BuzonEntrada entrada = new BuzonEntrada(config.capacidadEntrada, control);
        BuzonCuarentena cuarentena = new BuzonCuarentena(control);
        BuzonEntrega entrega = new BuzonEntrega(config.capacidadEntrega, config.servidoresEntrega, control);
        control.setBuzones(entrada, cuarentena, entrega);

        // Hilos: clientes
        Thread[] clientes = new Thread[config.clientesEmisores];
        for (int i = 0; i < clientes.length; i++) {
            int clienteId = i + 1;
            int mensajes = config.mensajesPorCliente > 0 ? config.mensajesPorCliente : (20 + (int) (Math.random() * 81));
            clientes[i] = new ClienteEmisor("Cliente-" + clienteId, mensajes, entrada);
        }

        // Hilos: filtros
        Thread[] filtros = new Thread[config.filtrosSpam];
        for (int i = 0; i < filtros.length; i++) {
            filtros[i] = new FiltroSpam("Filtro-" + (i + 1), entrada, cuarentena, entrega, control);
        }

        // Hilo: manejador de cuarentena
        Thread manejadorCuarentena = new ManejadorCuarentena(cuarentena, entrega, control);

        // Hilos: servidores
        Thread[] servidores = new Thread[config.servidoresEntrega];
        for (int i = 0; i < servidores.length; i++) {
            servidores[i] = new ServidorEntrega("Servidor-" + (i + 1), entrega);
        }

        long start = System.currentTimeMillis();
        System.out.println("Iniciando simulación...");
        
        System.out.println("Iniciando servidores de entrega...");
        for (Thread t : servidores) t.start();
        
        System.out.println("Iniciando filtros de spam...");
        for (Thread t : filtros) t.start();
        
        System.out.println("Iniciando manejador de cuarentena...");
        manejadorCuarentena.start();
        
        System.out.println("Iniciando clientes emisores...");
        for (Thread t : clientes) t.start();

        System.out.println("Esperando terminación de clientes...");
        for (Thread t : clientes) t.join();
        
        System.out.println("Esperando terminación de filtros...");
        for (Thread t : filtros) t.join();
        
        System.out.println("Esperando terminación de manejador de cuarentena...");
        manejadorCuarentena.join();
        
        System.out.println("Esperando terminación de servidores...");
        for (Thread t : servidores) t.join();

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("===============================================");
        System.out.println("    RESULTADOS DE LA SIMULACIÓN");
        System.out.println("===============================================");
        System.out.println("Tiempo total: " + elapsed + " ms");
        System.out.println("Buzón entrada vacío: " + entrada.estaVacio());
        System.out.println("Buzón cuarentena vacío: " + cuarentena.estaVacio());
        System.out.println("Buzón entrega vacío: " + entrega.estaVacio());
        System.out.println("FIN completamente distribuido: " + entrega.finCompletamenteDistribuido());
        
        boolean terminacionLimpia = entrada.estaVacio() && cuarentena.estaVacio() && entrega.estaVacio();
        System.out.println("Terminación limpia: " + (terminacionLimpia ? "✓ ÉXITO" : "✗ FALLO"));
        
        if (terminacionLimpia) {
            System.out.println("Todos los buzones están vacíos al finalizar");
            System.out.println("Sistema terminó correctamente");
        } else {
            System.out.println("Error: Algunos buzones no están vacíos");
        }
        System.out.println("===============================================");
    }
}