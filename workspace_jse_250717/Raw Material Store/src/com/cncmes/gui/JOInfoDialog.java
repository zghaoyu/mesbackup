package com.cncmes.gui;

import com.alibaba.fastjson2.JSON;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.JOInfo;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * *Zhong
 * *
 */
public class JOInfoDialog extends JDialog {
    private static final long serialVersionUID = 14L;
    String[] infoTitle = new String[] { "MoId", "MoCode NO", "PartNumber","DWG","Qty","OpSeq","OpCode" };
    private String joINFO = "com.cncmes.dto.JOInfo";
    private final JPanel contentPanel = new JPanel();
    private final JPanel dbInfoPanel = new JPanel();
    private static JOInfoDialog joInfoDialog = new JOInfoDialog();
    private JTextField txtJO;
    private JButton searchJO;
    private JTable tableJO;

    public JTable getTableJO() {
        return tableJO;
    }

    public void setTableJO(JTable tableJO) {
        this.tableJO = tableJO;
    }

    public static JOInfoDialog getInstance(){
        return joInfoDialog;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public static JOInfoDialog getJoInfo() {
        return joInfoDialog;
    }

    public static void setJoInfo(JOInfoDialog joInfoDialog) {
        JOInfoDialog.joInfoDialog = joInfoDialog;
    }

    public JTextField getTxtJO() {
        return txtJO;
    }

    public void setTxtJO(JTextField txtJO) {
        this.txtJO = txtJO;
    }

    public JButton getSearchJO() {
        return searchJO;
    }

    public void setSearchJO(JButton searchJO) {
        this.searchJO = searchJO;
    }

    public void showDialog(){

        joInfoDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        joInfoDialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                joInfoDialog.dispose();

            }
        });

        txtJO.setText("");
        joInfoDialog.setVisible(true);
    }


    private JOInfoDialog() {
        setTitle("Check JO Info");
        setModal(true);

        int width = 700;
        int height = 500;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblBarcode = new JLabel("MoCode NO:");
            GridBagConstraints gbc_lblBarcode = new GridBagConstraints();
            gbc_lblBarcode.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblBarcode.insets = new Insets(0, 0, 0, 5);
            gbc_lblBarcode.gridx = 0;
            gbc_lblBarcode.gridy = 0;
            contentPanel.add(lblBarcode, gbc_lblBarcode);
        }
        {
            txtJO = new JTextField();
            GridBagConstraints gbc_txtBarcode = new GridBagConstraints();
//            gbc_txtBarcode.fill = GridBagConstraints.BOTH;
            gbc_txtBarcode.anchor = GridBagConstraints.WEST;
            gbc_txtBarcode.gridx = 1;
            gbc_txtBarcode.gridy = 0;
            contentPanel.add(txtJO, gbc_txtBarcode);
            txtJO.setColumns(30);
        }
        {
            searchJO = new JButton("search");
            searchJO.setFont(new Font("Tahoma", Font.PLAIN, 12));
            GridBagConstraints gbc_btnSearch = new GridBagConstraints();
            gbc_btnSearch.fill = GridBagConstraints.HORIZONTAL;
            gbc_btnSearch.insets = new Insets(0, 0, 0, 0);
            gbc_btnSearch.gridx = 2;
            gbc_btnSearch.gridy = 0;
            contentPanel.add(searchJO, gbc_btnSearch);
        }
        searchJO.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String joNO = txtJO.getText().trim();
                try {
                    ArrayList<Object> joInfos = findJOInfoByMoCode(joNO);
                    showTable(joInfos);
                } catch (Exception throwables) {
                    throwables.printStackTrace();
                }
            }
        });



        {
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            getContentPanel().add(scrollPane);
            TableModel orderTableModel = new MyTableModel(infoTitle, new Object[0][3]);
            tableJO = new JTable(orderTableModel);
            tableJO.setRowHeight(20);

            tableJO.getTableHeader().setReorderingAllowed(false);
            scrollPane.setColumnHeaderView(tableJO);
            scrollPane.setViewportView(tableJO);

            GridBagConstraints gbc_table = new GridBagConstraints();
            gbc_table.fill = GridBagConstraints.HORIZONTAL;
            gbc_table.insets = new Insets(0, 0, 0, 0);
            gbc_table.gridx = 0;
            gbc_table.gridy = 1;
            gbc_table.gridwidth=3;
            contentPanel.add(scrollPane, gbc_table);


        }



    }
    public ArrayList<Object> findJOInfoByMoCode(String jono)
    {
        DAOImpl dao = new DAOImpl(joINFO,true);
        try {
            return dao.findJOInfoByMoCode(jono);
        } catch (SQLException throwables) {
            System.err.println("search error:this journal no hasn't loaded into the system ");
            throwables.printStackTrace();
        }

        return null;
    }
    private void showTable(ArrayList<Object> joInfos){
        Object[][] data = new Object[joInfos.size()][infoTitle.length];
        for(int i = 0;i<joInfos.size();i++)
        {
            JOInfo info = JSON.parseObject(JSON.toJSONString(joInfos.get(i)),JOInfo.class);
            data[i][0] = info.getId();
            data[i][1] = info.getMoCode();
            data[i][2] = info.getPartNumber();
            data[i][3] = info.getDwg();
            data[i][4] = info.getQty();
            data[i][5] = info.getOpSeq();
            data[i][6] = info.getOpCode();
        }
        getTableJO().setModel(new MyTableModel(infoTitle,data));

    }
    class MyTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 17L;
        private Object[][] myData;
        private String[] title;
        private int[] editableCol;
        private int rowCount = 0;
        private int colCount = 0;

        public MyTableModel(String[] tableTitle, Object[][] tableData, int... editableCol) {
            super();
            title = tableTitle;
            myData = tableData;
            colCount = tableTitle.length;
            if (null != tableData)
                rowCount = tableData.length;
            this.editableCol = editableCol;
        }

        @Override
        public int getRowCount() {
            return rowCount;
        }

        @Override
        public int getColumnCount() {
            return colCount;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return myData[rowIndex][columnIndex];
        }

        public void setValueAt(Object value, int row, int column) {
            myData[row][column] = value;
        }

        public String getColumnName(int column) {
            return title[column];
        }

        public boolean isCellEditable(int row, int column) {
            if (null != editableCol && editableCol.length > 0) {
                for (int i = 0; i < editableCol.length; i++) {
                    if (column == editableCol[i])
                        return true;
                }
            }
            return false;
        }

    }
}
