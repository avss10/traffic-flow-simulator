package com.traffic.flow.simulation.interaction.components;

import java.awt.*;
import javax.swing.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dialog window for display warnings.
 */
@ToString
@EqualsAndHashCode
public class AttentionDialog {

    public AttentionDialog(String frameName, String content) {
        Frame dialogFrame = new Frame();
        JDialog d1 = new JDialog(dialogFrame, frameName, true);
        Label label = new Label(content, Label.CENTER);
        d1.add(label);
        d1.setSize(400, 200);
        d1.setVisible(true);
        d1.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
}