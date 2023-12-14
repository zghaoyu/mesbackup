package com.cncmes.utils;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class GUIUtils {
	public static final Color GRAY1 = new Color(200, 202, 201);
	public static final Color GRAY2 = Color.decode("#f0f0f0");
	public static final Font contentFont = new Font("Tahoma", Font.PLAIN, 12);
	public static final Font menuFont = new Font("Microsoft JhengHei", Font.BOLD, 13);//main panel

	public static void setColor(JComponent jComponent, Color color) {
		jComponent.setBackground(color);
	}

	/**
	 * Check whether the text field is empty
	 * 
	 * @param tf
	 * @param input
	 * @return true:empty
	 */
	public static boolean checkEmpty(JTextField tf, String input) {
		String text = tf.getText().trim();
		if (0 == text.length()) {
			JOptionPane.showMessageDialog(null, input + " can't be blank");
			tf.grabFocus();
			return true;
		}
		return false;

	}

	/**
	 * Check whether the text field is empty and show message on Label.
	 */
	public static boolean checkEmpty(JTextField tf, String input, JLabel label) {
		String text = tf.getText().trim();
		if (0 == text.length()) {
			setLabelText(label, input + " can't be blank");
			tf.grabFocus();
			return true;
		} else {
			setLabelText(label, "");
		}
		return false;

	}

	public static void setLabelText(JLabel lblObj, String txt) {
		if (null != lblObj) {
			lblObj.setText(txt);
			lblObj.repaint();
		}
	}

	public static void setComboBoxValues(JComboBox<String> comboBox, String[] vals) {
		if (null != comboBox && null != vals && vals.length > 0) {
			comboBox.setModel(new DefaultComboBoxModel<String>(vals));
		} else {
			if (null != comboBox)
				comboBox.removeAllItems();
		}
		if (null != comboBox)
			comboBox.repaint();
	}

	public static void setComboBoxSelectedIdx(JComboBox<String> comboBox, String val) {
		if (null != comboBox && null != val && comboBox.getItemCount() > 0) {
			for (int i = 0; i < comboBox.getItemCount(); i++) {
				if (val.equals(comboBox.getItemAt(i))) {
					comboBox.setSelectedIndex(i);
					break;
				}
			}
		}
	}


}
