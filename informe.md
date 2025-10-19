# Caso 3 – Simulador de Centro de Mensajería (Java)

Autor: Equipo – login1, login2

## Diseño y clases

Diagrama (ASCII) de alto nivel:

```
ClientesEmisores --> [BuzonEntrada(cap L, wait/notify)] --consumen--> FiltrosSpam
     |                                                              |
     '---- INICIO/FIN/DATA ----------------------------------------'

FiltrosSpam --(DATA ok, semiactiva)--> [BuzonEntrega(cap K)] <--activa-- ServidoresEntrega(N)
FiltrosSpam --(DATA spam, semiactiva + t∈[10000..20000])--> [BuzonCuarentena(ilim.)]
ManejadorCuarentena (cada 1s, semiactiva) revisa BuzonCuarentena: descarta (1..21 %7==0),
decrementa contadores y cuando llegan a 0 envía a BuzonEntrega (semiactiva)

ControlEstado: monitor con contadores (FIN de clientes) + decisión única de FIN del sistema
```

Clases principales:

- `Mensaje` y `Mensaje.Tipo {INICIO, DATA, FIN}`; cada `DATA` tiene `clienteId`, `secuencial`, `spam`, `cuarentenaContador`.
- `BuzonEntrada`: capacidad limitada; `depositar/extraer` usan `synchronized` + `wait/notifyAll` (espera pasiva).
- `BuzonCuarentena`: lista ilimitada; agrega en semiactiva; `procesarUnaVuelta()` devuelve los mensajes que pasan a entrega y si llegó `FIN`.
- `BuzonEntrega`: capacidad limitada; escribe en semiactiva; lectura en activa con `pollActiva()`. Al recibir `FIN` arma un broadcast entregando una copia de `FIN` a cada servidor.
- `ClienteEmisor`: produce `INICIO`, N `DATA` (20..100 o fijo por config) y `FIN` (espera pasiva para publicar en entrada).
- `FiltroSpam`: consume de entrada en pasiva; si `DATA` y `spam` → cuarentena (semiactiva + contador 10000..20000); si válido → entrega (semiactiva). Cuenta FIN de clientes y, cuando `entrada` está vacía y `cuarentena` vacía y no habrá nuevos, emite un único `FIN` a entrega y otro a cuarentena (vía `ControlEstado.debenEmitirseFines()`).
- `ManejadorCuarentena`: semiactivo, itera cada segundo; por mensaje genera aleatorio [1..21]; si múltiplo de 7, descarta; si el contador llega a 0, envía a entrega. Termina al ver `FIN` en su buzón.
- `ServidorEntrega`: lectura en espera activa; procesa `DATA` con espera aleatoria corta; al recibir `FIN` termina. Marca inicio al ver un `INICIO`.
- `ControlEstado`: monitor global con contadores y decisión única de emisión de `FIN` al buzón de entrega y al de cuarentena.

## Sincronización por pareja

- Cliente → Buzón de entrada: espera pasiva; `depositar()` bloquea con `wait` si lleno y hace `notifyAll` al insertar.
- Filtro → Buzón de entrada: `extraer()` bloquea con `wait` si vacío. Despierta al cierre cuando ya no habrá más producción.
- Filtro → Buzón de cuarentena: semiactiva (siempre cabe); no usa `wait`.
- Filtro → Buzón de entrega: semiactiva; si lleno, bucle con `Thread.yield()` hasta insertar.
- Manejador → Buzón de cuarentena: semiactivo, una pasada por ciclo; usa sección crítica sincronizada para revisar/remover y luego envía a entrega fuera del candado.
- Servidor → Buzón de entrega: espera activa (`pollActiva()`); si no hay, devuelve `null` y el servidor hace `yield`. Tras armar `FIN`, el buzón reparte N copias (una por servidor) sin necesidad de bloquear servidores.
- Filtros ↔ ControlEstado: `synchronized` para contadores y condición única de emisión de FIN global.

## Reglas de terminación y limpieza

- Clientes: finalizan tras publicar su `FIN`.
- Filtros: finalizan cuando `extraer()` retorna `null` (no habrá más mensajes) y/o después de emitir los `FIN` globales.
- Manejador: finaliza al detectar `FIN` en cuarentena.
- Servidores: finalizan al recibir su copia de `FIN`.
- Limpieza: al finalizar, `App` reporta vacíos de los tres buzones. `BuzonEntrega` garantiza que, tras repartir todas las copias de `FIN`, su cola interna queda vacía.

## Validación realizada

Pruebas manuales (consola):

1. Terminación limpia: con `config.txt` por defecto, todos los hilos hacen `join()` y se imprime que los tres buzones quedan vacíos.
2. Distribución de FIN: se verifica por logs de cada `Servidor-X` que todos reciben `FIN`.
3. Sin pérdida: conteo visual de `INICIO` y volumen de `DATA` entregados (los spam pueden descartarse o enviarse tras cuarentena). No se reinsertan mensajes y no hay duplicados.
4. Cuarentena: se observan contadores decreciendo por lotes y descartes (múltiplos de 7) antes de llegar a 0.

## Ejecución

Compilar y ejecutar (Windows/PowerShell):

```
javac -d bin src/*.java
java -cp bin App config.txt
```

El archivo `config.txt` soporta:

```
clientes=3
mensajesPorCliente=30
filtros=3
servidores=3
capacidadEntrada=15
capacidadEntrega=10
```

Si `mensajesPorCliente <= 0`, cada cliente genera [20..100] mensajes aleatorios.

