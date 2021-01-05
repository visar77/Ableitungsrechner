package com.ableitungsrechner.GUI;

import com.ableitungsrechner.Math.DividingbyNullException;
import com.ableitungsrechner.Math.Function;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends JFrame implements ActionListener{
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static int width = screenSize.width,height = screenSize.height;
    private final JPanel panel = new JPanel();
    private final JButton okButton = new JButton();
    private final JTextField functionField = new JTextField(30);
    private final JLabel label = new JLabel();
    private final JLabel result = new JLabel();

    public Window(String title) {

        //window stuff
        setTitle(title);

        setBackground(Color.white);
        //setResizable(false);
        setSize(screenSize);
        setPreferredSize(screenSize);
        setLocationRelativeTo(null);

        //panel stuff
        panel.setLayout(null);

        okButton.setBounds(width/2+width/15,height/2,width/15,height/20);
        okButton.setText("OK");
        okButton.addActionListener(this);
        panel.add(okButton);

        functionField.setBounds(width/2-width/5,height/2,width/4,height/20);
        functionField.setText("Enter Function");
        functionField.setFont(new Font("Monospaced", Font.BOLD,height/22));
        panel.add(functionField);

        label.setBounds(width/2-width/4-width/20,height/4,width/2 + width/3,height/8);
        label.setText("Ableitungsrechner");
        label.setFont(new Font("Monospaced",Font.BOLD,height/10));
        label.setBackground(Color.darkGray);
        panel.add(label);

        result.setBounds(width/20,height/2+height/5,width,height/12);
        panel.add(result);

        add(panel);
        panel.setBackground(Color.white);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okButton) {
            Function function = new Function(functionField.getText());
            functionField.setText("");
            String fxandfdx = "";
            try {
                fxandfdx = "Result: " + function.getDerivative();
            }catch(Exception ex) {
                if(ex instanceof DividingbyNullException) fxandfdx = "You can't divide by null";
                ex.printStackTrace();
            }
            int scale = 0;
            while(true) {
                if (fxandfdx.length() > scale*18) {
                    scale++;
                    continue;
                }
                System.out.println("scale: " + scale);
                if(fxandfdx.length() <= scale*18) {
                    int resultFontSize = height / (scale*6+3);
                    result.setFont(new Font("Monospaced", Font.BOLD, resultFontSize));
                    break;
                }
            }
            result.setText(fxandfdx);
            revalidate();
            repaint();
        }
    }

}
