package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.dao.MallOrderItemMapper;
import com.igou.mall.dao.EvaluationMapper;
import com.igou.mall.dao.CustomerMapper;
import com.igou.mall.dao.CustomerAddressMapper;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.Product;
import com.igou.mall.model.entity.MallOrder;
import com.igou.mall.model.entity.MallOrderItem;
import com.igou.mall.model.entity.Evaluation;
import com.igou.mall.model.entity.Customer;
import com.igou.mall.model.entity.CustomerAddress;
import com.igou.mall.dao.BannerMapper;
import com.igou.mall.dao.HomeConfigMapper;
import com.igou.mall.dao.ShoppingCartMapper;
import com.igou.mall.dao.ProductCategoryMapper;
import com.igou.mall.model.entity.Banner;
import com.igou.mall.model.entity.HomeConfig;
import com.igou.mall.model.entity.ProductCategory;
import com.igou.mall.model.entity.ShoppingCart;
import com.igou.mall.config.CustomerAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/c-mall")
@CrossOrigin(origins = "*")
public class CMallController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerAddressMapper customerAddressMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private HomeConfigMapper homeConfigMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${mall.shipping.free-threshold:99}")
    private BigDecimal freeShippingThreshold;

    /**
     * 从请求中获取已认证的用户ID（由 CustomerAuthFilter 设置）
     */
    private Long getAuthCustomerId(HttpServletRequest request) {
        Object attr = request.getAttribute(CustomerAuthFilter.ATTR_CUSTOMER_ID);
        return attr != null ? (Long) attr : null;
    }

    private String firstImageUrl(String imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return "";
        String[] parts = imageUrls.split(",");
        return parts[0].trim();
    }

    @GetMapping("/products")
    public Result<Map<String, Object>> getProducts(@RequestParam(defaultValue = "") String category,
                                                    @RequestParam(defaultValue = "") String keyword,
                                                    @RequestParam(defaultValue = "ON_SHELF") String status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        List<Product> allProducts = productMapper.findAll(0, Integer.MAX_VALUE);

        // 按状态、分类、关键词筛选
        List<Product> filtered = allProducts.stream()
                .filter(p -> status == null || status.isEmpty() || status.equals(p.getStatus()))
                .filter(p -> category == null || category.isEmpty() || category.equals(p.getCategory()))
                .filter(p -> {
                    if (keyword == null || keyword.isEmpty()) return true;
                    String kw = keyword.toLowerCase();
                    String name = p.getProductName() != null ? p.getProductName().toLowerCase() : "";
                    String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                    return name.contains(kw) || desc.contains(kw);
                })
                .collect(Collectors.toList());

        int total = filtered.size();

        // 分页
        int fromIndex = page * size;
        if (fromIndex >= total) {
            filtered = new ArrayList<>();
        } else {
            int toIndex = Math.min(fromIndex + size, total);
            filtered = filtered.subList(fromIndex, toIndex);
        }

        List<Map<String, Object>> list = filtered.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getProductName());
            item.put("picture", firstImageUrl(p.getImageUrls()));
            item.put("price", p.getPrice());
            item.put("desc", p.getDescription());
            item.put("category", p.getCategory());
            item.put("brand", p.getBrand());
            item.put("salesCount", p.getSalesCount());
            item.put("stock", p.getStock());
            item.put("productType", p.getProductType());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/products/{id}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        Merchant merchant = merchantMapper.findById(product.getMerchantId());

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getProductName());
        result.put("desc", product.getDescription());
        result.put("detail", product.getDetail());
        result.put("price", product.getPrice());
        result.put("oldPrice", product.getMarketPrice());
        result.put("salesCount", product.getSalesCount());
        result.put("commentCount", 0);
        result.put("collectCount", 0);

        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(Map.of("id", product.getCategoryId(), "name", product.getCategory()));
        categories.add(Map.of("id", product.getCategoryId(), "name", product.getCategory()));
        result.put("categories", categories);

        List<String> mainPictures = new ArrayList<>();
        if (product.getImageUrls() != null) {
            for (String url : product.getImageUrls().split(",")) {
                mainPictures.add(url.trim());
            }
        }
        result.put("mainPictures", mainPictures);

        Map<String, Object> brand = new HashMap<>();
        brand.put("name", product.getBrand());
        result.put("brand", brand);

        // 商户信息
        if (merchant != null) {
            Map<String, Object> merchantInfo = new HashMap<>();
            merchantInfo.put("id", merchant.getId());
            merchantInfo.put("name", merchant.getMerchantName());
            result.put("merchant", merchantInfo);
        }

        Map<String, Object> details = new HashMap<>();
        List<Map<String, Object>> properties = new ArrayList<>();
        properties.add(Map.of("name", "品牌", "values", product.getBrand()));
        properties.add(Map.of("name", "分类", "values", product.getCategory()));
        properties.add(Map.of("name", "规格", "values", product.getSpec() != null ? product.getSpec() : "默认"));
        details.put("properties", properties);

        List<String> pictures = new ArrayList<>();
        if (product.getImageUrls() != null) {
            for (String url : product.getImageUrls().split(",")) {
                pictures.add(url.trim());
            }
        }
        details.put("pictures", pictures);
        result.put("details", details);

        return Result.success(result);
    }

    @PostMapping("/orders")
    @Transactional
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.error("用户不存在");
        }
        Long addressId = body.get("addressId") != null ? Long.valueOf(body.get("addressId").toString()) : null;
        String paymentMethod = (String) body.getOrDefault("paymentMethod", "wechat");
        String customerPhone = customer.getPhone();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Map<String, Object>> orderItems = new ArrayList<>();
        StringBuilder productNames = new StringBuilder();
        Long firstMerchantId = null;
        Long firstProductId = null;
        BigDecimal firstPrice = null;
        String firstImage = null;
        String rechargePhone = null;
        boolean isDigitalGoods = false;

        for (Map<String, Object> item : items) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            Integer count = Integer.valueOf(item.get("count").toString());
            String itemRechargePhone = (String) item.getOrDefault("rechargePhone", "");
            Boolean itemIsDigital = Boolean.TRUE.equals(item.get("isDigital"));

            if (itemIsDigital && !itemRechargePhone.isEmpty()) {
                rechargePhone = itemRechargePhone;
                isDigitalGoods = true;
            }

            Product product = productMapper.findById(skuId);
            if (product != null) {
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(count));
                totalAmount = totalAmount.add(itemTotal);

                Map<String, Object> oi = new HashMap<>();
                oi.put("productId", product.getId());
                oi.put("productName", product.getProductName());
                oi.put("price", product.getPrice());
                oi.put("quantity", count);
                oi.put("imageUrl", firstImageUrl(product.getImageUrls()));
                if (itemIsDigital) {
                    oi.put("rechargePhone", itemRechargePhone);
                    oi.put("isDigital", true);
                }
                orderItems.add(oi);

                if (productNames.length() > 0) productNames.append(", ");
                productNames.append(product.getProductName());

                if (firstMerchantId == null) {
                    firstMerchantId = product.getMerchantId();
                    firstProductId = product.getId();
                    firstPrice = product.getPrice();
                    firstImage = firstImageUrl(product.getImageUrls());
                }
            }
        }

        BigDecimal shippingFee = totalAmount.compareTo(freeShippingThreshold) >= 0 ? BigDecimal.ZERO : new BigDecimal("10");
        BigDecimal finalAmount = totalAmount.add(shippingFee);

        String orderCode = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%04d", new Random().nextInt(10000));

        MallOrder order = new MallOrder();
        order.setOrderCode(orderCode);
        order.setCustomerPhone(customerPhone);
        order.setMerchantId(firstMerchantId);
        order.setProductId(firstProductId);
        order.setProductName(productNames.toString());
        order.setProductImage(firstImage);
        order.setPrice(firstPrice);
        order.setQuantity(1);
        order.setOrderAmount(totalAmount);
        order.setAiDouDeduct(BigDecimal.ZERO);
        order.setPayAmount(finalAmount);
        order.setDiscountAmount(shippingFee.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ZERO : shippingFee);
        order.setStatus("CREATED");
        order.setPayMethod(paymentMethod);
        order.setDeliveryAddressId(addressId);
        if (isDigitalGoods && rechargePhone != null) {
            order.setRemark("充值手机号:" + rechargePhone);
        }
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        mallOrderMapper.insert(order);

        for (Map<String, Object> item : items) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            Integer count = Integer.valueOf(item.get("count").toString());
            Product product = productMapper.findById(skuId);
            if (product != null) {
                MallOrderItem orderItem = new MallOrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getProductName());
                orderItem.setProductImage(firstImageUrl(product.getImageUrls()));
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(count);
                orderItem.setItemAmount(product.getPrice().multiply(BigDecimal.valueOf(count)));
                mallOrderItemMapper.insert(orderItem);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderCode", order.getOrderCode());
        result.put("payAmount", finalAmount);
        result.put("totalAmount", totalAmount);
        result.put("shippingFee", shippingFee);
        result.put("items", orderItems);
        if (isDigitalGoods) {
            result.put("isDigital", true);
            result.put("rechargePhone", rechargePhone);
        }
        return Result.success(result);
    }

    @PostMapping("/orders/{id}/pay")
    public Result<String> payOrder(@PathVariable Long id) {
        mallOrderMapper.updateStatus(id, "PAID");
        return Result.success("支付成功");
    }

    @PutMapping("/orders/{id}/cancel")
    public Result<String> cancelOrder(@PathVariable Long id) {
        MallOrder order = mallOrderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        mallOrderMapper.updateStatus(id, "CANCELLED");
        return Result.success("取消成功");
    }

    @PutMapping("/orders/{id}/receive")
    public Result<String> receiveOrder(@PathVariable Long id) {
        MallOrder order = mallOrderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        mallOrderMapper.updateStatus(id, "COMPLETED");
        return Result.success("确认收货成功");
    }

    @DeleteMapping("/orders/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        MallOrder order = mallOrderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        mallOrderMapper.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/orders")
    public Result<List<Map<String, Object>>> getUserOrders(HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.success(new ArrayList<>());
        }
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.success(new ArrayList<>());
        }
        List<MallOrder> orders = mallOrderMapper.findByCustomerPhone(customer.getPhone());

        List<Map<String, Object>> result = new ArrayList<>();
        for (MallOrder order : orders) {
            Map<String, Object> o = new HashMap<>();
            o.put("id", order.getId());
            o.put("orderCode", order.getOrderCode());
            o.put("productName", order.getProductName());
            o.put("productImage", order.getProductImage());
            o.put("price", order.getPrice());
            o.put("quantity", order.getQuantity());
            o.put("orderAmount", order.getOrderAmount());
            o.put("payAmount", order.getPayAmount());
            o.put("status", order.getStatus());
            o.put("payMethod", order.getPayMethod());
            o.put("customerPhone", order.getCustomerPhone());
            o.put("remark", order.getRemark());
            o.put("createTime", order.getCreateTime());
            o.put("updateTime", order.getUpdateTime());

            List<MallOrderItem> items = mallOrderItemMapper.findByOrderId(order.getId());
            List<Map<String, Object>> itemMaps = new ArrayList<>();
            for (MallOrderItem item : items) {
                Map<String, Object> im = new HashMap<>();
                im.put("productId", item.getProductId());
                im.put("productName", item.getProductName());
                im.put("productImage", item.getProductImage());
                im.put("imageUrl", item.getProductImage());
                im.put("price", item.getPrice());
                im.put("quantity", item.getQuantity());
                im.put("itemAmount", item.getItemAmount());
                itemMaps.add(im);
            }
            o.put("items", itemMaps);
            result.add(o);
        }
        return Result.success(result);
    }

    @GetMapping("/orders/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        MallOrder order = mallOrderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderCode", order.getOrderCode());
        result.put("productName", order.getProductName());
        result.put("productImage", order.getProductImage());
        result.put("price", order.getPrice());
        result.put("quantity", order.getQuantity());
        result.put("orderAmount", order.getOrderAmount());
        result.put("payAmount", order.getPayAmount());
        result.put("status", order.getStatus());
        result.put("payMethod", order.getPayMethod());
        result.put("customerPhone", order.getCustomerPhone());
        result.put("remark", order.getRemark());
        result.put("createTime", order.getCreateTime());
        result.put("updateTime", order.getUpdateTime());

        List<MallOrderItem> items = mallOrderItemMapper.findByOrderId(order.getId());
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (MallOrderItem item : items) {
            Map<String, Object> im = new HashMap<>();
            im.put("productId", item.getProductId());
            im.put("productName", item.getProductName());
            im.put("productImage", item.getProductImage());
            im.put("price", item.getPrice());
            im.put("quantity", item.getQuantity());
            im.put("itemAmount", item.getItemAmount());
            itemMaps.add(im);
        }
        result.put("items", itemMaps);

        return Result.success(result);
    }

    @PostMapping("/evaluations")
    public Result<String> createEvaluation(@RequestBody Map<String, Object> evalData) {
        Long orderId = Long.valueOf(evalData.get("orderId").toString());

        MallOrder order = mallOrderMapper.findById(orderId);

        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(orderId);
        evaluation.setMerchantId(order.getMerchantId());
        evaluation.setProductId(order.getProductId());
        evaluation.setUserPhone(order.getCustomerPhone());
        evaluation.setScoreQuality(Integer.valueOf(evalData.get("scoreQuality").toString()));
        evaluation.setScoreDelivery(Integer.valueOf(evalData.get("scoreDelivery").toString()));
        evaluation.setScoreService(Integer.valueOf(evalData.get("scoreService").toString()));
        evaluation.setScoreAftersale(Integer.valueOf(evalData.get("scoreAftersale").toString()));
        evaluation.setScoreValue(Integer.valueOf(evalData.get("scoreValue").toString()));
        evaluation.setContent(evalData.get("content").toString());
        evaluation.setTags(evalData.getOrDefault("tags", "").toString());
        evaluation.setSentiment("POSITIVE");
        evaluation.setAiStatus("AUTO_PASS");

        evaluationMapper.insert(evaluation);
        mallOrderMapper.updateStatus(orderId, "EVALUATED");

        return Result.success("评价成功");
    }

    @GetMapping("/products/{id}/evaluations")
    public Result<List<Evaluation>> getProductEvaluations(@PathVariable Long id) {
        return Result.success(evaluationMapper.findAll(0, 100));
    }

    @PostMapping("/auth/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> params) {
        String phone = (String) params.get("phone");
        String password = (String) params.get("password");

        Customer existing = customerMapper.findByPhone(phone);
        if (existing != null) {
            return Result.error("手机号已注册");
        }

        Customer customer = new Customer();
        customer.setPhone(phone);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setNickname((String) params.getOrDefault("nickname", "用户" + phone.substring(7)));
        customer.setVipLevel("NORMAL");
        customer.setStatus("ACTIVE");
        customer.setRegisterTime(LocalDateTime.now());

        customerMapper.insert(customer);

        Map<String, Object> result = new HashMap<>();
        result.put("customer", customer);
        result.put("token", "CUST_" + customer.getId() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        return Result.success(result);
    }

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        String phone = (String) params.getOrDefault("phone", params.get("account"));
        String password = (String) params.get("password");

        Customer customer = customerMapper.findByPhone(phone);
        if (customer == null) {
            return Result.error(400, "用户不存在");
        }

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            return Result.error(400, "密码错误");
        }

        customer.setLastLoginTime(LocalDateTime.now());
        customerMapper.update(customer);

        Map<String, Object> result = new HashMap<>();
        result.put("token", "CUST_" + customer.getId() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        result.put("id", customer.getId());
        result.put("phone", customer.getPhone());
        result.put("nickname", customer.getNickname());
        result.put("vipLevel", customer.getVipLevel());

        return Result.success(result);
    }

    @GetMapping("/auth/user-info")
    public Result<Customer> getUserInfo(HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            Customer customer = new Customer();
            customer.setId(1L);
            customer.setPhone("13800138000");
            customer.setNickname("游客");
            customer.setVipLevel("NORMAL");
            return Result.success(customer);
        }
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.error("用户不存在");
        }
        customer.setPassword(null); // 安全：不返回密码
        return Result.success(customer);
    }

    // ========== 分类树（来自B端product_category表）==========
    @GetMapping("/product-categories/tree")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        List<ProductCategory> all = productCategoryMapper.findAll();
        // 构建二级树：level=1 作为父级，level=2 作为子级
        Map<Long, Map<String, Object>> parentCache = new LinkedHashMap<>();
        List<Map<String, Object>> tree = new ArrayList<>();

        for (ProductCategory pc : all) {
            if (pc.getLevel() == 1) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", pc.getId());
                node.put("name", pc.getCategoryName());
                node.put("categoryCode", pc.getCategoryCode());
                node.put("children", new ArrayList<>());
                parentCache.put(pc.getId(), node);
                tree.add(node);
            }
        }
        for (ProductCategory pc : all) {
            if (pc.getLevel() == 2 && pc.getParentId() != null) {
                Map<String, Object> parent = parentCache.get(pc.getParentId());
                if (parent != null) {
                    Map<String, Object> child = new HashMap<>();
                    child.put("id", pc.getId());
                    child.put("name", pc.getCategoryName());
                    child.put("categoryCode", pc.getCategoryCode());
                    child.put("parentId", pc.getParentId());
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(child);
                }
            }
        }
        return Result.success(tree);
    }

    @GetMapping("/products/categories")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();

        List<Product> allProducts = productMapper.findAll(0, 100);

        Map<String, List<Product>> productsByCategory = new LinkedHashMap<>();
        for (Product p : allProducts) {
            String category = p.getCategory();
            productsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(p);
        }

        long catId = 1;
        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", catId);
            category.put("name", entry.getKey());

            List<Map<String, Object>> children = new ArrayList<>();
            children.add(Map.of("id", 1, "name", "全部" + entry.getKey()));
            children.add(Map.of("id", 2, "name", "热门" + entry.getKey()));
            category.put("children", children);

            List<Map<String, Object>> goods = new ArrayList<>();
            for (Product p : entry.getValue().subList(0, Math.min(6, entry.getValue().size()))) {
                Map<String, Object> good = new HashMap<>();
                good.put("id", p.getId());
                good.put("name", p.getProductName());
                good.put("desc", p.getDescription());
                good.put("price", p.getPrice());
                good.put("picture", Collections.singletonList(firstImageUrl(p.getImageUrls())));
                good.put("productType", p.getProductType());
                goods.add(good);
            }
            category.put("goods", goods);

            categories.add(category);
            catId++;
        }

        return Result.success(categories);
    }

    @GetMapping("/products/new")
    public Result<List<Map<String, Object>>> getNewProducts() {
        List<Product> products = productMapper.findAll(0, 50);
        // 按创建时间降序，最新在前
        products.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        products = products.subList(0, Math.min(10, products.size()));
        return Result.success(productListToResult(products));
    }

    @GetMapping("/products/hot")
    public Result<List<Map<String, Object>>> getHotProducts() {
        List<Product> products = productMapper.findAll(0, 50);
        // 按销量降序，最热在前
        products.sort((a, b) -> Integer.compare(b.getSalesCount(), a.getSalesCount()));
        products = products.subList(0, Math.min(10, products.size()));
        return Result.success(productListToResult(products));
    }

    private List<Map<String, Object>> productListToResult(List<Product> products) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getProductName());
            item.put("picture", Collections.singletonList(firstImageUrl(p.getImageUrls())));
            item.put("price", p.getPrice());
            item.put("title", p.getProductName());
            item.put("alt", p.getDescription());
        item.put("productType", p.getProductType());
        result.add(item);
        }
        return result;
    }

    @GetMapping("/products/category/{categoryId}")
    public Result<List<Product>> getProductsByCategory(@PathVariable Long categoryId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.findByCategoryId(categoryId, page * size, size));
    }

    @GetMapping("/checkout/info")
    public Result<Map<String, Object>> getCheckoutInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("userAddresses", new ArrayList<>());
        info.put("couponList", new ArrayList<>());
        info.put("userPoints", 0);
        return Result.success(info);
    }

    @GetMapping("/address")
    public Result<List<Map<String, Object>>> getAddresses(HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.success(new ArrayList<>());
        }
        List<CustomerAddress> addresses = customerAddressMapper.findByCustomerId(customerId);
        List<Map<String, Object>> result = addresses.stream().map(addr -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", addr.getId());
            m.put("name", addr.getReceiverName());
            m.put("phone", addr.getReceiverPhone());
            m.put("province", addr.getProvince());
            m.put("city", addr.getCity());
            m.put("district", addr.getDistrict());
            m.put("address", addr.getDetailAddress());
            m.put("isDefault", addr.getIsDefault());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/address/{id}")
    public Result<Map<String, Object>> getAddress(@PathVariable Long id, HttpServletRequest request) {
        CustomerAddress addr = customerAddressMapper.findById(id);
        if (addr == null) {
            return Result.error("地址不存在");
        }
        // 验证地址归属
        Long customerId = getAuthCustomerId(request);
        if (customerId == null || !customerId.equals(addr.getCustomerId())) {
            return Result.error("无权访问该地址");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", addr.getId());
        m.put("name", addr.getReceiverName());
        m.put("phone", addr.getReceiverPhone());
        m.put("province", addr.getProvince());
        m.put("city", addr.getCity());
        m.put("district", addr.getDistrict());
        m.put("address", addr.getDetailAddress());
        m.put("isDefault", addr.getIsDefault());
        return Result.success(m);
    }

    @PostMapping("/address")
    public Result<Map<String, Object>> createAddress(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        CustomerAddress addr = new CustomerAddress();
        addr.setCustomerId(customerId);
        addr.setReceiverName((String) body.get("name"));
        addr.setReceiverPhone((String) body.get("phone"));
        addr.setProvince((String) body.get("province"));
        addr.setCity((String) body.get("city"));
        addr.setDistrict((String) body.get("district"));
        addr.setDetailAddress((String) body.get("address"));
        addr.setIsDefault(body.get("isDefault") != null ? Integer.valueOf(body.get("isDefault").toString()) : 0);
        addr.setCreateTime(LocalDateTime.now());
        addr.setUpdateTime(LocalDateTime.now());

        customerAddressMapper.insert(addr);

        Map<String, Object> result = new HashMap<>();
        result.put("id", addr.getId());
        result.put("name", addr.getReceiverName());
        result.put("phone", addr.getReceiverPhone());
        result.put("province", addr.getProvince());
        result.put("city", addr.getCity());
        result.put("district", addr.getDistrict());
        result.put("address", addr.getDetailAddress());
        result.put("isDefault", addr.getIsDefault());
        return Result.success(result);
    }

    @PutMapping("/address/{id}")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        CustomerAddress addr = customerAddressMapper.findById(id);
        if (addr == null) {
            return Result.error("地址不存在");
        }
        // 验证地址归属
        Long customerId = getAuthCustomerId(request);
        if (customerId == null || !customerId.equals(addr.getCustomerId())) {
            return Result.error("无权修改该地址");
        }
        if (body.get("name") != null) addr.setReceiverName((String) body.get("name"));
        if (body.get("phone") != null) addr.setReceiverPhone((String) body.get("phone"));
        if (body.get("province") != null) addr.setProvince((String) body.get("province"));
        if (body.get("city") != null) addr.setCity((String) body.get("city"));
        if (body.get("district") != null) addr.setDistrict((String) body.get("district"));
        if (body.get("address") != null) addr.setDetailAddress((String) body.get("address"));
        if (body.get("isDefault") != null) addr.setIsDefault(Integer.valueOf(body.get("isDefault").toString()));
        addr.setUpdateTime(LocalDateTime.now());

        customerAddressMapper.update(addr);
        return Result.success("更新成功");
    }

    @DeleteMapping("/address/{id}")
    public Result<String> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        CustomerAddress addr = customerAddressMapper.findById(id);
        if (addr == null) {
            return Result.error("地址不存在");
        }
        // 验证地址归属
        Long customerId = getAuthCustomerId(request);
        if (customerId == null || !customerId.equals(addr.getCustomerId())) {
            return Result.error("无权删除该地址");
        }
        customerAddressMapper.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/cart")
    public Result<List<Map<String, Object>>> getCart(HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.success(new ArrayList<>());
        }
        List<ShoppingCart> cartItems = shoppingCartMapper.findByCustomerId(customerId);
        List<Map<String, Object>> result = cartItems.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("skuId", item.getProductId());
            m.put("name", item.getProductName());
            m.put("picture", item.getProductImage());
            m.put("price", item.getProductPrice());
            m.put("count", item.getQuantity());
            m.put("selected", item.getSelected() != null && item.getSelected() == 1);
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/cart")
    public Result<Map<String, Object>> addCart(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        Long skuId = Long.valueOf(data.get("skuId").toString());
        Integer count = data.get("count") != null ? Integer.valueOf(data.get("count").toString()) : 1;

        Product product = productMapper.findById(skuId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        ShoppingCart existing = shoppingCartMapper.findByCustomerIdAndProductId(customerId, skuId);
        if (existing != null) {
            int newQty = existing.getQuantity() + count;
            existing.setQuantity(Math.max(1, newQty));
            existing.setUpdateTime(new Date());
            shoppingCartMapper.update(existing);
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setCustomerId(customerId);
            cart.setProductId(skuId);
            cart.setProductName(product.getProductName());
            cart.setProductImage(firstImageUrl(product.getImageUrls()));
            cart.setProductPrice(product.getPrice());
            cart.setQuantity(Math.max(1, count));
            cart.setSelected(1);
            cart.setCreateTime(new Date());
            cart.setUpdateTime(new Date());
            shoppingCartMapper.insert(cart);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getProductName());
        result.put("picture", Collections.singletonList(firstImageUrl(product.getImageUrls())));
        result.put("price", product.getPrice());
        result.put("skuId", skuId);
        result.put("count", Math.max(1, count));
        result.put("selected", true);

        return Result.success(result);
    }

    @PutMapping("/cart/{id}")
    public Result<Map<String, Object>> updateCart(@PathVariable Long id, @RequestBody Map<String, Object> data, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        Integer count = data.get("count") != null ? Integer.valueOf(data.get("count").toString()) : null;
        Boolean selected = data.get("selected") != null ? Boolean.valueOf(data.get("selected").toString()) : null;

        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        ShoppingCart existing = shoppingCartMapper.findByCustomerIdAndProductId(customerId, id);
        if (existing != null) {
            if (count != null) existing.setQuantity(Math.max(1, count));
            if (selected != null) existing.setSelected(selected ? 1 : 0);
            existing.setUpdateTime(new Date());
            shoppingCartMapper.update(existing);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getProductName());
        result.put("picture", Collections.singletonList(firstImageUrl(product.getImageUrls())));
        result.put("price", product.getPrice());
        result.put("skuId", id);
        result.put("count", count != null ? Math.max(1, count) : 1);
        result.put("selected", selected != null ? selected : true);

        return Result.success(result);
    }

    @DeleteMapping("/cart")
    public Result<String> deleteCart(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");
        if (ids != null) {
            for (Integer id : ids) {
                shoppingCartMapper.delete(id.longValue());
            }
        }
        Long productId = data.get("productId") != null ? Long.valueOf(data.get("productId").toString()) : null;
        if (productId != null) {
            ShoppingCart existing = shoppingCartMapper.findByCustomerIdAndProductId(customerId, productId);
            if (existing != null) {
                shoppingCartMapper.delete(existing.getId());
            }
        }
        return Result.success("删除成功");
    }

    @PostMapping("/cart/merge")
    public Result<List<Map<String, Object>>> mergeCart(@RequestBody List<Map<String, Object>> data, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.success(new ArrayList<>());
        }
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> item : data) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            Integer count = item.get("count") != null ? Integer.valueOf(item.get("count").toString()) : 1;
            Boolean selected = item.get("selected") != null ? Boolean.valueOf(item.get("selected").toString()) : true;

            Product product = productMapper.findById(skuId);
            if (product == null) continue;

            // 持久化到数据库
            if (customerId != null) {
                ShoppingCart existing = shoppingCartMapper.findByCustomerIdAndProductId(customerId, skuId);
                if (existing != null) {
                    existing.setQuantity(existing.getQuantity() + count);
                    existing.setSelected(selected ? 1 : 0);
                    existing.setUpdateTime(new Date());
                    shoppingCartMapper.update(existing);
                } else {
                    ShoppingCart cart = new ShoppingCart();
                    cart.setCustomerId(customerId);
                    cart.setProductId(skuId);
                    cart.setProductName(product.getProductName());
                    cart.setProductImage(firstImageUrl(product.getImageUrls()));
                    cart.setProductPrice(product.getPrice());
                    cart.setQuantity(count);
                    cart.setSelected(selected ? 1 : 0);
                    cart.setCreateTime(new Date());
                    cart.setUpdateTime(new Date());
                    shoppingCartMapper.insert(cart);
                }
            }

            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", product.getId());
            cartItem.put("name", product.getProductName());
            cartItem.put("picture", Collections.singletonList(firstImageUrl(product.getImageUrls())));
            cartItem.put("price", product.getPrice());
            cartItem.put("skuId", skuId);
            cartItem.put("count", count);
            cartItem.put("selected", selected);
            result.add(cartItem);
        }
        return Result.success(result);
    }

    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> getBanners() {
        List<Banner> banners = bannerMapper.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Banner b : banners) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", b.getId());
            item.put("imageUrl", b.getImageUrl());
            item.put("linkUrl", b.getLinkUrl());
            item.put("sort", b.getSort());
            item.put("status", b.getStatus());
            item.put("createTime", b.getCreateTime());
            result.add(item);
        }
        return Result.success(result);
    }

    @PostMapping("/banners")
    public Result<String> addBanner(@RequestBody Map<String, Object> data) {
        Banner banner = new Banner();
        banner.setImageUrl((String) data.get("imageUrl"));
        banner.setLinkUrl((String) data.getOrDefault("linkUrl", ""));
        banner.setSort(data.get("sort") != null ? Integer.valueOf(data.get("sort").toString()) : 0);
        banner.setStatus((String) data.getOrDefault("status", "ENABLED"));
        bannerMapper.insert(banner);
        return Result.success("添加成功");
    }

    @PutMapping("/banners/{id}")
    public Result<String> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Banner banner = bannerMapper.findById(id);
        if (banner == null) {
            return Result.error("Banner不存在");
        }
        if (data.get("imageUrl") != null) banner.setImageUrl((String) data.get("imageUrl"));
        if (data.get("linkUrl") != null) banner.setLinkUrl((String) data.get("linkUrl"));
        if (data.get("sort") != null) banner.setSort(Integer.valueOf(data.get("sort").toString()));
        if (data.get("status") != null) banner.setStatus((String) data.get("status"));
        bannerMapper.update(banner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/banners/{id}")
    public Result<String> deleteBanner(@PathVariable Long id) {
        Banner banner = bannerMapper.findById(id);
        if (banner == null) {
            return Result.error("Banner不存在");
        }
        bannerMapper.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/config/home")
    public Result<Map<String, Object>> getHomeConfig() {
        List<HomeConfig> configs = homeConfigMapper.findAll();
        Map<String, Object> config = new HashMap<>();
        // 默认值
        config.put("recommendCount", 8);
        config.put("newCount", 8);
        config.put("hotCount", 8);
        config.put("title", "商城");
        config.put("subtitle", "品质生活，尽在商城");
        // 从数据库覆盖
        for (HomeConfig hc : configs) {
            String val = hc.getConfigValue();
            // 尝试转为数字
            try {
                if (val != null && val.matches("\\d+")) {
                    config.put(hc.getConfigKey(), Integer.parseInt(val));
                } else {
                    config.put(hc.getConfigKey(), val);
                }
            } catch (Exception e) {
                config.put(hc.getConfigKey(), val);
            }
        }
        return Result.success(config);
    }

    @PostMapping("/config/home")
    public Result<String> saveHomeConfig(@RequestBody Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            HomeConfig config = new HomeConfig();
            config.setConfigKey(entry.getKey());
            config.setConfigValue(entry.getValue() != null ? entry.getValue().toString() : "");
            homeConfigMapper.upsert(config);
        }
        return Result.success("保存成功");
    }
}
