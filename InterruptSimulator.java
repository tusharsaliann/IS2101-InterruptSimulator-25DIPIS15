import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class InterruptSimulator {

    enum DeviceType {
        KEYBOARD(2, "Keyboard"),
        MOUSE(1, "Mouse"),
        PRINTER(0, "Printer");

        private final int priority;
        private final String name;

        DeviceType(int priority, String name) {
            this.priority = priority;
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        public String getName() {
            return name;
        }

        public static DeviceType fromIndex(int index) {
            switch (index) {
                case 1: return KEYBOARD;
                case 2: return MOUSE;
                case 3: return PRINTER;
                default: return null;
            }
        }
    }

    static class Device implements Runnable {
        private final DeviceType type;
        private final InterruptController controller;
        private final Random rand = new Random();

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
            } catch (InterruptedException e) {
                System.out.println(type.getName() + " device shutting down.");
            }
        }
    }

  static class InterruptController implements Runnable {
        
        // --- THIS IS THE FIX (Removed 'volatile') ---
        private final boolean[] pendingRequests = new boolean[DeviceType.values().length];
        private final boolean[] interruptMask = new boolean[DeviceType.values().length];
        
        private final Object isrLock = new Object();

        private final List<String> executionLog = new CopyOnWriteArrayList<>();
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                                                                     .withZone(ZoneId.systemDefault());

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // This block synchronizes all READS
                    synchronized (isrLock) {
                        DeviceType deviceToService = null;

                        for (int p = DeviceType.values().length - 1; p >= 0; p--) {
                            DeviceType currentDevice = DeviceType.values()[p];
                            
                            if (pendingRequests[currentDevice.ordinal()]) {
                                if (interruptMask[currentDevice.ordinal()]) {
                                    System.out.println(String.format("🚫 %s Interrupt Ignored (Masked)", currentDevice.getName()));
                                    pendingRequests[currentDevice.ordinal()] = false;
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

        // --- THIS IS THE FIX (Added 'synchronized') ---
        // This method synchronizes WRITES from device threads
        public void requestInterrupt(DeviceType type) {
            synchronized (isrLock) {
                pendingRequests[type.ordinal()] = true;
            }
        }

        private void handleISR(DeviceType type) throws InterruptedException {
            String startTime = formatter.format(Instant.now());
            String startLog = String.format("[%s] ✅ %s Interrupt Triggered -> Handling ISR...", startTime, type.getName());
            
            System.out.println(startLog);
            executionLog.add(startLog);

            Thread.sleep(1500); 

            String endTime = formatter.format(Instant.now());
            String endLog = String.format("[%s] 🏁 %s ISR Completed.", endTime, type.getName());
            
            System.out.println(endLog);
            executionLog.add(endLog);

            // This write is already inside the run() method's sync block
            pendingRequests[type.ordinal()] = false;
        }

        // --- THIS IS THE FIX (Added 'synchronized') ---
        // This method synchronizes WRITES from the main thread
        public void toggleMask(DeviceType type) {
            synchronized (isrLock) {
                interruptMask[type.ordinal()] = !interruptMask[type.ordinal()];
                System.out.println(String.format("\n--- %s interrupts are now %s ---\n",
                    type.getName(), interruptMask[type.ordinal()] ? "MASKED (Disabled)" : "ENABLED"));
            }
        }

        public void printHistory() {
            System.out.println("\n--- 📜 Interrupt Execution History ---");
            if (executionLog.isEmpty()) {
                System.out.println("No interrupts handled yet.");
            } else {
                executionLog.forEach(System.out::println);
            }
            System.out.println("----------------------------------------\n");
        }
    }
    public static void main(String[] args) {
        System.out.println("🚀 Interrupt Handling Simulation Starting...");
        
        InterruptController controller = new InterruptController();
        Thread controllerThread = new Thread(controller);
        controllerThread.start();

        Thread keyboardThread = new Thread(new Device(DeviceType.KEYBOARD, controller));
        Thread mouseThread = new Thread(new Device(DeviceType.MOUSE, controller));
        Thread printerThread = new Thread(new Device(DeviceType.PRINTER, controller));
        
        keyboardThread.start();
        mouseThread.start();
        printerThread.start();

        System.out.println("==================================================");
        System.out.println("   Simulation Running. Control Panel:");
        System.out.println("   [1] Toggle Keyboard Mask");
        System.out.println("   [2] Toggle Mouse Mask");
        System.out.println("   [3] Toggle Printer Mask");
        System.out.println("   [h] Show ISR History (Bonus)");
        System.out.println("   [q] Quit Simulation");
        System.out.println("==================================================");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                DeviceType deviceToToggle = null;

                switch (input.toLowerCase()) {
                    case "1":
                        deviceToToggle = DeviceType.fromIndex(1);
                        break;
                    case "2":
                        deviceToToggle = DeviceType.fromIndex(2);
                        break;
                    case "3":
                        deviceToToggle = DeviceType.fromIndex(3);
                        break;
                    case "h":
                        controller.printHistory();
                        break;
                    case "q":
                        System.out.println("🛑 Shutting down simulation...");
                        controllerThread.interrupt();
                        keyboardThread.interrupt();
                        mouseThread.interrupt();
                        printerThread.interrupt();
                        return;
                    default:
                        System.out.println("Invalid input. Use 1, 2, 3, h, or q.");
                        break;
                }

                if (deviceToToggle != null) {
                    controller.toggleMask(deviceToToggle);
                }
            }
        }
    }
}