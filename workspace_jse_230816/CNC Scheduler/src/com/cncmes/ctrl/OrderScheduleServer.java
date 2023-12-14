package com.cncmes.ctrl;

import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.CNCProcessCard;
import com.cncmes.dto.OrderScheduler;
import com.cncmes.utils.FileUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * *Zhong
 * *
 */
public class OrderScheduleServer {
    private String processCardDto = "com.cncmes.dto.CNCProcessCard";
    private String orderSchedulerDto = "com.cncmes.dto.OrderScheduler";
    //return all not finish cncProcessCard
    private List<OrderScheduler> arrange()
    {
        Double time = 0.0;
        List<OrderScheduler> list = getAllOrderSchedulers();
        List<Integer> record = new ArrayList<>();
        if(list.size() == 0) return null;
        int index = 0;
        int cncCapacity = 14;
        while (time<90.0)
        {
            if(index<=list.size())
            {
                time += list.get(index).getProcesstime();
                record.add(index);

            }
            index++;
        }
        return null;
    }
    private List<CNCProcessCard> getAllProcessCards()
    {
        ArrayList<Object> vos = null;
        ArrayList<CNCProcessCard> cncProcessCards = new ArrayList<>();

        DAO dao = new DAOImpl(processCardDto);
        try {
            vos = dao.findByCnd(new String[]{"is_delete"},new String[]{"0"});
            for(Object cncProcessCard:vos){
                cncProcessCards.add((CNCProcessCard)cncProcessCard);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return cncProcessCards;
    }

    private List<OrderScheduler> getAllOrderSchedulers()
    {
        List<CNCProcessCard> cncProcessCards = getAllProcessCards();
        if(cncProcessCards==null||cncProcessCards.size()<=0)
        {
            return null;
        }
        ArrayList<OrderScheduler> orderSchedulers = new ArrayList<>();
        for (CNCProcessCard cncProcessCard : cncProcessCards)
        {
            OrderScheduler orderInfo = getOrderInfoByMoCode(cncProcessCard.getOrder_no());
            //get process time from  process file

            orderInfo.setProcesstime(FileUtils.getNCProgramTime(orderInfo.getMoCode()));
            orderSchedulers.add(orderInfo);
        }
        Collections.sort(orderSchedulers);          //sort the orderSchedulers base on the requestTime and process time
        return orderSchedulers;
    }

    private OrderScheduler getOrderInfoByMoCode(String order_no)
    {
        DAOImpl dao = new DAOImpl(orderSchedulerDto,true);
        try {
            List<Object> os = dao.findOrderSchedulerByMoCode(order_no);
            for (Object o :os){
                return (OrderScheduler) o;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
