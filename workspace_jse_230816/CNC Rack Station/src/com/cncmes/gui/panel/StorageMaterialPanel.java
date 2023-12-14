package com.cncmes.gui.panel;

import com.cncmes.dto.CNCLine;
import com.cncmes.dto.Fixture;
import com.cncmes.dto.Rack;
import com.cncmes.gui.model.InputTableModel;
import com.cncmes.gui.model.LineComboBoxModel;
import com.cncmes.utils.GUIUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * *Zhong
 * *
 */
public class StorageMaterialPanel extends JPanel {
    private JPanel panel_input;
    private JPanel panel_input_title;
    private JPanel panel_input_content;

    private JLabel lblInput;
    private JLabel lblFixtureNo;
    private JLabel lblMaterialNo;
    private JLabel lblMessage;

    private JTextField input_fixture;
    private JTextField input_material;
    private JScrollPane scrollPane;
    private JTable table;
    private JButton btnAdd;
    private JButton btnClear;
    private JLabel subOrder;
    private JLabel lblRack;
    private JComboBox<CNCLine> comboBoxLineName;
    private JComboBox<Rack> comboBoxRack;
    private JButton btnSubmit;
    private JButton btnConfirm;
    private JPanel panel;
    private JLabel lblQty;
    private JLabel lblMaterialCount;
    private JButton btnDelete;
    private JButton btnDeleteAll;

    private Fixture currentFixture;
    private String[] tableTitle;
    private InputTableModel emptyTableModel;

    private static StorageMaterialPanel storageMaterialPanel = new StorageMaterialPanel();
    public static StorageMaterialPanel getInstance(){
        return storageMaterialPanel;
    }
    private StorageMaterialPanel(){
        this.setBackground(new Color(245, 245, 245));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 10, 7, 10, 0 };
        gridBagLayout.rowHeights = new int[] { 10, 5, 10, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        setLayout(gridBagLayout);

        panel_input = new JPanel();
        panel_input.setBorder(new LineBorder(Color.LIGHT_GRAY));
        panel_input.setBackground(Color.WHITE);
        GridBagConstraints gbc_panel_input = new GridBagConstraints();
        gbc_panel_input.insets = new Insets(0, 0, 5, 5);
        gbc_panel_input.fill = GridBagConstraints.BOTH;
        gbc_panel_input.gridx = 1;
        gbc_panel_input.gridy = 1;
        add(panel_input, gbc_panel_input);
        panel_input.setLayout(new BorderLayout(0, 0));

        panel_input_title = new JPanel();
        panel_input_title.setPreferredSize(new Dimension(10, 25));
        panel_input_title.setBackground(SystemColor.controlHighlight);
        panel_input.add(panel_input_title, BorderLayout.NORTH);

        lblInput = new JLabel("Storage Material");
        lblInput.setFont(new Font("Tahoma", Font.BOLD, 12));
        panel_input_title.add(lblInput);


        panel_input_content = new JPanel();
        panel_input_content.setBackground(Color.WHITE);
        panel_input.add(panel_input_content, BorderLayout.CENTER);
        GridBagLayout gbl_panel_input_content = new GridBagLayout();
        gbl_panel_input_content.columnWidths = new int[] { 10, 0, 120, 0, 30, 0, 120, 0, 0, 5, 0 };
        gbl_panel_input_content.rowHeights = new int[] { 35, 0, 20, 25, 10, 25, 25, 25, 0, 0, 0, 5, 0 };
        gbl_panel_input_content.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE };
        gbl_panel_input_content.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE };
        panel_input_content.setLayout(gbl_panel_input_content);

        subOrder = new JLabel("SubOrder No:");
        subOrder.setFont(GUIUtils.contentFont);
        GridBagConstraints gbc_lblLineName = new GridBagConstraints();
        gbc_lblLineName.anchor = GridBagConstraints.EAST;
        gbc_lblLineName.insets = new Insets(0, 0, 5, 5);
        gbc_lblLineName.gridx = 1;
        gbc_lblLineName.gridy = 1;
        panel_input_content.add(subOrder, gbc_lblLineName);

        comboBoxLineName = new JComboBox<CNCLine>();
        comboBoxLineName.setFont(GUIUtils.contentFont);

        comboBoxLineName.setModel(new LineComboBoxModel());
        comboBoxLineName.setPreferredSize(new Dimension(80, 20));
        GridBagConstraints gbc_comboBoxLineName = new GridBagConstraints();
        gbc_comboBoxLineName.insets = new Insets(0, 0, 5, 5);
        gbc_comboBoxLineName.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBoxLineName.gridx = 2;
        gbc_comboBoxLineName.gridy = 1;
        panel_input_content.add(comboBoxLineName, gbc_comboBoxLineName);

    }
}
