-- ============================================================
-- RailMind AI - 测试数据初始化脚本
-- 版本: V2__init_data.sql
-- 说明: 站点、车次、座位、票价、测试用户等基础数据
-- ============================================================

USE railmind;

-- ============================================================
-- 1. 站点数据 (主要城市火车站)
-- ============================================================
INSERT INTO t_station (code, name, city, province, longitude, latitude, bureau) VALUES
-- 华北
('BJP',  '北京站',     '北京', '北京',   116.428116, 39.906765, '北京局'),
('BJN',  '北京南站',   '北京', '北京',   116.378968, 39.865225, '北京局'),
('BJX',  '北京西站',   '北京', '北京',   116.321966, 39.894884, '北京局'),
('TJP',  '天津站',     '天津', '天津',   117.211685, 39.136547, '北京局'),
('TJN',  '天津南站',   '天津', '天津',   117.070081, 39.058546, '北京局'),
('SJP',  '石家庄站',   '石家庄', '河北', 114.486277, 38.008315, '北京局'),
('TYU',  '太原站',     '太原', '山西',   112.580838, 37.861263, '太原局'),
-- 华东
('SHH',  '上海站',     '上海', '上海',   121.457680, 31.250380, '上海局'),
('SNH',  '上海南站',   '上海', '上海',   121.430160, 31.156700, '上海局'),
('AOH',  '上海虹桥站', '上海', '上海',   121.326980, 31.194130, '上海局'),
('NJH',  '南京站',     '南京', '江苏',   118.796550, 32.088410, '上海局'),
('NKH',  '南京南站',   '南京', '江苏',   118.788370, 31.972430, '上海局'),
('HZH',  '杭州站',     '杭州', '浙江',   120.180060, 30.242130, '上海局'),
('HGH',  '杭州东站',   '杭州', '浙江',   120.219396, 30.291516, '上海局'),
('HFH',  '合肥站',     '合肥', '安徽',   117.305840, 31.875310, '上海局'),
('HFH2', '合肥南站',   '合肥', '安徽',   117.316970, 31.788730, '上海局'),
-- 华南
('GZQ',  '广州站',     '广州', '广东',   113.261560, 23.152090, '广州局'),
('GZN',  '广州南站',   '广州', '广东',   113.268720, 22.988830, '广州局'),
('IZQ',  '广州东站',   '广州', '广东',   113.326530, 23.150650, '广州局'),
('SZQ',  '深圳站',     '深圳', '广东',   114.122990, 22.525920, '广州局'),
('IOQ',  '深圳北站',   '深圳', '广东',   114.029130, 22.609930, '广州局'),
('WHN',  '武汉站',     '武汉', '湖北',   114.422580, 30.613220, '武汉局'),
('WCN',  '武汉南站',   '武汉', '湖北',   114.337890, 30.528940, '武汉局'),
('CSQ',  '长沙站',     '长沙', '湖南',   113.011330, 28.205330, '广州局'),
('CWQ',  '长沙南站',   '长沙', '湖南',   113.056930, 28.152990, '广州局'),
-- 华中/西南
('CDW',  '成都站',     '成都', '四川',   104.073330, 30.689280, '成都局'),
('ICW',  '成都东站',   '成都', '四川',   104.146580, 30.630210, '成都局'),
('CQW',  '重庆站',     '重庆', '重庆',   106.554830, 29.558990, '成都局'),
('CUW',  '重庆北站',   '重庆', '重庆',   106.564210, 29.609870, '成都局'),
('KMM',  '昆明站',     '昆明', '云南',   102.724170, 25.019650, '昆明局'),
('GYW',  '贵阳站',     '贵阳', '贵州',   106.710630, 26.576670, '成都局'),
-- 东北
('SYB',  '沈阳站',     '沈阳', '辽宁',   123.396880, 41.802280, '沈阳局'),
('SYD',  '沈阳北站',   '沈阳', '辽宁',   123.426630, 41.821840, '沈阳局'),
('DRT',  '大连站',     '大连', '辽宁',   121.625790, 38.921760, '沈阳局'),
('CCT',  '长春站',     '长春', '吉林',   125.332680, 43.889810, '沈阳局'),
('HBB',  '哈尔滨站',   '哈尔滨', '黑龙江', 126.637450, 45.756680, '哈尔滨局'),
-- 西北
('XAY',  '西安站',     '西安', '陕西',   108.962430, 34.275520, '西安局'),
('XAY2', '西安北站',   '西安', '陕西',   108.943580, 34.375660, '西安局'),
('LZJ',  '兰州站',     '兰州', '甘肃',   103.852190, 36.036840, '兰州局'),
('XNO',  '西宁站',     '西宁', '青海',   101.790130, 36.602710, '青藏铁路局'),
('WMR',  '乌鲁木齐站', '乌鲁木齐', '新疆', 87.581150, 43.779570, '乌鲁木齐局');

-- ============================================================
-- 2. 车次数据 (京沪/京广/沪昆等热门线路)
-- ============================================================
INSERT INTO t_train (train_no, train_type, start_station, end_station, departure_time, arrival_time, run_days, total_mileage) VALUES
-- 京沪高铁 (北京南→上海虹桥)
('G1',   'G', 'BJN', 'AOH', '09:00:00', '13:28:00', 1, 1318),
('G3',   'G', 'BJN', 'AOH', '10:00:00', '14:28:00', 1, 1318),
('G5',   'G', 'BJN', 'AOH', '11:00:00', '15:28:00', 1, 1318),
('G7',   'G', 'BJN', 'AOH', '14:00:00', '18:28:00', 1, 1318),
('G9',   'G', 'BJN', 'AOH', '15:00:00', '19:28:00', 1, 1318),
('G2',   'G', 'AOH', 'BJN', '07:00:00', '11:28:00', 1, 1318),
('G4',   'G', 'AOH', 'BJN', '08:00:00', '12:28:00', 1, 1318),
('G6',   'G', 'AOH', 'BJN', '09:30:00', '13:58:00', 1, 1318),
-- 京广高铁 (北京西→广州南)
('G65',  'G', 'BJX', 'GZN', '10:00:00', '18:18:00', 1, 2298),
('G67',  'G', 'BJX', 'GZN', '12:00:00', '20:18:00', 1, 2298),
('G69',  'G', 'BJX', 'GZN', '14:00:00', '22:18:00', 1, 2298),
('G66',  'G', 'GZN', 'BJX', '09:00:00', '17:18:00', 1, 2298),
('G68',  'G', 'GZN', 'BJX', '11:00:00', '19:18:00', 1, 2298),
-- 沪昆高铁 (上海虹桥→昆明南)
('G1371', 'G', 'AOH', 'KMM', '08:00:00', '19:55:00', 1, 2252),
('G1373', 'G', 'AOH', 'KMM', '10:00:00', '21:55:00', 1, 2252),
-- 成渝高铁 (成都东→重庆北)
('G8601', 'G', 'ICW', 'CUW', '08:00:00', '09:30:00', 1, 340),
('G8603', 'G', 'ICW', 'CUW', '10:00:00', '11:30:00', 1, 340),
('G8605', 'G', 'ICW', 'CUW', '14:00:00', '15:30:00', 1, 340),
-- 武广高铁 (武汉→广州南)
('G1111', 'G', 'WHN', 'GZN', '09:00:00', '13:18:00', 1, 1069),
('G1113', 'G', 'WHN', 'GZN', '11:00:00', '15:18:00', 1, 1069),
-- 西成高铁 (西安北→成都东)
('G2201', 'G', 'XAY2', 'ICW', '08:00:00', '11:30:00', 1, 658),
('G2203', 'G', 'XAY2', 'ICW', '14:00:00', '17:30:00', 1, 658),
-- 动车
('D311', 'D', 'BJN', 'SHH', '07:00:00', '13:23:00', 1, 1318),
('D313', 'D', 'BJN', 'SHH', '09:00:00', '15:23:00', 1, 1318),
-- 普快
('K101', 'K', 'BJP', 'SHH', '23:50:00', '15:45:00', 2, 1463),
('T109', 'T', 'BJP', 'SHH', '19:32:00', '09:14:00', 2, 1463);

-- ============================================================
-- 3. 车次途经站数据 (京沪高铁 G1 为例)
-- ============================================================
INSERT INTO t_train_station (train_id, station_code, station_order, arrival_time, departure_time, stop_duration, mileage) VALUES
-- G1: 北京南 → 济南西 → 南京南 → 上海虹桥
(1, 'BJN', 1, NULL,       '09:00:00', 0,    0),
(1, 'SJP', 2, '10:05:00', '10:08:00', 3,  283),
(1, 'TYU', 3, '11:10:00', '11:13:00', 3,  508),
(1, 'HFH2',4, '12:30:00', '12:33:00', 3,  972),
(1, 'NJH', 5, '12:50:00', '12:53:00', 3, 1023),
(1, 'AOH', 6, '13:28:00', NULL,       0, 1318),
-- G2: 上海虹桥 → 北京南
(2, 'AOH', 1, NULL,       '07:00:00', 0,    0),
(2, 'NJH', 2, '07:33:00', '07:36:00', 3,  295),
(2, 'HFH2',3, '08:10:00', '08:13:00', 3,  346),
(2, 'SJP', 4, '09:20:00', '09:23:00', 3, 1035),
(2, 'BJN', 5, '11:28:00', NULL,       0, 1318),
-- G65: 北京西 → 石家庄 → 郑州东 → 武汉 → 长沙南 → 广州南
(9, 'BJX', 1, NULL,       '10:00:00', 0,    0),
(9, 'SJP', 2, '11:05:00', '11:08:00', 3,  281),
(9, 'WHN', 3, '14:00:00', '14:05:00', 5, 1225),
(9, 'CWQ', 4, '15:30:00', '15:33:00', 3, 1591),
(9, 'GZN', 5, '18:18:00', NULL,       0, 2298);

-- ============================================================
-- 4. 座位类型数据 (每种车型的座位配置)
-- ============================================================
INSERT INTO t_seat_type (train_id, seat_type_code, seat_type_name, total_count, price_factor) VALUES
-- G1-G9 (京沪高铁) 每列车 5 种座位
(1, 'SW', '商务座',  28, 3.00),
(1, 'ZY', '一等座', 168, 1.80),
(1, 'ZE', '二等座', 868, 1.00),
(1, 'RW', '软卧',    0,  2.50),
(1, 'GR', '高级软卧', 0,  4.00),
(2, 'SW', '商务座',  28, 3.00),
(2, 'ZY', '一等座', 168, 1.80),
(2, 'ZE', '二等座', 868, 1.00),
(3, 'SW', '商务座',  28, 3.00),
(3, 'ZY', '一等座', 168, 1.80),
(3, 'ZE', '二等座', 868, 1.00),
(4, 'SW', '商务座',  28, 3.00),
(4, 'ZY', '一等座', 168, 1.80),
(4, 'ZE', '二等座', 868, 1.00),
(5, 'SW', '商务座',  28, 3.00),
(5, 'ZY', '一等座', 168, 1.80),
(5, 'ZE', '二等座', 868, 1.00),
-- G65-G69 (京广高铁)
(9, 'SW', '商务座',  28, 3.00),
(9, 'ZY', '一等座', 168, 1.80),
(9, 'ZE', '二等座', 868, 1.00),
(10, 'SW', '商务座', 28, 3.00),
(10, 'ZY', '一等座',168, 1.80),
(10, 'ZE', '二等座',868, 1.00),
(11, 'SW', '商务座', 28, 3.00),
(11, 'ZY', '一等座',168, 1.80),
(11, 'ZE', '二等座',868, 1.00),
-- 动车 D311/D313
(23, 'ZY', '一等座', 128, 1.80),
(23, 'ZE', '二等座', 688, 1.00),
(24, 'ZY', '一等座', 128, 1.80),
(24, 'ZE', '二等座', 688, 1.00),
-- 普快 K101/T109
(25, 'RW', '软卧',   72, 2.50),
(25, 'YW', '硬卧',  480, 1.50),
(25, 'YZ', '硬座',  600, 1.00),
(26, 'RW', '软卧',   72, 2.50),
(26, 'YW', '硬卧',  480, 1.50),
(26, 'YZ', '硬座',  600, 1.00);

-- ============================================================
-- 5. 票价数据 (基于里程计算)
-- ============================================================
INSERT INTO t_ticket_price (train_id, from_station, to_station, seat_type_code, price) VALUES
-- G1: 北京南→上海虹桥 (1318km)
(1, 'BJN', 'AOH', 'SW', 1748.00),
(1, 'BJN', 'AOH', 'ZY',  933.00),
(1, 'BJN', 'AOH', 'ZE',  553.00),
-- G1: 北京南→南京南 (1023km)
(1, 'BJN', 'NKH', 'SW', 1352.00),
(1, 'BJN', 'NKH', 'ZY',  725.00),
(1, 'BJN', 'NKH', 'ZE',  403.00),
-- G1: 北京南→石家庄 (283km)
(1, 'BJN', 'SJP', 'SW',  377.00),
(1, 'BJN', 'SJP', 'ZY',  203.00),
(1, 'BJN', 'SJP', 'ZE',  112.00),
-- G2: 上海虹桥→北京南
(2, 'AOH', 'BJN', 'SW', 1748.00),
(2, 'AOH', 'BJN', 'ZY',  933.00),
(2, 'AOH', 'BJN', 'ZE',  553.00),
-- G65: 北京西→广州南 (2298km)
(9, 'BJX', 'GZN', 'SW', 3033.00),
(9, 'BJX', 'GZN', 'ZY', 1623.00),
(9, 'BJX', 'GZN', 'ZE',  901.00),
-- G65: 北京西→武汉 (1225km)
(9, 'BJX', 'WHN', 'SW', 1615.00),
(9, 'BJX', 'WHN', 'ZY',  864.00),
(9, 'BJX', 'WHN', 'ZE',  480.00),
-- G65: 武汉→广州南 (1069km)
(9, 'WHN', 'GZN', 'SW', 1408.00),
(9, 'WHN', 'GZN', 'ZY',  754.00),
(9, 'WHN', 'GZN', 'ZE',  419.00),
-- D311: 北京南→上海 (动车)
(23, 'BJN', 'SHH', 'ZY', 690.00),
(23, 'BJN', 'SHH', 'ZE', 384.00),
-- K101: 北京→上海 (普快)
(25, 'BJP', 'SHH', 'RW', 456.00),
(25, 'BJP', 'SHH', 'YW', 283.00),
(25, 'BJP', 'SHH', 'YZ', 165.00);

-- ============================================================
-- 6. 运行图数据 (未来30天排班)
-- ============================================================
INSERT INTO t_train_schedule (train_id, travel_date, status)
SELECT t.id, d.dt, 1
FROM t_train t
CROSS JOIN (
    SELECT DATE_ADD(CURDATE(), INTERVAL n DAY) AS dt
    FROM (
        SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
        UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14
        UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
        UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24
        UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
    ) nums
) d;

-- ============================================================
-- 7. 测试用户 (密码均为 123456 的 BCrypt 加密)
-- ============================================================
INSERT INTO t_user (username, password, real_name, id_card, id_card_hash, phone, email, status) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', NULL, SHA2('110101199001011234', 256), '13800001111', 'zhangsan@test.com', 1),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', NULL, SHA2('110101199203021234', 256), '13800002222', 'lisi@test.com', 1),
('testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', NULL, SHA2('110101198805031234', 256), '13800003333', 'wangwu@test.com', 1);

-- ============================================================
-- 8. 测试乘车人
-- ============================================================
INSERT INTO t_passenger (user_id, name, id_card, id_card_hash, phone, type, status) VALUES
(1, '张三',   'ENCRYPTED_PLACEHOLDER', SHA2('110101199001011234', 256), '13800001111', 1, 1),
(1, '张小三', 'ENCRYPTED_PLACEHOLDER', SHA2('110101201501011234', 256), '13800001112', 2, 1),
(2, '李四',   'ENCRYPTED_PLACEHOLDER', SHA2('110101199203021234', 256), '13800002222', 1, 1),
(3, '王五',   'ENCRYPTED_PLACEHOLDER', SHA2('110101198805031234', 256), '13800003333', 1, 1);

-- ============================================================
-- 9. 初始票务库存 (未来7天, 京沪高铁G1全线路)
-- ============================================================
INSERT INTO t_ticket_inventory (train_id, travel_date, from_station, to_station, seat_type_code, total_count, sold_count, locked_count)
SELECT s.train_id, s.travel_date, 'BJN', 'AOH', st.seat_type_code, st.total_count, 0, 0
FROM t_train_schedule s
JOIN t_seat_type st ON st.train_id = s.train_id
WHERE s.train_id = 1  -- G1
AND s.travel_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND st.total_count > 0;

-- G1 北京南→南京南
INSERT INTO t_ticket_inventory (train_id, travel_date, from_station, to_station, seat_type_code, total_count, sold_count, locked_count)
SELECT s.train_id, s.travel_date, 'BJN', 'NKH', st.seat_type_code, st.total_count, 0, 0
FROM t_train_schedule s
JOIN t_seat_type st ON st.train_id = s.train_id
WHERE s.train_id = 1
AND s.travel_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND st.total_count > 0;

-- G2 上海虹桥→北京南
INSERT INTO t_ticket_inventory (train_id, travel_date, from_station, to_station, seat_type_code, total_count, sold_count, locked_count)
SELECT s.train_id, s.travel_date, 'AOH', 'BJN', st.seat_type_code, st.total_count, 0, 0
FROM t_train_schedule s
JOIN t_seat_type st ON st.train_id = s.train_id
WHERE s.train_id = 2
AND s.travel_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND st.total_count > 0;

-- G65 北京西→广州南
INSERT INTO t_ticket_inventory (train_id, travel_date, from_station, to_station, seat_type_code, total_count, sold_count, locked_count)
SELECT s.train_id, s.travel_date, 'BJX', 'GZN', st.seat_type_code, st.total_count, 0, 0
FROM t_train_schedule s
JOIN t_seat_type st ON st.train_id = s.train_id
WHERE s.train_id = 9
AND s.travel_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND st.total_count > 0;

-- G65 北京西→武汉
INSERT INTO t_ticket_inventory (train_id, travel_date, from_station, to_station, seat_type_code, total_count, sold_count, locked_count)
SELECT s.train_id, s.travel_date, 'BJX', 'WHN', st.seat_type_code, st.total_count, 0, 0
FROM t_train_schedule s
JOIN t_seat_type st ON st.train_id = s.train_id
WHERE s.train_id = 9
AND s.travel_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND st.total_count > 0;

-- D311 北京南→上海
INSERT INTO t_ticket_inventory (train_id, travel_date, from_station, to_station, seat_type_code, total_count, sold_count, locked_count)
SELECT s.train_id, s.travel_date, 'BJN', 'SHH', st.seat_type_code, st.total_count, 0, 0
FROM t_train_schedule s
JOIN t_seat_type st ON st.train_id = s.train_id
WHERE s.train_id = 23
AND s.travel_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND st.total_count > 0;

-- ============================================================
-- 完成
-- ============================================================
SELECT '测试数据初始化完成' AS result;
SELECT COUNT(*) AS station_count FROM t_station;
SELECT COUNT(*) AS train_count FROM t_train;
SELECT COUNT(*) AS price_count FROM t_ticket_price;
SELECT COUNT(*) AS inventory_count FROM t_ticket_inventory;
SELECT COUNT(*) AS user_count FROM t_user;
