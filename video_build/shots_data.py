# -*- coding: utf-8 -*-
"""Demo video shot manifest. Shared by capture / audio / build stages.

Each shot:
  id        : sequential string
  type      : "title" (rendered card) | "screen" (captured screenshot)
  side      : admin | risk | merchant | customer | intro
  route     : SPA route (e.g. /merchant/audit) or full URL
  menu      : for risk side, the left-menu text to click
  drill     : if True, click first data row / first pending item after navigation
  file      : screenshot filename (under shots/)
  title     : short on-screen caption / subtitle heading
  narration : spoken Chinese text (used for voiceover + subtitle)
"""

SHOTS = [
    # ---------------- 开场 ----------------
    {"id":"001","type":"title","side":"intro","route":None,"menu":None,"drill":False,
     "file":"shot_001.png","title":"商城生态系统 · 完整生态支撑能力演示",
     "narration":"各位评委好，接下来我将为大家展示我们建设的商城生态系统。这套系统围绕生态合作核心理念，构建了从统一门户接入、商户入驻、商品引入、订单服务、风控稽核到合作结算的全链路闭环，并在各个环节深度融入了人工智能能力。目前系统由四个前端应用构成：生态运营管理后台、C端消费者商城、商户入驻平台，以及风控稽核管理平台，它们共享同一后端服务。"},

    # ---------------- 一、统一门户 ----------------
    {"id":"002","type":"screen","side":"admin","route":"/login","menu":None,"drill":False,
     "file":"shot_002.png","title":"统一门户 · 登录入口",
     "narration":"首先看到的是统一门户，这是我们生态系统的入口层。我来演示完整的登录流程：输入用户名和密码，点击登录。"},
    {"id":"003","type":"screen","side":"admin","route":"/portal_after_login","menu":None,"drill":False,
     "file":"shot_003.png","title":"统一门户 · 平台选择（按权限过滤的5个平台）",
     "narration":"用户登录后进入平台选择页，系统根据角色权限动态展示可访问的平台。目前已接入五个业务平台，支持 OAuth2.0、JWT、API Key 三种认证方式。今日认证超过一万两千五百次，成功率高达百分之九十九点九五。"},
    {"id":"004","type":"screen","side":"admin","route":"/portal","menu":None,"drill":False,
     "file":"shot_004.png","title":"统一门户 · 运营仪表盘",
     "narration":"回到首页仪表盘，这里实时展示生态运营的核心指标：入驻商户一万六千八百三十二家、在售商品三千两百八十六个、今日订单一万五千六百三十二笔、月度结算一千两百八十六万元。右侧待办事项与预警信息，帮助运营人员快速定位问题。"},
    {"id":"005","type":"screen","side":"admin","route":"/system/platforms","menu":None,"drill":False,
     "file":"shot_005.png","title":"统一门户 · 接入平台管理（新增平台）",
     "narration":"平台接入也是可配置的。在系统管理、接入平台管理页面，点击新增平台，填写平台名称、选择认证方式为 JWT、选择图标后保存，列表随即刷新，新平台成功接入。后续其他系统就可以通过统一的单点登录入口登录。"},

    # ---------------- 二、生态入驻和商品引入 ----------------
    {"id":"006","type":"screen","side":"admin","route":"/merchant/list","menu":None,"drill":False,
     "file":"shot_006.png","title":"生态入驻 · 商户列表（瑞幸咖啡入驻流程）",
     "narration":"下面进入生态合作的核心——商户入驻与商品引入。生态合作的第一步是商户入驻，我们提供六环节审核流程：资质审核、业务复审、合规终审、合同签署、支付进件、正式入驻。这是商户列表，可以看到各商户的入驻状态。我们以瑞幸咖啡为例，它当前正处在合规终审环节。"},
    {"id":"007","type":"screen","side":"admin","route":"/merchant/audit","menu":None,"drill":True,
     "file":"shot_007.png","title":"AI识别 · 资质审核（OCR + 风险预判）",
     "narration":"这里我要特别指出，在商户入驻的第一个环节——资质审核中，我们集成了人工智能识别能力。当商户上传营业执照后，AI 自动通过 OCR 技术识别统一社会信用代码、企业名称、法定代表人、注册资本、经营范围等字段，置信度在百分之九十六到九十九之间；同时进行有效期检查、注册资本异常检测等五项自动风险校验。"},
    {"id":"008","type":"screen","side":"admin","route":"/merchant/compliance-audit","menu":None,"drill":True,
     "file":"shot_008.png","title":"生态入驻 · 合规终审",
     "narration":"在合规终审环节，审核人员查看商户各项资质信息，点击审核通过并提交，系统会自动将商户推入下一环节——合同签署。"},
    {"id":"009","type":"screen","side":"admin","route":"/merchant/contract-audit","menu":None,"drill":True,
     "file":"shot_009.png","title":"AI质检 · 合同签署（条款风险等级）",
     "narration":"在合同签署环节，我们同样集成了 AI 合同智能质检能力。AI 自动解析合同条款，对付款、终止、质量、价格、责任、争议解决六类条款给出风险等级与修改建议。点击某条高风险条款，即可查看具体的修改建议。"},
    {"id":"010","type":"screen","side":"admin","route":"/merchant/payment-audit","menu":None,"drill":True,
     "file":"shot_010.png","title":"生态入驻 · 支付进件",
     "narration":"接着进入支付进件环节，这里展示银行信息表单，商户提交进件信息后，整个六步入驻流程即告完成，可以正式入驻我们的生态。"},

    {"id":"011","type":"screen","side":"merchant","route":"http://localhost:3002/register","menu":None,"drill":False,"click_text":"申请入驻",
     "file":"shot_011.png","title":"商户端 · 入驻申请（自助提交）",
     "narration":"入驻完成后，商户即可登录我们提供的商户入驻平台，自助提交入驻申请。这是商户端的入驻申请表单，商户填写企业名称、商户类型、统一社会信用代码、联系人、银行账号等信息后提交，系统即收到入驻申请并进入后台审核流程。"},

    {"id":"012","type":"screen","side":"admin","route":"/product/audit","menu":None,"drill":True,
     "file":"shot_012.png","title":"AI审核 · 商品审核（14维度评分）",
     "narration":"回到运营管理后台，商户提交的商品进入商品审核流程。这里再次用到 AI 能力——AI 智能审核，对商品的十四个维度进行自动化评分，包括名称合规性、售价合理性、图片检查、详情完整性等。我点开一个评分较低的待审商品，查看 AI 逐项审核意见。"},
    {"id":"013","type":"screen","side":"admin","route":"/product/list","menu":None,"drill":False,
     "file":"shot_013.png","title":"商品管理 · 商品列表（已上架）",
     "narration":"审核通过后，商品状态自动从待审核同步为已上架，无需额外操作。回到商品列表，可以确认刚才审核通过的商品已经正常上架。"},
    {"id":"014","type":"screen","side":"admin","route":"/product/benefit","menu":None,"drill":True,
     "file":"shot_014.png","title":"数字权益 · 权益引入（商品子分类）",
     "narration":"在商品管理中，还有一类特殊商品——数字权益，这是生态合作中很关键的能力。权益引入入口在商品管理菜单下，作为商品的一个子分类管理，支持卡密直连、API 兑换、直连发券、积分兑换四种类型。我点击新增权益，填写商户、权益名称、类型、成本价与有效期后提交，列表随即刷新。所以权益引入并不是独立业务线，而是商品管理体系下的一个分类，与实物商品共享审核、上下架、库存与结算能力。"},

    {"id":"015","type":"screen","side":"customer","route":"http://localhost:3000/","menu":None,"drill":False,
     "file":"shot_015.png","title":"C端商城 · 首页与商品列表",
     "narration":"商品上架后，消费者在 C 端商城就能看到了。这是 C 端首页，展示轮播图与商品列表。"},
    {"id":"016","type":"screen","side":"customer","route":"http://localhost:3000/#/product","menu":None,"drill":True,
     "file":"shot_016.png","title":"C端商城 · 商品详情与加入购物车",
     "narration":"我点击一个商品进入详情页，查看商品详情，再点击加入购物车，可以看到导航栏购物车角标数字加一，购物车里已经出现了这件商品。"},

    # ---------------- 三、服务能力 ----------------
    {"id":"017","type":"screen","side":"admin","route":"/order/evaluation","menu":None,"drill":False,
     "file":"shot_017.png","title":"服务能力 · 五维度订单评价体系",
     "narration":"回到运营管理后台，我们构建了五维度订单评价体系：商品质量四点六五分、配送速度四点五二分、客服服务四点七一分、售后体验四点四三分、性价比四点五八分，综合评分四点六二，好评率百分之九十四点三。"},
    {"id":"018","type":"screen","side":"admin","route":"/order/evaluation","menu":None,"drill":True,
     "file":"shot_018.png","title":"服务能力 · 评价闭环与AI审核",
     "narration":"评价体系不是孤立展示，它驱动了一个闭环机制：评价采集、AI 加人工双轨内容审核、考核互通、运营调整，最终提升服务质量。在内容审核环节，AI 自动处理百分之八十七点三的审核，可疑内容人工复核，明确违规内容自动屏蔽。我点开一条评价，可以看到 AI 情感分析结果，并模拟审核操作，选择通过并提交。"},

    # ---------------- 四、风控稽核 ----------------
    {"id":"019","type":"screen","side":"risk","route":"http://localhost:3001/#/dashboard","menu":None,"drill":False,
     "file":"shot_019.png","title":"风控稽核 · 风控看板",
     "narration":"现在切换到风控稽核管理平台，这是通过统一门户单点登录的独立应用。首先看风控看板，实时展示事件总数、拦截率、风险商户数、名单命中数等核心指标。"},
    {"id":"020","type":"screen","side":"risk","route":"http://localhost:3001/#/rules","menu":"规则管理","drill":True,
     "file":"shot_020.png","title":"风控稽核 · 规则管理（新增规则）",
     "narration":"先看规则管理。我们支持条件规则、脚本规则、频率规则、关联规则四种类型，每条规则可配置优先级、触发条件和处置动作。我来新增一条规则：新注册用户大额下单检测，类型条件规则、场景下单风控、优先级 P3、处置人工审核，保存后列表刷新，新规则出现。"},
    {"id":"021","type":"screen","side":"risk","route":"http://localhost:3001/#/blacklist","menu":"名单库","drill":True,
     "file":"shot_021.png","title":"风控稽核 · 名单库（添加名单）",
     "narration":"再看名单库，管理黑名单、白名单、灰名单，覆盖手机号、IP、设备指纹、商户编号、信用代码等数据类型。我选择类型黑名单、数据类型手机号、填写号码后确认添加，即完成一条名单的录入。"},
    {"id":"022","type":"screen","side":"risk","route":"http://localhost:3001/#/events","menu":"风控事件","drill":False,
     "file":"shot_022.png","title":"风控稽核 · 风控事件（拦截/放行）",
     "narration":"接着看风控事件。当规则被触发时生成事件，运营人员可执行拦截、放行或转人工审核。我对一条待处理事件点击拦截并确认，状态变为已拦截；再对另一条点击放行，状态变为已放行。"},
    {"id":"023","type":"screen","side":"risk","route":"http://localhost:3001/#/disposition","menu":"处置管理","drill":False,
     "file":"shot_023.png","title":"风控稽核 · 处置管理（标准化方案+日志）",
     "narration":"处置管理定义了标准化处置方案，每项方案关联触发规则和执行动作，并有执行日志可追溯，保证每一次处置都有据可查。"},

    # ---------------- 五、结算能力 ----------------
    {"id":"024","type":"screen","side":"admin","route":"/finance/settlement","menu":None,"drill":False,
     "file":"shot_024.png","title":"结算能力 · 三类结算总览",
     "narration":"接下来是合作伙伴最关心的结算。结算管理支撑三类结算并行：AI 豆结算，月度三百二十六点五万；平台佣金结算，月度七百八十九点二万；商拓服务费结算，月度一百七十点八万。这里逐一展示结算概览、三个结算类型标签页，以及 AI 豆收支趋势图。"},
    {"id":"025","type":"screen","side":"admin","route":"/finance/settlement","menu":None,"drill":False,"tab":"结算规则","add_btn":"新增规则",
     "file":"shot_025.png","title":"结算能力 · 结算规则配置（新增佣金规则）",
     "narration":"结算规则可按商户灵活配置。我切换到结算规则标签页，为数码旗舰店设置一条新的佣金规则：佣金比例百分之八、结算周期月结、最低结算额一千元，保存后规则列表刷新。"},
    {"id":"026","type":"screen","side":"admin","route":"/finance/settlement","menu":None,"drill":False,"detail":True,
     "file":"shot_026.png","title":"结算能力 · 结算记录审批",
     "narration":"然后看结算记录。数码旗舰店七月有一笔待审批的佣金结算单，我点击详情查看，再点击审批、选择通过并确认，记录状态变为已通过，触发生成结算单。结算全流程八个节点，由 Seata 分布式事务、RocketMQ 消息队列、ClickHouse 实时分析共同保障大促高峰期的结算准确。"},

    # ---------------- 六、AI能力管理 ----------------
    {"id":"027","type":"screen","side":"admin","route":"/ai","menu":None,"drill":False,
     "file":"shot_027.png","title":"AI能力管理 · 六项AI能力矩阵",
     "narration":"在我们的生态系统中，AI 不是独立的外挂功能，而是深度嵌入每个业务流程的核心能力。系统提供统一的 AI 加应用模块，集中管理六项 AI 能力。前面演示中已经实际用到了其中三项：商户入驻智能识别、合同智能质检、商品 AI 智能审核。"},
    {"id":"028","type":"screen","side":"admin","route":"/ai","menu":None,"drill":False,"card":"信息高效录入",
     "file":"shot_028.png","title":"AI能力 · 信息高效录入",
     "narration":"下面四个能力在前面流程中没有直接触发，我来快速演示效果。第一，信息高效录入：AI 辅助商品信息补全与批量导入，录入效率提升六倍。点击开始识别，即可显示补全结果。"},
    {"id":"029","type":"screen","side":"admin","route":"/ai","menu":None,"drill":False,"card":"文案智能校对",
     "file":"shot_029.png","title":"AI能力 · 文案智能校对",
     "narration":"第二，文案智能校对：检测违禁词与绝对化表述。比如全网最低价、百分之百正品保证，AI 会标记最低价和百分之百为违规，并给出修改建议。"},
    {"id":"030","type":"screen","side":"admin","route":"/ai","menu":None,"drill":False,"card":"价格智能摸排",
     "file":"shot_030.png","title":"AI能力 · 价格智能摸排",
     "narration":"第三，价格智能摸排：自动采集京东、天猫、拼多多、淘宝、抖音五平台价格，进行多维度比对并给出定价建议。点击开始摸排，即可看到五平台价格对比表。"},
    {"id":"031","type":"screen","side":"admin","route":"/ai","menu":None,"drill":False,"card":"商品卖点提炼",
     "file":"shot_031.png","title":"AI能力 · 商品卖点提炼",
     "narration":"第四，商品卖点提炼：大模型自动生成核心卖点、营销文案、短标题与社交分享文案。点击生成，即可看到四种类型的 AI 生成内容。"},

    # ---------------- 七、总结 ----------------
    {"id":"032","type":"title","side":"intro","route":None,"menu":None,"drill":False,
     "file":"shot_032.png","title":"总结 · 六项核心能力全景",
     "narration":"总结一下，我们构建的商城生态系统完整覆盖了六项核心能力：第一，统一门户，多平台统一接入、单点登录，五个业务平台统一管理，认证成功率百分之九十九点九五；第二，生态入驻与商品引入，商户六步入驻审核、商品三步向导式入驻并含 AI 智能审核，数字权益作为商品分类统一管理；第三，服务能力，五维度评价体系与 AI 加人工双轨审核，评价驱动商户考核闭环；第四，风控稽核，独立平台、规则可配置、名单可添加、事件可拦截放行；第五，结算能力，三类结算并行、规则可配置、记录可审批，并由 Seata、RocketMQ、ClickHouse 技术保障；第六，AI 能力管理，六项 AI 能力深度嵌入业务全流程，提供统一的能力管理入口。这套系统从架构到体验都贯彻了生态合作理念，不同参与方使用专属应用端，共享统一业务中台与数据底座。感谢各位评委！"},
]
