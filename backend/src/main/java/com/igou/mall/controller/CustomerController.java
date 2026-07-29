package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.CustomerMapper;
import com.igou.mall.dao.CustomerTagMapper;
import com.igou.mall.model.entity.Customer;
import com.igou.mall.model.entity.CustomerTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerTagMapper customerTagMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestParam(defaultValue = "0") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Integer offset = page * size;
        List<Customer> customers = customerMapper.findPage(offset, size);
        Integer total = customerMapper.count();

        Map<String, Object> result = new HashMap<>();
        result.put("list", customers);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        return Result.success(customer);
    }

    @GetMapping("/search")
    public Result<List<Customer>> search(@RequestParam String keyword) {
        List<Customer> customers = customerMapper.search(keyword);
        return Result.success(customers);
    }

    @PutMapping("/{id}/vip")
    public Result<String> updateVipLevel(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String vipLevel = params.get("vipLevel");
        if (!"NORMAL".equals(vipLevel) && !"VIP".equals(vipLevel) && !"SVIP".equals(vipLevel)) {
            return Result.error("无效的VIP等级");
        }
        customerMapper.updateVipLevel(id, vipLevel);
        return Result.success("VIP等级更新成功");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        customer.setStatus(params.get("status"));
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.update(customer);
        return Result.success("状态更新成功");
    }

    @GetMapping("/{id}/tags")
    public Result<List<CustomerTag>> getCustomerTags(@PathVariable Long id) {
        List<CustomerTag> tags = customerTagMapper.findByCustomerId(id);
        return Result.success(tags);
    }

    @PostMapping("/{id}/tags")
    public Result<CustomerTag> addTag(@PathVariable Long id, @RequestBody Map<String, String> params) {
        CustomerTag tag = new CustomerTag();
        tag.setCustomerId(id);
        tag.setTagName(params.get("tagName"));
        tag.setTagType(params.getOrDefault("tagType", "USER"));
        tag.setCreateTime(LocalDateTime.now());
        customerTagMapper.insert(tag);
        return Result.success(tag);
    }

    @DeleteMapping("/{customerId}/tags/{tagName}")
    public Result<String> removeTag(@PathVariable Long customerId, @PathVariable String tagName) {
        customerTagMapper.deleteByCustomerIdAndTagName(customerId, tagName);
        return Result.success("标签已移除");
    }
}