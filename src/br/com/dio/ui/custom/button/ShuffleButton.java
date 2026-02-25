package br.com.dio.ui.custom.button;

import java.awt.event.ActionListener;
import javax.swing.JButton;

public class ShuffleButton extends JButton {

    public ShuffleButton(final ActionListener actionListener){
        this.setText("Embaralhar");
        this.addActionListener(actionListener);
    }

}
