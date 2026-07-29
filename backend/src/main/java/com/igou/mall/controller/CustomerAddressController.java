package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.CustomerAddressMapper;
import com.igou.mall.model.entity.CustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer/address")
@CrossOrigin(origins = "*")
public class CustomerAddressController {

    @Autowired
    private CustomerAddressMapper addressMapper;

    @GetMapping("/list")
    public Result<List<CustomerAddress>> getList(@RequestParam Long customerId) {
        List<CustomerAddress> addresses = addressMapper.findByCustomerId(customerId);
        return Result.success(addresses);
    }

    @GetMapping("/{id}")
    public Result<CustomerAddress> getById(@PathVariable Long id) {
        CustomerAddress address = addressMapper.findById(id);
        if (address == null) {
            return Result.error("地址不存在");
        }
        return Result.success(address);
    }

    @PostMapping
    public Result<CustomerAddress> create(@RequestBody CustomerAddress address) {
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.insert(address);
        return Result.success(address);
    }

    @PutMapping("/{id}")
    public Result<CustomerAddress> update(@PathVariable Long id, @RequestBody CustomerAddress address) {
        CustomerAddress existing = addressMapper.findById(id);
        if (existing == null) {
            return Result.error("地址不存在");
        }
        address.setId(id);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.update(address);
        return Result.success(address);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        addressMapper.delete(id);
        return Result.success("地址已删除");
    }
}