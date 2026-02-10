//package com.sky.controller.user;
//
//import com.sky.dto.OrdersPageQueryDTO;
//import com.sky.dto.OrdersPaymentDTO;
//import com.sky.dto.OrdersSubmitDTO;
//import com.sky.entity.OrderDetail;
//import com.sky.result.PageResult;
//import com.sky.result.Result;
//import com.sky.service.OrderService;
//import com.sky.vo.OrderPaymentVO;
//import com.sky.vo.OrderSubmitVO;
//import com.sky.vo.OrderVO;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
//@RestController
//@Slf4j
//@Api(tags = "C端-订单接口")
//@RequestMapping("/user/order")
//public class OderController {
//
//    @Autowired
//    OrderService orderService;
//
//    @PostMapping("/submit")
//    @ApiOperation("用户下单")
//    public Result<OrderSubmitVO> oderSubmit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
//        log.info("用户下单{}",ordersSubmitDTO);
//        OrderSubmitVO orderSubmitVO = orderService.oderSubmit(ordersSubmitDTO);
//        return Result.success(orderSubmitVO);
//    }
//    /**
//     * 订单支付
//     *
//     * @param ordersPaymentDTO
//     * @return
//     */
//    @PutMapping("/payment")
//    @ApiOperation("订单支付")
//    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        log.info("订单支付：{}", ordersPaymentDTO);
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
//        log.info("生成预支付交易单：{}", orderPaymentVO);
//        return Result.success(orderPaymentVO);
//    }
//
//    @GetMapping("/historyOrders")
//    @ApiOperation("历史订单查询")
//    public Result<PageResult> checkHistoryOrders( OrdersPageQueryDTO ordersPageQueryDTO){
//        log.info("查询历史订单");
//        PageResult pageResult=orderService.checkHistoryOrders(ordersPageQueryDTO);
//        return Result.success(pageResult);
//    }
//
//
//    @GetMapping("orderDetail/{id}")
//    @ApiOperation("查询订单详情")
//    public Result<OrderVO> checkOrderDetail(@PathVariable Long id){
//        log.info("查询订单详情");
//        OrderVO orderDetailList=orderService.checkOrderDetail(id);
//        return Result.success(orderDetailList);
//    }
//
//    @PutMapping("/cancel/{id}")
//    @ApiOperation("取消订单")
//    public Result cancelOrder(@PathVariable Long id) throws Exception {
//        log.info("取消订单");
//        orderService.cancelOrder(id);
//        return Result.success();
//    }
//
//
//
//
//    @PutMapping("/repetition/{id}")
//    @ApiOperation("再来一单")
//    public Result orderAgain(@PathVariable Long id){
//        log.info("再来一单");
//        orderService.orderAgain(id);
//        return Result.success();
//    }
//    @GetMapping("/reminder/{id}")
//    @ApiOperation("用户催单")
//    public Result reminder(@PathVariable("id") Long id) {
//        orderService.reminder(id);
//        return Result.success();
//    }
//
//}

package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderReturnVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("user")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
public class OderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){

        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO) ;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);

        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());

        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> checkHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult pageResult=orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderReturnVO> checkOrderDetail(@PathVariable Long id){
        OrderReturnVO orderDetailList=orderService.getOrderDetail(id);
        return Result.success(orderDetailList);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable Long id) throws Exception {
        orderService.cancelOrderById(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result orderAgain(@PathVariable Long id){
        orderService.orderAgainById(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable("id") Long id) {
        orderService.reminderById(id);
        return Result.success();
    }

}
