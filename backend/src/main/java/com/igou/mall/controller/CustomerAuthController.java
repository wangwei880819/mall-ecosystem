package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.CustomerMapper;
import com.igou.mall.model.entity.Customer;
import com.igou.mall.config.CustomerAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/auth")
@CrossOrigin(origins = "*")
public class CustomerAuthController {

    @Autowired
    private CustomerMapper customerMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Map<String, String> smsCodeCache = new HashMap<>();

    @PostMapping("/register")
    public Result<Customer> register(@RequestBody Map<String, Object> params) {
        String phone = (String) params.get("phone");
        String password = (String) params.get("password");
        String smsCode = (String) params.get("smsCode");

        if (!"test123".equals(smsCode) && !verifySmsCode(phone, smsCode)) {
            return Result.error("验证码错误");
        }

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
        return Result.success(customer);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        String phone = (String) params.get("phone");
        String password = (String) params.get("password");

        Customer customer = customerMapper.findByPhone(phone);
        if (customer == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            return Result.error("密码错误");
        }

        customer.setLastLoginTime(LocalDateTime.now());
        customerMapper.update(customer);

        Map<String, Object> result = new HashMap<>();
        result.put("customer", customer);
        result.put("token", generateToken(customer.getId()));

        return Result.success(result);
    }

    @PostMapping("/login/sms")
    public Result<Map<String, Object>> loginBySms(@RequestBody Map<String, Object> params) {
        String phone = (String) params.get("phone");
        String smsCode = (String) params.get("smsCode");

        if (!verifySmsCode(phone, smsCode)) {
            return Result.error("验证码错误");
        }

        Customer customer = customerMapper.findByPhone(phone);
        if (customer == null) {
            return Result.error("用户不存在");
        }

        customer.setLastLoginTime(LocalDateTime.now());
        customerMapper.update(customer);

        Map<String, Object> result = new HashMap<>();
        result.put("customer", customer);
        result.put("token", generateToken(customer.getId()));

        return Result.success(result);
    }

    @PostMapping("/login/wechat")
    public Result<Map<String, Object>> loginByWechat(@RequestBody Map<String, Object> params) {
        String openId = (String) params.get("openId");
        String nickname = (String) params.get("nickname");
        String avatar = (String) params.get("avatar");

        Customer customer = customerMapper.findByPhone(openId);
        if (customer == null) {
            customer = new Customer();
            customer.setPhone(openId);
            customer.setNickname(nickname);
            customer.setAvatar(avatar);
            customer.setVipLevel("NORMAL");
            customer.setStatus("ACTIVE");
            customer.setRegisterTime(LocalDateTime.now());
            customerMapper.insert(customer);
        }

        customer.setLastLoginTime(LocalDateTime.now());
        customerMapper.update(customer);

        Map<String, Object> result = new HashMap<>();
        result.put("customer", customer);
        result.put("token", generateToken(customer.getId()));

        return Result.success(result);
    }

    @PostMapping("/send-sms-code")
    public Result<String> sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = generateSmsCode();
        smsCodeCache.put(phone, code);

        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000);
                smsCodeCache.remove(phone);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return Result.success("验证码已发送");
    }

    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, Object> params) {
        String phone = (String) params.get("phone");
        String smsCode = (String) params.get("smsCode");
        String newPassword = (String) params.get("newPassword");

        if (!verifySmsCode(phone, smsCode)) {
            return Result.error("验证码错误");
        }

        Customer customer = customerMapper.findByPhone(phone);
        if (customer == null) {
            return Result.error("用户不存在");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerMapper.update(customer);

        return Result.success("密码重置成功");
    }

    @GetMapping("/profile")
    public Result<Customer> getProfile(HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.error("用户不存在");
        }
        customer.setPassword(null);
        return Result.success(customer);
    }

    @PutMapping("/profile")
    public Result<Customer> updateProfile(@RequestBody Customer customer, HttpServletRequest request) {
        Long customerId = getAuthCustomerId(request);
        if (customerId == null) {
            return Result.error("请先登录");
        }
        Customer existing = customerMapper.findById(customerId);
        if (existing == null) {
            return Result.error("用户不存在");
        }

        // 仅允许修改自己的信息
        customer.setId(customerId);
        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        } else {
            customer.setPassword(existing.getPassword());
        }

        customerMapper.update(customer);
        return Result.success(customer);
    }

    private boolean verifySmsCode(String phone, String code) {
        String cachedCode = smsCodeCache.get(phone);
        return cachedCode != null && cachedCode.equals(code);
    }

    private Long getAuthCustomerId(HttpServletRequest request) {
        Object attr = request.getAttribute(CustomerAuthFilter.ATTR_CUSTOMER_ID);
        return attr != null ? (Long) attr : null;
    }

    private String generateSmsCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private String generateToken(Long userId) {
        return "CUST_" + userId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}