# IS2101-InterruptSimulator-25DIPIS15
# Java Interrupt Controller Simulation

## Description

This is a multithreaded Java program built to simulate a computer's Interrupt Controller. It manages asynchronous interrupt requests from three different I/O devices (Keyboard, Mouse, and Printer).

The simulation demonstrates two core OS concepts:
1.  **Prioritization**: Interrupts are handled based on a set priority (Keyboard > Mouse > Printer).
2.  **Masking**: The user can "mask" (disable or ignore) interrupts from any specific device at runtime.

## Features

* **Multithreaded Simulation**: Each device (Keyboard, Mouse, Printer) and the Controller run on their own threads to simulate real, asynchronous behavior.
* **Priority-Based Handling**: The controller *always* services the highest-priority interrupt that is currently pending.
* **Interactive Controls**: The user can toggle masks for any device while the simulation is running.
* **ISR History Log**: (Bonus Feature) The program keeps a timestamped log of all completed ISRs, which can be printed at any time.

## How to Compile and Run

This is a single-file program.

### 1. Compile

```bash
javac InterruptSimulator.java
````

### 2\. Run

```bash
java InterruptSimulator
```

## How to Use (Controls)

Once running, the program is fully interactive:

  * `1`: Toggle (mask/unmask) the **Keyboard**
  * `2`: Toggle (mask/unmask) the **Mouse**
  * `3`: Toggle (mask/unmask) the **Printer**
  * `h`: Print the current ISR execution **History**
  * `q`: **Quit** the simulation

### Sample Output

Here is an example of the program running, showing priority and masking.

```
🚀 Interrupt Handling Simulation Starting...
==================================================
   Simulation Running. Control Panel:
   [1] Toggle Keyboard Mask
   [2] Toggle Mouse Mask
   [3] Toggle Printer Mask
   [h] Show ISR History (Bonus)
   [q] Quit Simulation
==================================================

>>> [Printer] requests interrupt! <<<
>>> [Keyboard] requests interrupt! <<<

[10:15:31.500] ✅ Keyboard Interrupt Triggered -> Handling ISR...
[10:15:33.002] 🏁 Keyboard ISR Completed.
[10:15:33.104] ✅ Printer Interrupt Triggered -> Handling ISR...
[10:15:34.606] 🏁 Printer ISR Completed.

3
--- Printer interrupts are now MASKED (Disabled) ---

>>> [Printer] requests interrupt! <<<
🚫 Printer Interrupt Ignored (Masked)

h
--- 📜 Interrupt Execution History ---
[10:15:31.500] ✅ Keyboard Interrupt Triggered -> Handling ISR...
[10:15:33.002] 🏁 Keyboard ISR Completed.
[10:15:33.104] ✅ Printer Interrupt Triggered -> Handling ISR...
[10:15:34.606] 🏁 Printer ISR Completed.
----------------------------------------
```

```
```
