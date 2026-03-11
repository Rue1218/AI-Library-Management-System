-- ============================================================
-- 图书馆管理系统 · 完整数据库脚本
-- 适用后端：library-management-backend（MyBatis-Plus + MD5）
-- 使用方法：
--   CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   USE library_db;
--   SOURCE library_db.sql;
-- 测试账号：
--   管理员   admin   / admin123
--   普通用户  user001 / 123456
-- ============================================================

CREATE DATABASE IF NOT EXISTS `library_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `library_db`;

-- ============================================================
-- 1. 用户表
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`        VARCHAR(64)  NOT NULL                COMMENT '用户名',
  `password`        VARCHAR(255) NOT NULL                COMMENT '密码（MD5）',
  `real_name`       VARCHAR(64)  DEFAULT NULL            COMMENT '真实姓名',
  `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `email`           VARCHAR(128) DEFAULT NULL            COMMENT '邮箱',
  `role`            TINYINT      NOT NULL DEFAULT 1      COMMENT '角色：0=管理员，1=普通用户',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0=禁用，1=正常',
  `avatar`          VARCHAR(500) DEFAULT NULL            COMMENT '头像 URL',
  `last_login_time` DATE         DEFAULT NULL            COMMENT '最后登录时间',
  `deleted`         INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除，1=已删除',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone`  (`phone`),
  KEY `idx_email`  (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 图书分类表（在 book 之前建，供外键引用）
-- ============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(64) NOT NULL                COMMENT '分类名称',
  `parent_id`   BIGINT      NOT NULL DEFAULT 0     COMMENT '父分类 ID（0=顶级）',
  `sort`        INT         NOT NULL DEFAULT 0     COMMENT '排序权重',
  `deleted`     INT         NOT NULL DEFAULT 0,
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- ============================================================
-- 3. 图书表
-- ============================================================
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `isbn`            VARCHAR(50)  DEFAULT NULL            COMMENT 'ISBN',
  `title`           VARCHAR(255) NOT NULL                COMMENT '书名',
  `author`          VARCHAR(100) DEFAULT NULL            COMMENT '作者',
  `publisher`       VARCHAR(128) DEFAULT NULL            COMMENT '出版社',
  `publish_date`    DATE         DEFAULT NULL            COMMENT '出版日期',
  `category_id`     BIGINT       DEFAULT NULL            COMMENT '分类 ID',
  `cover_url`       VARCHAR(500) DEFAULT NULL            COMMENT '封面图片 URL',
  `description`     TEXT                                 COMMENT '图书简介',
  `total_stock`     INT          NOT NULL DEFAULT 1      COMMENT '总库存',
  `available_stock` INT          NOT NULL DEFAULT 1      COMMENT '可借数量',
  `location`        VARCHAR(128) DEFAULT NULL            COMMENT '存放位置',
  `deleted`         INT          NOT NULL DEFAULT 0,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isbn`          (`isbn`),
  KEY `idx_title`               (`title`),
  KEY `idx_author`              (`author`),
  KEY `idx_category_id`         (`category_id`),
  KEY `idx_available_stock`     (`available_stock`),
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- ============================================================
-- 4. 借阅记录表
-- ============================================================
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT        NOT NULL                COMMENT '用户 ID',
  `book_id`      BIGINT        NOT NULL                COMMENT '图书 ID',
  `borrow_code`  VARCHAR(50)   DEFAULT NULL            COMMENT '借阅编号',
  `borrow_date`  DATE          NOT NULL                COMMENT '借阅日期',
  `due_date`     DATE          NOT NULL                COMMENT '应还日期',
  `return_date`  DATE          DEFAULT NULL            COMMENT '实际归还日期',
  `status`       TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0=借阅中，1=已归还，2=逾期，3=续借待审核',
  `renew_count`  INT           NOT NULL DEFAULT 0      COMMENT '续借次数',
  `overdue_fine` DECIMAL(10,2) NOT NULL DEFAULT 0.00  COMMENT '逾期费用',
  `operator_id`  BIGINT        DEFAULT NULL            COMMENT '操作员 ID（管理员）',
  `deleted`      INT           NOT NULL DEFAULT 0,
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_borrow_code` (`borrow_code`),
  KEY `idx_user_id`   (`user_id`),
  KEY `idx_book_id`   (`book_id`),
  KEY `idx_status`    (`status`),
  KEY `idx_borrow_date` (`borrow_date`),
  KEY `idx_due_date`  (`due_date`),
  CONSTRAINT `fk_borrow_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- ============================================================
-- 5. 预约表
-- ============================================================
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id`           BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT     NOT NULL                COMMENT '用户 ID',
  `book_id`      BIGINT     NOT NULL                COMMENT '图书 ID',
  `reserve_date` DATE       NOT NULL                COMMENT '预约日期',
  `expire_date`  DATE       NOT NULL                COMMENT '失效日期',
  `status`       TINYINT    NOT NULL DEFAULT 0      COMMENT '状态：0=等待中，1=可取书，2=已取消，3=已完成',
  `notified`     TINYINT(1) NOT NULL DEFAULT 0     COMMENT '是否已通知',
  `deleted`      INT        NOT NULL DEFAULT 0,
  `create_time`  DATETIME   DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_status`  (`status`),
  CONSTRAINT `fk_reserve_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_reserve_book` FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ============================================================
-- 6. 操作日志表
-- ============================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator_id`    BIGINT       NOT NULL                COMMENT '操作员 ID',
  `operation_type` VARCHAR(64)  NOT NULL                COMMENT '操作类型',
  `target_id`      BIGINT       DEFAULT NULL            COMMENT '目标 ID',
  `content`        VARCHAR(500) DEFAULT NULL            COMMENT '操作内容',
  `ip_address`     VARCHAR(64)  DEFAULT NULL            COMMENT 'IP 地址',
  `deleted`        INT          NOT NULL DEFAULT 0,
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator_id`    (`operator_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time`    (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 7. 图书评论表
-- ============================================================
DROP TABLE IF EXISTS `book_comment`;
CREATE TABLE `book_comment` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `book_id`     BIGINT   NOT NULL                COMMENT '图书 ID',
  `user_id`     BIGINT   NOT NULL                COMMENT '用户 ID',
  `rating`      TINYINT  NOT NULL DEFAULT 5      COMMENT '评分 1-5',
  `content`     TEXT     NOT NULL                COMMENT '评论内容',
  `like_count`  INT      NOT NULL DEFAULT 0      COMMENT '点赞数',
  `reply_count` INT      NOT NULL DEFAULT 0      COMMENT '回复数',
  `parent_id`   BIGINT   DEFAULT NULL            COMMENT '父评论 ID',
  `deleted`     INT      NOT NULL DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_book_id`   (`book_id`),
  KEY `idx_user_id`   (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书评论表';

-- ============================================================
-- 8. 评论点赞表
-- ============================================================
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `comment_id`  BIGINT   NOT NULL                COMMENT '评论 ID',
  `user_id`     BIGINT   NOT NULL                COMMENT '用户 ID',
  `deleted`     INT      NOT NULL DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- ============================================================
-- ==================== 测试数据 ====================
-- ============================================================

-- ============================================================
-- 用户数据
-- 密码说明（纯 MD5，无盐）：
--   admin123 → 0192023a7bbd73250516f069df18b500
--   123456   → e10adc3949ba59abbe56e057f20f883e
-- ============================================================
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin',   '0192023a7bbd73250516f069df18b500', '系统管理员', '13800000000', 'admin@library.com',    0, 1),
('user001', 'e10adc3949ba59abbe56e057f20f883e', '张小明',   '13800138001', 'zhangxm@example.com',  1, 1),
('user002', 'e10adc3949ba59abbe56e057f20f883e', '李小红',   '13800138002', 'lihong@example.com',   1, 1),
('user003', 'e10adc3949ba59abbe56e057f20f883e', '王小刚',   '13800138003', 'wangxg@example.com',   1, 1),
('user004', 'e10adc3949ba59abbe56e057f20f883e', '赵丽',     '13800138004', 'zhaoli@example.com',   1, 1),
('user005', 'e10adc3949ba59abbe56e057f20f883e', '刘洋',     '13800138005', 'liuyang@example.com',  1, 1),
('user006', 'e10adc3949ba59abbe56e057f20f883e', '陈静',     '13800138006', 'chenjing@example.com', 1, 1),
('user007', 'e10adc3949ba59abbe56e057f20f883e', '杨涛',     '13800138007', 'yangtao@example.com',  1, 1),
('user008', 'e10adc3949ba59abbe56e057f20f883e', '周芳',     '13800138008', 'zhoufang@example.com', 1, 1),
('user009', 'e10adc3949ba59abbe56e057f20f883e', '吴强',     '13800138009', 'wuqiang@example.com',  1, 1);

-- ============================================================
-- 分类数据（必须在 book 之前插入，供外键引用）
-- id 按自增顺序：计算机=1 … 世界史=16
-- ============================================================
INSERT INTO `category` (`name`, `parent_id`, `sort`) VALUES
('计算机',      0, 1),  -- id=1
('文学',        0, 2),  -- id=2
('历史',        0, 3),  -- id=3
('哲学',        0, 4),  -- id=4
('经济管理',    0, 5),  -- id=5
('儿童文学',    0, 6),  -- id=6
('小说',        2, 1),  -- id=7  (parent=文学)
('编程语言',    1, 2),  -- id=8  (parent=计算机)
('数据库',      1, 3),  -- id=9  (parent=计算机)
('算法',        1, 4),  -- id=10 (parent=计算机)
('Web开发',     1, 5),  -- id=11 (parent=计算机)
('科幻',        2, 2),  -- id=12 (parent=文学)
('文学名著',    2, 3),  -- id=13 (parent=文学)
('中国古代史',  3, 1),  -- id=14 (parent=历史)
('中国近现代史',3, 2),  -- id=15 (parent=历史)
('世界史',      3, 3);  -- id=16 (parent=历史)

-- ============================================================
-- 图书数据（43 本）
-- ============================================================
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `publish_date`, `category_id`, `cover_url`, `description`, `total_stock`, `available_stock`, `location`) VALUES
('978-7-111-54789-0', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2023-06-01', 1, '', '系统级编程经典教材', 15, 10, 'A区-3楼-01'),
('978-7-121-12345-6', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', '2024-01-01', 1, '', 'Python入门经典', 20, 15, 'A区-3楼-02'),
('978-7-111-54321-7', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '2023-06-01', 1, '', '算法权威教材', 12, 8, 'A区-3楼-03'),
('978-7-121-67890-1', '三体', '刘慈欣', '重庆出版社', '2022-01-01', 7, '', '科幻经典之作', 18, 12, 'B区-1楼-01'),
('978-7-5321-23456-7', '活着', '余华', '作家出版社', '2021-05-01', 13, '', '感人至深的生命之书', 25, 20, 'B区-2楼-01'),
('978-7-108-08901-2', '人类简史', '尤瓦尔·赫拉利', '中信出版社', '2023-03-01', 14, '', '从智人到现代人的演化史', 15, 10, 'C区-1楼-01'),
('978-7-115-42891-5', 'Vue3实战', '梁峻', '人民邮电出版社', '2024-06-01', 1, '', 'Vue3从入门到实战', 18, 14, 'A区-3楼-04'),
('978-7-121-34567-8', '红楼梦', '曹雪芹', '人民文学出版社', '2020-05-01', 13, '', '中国古典四大名著之一', 30, 25, 'B区-2楼-02'),
('978-7-5399-88765-4', '明朝那些事儿', '当年明月', '北京联合出版公司', '2021-08-01', 14, '', '用幽默解读历史', 20, 16, 'C区-1楼-02'),
('978-7-111-65432-1', 'Java核心技术', 'Cay S. Horstmann', '机械工业出版社', '2023-11-01', 1, '', 'Java经典参考书', 15, 11, 'A区-3楼-05'),
('978-7-115-48765-9', 'Spring Boot实战', 'Craig Walls', '人民邮电出版社', '2024-03-01', 1, '', 'Spring Boot权威指南', 18, 13, 'A区-3楼-06'),
('978-7-121-23456-7', '金字塔原理', '芭芭拉·明托', '民主与建设出版社', '2022-07-01', 5, '', '思考和表达的逻辑', 20, 17, 'D区-1楼-01'),
('978-7-115-35678-6', '数据结构与算法之美', '刘华', '人民邮电出版社', '2024-02-01', 1, '', '面试必读算法书', 22, 18, 'A区-3楼-07'),
('978-7-121-56789-0', '浪潮之巅', '吴军', '人民邮电出版社', '2021-12-01', 5, '', 'IT巨头的兴衰史', 15, 11, 'D区-1楼-02'),
('978-7-111-78901-3', '代码整洁之道', 'Robert C. Martin', '人民邮电出版社', '2023-08-01', 1, '', '软件工程经典著作', 12, 9, 'A区-3楼-08'),
('978-7-115-45678-3', '数学之美', '吴军', '人民邮电出版社', '2022-05-01', 1, '', '用数学思维看世界', 16, 12, 'A区-3楼-09'),
('978-7-121-98765-4', '百年孤独', '加西亚·马尔克斯', '南海出版公司', '2021-09-01', 13, '', '魔幻现实主义文学经典', 20, 15, 'B区-2楼-03'),
('978-7-5399-76543-2', '追风筝的人', '卡勒德·胡赛尼', '上海人民出版社', '2021-03-01', 7, '', '关于友谊与救赎的故事', 18, 14, 'B区-1楼-02'),
('978-7-5322-65432-1', '解忧杂货店', '东野圭吾', '南海出版公司', '2022-08-01', 7, '', '温情悬疑小说', 16, 13, 'B区-1楼-03'),
('978-7-111-87654-3', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '2023-01-01', 1, '', 'MySQL入门经典', 14, 10, 'A区-4楼-01'),
('978-7-115-56789-1', 'Redis设计与实现', '黄健宏', '人民邮电出版社', '2024-04-01', 1, '', '深度讲解Redis内部设计', 10, 7, 'A区-4楼-02'),
('978-7-121-34567-9', '剑指Offer', '何海涛', '电子工业出版社', '2023-05-01', 1, '', '程序员面试宝典', 20, 16, 'A区-4楼-03'),
('978-7-111-65432-9', 'Effective Java', 'Joshua Bloch', '机械工业出版社', '2023-09-01', 1, '', 'Java最佳实践', 12, 8, 'A区-4楼-04'),
('978-7-115-78901-2', 'Go语言实战', 'William Kennedy', '人民邮电出版社', '2024-05-01', 1, '', 'Go语言进阶指南', 15, 11, 'A区-4楼-05'),
('978-7-121-45678-3', '黑客与画家', 'Paul Graham', '人民邮电出版社', '2021-11-01', 1, '', '硅谷创业之父文集', 10, 7, 'A区-4楼-06'),
('978-7-5399-12345-6', '挪威的森林', '村上春树', '上海译文出版社', '2021-06-01', 13, '', '青春成长小说', 18, 14, 'B区-2楼-04'),
('978-7-5321-34567-8', '平凡的世界', '路遥', '人民文学出版社', '2020-12-01', 13, '', '当代农村题材巨著', 25, 21, 'B区-2楼-05'),
('978-7-108-23456-7', '万历十五年', '黄仁宇', '中华书局', '2021-04-01', 14, '', '历史研究经典', 12, 9, 'C区-1楼-03'),
('978-7-5399-45678-9', '史记', '司马迁', '中华书局', '2020-01-01', 14, '', '中国第一部纪传体通史', 20, 18, 'C区-2楼-01'),
('978-7-111-34567-8', '刻意练习', 'Anders Ericsson', '机械工业出版社', '2022-03-01', 5, '', '如何从新手成为大师', 16, 12, 'D区-1楼-03'),
('978-7-115-23456-7', '影响力', 'Robert Cialdini', '人民邮电出版社', '2022-06-01', 5, '', '说服心理学经典', 14, 10, 'D区-1楼-04'),
('978-7-121-78901-3', '富爸爸穷爸爸', 'Robert Kiyosaki', '南海出版公司', '2021-07-01', 5, '', '财商教育启蒙书', 18, 15, 'D区-2楼-01'),
('978-7-5322-12345-6', '小王子', '圣埃克苏佩里', '人民文学出版社', '2021-02-01', 6, '', '经典法语童话', 30, 28, 'E区-1楼-01'),
('978-7-5399-23456-7', '夏洛的网', 'E.B. White', '上海译文出版社', '2021-05-01', 6, '', '关于友谊的童话', 25, 22, 'E区-1楼-02'),
('978-7-121-56789-1', '失控', 'Kevin Kelly', '新星出版社', '2021-08-01', 1, '', '科技哲学思考', 10, 6, 'A区-4楼-07'),
('978-7-111-56789-1', '黑客帝国', '莱昂纳多·迪卡普里奥', '北京联合出版公司', '2022-01-01', 7, '', '科幻电影同名小说', 15, 12, 'B区-1楼-04'),
('978-7-115-67890-1', 'Unity3D游戏开发', '宣雨松', '人民邮电出版社', '2024-01-01', 1, '', '游戏开发实战指南', 12, 9, 'A区-4楼-08'),
('978-7-121-89012-3', 'C++ Primer', 'Stanley B. Lippman', '电子工业出版社', '2023-07-01', 1, '', 'C++经典教程', 14, 10, 'A区-4楼-09'),
('978-7-111-89012-3', '论语', '孔子', '中华书局', '2020-05-01', 4, '', '儒家经典著作', 20, 17, 'F区-1楼-01'),
('978-7-5399-56789-1', '道德经', '老子', '中华书局', '2020-06-01', 4, '', '道家经典著作', 18, 15, 'F区-1楼-02'),
('978-7-108-34567-8', '孙子兵法', '孙武', '中华书局', '2020-07-01', 4, '', '古代军事经典', 16, 13, 'F区-1楼-03'),
('978-7-115-89012-3', '沉思录', '马可·奥勒留', '中央编译出版社', '2021-09-01', 4, '', '西方哲学经典', 12, 9, 'F区-1楼-04'),
('978-7-121-90123-4', '设计的要素', 'Craig M. Berry', '人民邮电出版社', '2024-02-01', 1, '', 'UI设计实战指南', 15, 12, 'A区-4楼-10');

-- ============================================================
-- 借阅记录（20 条）
-- ============================================================
INSERT INTO `borrow_record` (`user_id`, `book_id`, `borrow_code`, `borrow_date`, `due_date`, `return_date`, `status`, `renew_count`, `overdue_fine`, `operator_id`) VALUES
(2,  1,  'BR20260101001', '2026-01-01', '2026-01-31', NULL,         0, 0, 0.00, 1),
(2,  5,  'BR20260102002', '2026-01-02', '2026-02-01', '2026-01-28', 1, 0, 0.00, 1),
(3,  3,  'BR20260103003', '2026-01-03', '2026-02-03', NULL,         0, 1, 0.00, 1),
(3,  7,  'BR20260104004', '2026-01-04', '2026-02-04', NULL,         0, 0, 0.00, 1),
(4,  9,  'BR20260105005', '2026-01-05', '2026-02-05', NULL,         0, 0, 0.00, 1),
(4,  12, 'BR20260106006', '2026-01-06', '2026-02-06', '2026-02-04', 1, 0, 0.00, 1),
(5,  15, 'BR20260107007', '2026-01-07', '2026-02-07', NULL,         0, 0, 0.00, 1),
(5,  18, 'BR20260108008', '2026-01-08', '2026-02-08', '2026-02-06', 1, 0, 0.00, 1),
(6,  20, 'BR20260109009', '2026-01-09', '2026-02-09', NULL,         0, 0, 0.00, 1),
(6,  25, 'BR20260110010', '2026-01-10', '2026-02-10', NULL,         0, 0, 0.00, 1),
(2,  30, 'BR20260111011', '2026-01-11', '2026-02-11', NULL,         0, 0, 0.00, 1),
(3,  35, 'BR20260112012', '2026-01-12', '2026-02-12', NULL,         0, 0, 0.00, 1),
(4,  40, 'BR20260113013', '2026-01-13', '2026-02-13', NULL,         0, 0, 0.00, 1),
(7,  2,  'BR20260114014', '2026-01-14', '2026-02-14', NULL,         0, 0, 0.00, 1),
(8,  8,  'BR20260115015', '2026-01-15', '2026-02-15', NULL,         0, 0, 0.00, 1),
(9,  14, 'BR20260116016', '2026-01-16', '2026-02-16', NULL,         0, 0, 0.00, 1),
(2,  22, 'BR20260117017', '2026-01-17', '2026-02-17', '2026-01-20', 1, 0, 0.00, 1),
(3,  28, 'BR20260118018', '2026-01-18', '2026-02-18', NULL,         0, 0, 0.00, 1),
(4,  33, 'BR20260119019', '2026-01-19', '2026-02-19', NULL,         0, 0, 0.00, 1),
(5,  38, 'BR20260120020', '2026-01-20', '2026-02-20', NULL,         0, 0, 0.00, 1);

-- ============================================================
-- 预约记录（5 条）
-- ============================================================
INSERT INTO `reservation` (`user_id`, `book_id`, `reserve_date`, `expire_date`, `status`, `notified`) VALUES
(2,  6,  '2026-01-15', '2026-01-22', 0, 0),
(3,  10, '2026-01-16', '2026-01-23', 0, 0),
(4,  16, '2026-01-17', '2026-01-24', 1, 1),
(5,  21, '2026-01-18', '2026-01-25', 0, 0),
(6,  27, '2026-01-19', '2026-01-26', 0, 0);

-- ============================================================
-- 操作日志（12 条）
-- ============================================================
INSERT INTO `operation_log` (`operator_id`, `operation_type`, `target_id`, `content`, `ip_address`) VALUES
(1, 'LOGIN',    1,  '管理员登录系统',                   '127.0.0.1'),
(1, 'BOOK_ADD', 1,  '新增图书：深入理解计算机系统',     '127.0.0.1'),
(1, 'BOOK_ADD', 2,  '新增图书：Python编程从入门到实践', '127.0.0.1'),
(1, 'BORROW',   1,  '处理借阅：BR20260101001',          '127.0.0.1'),
(1, 'BORROW',   2,  '处理借阅：BR20260102002',          '127.0.0.1'),
(1, 'RETURN',   2,  '处理还书：BR20260102002',          '127.0.0.1'),
(2, 'LOGIN',    2,  '用户张小明登录',                   '192.168.1.100'),
(2, 'BORROW',   1,  '借阅图书：深入理解计算机系统',     '192.168.1.100'),
(3, 'LOGIN',    3,  '用户李小红登录',                   '192.168.1.101'),
(3, 'BORROW',   3,  '借阅图书：算法导论',               '192.168.1.101'),
(4, 'LOGIN',    4,  '用户王小刚登录',                   '192.168.1.102'),
(4, 'RESERVE',  4,  '预约图书：明朝那些事儿',           '192.168.1.102');

-- ============================================================
-- 图书评论（7 条）
-- ============================================================
INSERT INTO `book_comment` (`book_id`, `user_id`, `rating`, `content`, `like_count`, `reply_count`) VALUES
(1, 2, 5, '这本书非常经典，内容详实，适合作为计算机系统的入门教材。强烈推荐！', 5, 0),
(1, 3, 4, '讲解深入浅出，但是有些章节对于初学者来说稍微有点难懂。',           2, 1),
(2, 4, 5, 'Python入门必读，例子丰富，代码风格也很好。',                       8, 0),
(2, 5, 5, '跟着书上的例子动手实践，学习效果很好。',                           3, 0),
(3, 6, 4, '算法领域的权威著作，就是太厚了，需要耐心阅读。',                   4, 0),
(4, 7, 5, '科幻小说的巅峰之作，构思宏大，情节引人入胜！',                     12, 2),
(5, 8, 5, '读完后久久不能平静，生命的意义到底是什么？',                       6, 1);

-- ============================================================
-- 评论点赞（14 条）
-- ============================================================
INSERT INTO `comment_like` (`comment_id`, `user_id`) VALUES
(1, 3), (1, 4), (1, 5),
(2, 2),
(3, 2), (3, 5), (3, 6),
(4, 2),
(5, 2),
(6, 2), (6, 3), (6, 4), (6, 5),
(7, 2);

-- ============================================================
-- 测试账号
--   管理员   admin    密码 admin123
--   普通用户  user001  密码 123456
-- ============================================================
