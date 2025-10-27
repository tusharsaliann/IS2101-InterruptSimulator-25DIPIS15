# IS2101-InterruptSimulator-25DIPIS15
# Java Interrupt Controller Simulation

## Description

This is a multithreaded Java program built to simulate a computer's Interrupt Controller. It manages asynchronous interrupt requests from three different I/O devices (Keyboard, Mouse, and Printer).

The simulation demonstrates two core OS concepts:
1.  **Prioritization**: Interrupts are handled based on a set priority (Keyboard > Mouse > Printer).
2.  **Masking**: The user can "mask" (disable or ignore) interrupts from any specific device at runtime.

This version is a simplified implementation, focusing only on the core logic.

## How to Compile and Run

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
  * `q`: **Quit** the simulation

### Sample Output

Here is an example of the program running, showing priority and masking.

```
🚀 Interrupt Simulation Starting...
Controls: [1] Keyboard [2] Mouse [3] Printer [q] Quit

>>> [Printer] requests interrupt! <<<

>>> [Keyboard] requests interrupt! <<<

✅ Keyboard Interrupt Triggered -> Handling ISR...
🏁 Keyboard ISR Completed.
✅ Printer Interrupt Triggered -> Handling ISR...
🏁 Printer ISR Completed.

1
--- Keyboard interrupts are now MASKED (Disabled) ---

>>> [Keyboard] requests interrupt! <<<
🚫 Keyboard Interrupt Ignored (Masked)

q
🛑 Shutting down...
Interrupt Controller shutting down.
```

```
```
