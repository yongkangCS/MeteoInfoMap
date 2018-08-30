/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.forms;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Yaqiang Wang 
 */
public class ShapeFileChooser extends JFileChooser {
    
    JComboBox encodingCB;
    
    /**
     * Constructor
     */
    public ShapeFileChooser() {
        super();
        encodingCB = new JComboBox();
        SortedMap m = Charset.availableCharsets();
        DefaultComboBoxModel cbm = new DefaultComboBoxModel();
        cbm.addElement("System");
        Iterator ir = m.keySet().iterator();
        while (ir.hasNext()) {
            String n = (String) ir.next();
            Charset e = (Charset) m.get(n);
            cbm.addElement(e.displayName());
        }
        encodingCB.setModel(cbm);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 10, 0, 0);
        constraints.gridx = 0;
        constraints.gridy = 0; 
        panel.add(new JLabel("Encoding:"), constraints);
        constraints.gridy = 1;
        panel.add(encodingCB, constraints);
        setAccessory(panel);
    }
    
    /**
     * Get encoding string
     * @return Encoding string
     */
    public String getEncoding() {
        String encoding = this.encodingCB.getSelectedItem().toString();
        if (encoding.equals("System")){
            encoding = Charset.defaultCharset().displayName();
        }
        return encoding;
    }
}
