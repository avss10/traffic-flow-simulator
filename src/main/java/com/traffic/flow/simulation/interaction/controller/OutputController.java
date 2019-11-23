package com.traffic.flow.simulation.interaction.controller;

import javax.swing.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Output controller
 */
@ToString
@EqualsAndHashCode
public class OutputController {

    private int txtRow = 25;
    public JScrollPane resultPanel;
    public JTextArea textArea;

    public OutputController() {
        this.resultPanel = resultPanel();
    }

    private JScrollPane resultPanel() {
        // Result Panel
        this.textArea = new JTextArea();
        textArea.setRows(txtRow);
        textArea.setText("This text area shows traffic flow simulator updates!\n");
        JScrollPane bottomPanel = new JScrollPane(textArea);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        return bottomPanel;
    }
}
