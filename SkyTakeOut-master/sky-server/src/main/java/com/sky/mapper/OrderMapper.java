package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insertOrders(Orders orders);
    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> queryHistoryOrdersList(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{id}")
    Orders queryById(Long id);

    Page<Orders> queryConditionSearchOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    Integer countStatus(Integer toBeConfirmed);

    Integer checkValidOrderCount();

    Double queryTurnover();

    Integer queryAllOrderCount();

    Integer queryWaitingOrders();

    Integer queryDeliveredOrders();

    Integer queryCompletedOrders();

    Integer queryCancelledOrders();

    Integer queryAllOrders();

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrdertimeLT(Integer status, LocalDateTime orderTime);

    Double sumByMap(Map map);

    Integer todayValidOrderCount(Map map);

    Integer todayOrderCount(LocalDateTime begin, LocalDateTime end);

    Integer queryTotalOrderCount();

    Integer queryTotalValidOrderCount();

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);

    Integer countByMap(Map map);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    Orders getById(Long id);

    Page<Orders> conditionSearchOrder(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer  status);

    /**
     * 根据状态查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getOrdersByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);



    Double getOrdersByBeginAndEnd(Integer status, LocalDateTime begin, LocalDateTime end);

    Integer todayOrderByCount(Integer status,LocalDateTime begin, LocalDateTime end);


}

