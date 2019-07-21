/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50622
 Source Host           : localhost:3306
 Source Schema         : wywhdgg-conf

 Target Server Type    : MySQL
 Target Server Version : 50622
 File Encoding         : 65001

 Date: 22/07/2019 00:00:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for conf_env
-- ----------------------------
DROP TABLE IF EXISTS `conf_env`;
CREATE TABLE `conf_env`  (
  `env` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'Env',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '环境名称',
  `order` tinyint(4) NOT NULL DEFAULT 0 COMMENT '显示排序',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`env`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置环境' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of conf_env
-- ----------------------------
INSERT INTO `conf_env` VALUES ('ppe', '预发布环境', 2, '2019-07-21 15:56:22', NULL);
INSERT INTO `conf_env` VALUES ('product', '生产环境', 3, '2019-07-21 15:56:22', NULL);
INSERT INTO `conf_env` VALUES ('test', '测试环境', 1, '2019-07-21 15:56:22', NULL);

-- ----------------------------
-- Table structure for conf_node
-- ----------------------------
DROP TABLE IF EXISTS `conf_node`;
CREATE TABLE `conf_node`  (
  `env` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'Env',
  `key` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置Key',
  `appname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属项目AppName',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置描述',
  `value` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置Value',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`env`, `key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置信息' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of conf_node
-- ----------------------------
INSERT INTO `conf_node` VALUES ('test', 'default.key01', 'default', '测试配置01', '1', NULL, '2019-07-21 15:56:45', NULL);
INSERT INTO `conf_node` VALUES ('test', 'default.key02', 'default', '测试配置02', '2', NULL, '2019-07-21 15:56:45', NULL);
INSERT INTO `conf_node` VALUES ('test', 'default.key03', 'default', '测试配置03', '3', NULL, '2019-07-21 15:56:45', NULL);

-- ----------------------------
-- Table structure for conf_node_log
-- ----------------------------
DROP TABLE IF EXISTS `conf_node_log`;
CREATE TABLE `conf_node_log`  (
  `env` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'Env',
  `key` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置Key',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置描述',
  `value` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置Value',
  `addtime` datetime(0) NOT NULL COMMENT '操作时间',
  `optuser` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作人',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '修改时间'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置节点日志' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for conf_node_msg
-- ----------------------------
DROP TABLE IF EXISTS `conf_node_msg`;
CREATE TABLE `conf_node_msg`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `addtime` datetime(0) NOT NULL,
  `env` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'Env',
  `key` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置Key',
  `value` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置Value',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置节点信息' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for conf_project
-- ----------------------------
DROP TABLE IF EXISTS `conf_project`;
CREATE TABLE `conf_project`  (
  `appname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'AppName',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目名称',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`appname`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置项目' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of conf_project
-- ----------------------------
INSERT INTO `conf_project` VALUES ('default', '示例项目', '0000-00-00 00:00:00', NULL);

-- ----------------------------
-- Table structure for conf_user
-- ----------------------------
DROP TABLE IF EXISTS `conf_user`;
CREATE TABLE `conf_user`  (
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '账号',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `permission` tinyint(4) NOT NULL DEFAULT 0 COMMENT '权限：0-普通用户、1-管理员',
  `permission_data` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限配置数据',
  `sort` tinyint(2) NULL DEFAULT NULL COMMENT '排序',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置账户' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of conf_user
-- ----------------------------
INSERT INTO `conf_user` VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 1, NULL, NULL, NULL, '2019-07-21 15:58:02', NULL);
INSERT INTO `conf_user` VALUES ('user', 'e10adc3949ba59abbe56e057f20f883e', 0, 'default#test,default#ppe', NULL, NULL, '2019-07-21 15:58:02', NULL);

SET FOREIGN_KEY_CHECKS = 1;
