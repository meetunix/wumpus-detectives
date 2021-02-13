<h1 align="center">wumpus-detectives</h1>

<p align="center">
<a href="https://github.com/meetunix/wumpus-detectives/blob/main/LICENSE" title="License">
<img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

[English README here](README.md)

WumpusDetectives ist ein vollständiges Multi-Agenten-Framework für die
Simulation einer erweiterten
[Wumpus-Welt](https://de.wikipedia.org/wiki/Wumpus-Welt).
Die Welt wird durch den wumpusMonitor simuliert.
Die Agenten (wumpusDetective) können über ein simples, auf TCP basierendes
Protokoll, untereinander und mit dem Monitor kommunizieren.

## Die Agenten (wumpusDetective)

Die Agenten sind in Java implementiert und können sich untereinander über ein sehr
einfaches Protokoll (via TCP) Nachrichten schicken. Jeder Agent benötigt eine
installierte Java-Laufzeit-umgebung (JRE). Der Agent selbst ist eine JAR-Datei in
der bereits alle Abhängigkeiten verpackt sind. Die derzeit verwendete Agentenlogik
*CarefulAgent* wurde von [LiquidFun](https://github.com/LiquidFun) entwickelt.

[**Dokumentation zu wumpusDetective**](wumpusDetective/README_DE.md)


**Beispielausgabe des Monitors und eines Agenten (CarefulAgent):**

![Ausgabe CarefulAgent](media/agent_4_agents.gif)



## Der Monitor (wumpusMonitor)

Der Monitor generiert und verwaltet die Spielwelt. Er bietet eine Schnittstelle,
über die sich  Agenten registrieren, ihren Zustand melden und
Informationen zu ihrer aktuellen Position abrufen können.
Des Weiteren bietet der Monitor auch ein HTTP-Endpoint über den der gesamte Zustand der
Welt erfragt werden kann. Eine externe Software kann diese Daten
zur Visualisierung nutzen. Für jede Simulaion werden Parameter und Ergebnisse
in die Datei `results_benchmark.csv` zu späteren Auswertung gespeichert.


[**Dokumentation des Monitors**](wumpusMonitor/README_DE.md)




## Gemeinsame Bibliothek (wumpusCore)

*wumpusCore* bietet gemeinsam benutze Klassen und wird automatisch mit übersetzt.


## Übersetzen

Voraussetzung ist Java 11 und Apache Maven.

Mit folgendem Befehl lassen sich beide Projekte: *wumpusDetective* und
*wumpusMonitor* übersetzen.

```
mvn clean compile package
```

## Simulation und Ergebnisvisualisierung

Mithilfe des Shell-Skriptes `benchmark.sh` lassen sich Simulationsläufe
mit verschiedenen Parametern durchführen.
Zur Ausführung wird [GNU Parallel](https://www.gnu.org/software/parallel/)
benötigt, das sich um die nebenläufige Ausführung von Monitor und der Agenten kümmert.
GNU Parallel sollte in jeder gängigen Linux-Distribution als Paket vorliegen, meist
unter dem namen `parallel`.
Da für jeden Agenten eine eigene Java-VM gestartet wird, ist der Speicherbedarf
relativ hoch. Eine Simulation mit 32 Agenten benötigt auf einem System, ca. 14 GiB Speicher.
Die Agenten können bei bedarf auf verschiedene Systeme ausgelagert werden, sodass auch Simulationen
mit mehr als 100 Agenten möglich sind.


Das Python-Skript `plot_results.py` visualisiert die Simulationsergebnisse:

    plot_results.py results_benchmark.csv

![Steps](misc/result_steps.png)
![Reward](misc/result_rewards.png)

## Visualisierung (wumpusVisualization)

Visualisiert die simulierte Welt.
