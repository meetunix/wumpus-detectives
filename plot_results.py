#!/usr/bin/env python3

import sys

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

# do not change
AGENTS = [2, 4, 8, 16, 32]
DISTANCES = [0, 1, 2, 3, 4, 6, 8, 10, 20, 30]

RESULT_FILE = sys.argv[1]

N = 0


def plot(result_set, y_label, filename, max_val, steps):
    # plot results
    plt.figure(figsize=(16, 8))
    plt.style.use("seaborn")
    plt.xlabel("Kommunikationsdistanz", fontsize=18)
    plt.xticks(range(len(DISTANCES)), DISTANCES, size=16)
    a = np.linspace(0, max_val, steps, endpoint=True)
    plt.yticks(a, size=16)
    plt.ylim([0, max_val])

    palette = plt.get_cmap("Set1")

    plt.ylabel(y_label, fontsize=18)

    c = 0
    for result in result_set.items():
        plt.plot(
            # DISTANCES,
            result[1],
            marker="",
            color=palette(c),
            linewidth=2,
            alpha=0.9,
            label=result[0],
        )
        c += 1

    plt.title(
        f"Simulationsergebnisse - Feldgröße: 32x57   N = {N}",
        fontsize=20,
        fontweight="bold",
        pad=20,
    )

    plt.legend(
        loc="upper center",
        bbox_to_anchor=(1.05, 0.7),
        shadow=True,
        ncol=1,
        title="Agenten",
        title_fontsize=18,
        fontsize=16,
    )
    plt.savefig(filename)


results = pd.read_csv(RESULT_FILE)
N = results.shape[0]

# result_steps: Agents -> [avg(steps(0)), ... avg(steps(30))]
# result_rewards: Agents -> [avg(rewards(0)), ... avg(rewards(30))]
results_steps = {}
results_rewards = {}
for agent in AGENTS:
    results_steps.update({agent: []})
    results_rewards.update({agent: []})

for agent in AGENTS:
    for dist in DISTANCES:
        series = results.query(f"agents == {agent} & radius == {dist}")["steps"]
        results_steps.get(agent).append(series.median())
        series = results.query(f"agents == {agent} & radius == {dist}")["reward"]
        results_rewards.get(agent).append(series.median() / agent)

plot(results_steps, r"Median Simulationsschritte", "result_steps.png", 3000, 6)
plot(results_rewards, r"Median reward pro Agent", "result_rewards.png", 40000, 6)
