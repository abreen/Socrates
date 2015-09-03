package io.breen.socrates.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DetailOptionPane extends JOptionPane {

    public static void showMessageDialog(Component parent, Object message, String title,
                                         int messageType, String detailText)
    {
        String[] opts = {"OK", "Details..."};
        int opt = JOptionPane.showOptionDialog(
                parent, message, title, JOptionPane.DEFAULT_OPTION, messageType, null, opts, opts[0]
        );

        if (opt == 1) {
            JTextArea textArea = new JTextArea(detailText);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            JButton closeButton = new JButton("Close");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeButton);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.PAGE_END);

            final JDialog dialog = new JDialog((Frame)null, "Details", true);
            dialog.setContentPane(panel);
            dialog.setSize(new Dimension(400, 300));
            dialog.setLocationRelativeTo(null);

            closeButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    }
            );

            dialog.setVisible(true);
        }
    }
}
