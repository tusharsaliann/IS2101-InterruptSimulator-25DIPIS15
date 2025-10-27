import java.util.Random;
import java.util.Scanner;

public class InterruptSimulator {

    enum DeviceType {
        KEYBOARD(2, "Keyboard"),
        MOUSE(1, "Mouse"),
        PRINTER(0, "Printer");

        final int priority;
        final String name;

        DeviceType(int priority, String name) {
            this.priority = priority;
            this.name = name;
        }
        String getName() { return name; }
    }

    static class Device implements Runnable {
        DeviceType type;
        InterruptController controller;
        Random rand = new Random();

        public Device(DeviceType type, InterruptController controller) {
            this.type = type;
            this.controller = controller;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int sleepTime = 3000 + rand.nextInt(7000);
                    Thread.sleep(sleepTime);
                    System.out.println(String.format("\n>>> [%s] requests interrupt! <<<\n", type.getName()));
                    controller.requestInterrupt(type);
                }
            } catch (InterruptedException e) { }
        }
    }

    static class InterruptController implements Runnable {
        boolean[] pending = new boolean[DeviceType.values().length];
        boolean[] mask = new boolean[DeviceType.values().length];
        Object isrLock = new Object();

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    synchronized (isrLock) {
                        DeviceType deviceToService = null;
                        for (int p = DeviceType.values().length - 1; p >= 0; p--) {
                            DeviceType currentDevice = DeviceType.values()[p];
                            int index = currentDevice.ordinal();
                            if (pending[index]) {
                                if (mask[index]) {
                                    System.out.println(String.format("🚫 %s Interrupt Ignored (Masked)", currentDevice.getName()));
                                    pending[index] = false;
                                } else {
                                    deviceToService = currentDevice;
                                    break; 
                                }
                            }
                        }
                        if (deviceToService != null) {
                            handleISR(deviceToService);
                        }
                    } 
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupt Controller shutting down.");
            }
        }

        public void requestInterrupt(DeviceType type) {
            synchronized (isrLock) {
                pending[type.ordinal()] = true;
            }
        }

        private void handleISR(DeviceType type) throws InterruptedException {
            System.out.println(String.format("✅ %s Interrupt Triggered -> Handling ISR...", type.getName()));
            Thread.sleep(1500); 
            System.out.println(String.format("🏁 %s ISR Completed.", type.getName()));
            pending[type.ordinal()] = false;
        }

        public void toggleMask(DeviceType type) {
            synchronized (isrLock) {
                int index = type.ordinal();
                mask[index] = !mask[index];
                System.out.println(String.format("\n--- %s interrupts are now %s ---\n",
                    type.getName(), mask[index] ? "MASKED (Disabled)" : "ENABLED"));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Interrupt Simulation Starting...");
        InterruptController controller = new InterruptController();
        Thread controllerThread = new Thread(controller);
        controllerThread.start();
        Thread keyboard = new Thread(new Device(DeviceType.KEYBOARD, controller));
        Thread mouse = new Thread(new Device(DeviceType.MOUSE, controller));
        Thread printer = new Thread(new Device(DeviceType.PRINTER, controller));
        keyboard.start();
        mouse.start();
        printer.start();

        System.out.println("Controls: [1] Keyboard [2] Mouse [3] Printer [q] Quit");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            switch (input.toLowerCase()) {
                case "1": controller.toggleMask(DeviceType.KEYBOARD); break;
                case "2": controller.toggleMask(DeviceType.MOUSE); break;
                case "3": controller.toggleMask(DeviceType.PRINTER); break;
                case "q":
                    System.out.println("🛑 Shutting down...");
                    controllerThread.interrupt();
                    keyboard.interrupt();
                    mouse.interrupt();
                    printer.interrupt();
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid input. Use 1, 2, 3, or q.");
                    break;
            }
        }
    }
}
