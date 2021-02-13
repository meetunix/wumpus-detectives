# wumpusMonitor

Der Monitor generiert und verwaltet die Spielwelt. Er bietet eine Schnittstelle,
über das sich die Agenten registrieren, ihren Zustand melden und
Informationen zu ihrer aktuellen Position abrufen können.
Des Weiteren bietet der Monitor auch ein HTTP-Endpoint über den der gesamte Zustand der
Welt erfragt werden kann. Eine externe Software kann diese Daten
zur Visualisierung nutzen.


## Starten

```
java -jar target/wumpusMonitor-[VERSION]-jar-with-dependencies.jar -b http://127.0.0.1:12345 -w wumpus://localhost:6666 -l warn
```

`-b` Basis-URL auf der der HTTP-Server lauscht.

`-w` URL zur Agent -> Monitor-Kommunikation.

`-l` Log-Level [warn,info, debug, trace]


Optionale Parameter:


`-t` Drosselung der Simulation pro Schritt um X Millisekunden (default: 0)

`-r` Kommunikationsradius der Agenten (Felder) (default: 0)

`-s` Dauer der Subscription-Phase in Sekunden (default: 5 Sekunden)

Der Monitor schreibt eine Log-Datei in das Verzeichnis, aus dem er gestartet wurde:
`wumpusMonitor.log`


## Ausgabe auf Kommandozeile

Beispielausgabe des Monitors mit 8 Agenten, einer Kommunikationsreichweite von 2 Feldern
und keiner Drosselung:

![Ausgabe auf Kommandozeile](../media/monitor_8_agents.webm)


## HTTP REST-API

Um den aktuellen Zustand der Welt zu erfragen existiert ein HTTP-Endpoint:
`http://[BASIS-URL:PORT]/wumpus/worldstate`.

Eine einfache Abfrage kann mit `curl` geschehen:

Sollte der Statuscode **204 No Content** zurückkommen, so befindet sich der Monitor noch
in der *subscription-phase* und die Welt wurde noch nicht generiert.

### Anfragen an den Endpoint

#### 1. Möglichkeit: unkomprimiert (m2m)

Unkomprimiertes JSON-Objekt, bei einer 32x32 Feldgröße: ca. 47 KiB

```
curl -H "accept: application/json" 'http://127.0.0.1:12345/wumpus/worldstate'
```

[Beispielergebnis](wumpusMonitor/samples/sample_world_state.json)

#### 2. Möglichkeit: unkomprimiert (m2h)

Für einen Menschen besser lesbar, bei einer 32x32 Feldgröße: ca. 67 KiB

```
curl -H "accept: application/json" 'http://127.0.0.1:12345/wumpus/worldstate?human=true'
```

[Beispielergebnis](wumpusMonitor/samples/sample_world_state_hr.json)


#### 3. Möglichkeit: gzip komprimiert (m2m)

GZIP-komprimiertes JSON-Objekt, bei einer 32x32 Feldgröße: < 1 KiB

```
curl -H "accept: application/json" -H "accept-encoding: gzip" 'http://127.0.0.1:12345/wumpus/worldstate'
```

[Beispielergebnis](wumpusMonitor/samples/sample_world_state.json.gzip)
