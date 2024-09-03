package com.cncmes.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.cncmes.dto.CNCMaterial;
import com.cncmes.dto.CNCProcessCard;
import com.cncmes.dto.Fixture;
import com.cncmes.dto.FixtureMaterial;
import com.cncmes.gui.dialog.AddFixture;
import com.cncmes.gui.model.InputTableModel;
import com.cncmes.gui.panel.InputPanel;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.GUIUtils;

/**
 * 夹具条码与材料条码绑定界面的按钮监听器
 *
 * @author W000586 Hui Zhi Fang 2022/4/12
 */
public class InputPanelListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        InputPanel inputPanel = InputPanel.getInstance();
        String command = e.getActionCommand();
        if (command.equals(inputPanel.getBtnAdd().getText())) {
            addData(inputPanel);

        } else if (command.equals(inputPanel.getBtnClear().getText())) {
            inputPanel.clearAllData();

        } else if (command.equals(inputPanel.getBtnConfirm().getText())) {
            checkFixtureStatus(inputPanel);

        } else if (command.equals(inputPanel.getBtnDelete().getText())) {
            deleteFixtureMaterial();

        } else if (command.equals(inputPanel.getBtnDeleteAll().getText())) {
            deleteAllData();

        } else if (command.equals(inputPanel.getBtnSubmit().getText())) {

        }
    }

    // Confirm button
    private void checkFixtureStatus(InputPanel inputPanel) {
        String fixtureNo = inputPanel.getInput_fixture().getText().trim();
        ArrayList<FixtureMaterial> fixtureMaterials;
        // 1. Check empty
        if (!GUIUtils.checkEmpty(inputPanel.getInput_fixture(), "Fixture No", inputPanel.getLblMessage())) {

            // 2. Query fixture No
            Fixture fixture = DataUtils.getFixtureInfo(fixtureNo);
            if (null == fixture) {
                // add fixture
                int result = JOptionPane.showConfirmDialog(null, "Fixture No : " + fixtureNo + " 未录入系统，是否要新增？", "Error",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    AddFixture dialog = AddFixture.getInstance();
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setVisible(true);
                } else {
                    inputPanel.clearAllData();
                }
            } else {
                // 3. Query materials on fixture
                fixtureMaterials = DataUtils.getFixtureMaterial(fixture.getId(), 0, 0);
                fixtureMaterials.addAll(DataUtils.getFixtureMaterial(fixture.getId(), 1, 0));
                if (fixtureMaterials.size() > 0) {
                    int result = JOptionPane.showConfirmDialog(null, "夹具" + fixtureNo + "已绑定材料，是否要进行修改？", "Comfirm",
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        // display data
                        showTable(fixtureMaterials);
                        inputPanel.setFixtureEditable(false);
                        inputPanel.setMaterialEditable(true);
                        inputPanel.setCurrentFixture(fixture);
                    } else if (result == JOptionPane.NO_OPTION) {
                        inputPanel.clearAllData();
                    }
                } else {
                    inputPanel.clearTable();
                    inputPanel.setFixtureEditable(false);
                    inputPanel.setMaterialEditable(true);
                    inputPanel.setCurrentFixture(fixture);
                    JOptionPane.showMessageDialog(null, "夹具状态正常，请输入材料条码");
                }
            }
        }
    }

    // Add button
    private void addData(InputPanel inputPanel) {
        ArrayList<FixtureMaterial> fixtureMaterials = new ArrayList<>();
        String fixtureNo = inputPanel.getInput_fixture().getText().trim();
        String materialNo = inputPanel.getInput_material().getText().trim();

        if (dataIsOkToSave(inputPanel)) {
            // 1.Get fixture
            Fixture fixture = inputPanel.getCurrentFixture();
            if (null == fixture) {
                JOptionPane.showMessageDialog(null, "请先确认夹具 : " + fixtureNo + " 的状态", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {

                // 2.Get material by 'material_no'
                CNCMaterial material = DataUtils.getMaterial(materialNo);
                if (null == material) {
                    JOptionPane.showMessageDialog(null, "Material No : " + materialNo + " 未录入系统", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    ArrayList<FixtureMaterial> fixtureMaterial = DataUtils.getFixtureMaterialByMaterialId(material.getId(), 0, 0, inputPanel.getCncLine().getId(), inputPanel.getRack().getId());
                    if (fixtureMaterial != null && fixtureMaterial.size() > 0) { //材料已被绑定过
                        int result = JOptionPane.showConfirmDialog(null, "Material No : " + materialNo + " 已绑定夹具，是否要重新绑定？", "Error",
                                JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            // Save data
                            if (DataUtils.saveFixtureMaterial(fixture.getId(), material.getId(), inputPanel.getCncLine().getId(), inputPanel.getRack().getId())) {
                                DataUtils.deleteFixtureMaterialById(fixtureMaterial.get(0).getId());
                                JOptionPane.showMessageDialog(null, "保存成功");
                                // display data
                                inputPanel.getInput_material().setText("");
                                fixtureMaterials = DataUtils.getFixtureMaterial(fixture.getId(), 0, 0);
                                showTable(fixtureMaterials);
                            } else {
                                JOptionPane.showMessageDialog(null, "保存失败", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }

                    } else {
                        // 3. Save data
                        if (DataUtils.saveFixtureMaterial(fixture.getId(), material.getId(), inputPanel.getCncLine().getId(), inputPanel.getRack().getId())) {
                            JOptionPane.showMessageDialog(null, "保存成功");
                            // display data
                            inputPanel.getInput_material().setText("");
                            fixtureMaterials = DataUtils.getFixtureMaterial(fixture.getId(), 0, 0);
                            showTable(fixtureMaterials);
                        } else {
                            JOptionPane.showMessageDialog(null, "保存失败", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    /*
     * private boolean checkDrawing(CNCMaterial material) {
     *
     * return false; }
     */

    private void refreshTable() {
        InputPanel inputPanel = InputPanel.getInstance();
        ArrayList<FixtureMaterial> fixtureMaterials;
        Fixture fixture;

        fixture = inputPanel.getCurrentFixture();
        if (null != fixture) {
            fixtureMaterials = DataUtils.getFixtureMaterial(fixture.getId(), 0, 0);
            showTable(fixtureMaterials);
        }

    }

    // need to modify
    private void showTable(ArrayList<FixtureMaterial> fixtureMaterials) {
        InputPanel inputPanel = InputPanel.getInstance();
        if (fixtureMaterials.size() > 0) {
            String[] tableTitle = inputPanel.getTableTitle();
            int rowCount = fixtureMaterials.size();
            int colCount = tableTitle.length;
            Object[][] tableData = new Object[rowCount][colCount];
            CNCMaterial material;
            CNCProcessCard processCard;
            for (int i = 0; i < rowCount; i++) {
                material = DataUtils.getMaterialById(fixtureMaterials.get(i).getMaterial_id());
                if (null != material) {
                    processCard = DataUtils.getProcessCard(material.getProcesscard_id());
                    if (null != processCard) {
                        tableData[i][0] = fixtureMaterials.get(i).getId();
                        tableData[i][1] = material;
                        tableData[i][2] = processCard.getOrder_no();
                        tableData[i][3] = processCard.getDrawing_no();
                        tableData[i][4] = fixtureMaterials.get(i).getScan_time();
                    }
                }
            }
            inputPanel.refreshTable(new InputTableModel(tableData, tableTitle));

        } else {
            inputPanel.clearTable();
        }
    }

    private boolean dataIsOkToSave(InputPanel inputPanel) {
        if (!GUIUtils.checkEmpty(inputPanel.getInput_fixture(), "Fixture No", inputPanel.getLblMessage())) {
            if (!GUIUtils.checkEmpty(inputPanel.getInput_material(), "Material No", inputPanel.getLblMessage())) {
                if (inputPanel.getCncLine() != null && inputPanel.getRack() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void deleteAllData() {
        InputPanel inputPanel = InputPanel.getInstance();
        int rowCount = inputPanel.getTable().getRowCount();
        if (rowCount > 0) {
            int result = JOptionPane.showConfirmDialog(null, "请确认是否要删除所有数据？", "Comfirm", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                for (int i = 0; i < rowCount; i++) {
                    int fixtureMaterialId = (int) inputPanel.getTable().getModel().getValueAt(i, 0);
                    DataUtils.deleteFixtureMaterialById(fixtureMaterialId);
                }
                refreshTable();
            }
        }
    }

    private void deleteFixtureMaterial() {
        InputPanel inputPanel = InputPanel.getInstance();
        int[] rows = inputPanel.getTable().getSelectedRows();
        if (rows.length > 0) {
            int result = JOptionPane.showConfirmDialog(null, "请确认是否要删除选中的数据？", "Comfirm", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                for (int i = 0; i < rows.length; i++) {
                    int fixtureMaterialId = (int) inputPanel.getTable().getModel().getValueAt(rows[i], 0);
                    DataUtils.deleteFixtureMaterialById(fixtureMaterialId);
                }
                refreshTable();
            }

        }
    }

}
