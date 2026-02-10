-- 创建操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '记录的url',
  `port_check` int(0) NOT NULL COMMENT '1是后端，2是前端',
  `ip` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '客户端IP地址',
  `method` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调用的方法名',
  `exception_detected` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '异常信息',
  `args` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '方法参数',
  `user_id` int(0) NOT NULL COMMENT '操作用户ID',
  `update_time` datetime(0) NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;