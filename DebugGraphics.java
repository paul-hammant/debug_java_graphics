import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DebugGraphics {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Environment & Resolution Diagnostics");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null);

            // Main panel with BorderLayout
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Status panel for displaying dimensions
            JPanel statusPanel = new JPanel(new BorderLayout());
            JLabel sizeLabel = new JLabel("Resize the window to see dimensions", SwingConstants.CENTER);
            sizeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            statusPanel.add(sizeLabel, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

            JButton detailsButton = new JButton("Print Details to Console");
            detailsButton.setFont(new Font("Arial", Font.BOLD, 14));
            detailsButton.addActionListener(e -> printSystemDetails(frame));

            JButton maximizeButton = new JButton("Maximize Window");
            maximizeButton.setFont(new Font("Arial", Font.BOLD, 14));
            maximizeButton.addActionListener(e -> {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            });

            buttonPanel.add(detailsButton);
            buttonPanel.add(maximizeButton);

            // Add components to main panel
            mainPanel.add(statusPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Add main panel to frame
            frame.add(mainPanel);

            // Component resize listener
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    Dimension size = frame.getSize();
                    Rectangle bounds = frame.getBounds();
                    int state = frame.getExtendedState();
                    String stateStr = (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH ?
                            "MAXIMIZED" : "NORMAL";

                    sizeLabel.setText(String.format("<html>Window Size: %d × %d<br>Window Bounds: (%d, %d, %d, %d)<br>State: %s</html>",
                            size.width, size.height, bounds.x, bounds.y, bounds.width, bounds.height, stateStr));
                }
            });

            frame.setVisible(true);
        });
    }

    private static void printSystemDetails(JFrame frame) {
        System.out.println("\n====== ENVIRONMENT & RESOLUTION DIAGNOSTICS ======");
        System.out.println("Time: " + new java.util.Date());

        // Window information
        System.out.println("\n----- WINDOW INFORMATION -----");
        Dimension size = frame.getSize();
        Rectangle bounds = frame.getBounds();
        int state = frame.getExtendedState();
        System.out.println("Window Size: " + size.width + " × " + size.height);
        System.out.println("Window Bounds: (" + bounds.x + ", " + bounds.y + ", " +
                bounds.width + ", " + bounds.height + ")");
        System.out.println("Extended State: " + state + " (" +
                ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH ? "MAXIMIZED" : "NORMAL") + ")");
        System.out.println("Window Decorations: " + frame.isUndecorated());

        // Screen information
        System.out.println("\n----- SCREEN INFORMATION -----");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        System.out.println("Number of screens: " + devices.length);

        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice device = devices[i];
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
            AffineTransform transform = config.getDefaultTransform();

            System.out.println("\nScreen #" + (i + 1) + ":");
            System.out.println("  Display ID: " + device.getIDstring());
            System.out.println("  Bounds: (" + screenBounds.x + ", " + screenBounds.y + ", " +
                    screenBounds.width + ", " + screenBounds.height + ")");
            System.out.println("  Screen Insets: top=" + insets.top + ", left=" + insets.left +
                    ", bottom=" + insets.bottom + ", right=" + insets.right);
            System.out.println("  Usable Bounds: (" + (screenBounds.x + insets.left) + ", " +
                    (screenBounds.y + insets.top) + ", " +
                    (screenBounds.width - insets.left - insets.right) + ", " +
                    (screenBounds.height - insets.top - insets.bottom) + ")");
            System.out.println("  Transform Scale: scaleX=" + transform.getScaleX() +
                    ", scaleY=" + transform.getScaleY());
            System.out.println("  Bits per pixel: " + device.getDisplayMode().getBitDepth());
            System.out.println("  Refresh rate: " + device.getDisplayMode().getRefreshRate() + "Hz");
        }

        // Desktop environment information - directly from System.getenv()
        System.out.println("\n----- DESKTOP ENVIRONMENT -----");
        Map<String, String> env = System.getenv();
        String[] desktopVars = {"XDG_SESSION_TYPE", "XDG_CURRENT_DESKTOP", "DESKTOP_SESSION"};
        for (String var : desktopVars) {
            System.out.println(var + ": " + env.getOrDefault(var, "[not set]"));
        }

        // Try to get more desktop info with shell command - properly evaluating variables
        try {
            System.out.println("\n----- DESKTOP ENVIRONMENT (Shell) -----");
            Process process = Runtime.getRuntime().exec(new String[] {"bash", "-c", "env | grep -E 'XDG_|DESKTOP|GNOME|KDE|WAYLAND'"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error getting shell desktop info: " + e.getMessage());
        }

        // Specific environment variables
        System.out.println("\n----- ENVIRONMENT VARIABLES -----");
        String[] relevantVars = {
                // X11 related
                "DISPLAY", "XAUTHORITY", "GNOME_DESKTOP_SESSION_ID", "KDE_FULL_SESSION",
                // Wayland related
                "WAYLAND_DISPLAY", "MOZ_ENABLE_WAYLAND",
                // Scale related
                "GDK_SCALE", "GDK_DPI_SCALE", "QT_SCALE_FACTOR", "QT_AUTO_SCREEN_SCALE_FACTOR",
                "QT_SCREEN_SCALE_FACTORS", "_JAVA_OPTIONS", "JAVA_TOOL_OPTIONS",
                // Crostini related
                "SOMMELIER_VERSION", "SOMMELIER_ACCELERATED", "SOMMELIER_DRM_DEVICE",
                // General
                "LANG", "SHELL", "TERM", "HOME", "USER"
        };

        for (String var : relevantVars) {
            System.out.println(var + "=" + env.getOrDefault(var, "[not set]"));
        }

        // Java specific properties
        System.out.println("\n----- JAVA PROPERTIES -----");
        Properties props = System.getProperties();
        String[] relevantProps = {
                "java.version", "java.vendor", "java.vm.name", "java.vm.version",
                "os.name", "os.version", "os.arch", "file.encoding",
                "user.language", "user.country", "sun.java2d.uiScale", "sun.java2d.dpiaware"
        };

        for (String prop : relevantProps) {
            System.out.println(prop + "=" + props.getProperty(prop, "[not set]"));
        }

        // Try to detect if running in Crostini
        System.out.println("\n----- CROSTINI DETECTION -----");
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            System.out.println("Kernel version: " + version);
            boolean isCrostini = version != null && version.toLowerCase().contains("chrome");
            System.out.println("Likely running in Crostini: " + isCrostini);

            // Additional check for Linux container on ChromeOS
            process = Runtime.getRuntime().exec(new String[] {"bash", "-c", "grep -i chrome /sys/devices/virtual/dmi/id/product_name 2>/dev/null || echo 'Not found'"});
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String product = reader.readLine();
            System.out.println("ChromeOS hardware detection: " + product);
            reader.close();
        } catch (Exception e) {
            System.out.println("Error detecting Crostini: " + e.getMessage());
        }

        // Divider for readability
        System.out.println("\n============================================");
    }
}