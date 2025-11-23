package karnickeldev.solar.ui;

import karnickeldev.solar.Metadata;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 07.10.2024
 */
public class ErrorTextbox extends JDialog {

    public ErrorTextbox(String errorMessage) {
        setTitle(Metadata.APP_NAME + " v" + Metadata.VERSION);

        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dialogWidth = screenSize.width / 4;
        int dialogHeight = screenSize.height / 5;
        setSize(dialogWidth, dialogHeight);
        setLocationRelativeTo(null);

        // Set layout
        setLayout(null);

        setIconImage(((ImageIcon) javax.swing.UIManager.getIcon("OptionPane.errorIcon")).getImage());

        // Custom label with white text
        JTextPane text = new JTextPane();

        text.setText("An error occurred while running " + Metadata.APP_NAME + "\n\n" + errorMessage);

        StyledDocument documentStyle = text.getStyledDocument();
        SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), centerAttribute, false);

        text.setSize((6 * dialogWidth) / 10, (5 * dialogHeight) / 10);
        text.setLocation(2 * dialogWidth / 10, dialogHeight / 10);

        text.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        text.setAlignmentY(JTextArea.TOP_ALIGNMENT);
        text.setEditable(false);
        text.setBackground(getBackground());
        text.setFocusable(false);

        Font font = new Font("monospace", Font.PLAIN, 14);
        text.setFont(font);
        text.setOpaque(true);
        add(text);

        // Error Icon
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.errorIcon"), JLabel.CENTER);
        iconLabel.setLocation(0, dialogHeight / 10);
        int dimension = Math.min((2 * dialogWidth) / 10, (6 * dialogHeight) / 10);
        iconLabel.setSize(dimension, dimension);
        add(iconLabel);

        JButton closeButton = new JButton("Close");
        closeButton.setFocusPainted(false);
        closeButton.setOpaque(true);
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        closeButton.setSize((2 * dialogWidth) / 10, (dialogHeight) / 10);
        closeButton.setLocation(dialogWidth / 2 - closeButton.getWidth() / 2, (7 * dialogHeight) / 10);

        closeButton.addActionListener(e -> dispose());

        add(closeButton);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        Toolkit.getDefaultToolkit().beep();
    }

}
