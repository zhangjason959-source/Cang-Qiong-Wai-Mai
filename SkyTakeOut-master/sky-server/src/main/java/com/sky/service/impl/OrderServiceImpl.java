package com.sky.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.build.OrderItem;
import com.sky.build.OrderItemBuilder;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    // 目前该功能的传回来eliveryStatus只能是0，目前先把他默认修改为1
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //异常情况的处理（收货地址为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            // 抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 获取用户的购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);

        if (cartList == null || cartList.size() == 0) {
            // 抛出业务异常
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        BeanUtils.copyProperties(ordersSubmitDTO,orders); //BeanUtils.copyProperties(source, target) 是将 source 对象的属性值，复制到 target 对象的同名属性上。
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        ordersSubmitDTO.setDeliveryStatus(1);// 前端传回来的deliveryStatus只能是0，目前先把他修改为1
        orders.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus());


        // 向订单表插入一条数据
        orderMapper.insertOrders(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        // 向订单明细表插入多条数据
        orderDetailMapper.insertBatch(orderDetailList);
        // 删除购物车数据
        shoppingCartMapper.deleteByUserId(shoppingCart);

        OrderSubmitVO orderVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderVO;
    }

    @Transactional
    @Override
    public OrderSubmitVO oderSubmit(OrdersSubmitDTO ordersSubmitDTO) {
         /*
        OrderItemBuilder orderBuilder = new OrderItemBuilder();
        OrderItem  orderItem =    orderBuilder.buildOrderDetails()
                                                .buildAddress();
        List<OrderDetail> orderDetails = orderItem.getOrderDetails();
        Orders order = orderItem.getOrder();
        User user = orderItem.getUser();*/

        // 1、面试官问：哥们有没有使用过设计模式  0、他的场景 1、提高代码阅读性  2、方便后期维护
        // 2、在使用构造器设计模式：就是如何在类与类之间传参数
        //构造器传参   //@Component  @Autowire(就不好传递非spring管理的类型)  // threadLocal
        // 3、有关分库分表的  高表以及宽表对应的知识

        // 只有主干，没有分支
        OrderItemBuilder orderItemBuilder =
                new OrderItemBuilder(addressBookMapper,shoppingCartMapper,userMapper,ordersSubmitDTO);
        OrderItem orderItem = orderItemBuilder.builderOrders()
                .builderOrderDetails()
                .build();

        Orders orders = orderItem.getOrders();
        List<OrderDetail> orderDetailList = orderItem.getOrderDetailList();
        orderMapper.insertOrders(orders);
        orderDetailMapper.insertDetailList(orderDetailList);
        //5、删除购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCartMapper.deleteByUserId(shoppingCart);

        OrderSubmitVO orderVO = buildeOrderVo(orders);//封装前端VO对象提交返回信息
        return orderVO;

//        //异常情况的处理（收货地址为空、超出配送范围、购物车为空）
//        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
//        if (addressBook == null) {
//            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
//        }
//        Long userId = BaseContext.getCurrentId();
//        ShoppingCart shoppingCart = new ShoppingCart();
//        shoppingCart.setUserId(userId);
//        //查询当前用户的购物车数据
//        List<ShoppingCart> shoppingCartList = shoppingCartMapper.checkShoppingCartList(shoppingCart);
//        if (shoppingCartList == null || shoppingCartList.size() == 0) {
//            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
//        }
//        //插入必要字段（未支付）
//        Orders orders = new Orders();
//        BeanUtils.copyProperties(ordersSubmitDTO,orders);
//        orders.setId(Long.parseLong(String.valueOf(System.currentTimeMillis())));
//        orders.setNumber(String.valueOf(System.currentTimeMillis()));
//        orders.setStatus(Orders.PENDING_PAYMENT);
//        orders.setUserId(userId);
//        orders.setOrderTime(LocalDateTime.now());
//        orders.setPayStatus(Orders.UN_PAID);
//        orders.setPhone(addressBook.getPhone());
//        orders.setAddress(addressBook.getDetail());
//        orders.setConsignee(addressBook.getConsignee());
//
//        //执行插入
//        orderMapper.insertOrders(orders);
////插入订单明细
//        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
//        for (ShoppingCart cart : shoppingCartList) {
//            OrderDetail orderDetail = new OrderDetail();
//            BeanUtils.copyProperties(cart, orderDetail);
//            orderDetail.setOrderId(orders.getId());
//            orderDetailList.add(orderDetail);//存入list集合
////            OrderDetail.builder().name(cart.getName())
////                    .image(cart.getImage())
////                    .orderId(orders.getId())
////                    .dishId(cart.getDishId())
////                    .setmealId(cart.getSetmealId())
////                    .dishFlavor(cart.getDishFlavor())
////                    .number(cart.getNumber())
////                    .amount(cart.getAmount()).build();
//        }
//        //批量插入
//        orderDetailMapper.insertDetailList(orderDetailList);
//        //结账后删除该用户的购物车
//        shoppingCartMapper.deleteByUserId(shoppingCart);
//        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().orderAmount(orders.getAmount())
//                .orderTime(orders.getOrderTime())
//                .id(orders.getId())
//                .orderNumber(orders.getNumber()).build();
//        return orderSubmitVO;
    }
    private OrderSubmitVO buildeOrderVo(Orders orders) {
        OrderSubmitVO orderVO = new OrderSubmitVO();
        orderVO.setId(orders.getId());
        //正常操作是需要验价的
        orderVO.setOrderAmount(orders.getAmount());
        orderVO.setOrderTime(orders.getOrderTime());
        orderVO.setId(orders.getId());
        return orderVO;
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//

        // 生成空JSON,跳过微信支付流程
        JSONObject jsonObject =new JSONObject();


        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        // 通过WebSocket实现来单提醒 type orderId content
        Map map = new HashMap();
        map.put("type", 1); // 1表示来单提醒，2表示客户催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);



        //////////////////////////////////////////////
//        Map map = new HashMap();
//        map.put("type", 1);//消息类型，1表示来单提醒
//        map.put("orderId", orders.getId());
//        map.put("content", "订单号：" + outTradeNo);

        //通过WebSocket实现来单提醒，向客户端浏览器推送消息
//        webSocketServer.sendToAllClient(JSON.toJSONString(map));
        ///////////////////////////////////////////////////
    }

    @Override
    public PageResult checkHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());//1.获得分页数量和每页记录条数
        Page<Orders> page=orderMapper.queryHistoryOrdersList(ordersPageQueryDTO);//去数据库查，查出来的封装到page对象里面
        List<OrderVO>orderVOS=new ArrayList<>();
        if (page!=null &&page.getTotal()>0){
            for (Orders orders : page) {
                Long id = orders.getId();
                //查询订单明细
                List<OrderDetail> detailList=orderDetailMapper.queryByOrderId(id);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(detailList);
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),orderVOS);
    }

    @Override
    public OrderVO checkOrderDetail(Long id) {
        List<OrderDetail> detailList=orderDetailMapper.queryByDishId(id);
        Orders orders=orderMapper.queryById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(detailList);
        return orderVO;
    }

    /**
     * 前台用户取消订单
     *
     * @param id
     */
    @Override
    public void cancelOrder(Long id) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.queryById(id);
        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额
            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 后台取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrderBackend(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.queryById(ordersCancelDTO.getId());
        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @throws Exception
     */
    @Override
    public void rejectionOrders(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders ordersDB = orderMapper.queryById(ordersRejectionDTO.getId());
        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
            Orders orders = Orders.builder()
                    .id(ordersConfirmDTO.getId())
                    .status(Orders.CONFIRMED)
                    .build();

            orderMapper.update(orders);
    }

    /**
     * 派单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.queryById(id);
        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
            // 根据id查询订单
            Orders ordersDB = orderMapper.queryById(id);
            // 校验订单是否存在，并且状态为4
            if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
                throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
            }
            Orders orders = new Orders();
            orders.setId(ordersDB.getId());
            // 更新订单状态,状态转为完成
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(orders);
    }

    @Override
    public OrderStatisticsVO queryOrderStatistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        return orderStatisticsVO;
    }

    @Override
    public void reminder(Long id) {
        // 查询订单是否存在
        Orders orders = orderMapper.queryById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //基于WebSocket实现催单
        Map map = new HashMap();
        map.put("type", 2);//2代表用户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    @Override
    @Transactional
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Long userId=BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        //下一条sql进行分页自动加入limit关键字分页
        Page<Orders> orders=orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> ordersList = orders.getResult();

        List<OrderReturnVO> orderReturnVOList = new ArrayList<>();

        for(Orders order : ordersList){
            List<OrderDetail> detailList = orderDetailMapper.queryByOrderId(order.getId());
            orderReturnVOList.add(OrderReturnVO.builder()
                    .id(order.getId())
                    .orderTime(order.getOrderTime())
                    .payStatus(order.getPayStatus())
                    .status(order.getStatus())
                    .amount(order.getAmount())
                    .phone(order.getPhone())
                    .address(order.getAddress())
                    .consignee(order.getConsignee())
                    .orderDetailList(detailList)
                    .build());
        }


        return new PageResult(orders.size(), orderReturnVOList);
    }

    /**
     * 订单详情
     * @param id
     * @return
     */
    @Override
    public OrderReturnVO getOrderDetail(Long id) {
        Orders order = orderMapper.getById(id);
        OrderReturnVO orderReturnVO = OrderReturnVO.builder()
                .id(order.getId())
                .number(order.getNumber())
                .orderTime(order.getOrderTime())
                .payStatus(order.getPayStatus())
                .payMethod(order.getPayMethod())
                .deliveryStatus(order.getDeliveryStatus())
                .status(order.getStatus())
                .amount(order.getAmount())
                .phone(order.getPhone())
                .address(order.getAddress())
                .consignee(order.getConsignee())
                .orderDetailList(orderDetailMapper.queryByOrderId(id))
                .build();
        return orderReturnVO;
    }

    /**
     * 订单取消
     * @param id
     */
    @Override
    public void cancelOrderById(Long id) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(id);

        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orderMapper.update(orders);

    }

    /**
     * by myself
     * 再来一单
     * @param id
     */
    @Override
    public void orderAgainById(Long id) {
        Long userId=BaseContext.getCurrentId();
        List<OrderDetail> detailList = orderDetailMapper.queryByOrderId(id);
        for (OrderDetail orderDetail : detailList) {
            //将订单明细表遍历出来放到购物车，忽略id，再填充到数据库
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart,id.toString());
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearchOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page=orderMapper.conditionSearchOrder(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);

    }

    @Override
    public OrderStatisticsVO queryOrdersStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        orderStatisticsVO.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 订单确认
     * @param ordersConfirmDTO
     */
    @Override
    public void confirmByOrdersConfirmDTO(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(3)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 订单拒绝
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectionOrderByOrdersRejectionDTO(OrdersRejectionDTO ordersRejectionDTO) {
        // 查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || ordersDB.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(ordersDB.getStatus()==Orders.PAID){
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
            log.info("申请退款");
        }


       Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
       orderMapper.update(orders);
    }

    @Override
    public void deliveryById(Long id) {
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || ordersDB.getStatus() != Orders.CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void completeById(Long id) {

        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || ordersDB.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminderById(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map map = new HashMap();
        map.put("type",2); //1表示来单提醒，2表示来单催单
        map.put("orderId",id);
        map.put("content","您有一个待处理订单，订单号：" + ordersDB.getNumber());

        // 通过websocket向客户端推送消息
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }


    /**
     * 获取订单菜品信息
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDishes(getOrderDishesStr(orders));//获取订单菜品信息
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void orderAgain(Long id) {
        Long userId=BaseContext.getCurrentId();
        List<OrderDetail> detailList = orderDetailMapper.queryByOrderId(id);
        for (OrderDetail orderDetail : detailList) {
            //将订单明细表遍历出来放到购物车，忽略id，再填充到数据库
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart,id.toString());
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public PageResult conditionSearchOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders>page=orderMapper.queryConditionSearchOrders(ordersPageQueryDTO);
        List<OrderVO> orderDetails= GetOrderDetails(page);
        return new PageResult(page.getTotal(),orderDetails);
        //return new PageResult(page.getTotal(),page.getResult());
    }



    private List<OrderVO> GetOrderDetails(Page<Orders> page){
        List<OrderVO> orderVOList=new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO = new OrderVO();
            //List<OrderDetail> detailList = orderDetailMapper.queryByOrderId(orders.getId());
            BeanUtils.copyProperties(orders,orderVO);
            //orderVO.setOrderDetailList(detailList);
            String orderDishes = getOrderDishesStr(orders);
            orderVO.setOrderDishes(orderDishes);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.queryByOrderId(orders.getId());
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }


}
