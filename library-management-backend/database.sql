-- 数据库创建
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色：0=管理员，1=普通用户',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
  `last_login_time` DATE DEFAULT NULL COMMENT '最后登录时间',
  `deleted` INT DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图书分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书表
CREATE TABLE IF NOT EXISTS `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `isbn` VARCHAR(32) NOT NULL COMMENT 'ISBN号',
  `title` VARCHAR(255) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) NOT NULL COMMENT '作者',
  `publisher` VARCHAR(128) DEFAULT NULL COMMENT '出版社',
  `publish_date` DATE DEFAULT NULL COMMENT '出版日期',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
  `description` TEXT COMMENT '图书简介',
  `total_stock` INT NOT NULL DEFAULT 1 COMMENT '总库存量',
  `available_stock` INT NOT NULL DEFAULT 1 COMMENT '可借数量',
  `location` VARCHAR(128) DEFAULT NULL COMMENT '存放位置',
  `deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isbn` (`isbn`),
  KEY `idx_title` (`title`),
  KEY `idx_author` (`author`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_available_stock` (`available_stock`),
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS `borrow_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `borrow_code` VARCHAR(32) NOT NULL COMMENT '借阅编号',
  `borrow_date` DATE NOT NULL COMMENT '借阅日期',
  `due_date` DATE NOT NULL COMMENT '应还日期',
  `return_date` DATE DEFAULT NULL COMMENT '实际归还日期',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=借阅中，1=已归还，2=逾期，3=待审核(续借)',
  `renew_count` INT DEFAULT 0 COMMENT '续借次数',
  `overdue_fine` DECIMAL(10,2) DEFAULT 0 COMMENT '逾期费用',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID（管理员）',
  `deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_borrow_code` (`borrow_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_status` (`status`),
  KEY `idx_borrow_date` (`borrow_date`),
  KEY `idx_due_date` (`due_date`),
  CONSTRAINT `fk_borrow_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 预约表
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `reserve_date` DATE NOT NULL COMMENT '预约日期',
  `expire_date` DATE NOT NULL COMMENT '预约失效日期',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=等待中，1=可取书，2=已取消，3=已完成',
  `notified` TINYINT(1) DEFAULT 0 COMMENT '是否已通知',
  `deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_reserve_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_reserve_book` FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作员ID',
  `operation_type` VARCHAR(64) NOT NULL COMMENT '操作类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '操作目标ID',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '操作内容',
  `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 初始化管理员账号
-- 密码是 admin123 的 BCrypt 加密
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', '$2a$10$3Ch6MH43vAZ3H150vi.W4e6NmhEabePx2k3tIR7Lp0oAC0XaF1dz6', '系统管理员', 0, 1);

-- 初始化分类数据
INSERT INTO `category` (`name`, `parent_id`, `sort`) VALUES
('文学', 0, 1),
('科技', 0, 2),
('历史', 0, 3),
('小说', 1, 1),
('计算机', 2, 1);

-- 初始化一些图书
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `category`, `total_stock`, `available_stock`, `location`) VALUES
('978-7-111-12345-6', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '计算机', 5, 3, 'A区-3楼-01'),
('978-7-121-23456-7', '百年孤独', '加西亚·马尔克斯', '人民文学出版社', '小说', 3, 2, 'B区-2楼-15'),
('978-7-302-34567-8', 'JavaScript高级程序设计', 'Nicholas C. Zakas', '清华大学出版社', '计算机', 8, 6, 'A区-3楼-08');