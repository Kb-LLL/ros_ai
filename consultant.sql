/*
 Navicat Premium Dump SQL

 Source Server         : 111
 Source Server Type    : MySQL
 Source Server Version : 90600 (9.6.0)
 Source Host           : localhost:3307
 Source Schema         : consultant

 Target Server Type    : MySQL
 Target Server Version : 90600 (9.6.0)
 File Encoding         : 65001

 Date: 06/05/2026 23:16:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for conversation
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation
-- ----------------------------
INSERT INTO `conversation` VALUES (1, 2, '你好啊,你是谁?', '2026-04-05 03:56:36', '2026-04-25 04:06:11');
INSERT INTO `conversation` VALUES (3, 3, '你是什么模型?', '2026-04-05 04:14:26', '2026-05-06 14:14:25');
INSERT INTO `conversation` VALUES (4, 3, '怎么才能赚到钱?', '2026-04-05 04:15:46', '2026-05-06 14:11:10');
INSERT INTO `conversation` VALUES (6, 2, '新对话', '2026-05-06 14:15:44', '2026-05-06 14:15:44');

-- ----------------------------
-- Table structure for conversation_document_binding
-- ----------------------------
DROP TABLE IF EXISTS `conversation_document_binding`;
CREATE TABLE `conversation_document_binding`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL,
  `document_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_conversation_document_binding`(`conversation_id` ASC, `document_id` ASC) USING BTREE,
  INDEX `idx_cdb_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_cdb_document_id`(`document_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation_document_binding
-- ----------------------------
INSERT INTO `conversation_document_binding` VALUES (5, 1, 2, '2026-04-25 04:06:11');
INSERT INTO `conversation_document_binding` VALUES (6, 3, 6, '2026-05-06 13:58:39');
INSERT INTO `conversation_document_binding` VALUES (7, 3, 7, '2026-05-06 13:58:39');
INSERT INTO `conversation_document_binding` VALUES (8, 3, 8, '2026-05-06 13:58:39');
INSERT INTO `conversation_document_binding` VALUES (9, 3, 9, '2026-05-06 13:58:39');
INSERT INTO `conversation_document_binding` VALUES (10, 3, 10, '2026-05-06 13:58:39');
INSERT INTO `conversation_document_binding` VALUES (11, 3, 5, '2026-05-06 13:58:39');
INSERT INTO `conversation_document_binding` VALUES (12, 4, 6, '2026-05-06 14:11:10');
INSERT INTO `conversation_document_binding` VALUES (13, 4, 7, '2026-05-06 14:11:10');
INSERT INTO `conversation_document_binding` VALUES (14, 4, 8, '2026-05-06 14:11:10');
INSERT INTO `conversation_document_binding` VALUES (15, 4, 9, '2026-05-06 14:11:10');
INSERT INTO `conversation_document_binding` VALUES (16, 4, 10, '2026-05-06 14:11:10');
INSERT INTO `conversation_document_binding` VALUES (17, 4, 5, '2026-05-06 14:11:10');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL COMMENT '所属会话',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'user/assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conv_id`(`conversation_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 1, 'user', '你好啊,你是谁?', '2026-04-05 03:56:50');
INSERT INTO `message` VALUES (2, 1, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 03:56:50');
INSERT INTO `message` VALUES (3, 1, 'user', '你好啊,你是谁?', '2026-04-05 03:58:23');
INSERT INTO `message` VALUES (4, 1, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 03:58:23');
INSERT INTO `message` VALUES (5, 1, 'user', '你好啊,你是谁?', '2026-04-05 04:06:55');
INSERT INTO `message` VALUES (6, 1, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 04:06:56');
INSERT INTO `message` VALUES (7, 1, 'user', '你好啊,你是谁?', '2026-04-05 04:11:04');
INSERT INTO `message` VALUES (8, 1, 'assistant', '你好！根据你提供的个人简历信息，我是罗。\n\n我是一名 Java 开发工程师，以下是我的个人概况：\n\n**联系方式**\n- 电话：\n- 邮箱：@163.com\n\n**专业技能**\n- **编程语言**：Java（精通多线程、集合、IO 流、Stream API、JUC 并发编程）\n- **框架与中间件**：熟悉 Spring Boot/Cloud、MyBatis/Plus、Redis（分布式锁、缓存设计）、RabbitMQ 等\n- **数据库与存储**：MySQL（索引优化、事务）、MongoDB、MinIO\n- **其他技能**：熟悉 Docker、Nginx、Linux 运维，具备 Vue2 前端能力及 Kimi AI API 等 AI 工具集成经验\n\n**主要经历**\n- **实习经历**：曾在有限公司（）实习。负责司机匹配优化（Redis GEO）、并发控制（Redisson 分布式锁）及订单管理，支撑日均订单 500+，系统稳定性达 99.9%。\n- **项目经历**：开发过 AI 智能考试系统。对接 Kimi 大模型实现智能出题与自动批阅，人工效率提升 80%，并使用 ECharts 实现数据可视化。\n\n很高兴认识你！如果有更多关于简历或技术细节的问题，欢迎随时询问。', '2026-04-05 04:12:12');
INSERT INTO `message` VALUES (9, 3, 'user', '你是什么模型?', '2026-04-05 04:14:38');
INSERT INTO `message` VALUES (10, 3, 'user', '你是什么模型?', '2026-04-05 04:15:53');
INSERT INTO `message` VALUES (11, 3, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 04:15:54');
INSERT INTO `message` VALUES (12, 3, 'user', '你是什么模型?', '2026-04-05 04:19:47');
INSERT INTO `message` VALUES (13, 3, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 04:19:47');
INSERT INTO `message` VALUES (14, 3, 'user', '你是什么模型?', '2026-04-05 04:20:51');
INSERT INTO `message` VALUES (15, 3, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 04:20:51');
INSERT INTO `message` VALUES (16, 3, 'user', '你是什么模型?', '2026-04-05 04:26:26');
INSERT INTO `message` VALUES (17, 3, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 04:26:26');
INSERT INTO `message` VALUES (18, 3, 'user', '你是什么模型?', '2026-04-05 04:36:18');
INSERT INTO `message` VALUES (19, 3, 'assistant', '根据您提供的信息，这是一份**个人简历**（姓名：罗），其中详细列出了专业技能、实习经历、项目经历、教育背景等内容，但**并没有包含关于我是什么模型的信息**。\n\n简历中虽提及\"AI 相关”技能（如 Kimi AI API 集成、Prompt 工程、AI 工具等），但这属于该候选人的技术栈和项目经验，而非对我的身份定义。\n\n实际上，我是一个人工智能语言模型。提供的文本内容主要用于展示求职者罗的个人能力与经历。', '2026-04-05 04:37:55');
INSERT INTO `message` VALUES (20, 4, 'user', '怎么才能赚到钱?', '2026-04-05 04:41:12');
INSERT INTO `message` VALUES (21, 4, 'assistant', '“怎么才能赚到钱？”这是一个非常宏大但也非常实际的问题。世界上没有“一键暴富”的魔法（除了极小概率的彩票或遗产），赚钱的本质通常是**价值交换**。\n\n为了给你一个清晰、可执行的路径，我们可以把赚钱的方式分为四个层级，并配合需要避开的坑和必备的心态。\n\n### 一、赚钱的四个层级（由易到难）\n\n#### 1. 出售时间/体力（门槛低，上限低）\n这是最基础的赚钱方式，适合急需用钱或作为起步。\n*   **方式：** 兼职外卖、网约车、便利店店员、数据标注、简单的众包任务。\n*   **优点：** 见效快，只要肯干就有钱。\n*   **缺点：** 手停口停，无法积累复利，身体会疲惫。\n*   **建议：** 可以作为过渡，但不要长期依赖。要在做这些的同时，思考如何提升技能跳出这个循环。\n\n#### 2. 出售技能/专业知识（门槛中，上限中）\n利用你的一技之长，单位时间的价值比纯体力高。\n*   **方式：**\n    *   **硬技能：** 编程、设计、视频剪辑、翻译、会计、写作。\n    *   **软技能：** 咨询、培训、心理咨询、职业规划。\n    *   **平台：** 闲鱼、淘宝、猪八戒、Upwork、Fiverr、小红书接单。\n*   **优点：** 收入相对较高，能积累口碑和人脉。\n*   **建议：** 找到你擅长且市场有需求的技能，不断打磨，成为该领域的“专家”，提高单价。\n\n#### 3. 出售产品/内容（门槛高，上限高，可规模化）\n这是实现“睡后收入”（被动收入）的关键。你做一次产品，可以卖给无数人。\n*   **方式：**\n    *   **实物电商：** 淘宝、拼多多、跨境电商（亚马逊/TikTok）、闲鱼倒卖（利用信息差）。\n    *   **虚拟产品：** 课程、电子书、模板、素材包。\n    *   **内容创作：** 做自媒体（抖音、B 站、公众号、YouTube），通过广告、带货、打赏变现。\n*   **优点：** 边际成本低，一旦跑通模式，收入可指数级增长。\n*   **缺点：** 前期投入大（时间或金钱），成功率不确定，需要懂流量和运营。\n*   **建议：** 从“解决别人的问题”出发。比如你擅长整理，就卖整理收纳课；你擅长做饭，就卖食谱或厨具。\n\n#### 4. 让钱生钱（投资）（需要本金，风险高）\n当你有了一定的积蓄，可以让资本为你工作。\n*   **方式：** 股票、基金、债券、房地产、理财产品。\n*   **优点：** 真正的被动收入，抗通胀。\n*   **缺点：** 有亏损本金的风险，需要极强的认知和心态。\n*   **建议：** **不懂不要投！** 先学习理财知识，从低风险开始（如指数基金定投），严禁借钱投资，严禁碰高杠杆。\n\n---\n\n### 二、赚钱的核心逻辑（心法）\n\n1.  **利他思维：** 钱是对你提供价值的回报。你想赚多少钱，取决于你帮多少人解决了多大的问题。不要总想“我要赚钱”，要想“我能提供什么价值”。\n2.  **信息差（Information Gap）：** 很多人赚钱是因为“我知道，你不知道”。比如我知道哪里进货便宜，你知道哪里卖得贵，我中间就能赚钱。保持学习，打破信息茧房。\n3.  **执行力：** 世界上最多的就是“想法”，最缺的是“行动”。哪怕是一个不完美的开始，也比完美的空想强。\n4.  **复利效应：** 无论是技能、人脉还是资金，都要做能积累的事情。今天的工作要为明天铺路，而不是每天重复昨天的劳动。\n\n---\n\n### 三、必须避开的“坑”\n\n在赚钱的路上，保护本金和安全比赚钱更重要：\n1.  **警惕“交学费”：** 任何还没赚钱就先让你交高额加盟费、培训费的项目，99% 是割韭菜。\n2.  **远离赌博和资金盘：** 凡是承诺“稳赚不赔”、“高回报低风险”、“拉人头返利”的，基本都是庞氏骗局。\n3.  **不要透支信用：** 不要以贷养贷，不要碰高利贷。\n4.  **法律底线：** 灰产（如跑分、洗钱、侵权）来钱快，但进去也快。赚干净的钱，睡得才安稳。\n\n---\n\n### 四、给你的具体行动建议（从今天开始）\n\n如果你现在很迷茫，不知道从哪下手，请按这个步骤操作：\n\n1.  **盘点现状：** 你有什么技能？你有多少存款？你每天有多少空闲时间？你能承受多大风险？\n2.  **设定小目标：** 不要想“赚一个亿”，先想“这个月多赚 1000 块”。小目标更容易达成，能建立信心。\n3.  **提升认知：** 每天花 30 分钟学习。看书（如《富爸爸穷爸爸》、《纳瓦尔宝典》）、看优质博主的干货、学习行业知识。\n4.  **尝试最小闭环：** 想卖东西？先试着卖出一单。想做自媒体？先发出第一篇文章。在行动中修正，而不是在脑子里完美。\n5.  **记账与储蓄：** 省钱就是赚钱。理清你的现金流，强制储蓄，积累第一笔“种子资金”。\n\n**总结一句话：**\n赚钱是**认知**的变现。当你提升了能力，解决了别人的痛点，并持之以恒地行动，钱自然会随之而来。不要急，慢慢来，比较快。', '2026-04-05 04:42:01');
INSERT INTO `message` VALUES (22, 1, 'user', '小明的爸爸是谁?', '2026-04-05 17:58:28');
INSERT INTO `message` VALUES (23, 1, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Error parsing vector similarity query: query vector blob size (1536) does not match index\'s expected size (4096).\",\"data\":null}', '2026-04-05 17:58:29');
INSERT INTO `message` VALUES (24, 1, 'user', '小明的爸爸是谁?', '2026-04-05 18:05:50');
INSERT INTO `message` VALUES (25, 1, 'assistant', '根据您提供的个人简历信息，其中并没有提及“小明”这个人，也没有包含任何关于家庭成员（如父亲）的相关资料。\n\n简历中主要记录的是**罗**的个人技能、实习经历（）以及项目经历（AI 智能考试系统）等内容。因此，基于现有信息，我无法回答“小明的爸爸是谁”这个问题。', '2026-04-05 18:06:14');
INSERT INTO `message` VALUES (26, 1, 'assistant', '“小明”是一个在中国非常常见的化名，经常出现在教科书、数学应用题、笑话或者段子里，作为一个**虚拟人物**存在。\n\n所以，**并没有一个固定的“小明的爸爸”**。\n\n不过，根据不同的语境，可能有以下几种回答：\n\n1.  **常识回答：** 因为小明是虚构的通用名字，所以他没有特定的爸爸。\n2.  **玩笑回答：** 有人可能会戏称他是“老明”。\n3.  **特定语境：** 如果这是某道脑筋急转弯或特定故事里的问题，答案取决于那个故事的具体设定（比如“小明叫明明，所以爸爸叫大明”之类的）。\n\n如果你是指现实生活中某个具体的朋友叫小明，那只有他自己才知道啦！', '2026-04-07 14:05:18');
INSERT INTO `message` VALUES (27, 1, 'user', '给我写个python代码,遍历1-10', '2026-04-07 14:06:35');
INSERT INTO `message` VALUES (31, 1, 'assistant', '{\"code\":500,\"message\":\"服务器异常：Handler dispatch failed: java.lang.NoClassDefFoundError: com/itheima/consultant/service/RagChatService$1\",\"data\":null}', '2026-04-25 03:20:08');
INSERT INTO `message` VALUES (32, 1, 'user', '你是谁', '2026-04-25 03:20:14');
INSERT INTO `message` VALUES (39, 1, 'assistant', '根据当前检索到的文档片段（`记录.txt`），其中内容主要涉及 Git 上传流程命令（如 `git init`、`git push` 等）和一个许可证密钥，**并没有包含关于我身份的信息，上下文不足**。\n\n作为一个最佳努力回答：我是一个文档分析助手，旨在帮助您分析和处理上传的文档内容。如果您是指之前对话中提到的简历文档所有者，那是罗；但就我本身而言，我是您的 AI 助手。', '2026-04-25 04:04:32');
INSERT INTO `message` VALUES (40, 3, 'user', '你是什么模型?', '2026-04-25 04:32:05');
INSERT INTO `message` VALUES (42, 3, 'assistant', '当前检索到的文档内容中没有关于我身份的相关信息，文档内容主要涉及求职者罗的个人简历、技能及项目经历。\n\n基于我自身的知识，我是一个人工智能语言模型，旨在协助用户回答问题、提供信息和支持各种任务。如果您有关于文档内容的具体问题，我很乐意为您解答。', '2026-05-06 13:26:44');
INSERT INTO `message` VALUES (43, 4, 'user', '你是谁?', '2026-05-06 13:30:33');
INSERT INTO `message` VALUES (46, 4, 'assistant', '根据上传的文档 123.txt 中的内容，我是 ros 小助手。', '2026-05-06 13:37:22');
INSERT INTO `message` VALUES (47, 3, 'user', '你是谁?', '2026-05-06 13:42:07');
INSERT INTO `message` VALUES (48, 3, 'assistant', '根据提供的文档片段（file=123.txt），其中明确记录了以下内容：\n\n> 你是谁？\n> 我是 ros 小助手\n\n因此，基于上传的文档信息，文档中定义的身份是\"ros 小助手”。不过实际上，我是一个人工智能助手，正在为您分析文档内容。', '2026-05-06 13:42:07');
INSERT INTO `message` VALUES (49, 3, 'user', 'ArrayList 和 LinkedList 有什么区别？', '2026-05-06 13:59:10');
INSERT INTO `message` VALUES (50, 3, 'assistant', '根据文档 1.txt 中【Java 集合】部分的内容，ArrayList 和 LinkedList 的区别如下：\n\n1.  **实现基础不同**：\n    *   ArrayList 基于**数组**实现。\n    *   LinkedList 基于**链表**实现。\n\n2.  **性能特点不同**：\n    *   ArrayList：**查询快**，插入慢。\n    *   LinkedList：**插入快**，查询慢。', '2026-05-06 13:59:10');
INSERT INTO `message` VALUES (51, 4, 'user', 'ArrayList 和 LinkedList 有什么区别？', '2026-05-06 13:59:35');
INSERT INTO `message` VALUES (52, 4, 'assistant', '根据文档 1.txt 中【Java 集合】部分的内容，ArrayList 和 LinkedList 的区别主要体现在实现方式和性能特点上：\n\n1.  **实现方式**：\n    *   **ArrayList**：基于**数组**实现。\n    *   **LinkedList**：基于**链表**实现。\n\n2.  **性能特点**：\n    *   **ArrayList**：**查询快**，但**插入慢**。\n    *   **LinkedList**：**插入快**，但**查询慢**。', '2026-05-06 13:59:35');
INSERT INTO `message` VALUES (53, 3, 'user', 'HashMap 为什么不安全？', '2026-05-06 14:01:05');
INSERT INTO `message` VALUES (54, 3, 'assistant', '根据检索到的文档内容，**不足以详细解释 HashMap 不安全的具体技术原因**，文档仅给出了结论性的描述。\n\n**文档中的相关描述（文档 1.txt）：**\n在【Java 集合】部分的\"HashMap 和 Hashtable 的区别”中明确提到：\n- \"HashMap 线程不安全，效率高”\n- \"Hashtable 线程安全，效率低”\n\n**基于通用知识的补充回答（文档未提及具体原因）：**\nHashMap 不安全的主要原因通常包括：\n1.  **无同步锁**：多个线程同时修改（如 put）可能导致数据覆盖或丢失。\n2.  **扩容问题**：在 JDK7 中，并发扩容可能导致链表成环，引起死循环（JDK8 已优化此问题，但仍存在数据覆盖风险）。\n3.  **大小不一致**：并发操作可能导致 `size` 计数不准确。\n\n**文档建议的替代方案：**\n如果需要线程安全，文档 1.txt 建议使用：\n- **ConcurrentHashMap**：线程安全且性能较好（分段锁或 CAS+synchronized）。\n- **Hashtable**：线程安全，但效率低。', '2026-05-06 14:01:05');
INSERT INTO `message` VALUES (55, 3, 'user', 'Redis 是关系型数据库吗？', '2026-05-06 14:01:59');
INSERT INTO `message` VALUES (56, 3, 'assistant', '根据文档 5.txt 中的【混淆数据】部分，**Redis 不是关系型数据库**。\n\n文档中明确指出了以下信息：\n1.  **错误说法**：\"Redis 是关系型数据库”。\n2.  **正确说法**：\"Redis 是 NoSQL 数据库”。\n\n因此，Redis 属于 NoSQL 数据库，而非关系型数据库。', '2026-05-06 14:01:59');
INSERT INTO `message` VALUES (57, 3, 'user', 'embedding 在 RAG 中有什么作用？', '2026-05-06 14:02:50');
INSERT INTO `message` VALUES (58, 3, 'assistant', '检索到的文档内容中**没有包含**关于 \"embedding\" 或 \"RAG\"（检索增强生成）的相关信息。提供的文档主要涉及 Spring Boot、Java 集合、MySQL 基础以及一些技术概念的辨析。\n\n基于通用知识，为您简要解答：\n\n在 RAG（Retrieval-Augmented Generation，检索增强生成）架构中，**Embedding（嵌入）** 的核心作用是**将文本数据转换为向量表示**。具体功能包括：\n\n1.  **语义表示**：将非结构化的文本（如文档、问题）映射到高维向量空间，使得语义相似的文本在空间中的距离更近。\n2.  **支持向量检索**：通过将用户查询（Query）和知识库文档（Document）都转化为向量，系统可以使用向量相似度算法（如余弦相似度）来快速检索出与问题最相关的文档片段，而不是仅仅依赖关键词匹配。\n3.  **连接检索与生成**：检索到的相关上下文随后会被提供给大语言模型（LLM），帮助模型生成更准确、基于事实的回答。\n\n简而言之，Embedding 是实现 RAG 中“语义检索”关键步骤的技术基础。', '2026-05-06 14:02:50');
INSERT INTO `message` VALUES (59, 3, 'user', 'Redis 是关系型数据库吗？', '2026-05-06 14:06:15');
INSERT INTO `message` VALUES (60, 3, 'assistant', '根据文档 5.txt 中的【混淆数据】部分，Redis **不是**关系型数据库。\n\n文档中明确列出了以下辨析：\n1.  **错误说法**：\"Redis 是关系型数据库”。\n2.  **正确说法**：\"Redis 是 NoSQL 数据库”。\n\n因此，基于上传文档的信息，Redis 被归类为 NoSQL 数据库。', '2026-05-06 14:06:15');
INSERT INTO `message` VALUES (61, 4, 'user', 'Redis 是关系型数据库吗？', '2026-05-06 14:06:32');
INSERT INTO `message` VALUES (62, 4, 'assistant', '根据上传的文档 5.txt 中的【混淆数据】部分，Redis **不是**关系型数据库。\n\n文档中明确指出：\n1.  \"Redis 是关系型数据库”这一说法是**错误**的。\n2.  正确的分类是：**Redis 是 NoSQL 数据库**。', '2026-05-06 14:06:33');
INSERT INTO `message` VALUES (63, 3, 'user', '[图片 1 张] 大模型的蒸馏.png\n读取图片内容', '2026-05-06 14:14:25');
INSERT INTO `message` VALUES (64, 3, 'assistant', '该图片内容主要阐述了**大模型蒸馏（Knowledge Distillation）**的概念及其应用场景。以下是图片内容的详细分析：\n\n---\n\n### **核心内容概述**\n图片通过类比“老师”和“学生”的关系，解释了大模型蒸馏的核心思想：将一个强大但资源消耗高的“大模型”（教师模型）的知识传递给一个较小、资源需求低的“小模型”（学生模型），使小模型能够快速掌握大模型的精华部分，并在性能上接近大模型的表现。\n\n---\n\n### **关键信息提取**\n1. **背景与问题**：\n   - 大模型运行需要极高的硬件资源，通常依赖服务器集群和大量 GPU（显卡）。\n   - 为了满足低性能设备的需求，需要对大模型进行优化。\n\n2. **解决方案：蒸馏（Distillation）**：\n   - 将大模型（教师模型）的知识“浓缩”并传授给小模型（学生模型）。\n   - 小模型只需学习大模型中最精华、最重要的部分，从而在资源受限的情况下实现高效运行。\n\n3. **类比说明**：\n   - 假设有一个“超强的老师（大模型）”，他能讲解复杂的知识。\n   - 想把这些知识传授给一群“学生（小模型）”。\n   - 通过“蒸馏”方式，让学生们快速掌握老师知识的精华部分，虽然他们没有老师那样强大，但依然能表现出色。\n\n4. **目标**：\n   - 在资源有限的情况下，让小模型具备接近大模型的性能，同时降低硬件需求。\n\n---\n\n### **总结**\n图片通过生动的类比，清晰地解释了大模型蒸馏的核心思想：将复杂模型的知识压缩并传递给轻量化模型，以实现高性能与低资源消耗之间的平衡。这是一种在人工智能领域中广泛应用的技术，尤其适用于部署到边缘设备或低性能计算环境的场景。', '2026-05-06 14:14:25');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（MD5加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '昵称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2, 'aaaa', '33374c88e3a7570c211c203b4795fc24', 'aa', '2026-03-21 14:58:04');
INSERT INTO `user` VALUES (3, 'bbbb', '33374c88e3a7570c211c203b4795fc24', 'asas', '2026-04-01 15:21:22');

-- ----------------------------
-- Table structure for user_document
-- ----------------------------
DROP TABLE IF EXISTS `user_document`;
CREATE TABLE `user_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'pdf/docx/xlsx',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `status` tinyint NULL DEFAULT 1 COMMENT '1-正常 0-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_document
-- ----------------------------
INSERT INTO `user_document` VALUES (1, 3, '记录.txt', 'txt', 254, 0, '2026-04-05 17:53:49');
INSERT INTO `user_document` VALUES (2, 2, '11.md', 'md', 45, 1, '2026-04-05 17:58:15');
INSERT INTO `user_document` VALUES (3, 2, '记录.txt', 'txt', 342, 1, '2026-04-23 08:16:33');
INSERT INTO `user_document` VALUES (4, 3, '123.txt', 'txt', 30, 0, '2026-05-06 13:36:12');
INSERT INTO `user_document` VALUES (5, 3, '123.txt', 'txt', 30, 1, '2026-05-06 13:41:12');
INSERT INTO `user_document` VALUES (6, 3, '文档4.pdf', 'pdf', 27842, 1, '2026-05-06 13:56:25');
INSERT INTO `user_document` VALUES (7, 3, '文档2.md', 'md', 357, 1, '2026-05-06 13:56:25');
INSERT INTO `user_document` VALUES (8, 3, '文档5.txt', 'txt', 239, 1, '2026-05-06 13:56:25');
INSERT INTO `user_document` VALUES (9, 3, '文档3.docx', 'docx', 10559, 1, '2026-05-06 13:56:25');
INSERT INTO `user_document` VALUES (10, 3, '文档1.txt', 'txt', 508, 1, '2026-05-06 13:56:25');
INSERT INTO `user_document` VALUES (11, 3, '11.md', 'md', 3389, 0, '2026-05-06 14:07:44');

SET FOREIGN_KEY_CHECKS = 1;
