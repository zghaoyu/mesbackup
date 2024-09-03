package com.cncmes.gui;

import com.alibaba.fastjson2.JSON;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.JOInfo;
import com.cncmes.dto.OrderScheduler;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * *Zhong
 * *
 */
public class OrderStatusDialog extends JDialog {
    private JComboBox<String> comboBoxIP;
    private JComboBox<String> comboBoxCommand;
    private JTextField textField_Info;

    private static final long serialVersionUID = 18L;
    private final JPanel contentPanel = new JPanel();
    private static OrderStatusDialog orderStatusDialog = new OrderStatusDialog();

    private JTextField txtJO;
    private JButton searchJO;
    private JTable tableJO;
    private JTable tableUncompleted;
    private JTable tableComplete;
    private String orderSchedulerDTO = "com.cncmes.dto.OrderScheduler";
    String[] UncompleteInfoTitle = new String[]{"订单", "交货日期", "待加工数", "加工时间"};
    String[] CompleteInfoTitle = new String[]{"订单", "交货日期", "待加工数", "加工时间"};
    private static boolean[] lockFlags = new boolean[6];
    private static boolean[] ncUploadFlags = new boolean[6];
    private static boolean doorClosedFlag = false;

    public static OrderStatusDialog getInstance() {
        return orderStatusDialog;
    }

    private void fitTableColumns(JTable myTable) {
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration<TableColumn> columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) header.getDefaultRenderer().getTableCellRendererComponent
                    (myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent
                        (myTable, myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column);
            column.setWidth((int) (width * 1.5) + myTable.getIntercellSpacing().width);
        }
    }

    private void setTableModel(JTable myTable, String[] title, Object[][] data) {
        myTable.setModel(new MyTableModel(title, data));
        myTable.setRowHeight(25);
        fitTableColumns(myTable);
    }

    /**
     * Create the dialog.
     */
    private OrderStatusDialog() {
        setModal(true);
        setTitle("Query Order Status");
        setResizable(false);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 700;
        int height = 560;
        setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();

        gbl_contentPanel.columnWidths = new int[]{0, 0};
        gbl_contentPanel.rowHeights = new int[]{30, 175, 30, 130, 30, 125};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 2.0, 0.0, 1.0, 0.0, 1.0};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblCommonSetting = new JLabel("未完成订单");
            GridBagConstraints gbc_lblCommonSetting = new GridBagConstraints();
            gbc_lblCommonSetting.insets = new Insets(0, 0, 5, 0);
            gbc_lblCommonSetting.gridx = 0;
            gbc_lblCommonSetting.gridy = 0;
            contentPanel.add(lblCommonSetting, gbc_lblCommonSetting);
            JButton refreshUnComplete = new JButton("刷新");
            GridBagConstraints gbc_RefreshunComplete = new GridBagConstraints();
            gbc_RefreshunComplete.insets = new Insets(0, 0, 5, 0);
            gbc_RefreshunComplete.gridx = 1;
            gbc_RefreshunComplete.gridy = 0;
            contentPanel.add(refreshUnComplete, gbc_RefreshunComplete);
            refreshUnComplete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getTableComplete().setModel(new MyTableModel(CompleteInfoTitle,completeTableData(getAllCompletedOrder())));
                    getTableUncompleted().setModel(new MyTableModel(UncompleteInfoTitle,uncompleteTableData(getAllUncompletedOrder())));
                }
            });

        }
        {
            JScrollPane scrollPaneCommonSetting = new JScrollPane();
            GridBagConstraints gbc_scrollPaneCommonSetting = new GridBagConstraints();
            gbc_scrollPaneCommonSetting.insets = new Insets(0, 0, 5, 0);
            gbc_scrollPaneCommonSetting.fill = GridBagConstraints.BOTH;
            gbc_scrollPaneCommonSetting.gridx = 0;
            gbc_scrollPaneCommonSetting.gridy = 1;
            contentPanel.add(scrollPaneCommonSetting, gbc_scrollPaneCommonSetting);
            {
                tableUncompleted = new JTable(uncompleteTableData(getAllUncompletedOrder()), UncompleteInfoTitle);
                scrollPaneCommonSetting.setViewportView(tableUncompleted);

            }
        }
        {
            JLabel lblComplete = new JLabel("已完成订单");
            GridBagConstraints gbc_lblComplete = new GridBagConstraints();
            gbc_lblComplete.insets = new Insets(0, 0, 5, 0);
            gbc_lblComplete.gridx = 0;
            gbc_lblComplete.gridy = 2;
            contentPanel.add(lblComplete, gbc_lblComplete);
        }
        {
            JScrollPane scrollPaneComplete = new JScrollPane();
            GridBagConstraints gbc_scrollPaneDatabaseSetting = new GridBagConstraints();
            gbc_scrollPaneDatabaseSetting.insets = new Insets(0, 0, 5, 0);
            gbc_scrollPaneDatabaseSetting.fill = GridBagConstraints.BOTH;
            gbc_scrollPaneDatabaseSetting.gridx = 0;
            gbc_scrollPaneDatabaseSetting.gridy = 3;
            contentPanel.add(scrollPaneComplete, gbc_scrollPaneDatabaseSetting);
            {
                tableComplete = new JTable(completeTableData(getAllCompletedOrder()), CompleteInfoTitle);
                scrollPaneComplete.setViewportView(tableComplete);
            }
        }

        {
            txtJO = new JTextField("请输入订单号:");
            GridBagConstraints gbc_txtBarcode = new GridBagConstraints();
            gbc_txtBarcode.fill = GridBagConstraints.BOTH;
            gbc_txtBarcode.gridx = 0;
            gbc_txtBarcode.gridy = 4;
            contentPanel.add(txtJO, gbc_txtBarcode);
            txtJO.setColumns(30);

            txtJO.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtJO.getText().equals("请输入订单号:")){
                    txtJO.setText("");     //将提示文字清空
                    txtJO.setForeground(Color.black);  //设置用户输入的字体颜色为黑色
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtJO.getText().equals("")){
                    txtJO.setForeground(Color.gray); //将提示文字设置为灰色
                    txtJO.setText("请输入订单号:");     //显示提示文字
                }
            }
        });
        }
        {
            searchJO = new JButton("search");
            searchJO.setFont(new Font("Tahoma", Font.PLAIN, 12));
            GridBagConstraints gbc_btnSearch = new GridBagConstraints();
            gbc_btnSearch.fill = GridBagConstraints.HORIZONTAL;
            gbc_btnSearch.insets = new Insets(0, 0, 0, 0);
            gbc_btnSearch.gridx = 1;
            gbc_btnSearch.gridy = 4;
            contentPanel.add(searchJO, gbc_btnSearch);
        }
        searchJO.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String joNO = txtJO.getText().trim();
                try {
                    showTable(getOrderSchedulerByOrder(joNO));
//                    ArrayList<Object> joInfos = findJOInfoByMoCode(joNO);
//                    showTable(joInfos);
                } catch (Exception throwables) {
                    throwables.printStackTrace();
                }
            }
        });
        {
            JScrollPane scrollPane = new JScrollPane();
            contentPanel.add(scrollPane);
            TableModel orderTableModel = new MyTableModel(CompleteInfoTitle, new Object[0][3]);
            tableJO = new JTable(orderTableModel);
            tableJO.getTableHeader().setReorderingAllowed(false);
            scrollPane.setColumnHeaderView(tableJO);
            scrollPane.setViewportView(tableJO);

            GridBagConstraints gbc_table = new GridBagConstraints();
            gbc_table.fill = GridBagConstraints.BOTH;
            gbc_table.insets = new Insets(0, 0, 5, 0);
            gbc_table.gridx = 0;
            gbc_table.gridy = 5;
            contentPanel.add(scrollPane, gbc_table);


        }
    }

    public JTable getTableJO() {
        return tableJO;
    }

    public void setTableJO(JTable tableJO) {
        this.tableJO = tableJO;
    }

    public void setButtonsEnabled(boolean enabled, boolean force) {
        try {
            if (force) {

            }
        } catch (Exception e) {
        }
    }
    private void showTable(ArrayList<OrderScheduler> orderSchedulers){
        Object[][] data = new Object[orderSchedulers.size()][CompleteInfoTitle.length];
        for(int i = 0;i<orderSchedulers.size();i++)
        {
            OrderScheduler orderScheduler = JSON.parseObject(JSON.toJSONString(orderSchedulers.get(i)), OrderScheduler.class);
            data[i][0] = orderScheduler.getMoCode();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            if(orderScheduler.getPeRequestDate()!=null)
            {
                data[i][1] = ft.format(orderScheduler.getPeRequestDate());
            }else data[i][1] = orderScheduler.getPeRequestDate();
            data[i][2] = orderScheduler.getQty();
            data[i][3] = orderScheduler.getProcesstime();
        }
        getTableJO().setModel(new MyTableModel(CompleteInfoTitle,data));
    }
    public ArrayList<OrderScheduler> getOrderSchedulerByOrder(String moCode)
    {
        ArrayList<OrderScheduler> orderSchedulers = new ArrayList<>();
        DAO dao = new DAOImpl(orderSchedulerDTO);
        try {
            List<Object> list = dao.findByCnd(new String[]{"moCode"}, new String[]{moCode});
            for (Object o : list) {
                orderSchedulers.add((OrderScheduler) o);
            }
//            System.out.println(orderSchedulers);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return orderSchedulers;
    }

    public ArrayList<OrderScheduler> getAllUncompletedOrder() {
        DAO dao = new DAOImpl(orderSchedulerDTO);
        ArrayList<OrderScheduler> orderSchedulers = new ArrayList<>();
        try {
            List<Object> list = dao.findByCnd(new String[]{"have_finish"}, new String[]{"0"});
            for (Object o : list) {
                orderSchedulers.add((OrderScheduler) o);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return orderSchedulers;
    }

    public ArrayList<OrderScheduler> getAllCompletedOrder() {
        DAO dao = new DAOImpl(orderSchedulerDTO);
        ArrayList<OrderScheduler> orderSchedulers = new ArrayList<>();
        List<Object> list;
        try {
            list = dao.findByCnd(new String[]{"have_finish"}, new String[]{"1"});
            for (Object o : list) {
                orderSchedulers.add((OrderScheduler) o);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return orderSchedulers;
    }

    public JTable getTableUncompleted() {
        return tableUncompleted;
    }

    public void setTableUncompleted(JTable tableUncompleted) {
        this.tableUncompleted = tableUncompleted;
    }

    public JTable getTableComplete() {
        return tableComplete;
    }

    public void setTableComplete(JTable tableComplete) {
        this.tableComplete = tableComplete;
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

    private void showUncompleteTable(ArrayList<OrderScheduler> uncompleteOrders) {
        Object[][] data = new Object[uncompleteOrders.size()][UncompleteInfoTitle.length];
        for (int i = 0; i < uncompleteOrders.size(); i++) {
            data[i][0] = uncompleteOrders.get(i).getMoCode();
            data[i][1] = uncompleteOrders.get(i).getPeRequestDate();
            data[i][2] = uncompleteOrders.get(i).getQty();
            data[i][3] = uncompleteOrders.get(i).getProcesstime();
        }
        getTableUncompleted().setModel(new MyTableModel(UncompleteInfoTitle, data));

    }

    private Object[][] uncompleteTableData(ArrayList<OrderScheduler> uncompleteOrders) {
        Object[][] data = new Object[uncompleteOrders.size()][UncompleteInfoTitle.length];
        for (int i = 0; i < uncompleteOrders.size(); i++) {
            OrderScheduler orderScheduler = JSON.parseObject(JSON.toJSONString(uncompleteOrders.get(i)), OrderScheduler.class);
            data[i][0] = orderScheduler.getMoCode();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            if(orderScheduler.getPeRequestDate()!=null)
            {
                data[i][1] = ft.format(orderScheduler.getPeRequestDate());
            }else data[i][1] = orderScheduler.getPeRequestDate();
            data[i][2] = orderScheduler.getQty();
            data[i][3] = orderScheduler.getProcesstime();
        }
        return data;
    }
    private Object[][] completeTableData(ArrayList<OrderScheduler> completeOrders) {
        Object[][] data = new Object[completeOrders.size()][UncompleteInfoTitle.length];
        for (int i = 0; i < completeOrders.size(); i++) {
            OrderScheduler orderScheduler = JSON.parseObject(JSON.toJSONString(completeOrders.get(i)), OrderScheduler.class);
            data[i][0] = orderScheduler.getMoCode();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            if(orderScheduler.getPeRequestDate()!=null)
            {
                data[i][1] = ft.format(orderScheduler.getPeRequestDate());
            }else data[i][1] = orderScheduler.getPeRequestDate();
            data[i][2] = orderScheduler.getQty();
            data[i][3] = orderScheduler.getProcesstime();
        }
        return data;
    }

}
