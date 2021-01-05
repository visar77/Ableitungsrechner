package com.ableitungsrechner;

import com.ableitungsrechner.GUI.Window;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Window("Visars Ableitungsrechner"));
    }

}
