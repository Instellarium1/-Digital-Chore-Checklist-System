package chorechecklist;

import chorechecklist.ui.DigitalChoreChecklistApp;
import javax.swing.SwingUtilities;

/** The single application entry point. Run the whole system from this class. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DigitalChoreChecklistApp().setVisible(true));
    }
}
