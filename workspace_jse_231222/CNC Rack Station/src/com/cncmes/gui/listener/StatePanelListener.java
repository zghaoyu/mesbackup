package com.cncmes.gui.listener;

import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.Fixture;
import com.cncmes.dto.FixtureMaterial;
import com.cncmes.gui.model.StateTableModel;
import com.cncmes.gui.panel.StatePanel;
import com.cncmes.utils.DataUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


/**
 * *Zhong
 * *
 */
public class StatePanelListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        StatePanel statePanel = StatePanel.getInstance();
        ArrayList<FixtureMaterial> fixtureMaterials = null;
        String command = e.getActionCommand();
        int is_released = 0;
        int is_deleted = 0;

        if(command.equals(statePanel.getBtnSearch().getText()))
        {
            String fixtureNo = statePanel.getTextField().getText();
            if(fixtureNo!=null || !fixtureNo.equals(""))
            {
                if(statePanel.getRdbtnNewRadioButton().isSelected())       //all button    0 0 + 1 1
                {
//                    is_released = 1;
                    ArrayList<FixtureMaterial> standBy = searchFixtureMaterial(fixtureNo,0,0,statePanel.getCncLine().getId(),statePanel.getRack().getId());
                    ArrayList<FixtureMaterial> delete = searchFixtureMaterial(fixtureNo,1,1,statePanel.getCncLine().getId(),statePanel.getRack().getId());
                    if(standBy == null && delete != null) fixtureMaterials = delete;
                    if(delete == null && standBy != null) fixtureMaterials = standBy;
                    if(delete != null && standBy != null) {
                        fixtureMaterials = standBy;
                        fixtureMaterials.addAll(delete);
                    }
                }
                if(statePanel.getRdbtnEmpty().isSelected())         //Delete button        1 1
                {
//                    is_released = 1;
                    fixtureMaterials = searchFixtureMaterial(fixtureNo,1,1,statePanel.getCncLine().getId(),statePanel.getRack().getId());
                }
                if(statePanel.getRdbtnPlaced().isSelected())    //standBy
                {
                    fixtureMaterials = searchFixtureMaterial(fixtureNo,is_released,is_deleted,statePanel.getCncLine().getId(),statePanel.getRack().getId());
                }
                if(fixtureMaterials == null)
                {
                    JOptionPane.showMessageDialog(null, "This fixture unbind material in this line and rack.Please check search info.", "tips",JOptionPane.ERROR_MESSAGE);
                }

                showTable(fixtureMaterials);
            }

        }
    }
    private ArrayList<FixtureMaterial> searchFixtureMaterial(String fixtureNo, int is_released, int is_deleted,int line_id,int rack_id)
    {
        ArrayList<FixtureMaterial> fixtureMaterials = null;
        Fixture fixture = DataUtils.getFixtureInfo(fixtureNo);
        try {
            if (null == fixture) {
                JOptionPane.showMessageDialog(null, "This fixture no load into system.", "tips",JOptionPane.ERROR_MESSAGE);
                return null;
            }
            fixtureMaterials = DataUtils.getFixtureMaterialOfRack(fixture.getId(), is_released, is_deleted,line_id,rack_id);
            if(fixtureMaterials.size() == 0)
            {
//                JOptionPane.showMessageDialog(null, "This fixture unbind material in this line and rack.Please check search info.", "tips",JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return fixtureMaterials;
    }
    private void showTable(ArrayList<FixtureMaterial> fixtureMaterials){
        StatePanel statePanel = StatePanel.getInstance();
        String[] title = statePanel.getTableTitle();
        JTable table = statePanel.getTable();
        if(fixtureMaterials == null)
        {
            table.setModel(new DefaultTableModel(
                    new Object[][] {
                            {null, null, null, null, null},

                    },
                    new String[] {
                            "Sequence", "Fixture No", "Materials", "State", "Input Time"
                    }
            ));
        }
        Object[][] date = new Object[fixtureMaterials.size()][title.length];
        for(int i = 0;i < fixtureMaterials.size();i++)
        {
            FixtureMaterial fixtureMaterial = fixtureMaterials.get(i);
            String status = "UnKnow";
            if(fixtureMaterial.getIs_released()==0 && fixtureMaterial.getIs_deleted() == 0 )
            {
                status = "StandBy";
            }
            else if(fixtureMaterial.getIs_released() ==1 && fixtureMaterial.getIs_deleted() == 0)
            {
                status = "Wait for manual operation";
            }
            else status = "Having deleted";
            date[i][0] = i+1;
            date[i][1] = DataUtils.getFixtureById(fixtureMaterial.getFixture_id()).getFixture_no();
            date[i][2] = DataUtils.getMaterialById(fixtureMaterial.getMaterial_id()).getMaterial_no();
            date[i][3] = status;
            date[i][4] = fixtureMaterial.getScan_time();
        }
        table.setModel(new StateTableModel(date,title));

    }
}
