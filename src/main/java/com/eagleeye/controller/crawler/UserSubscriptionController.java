package com.eagleeye.controller.crawler;

import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.UserSubscriptionCreateDTO;
import com.eagleeye.model.dto.UserSubscriptionUpdateDTO;
import com.eagleeye.model.vo.CrawlerConfigVO;
import com.eagleeye.model.vo.UserSubscriptionVO;
import com.eagleeye.service.crawler.UserSubscriptionService;
// import org.springframework.security.core.Authentication; // To get current user ID
// import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户信息源订阅控制器
 */
@RestController
@RequestMapping("/v1/crawler/subscriptions")
@Api(tags = "用户 - 信息源订阅管理接口")
@Validated
public class UserSubscriptionController {

    @Resource
    private UserSubscriptionService userSubscriptionService;

    // Placeholder for getting current user ID - replace with actual security implementation
    private Long getCurrentUserId() {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        //     UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //     // Assuming your UserDetails implementation has a method to get the ID
        //     // return ((YourUserDetailsImplementation) userDetails).getId();
        // }
        // --- Temporary Placeholder ---
        // In a real app, throw an exception or handle unauthenticated access properly
        System.err.println("Warning: Using placeholder user ID 1L in UserSubscriptionController. Implement actual user retrieval.");
        return 1L; 
        // --- End Temporary Placeholder ---
    }

    @ApiOperation("获取可订阅的信息源列表")
    @GetMapping("/available")
    public CommonResult<List<CrawlerConfigVO>> listAvailableConfigs() {
        List<CrawlerConfigVO> configs = userSubscriptionService.listAvailableConfigs();
        return CommonResult.success(configs);
    }

    @ApiOperation("订阅一个信息源")
    @PostMapping
    public CommonResult<Long> subscribe(
            @ApiParam("订阅参数") @Valid @RequestBody UserSubscriptionCreateDTO createDTO) {
        Long userId = getCurrentUserId(); // Get current user ID
        Long subscriptionId = userSubscriptionService.subscribe(createDTO, userId);
        if (subscriptionId != null) {
            return CommonResult.success(subscriptionId, "订阅成功");
        } else {
            // Could fail if already subscribed or config invalid
            return CommonResult.failed("订阅失败（可能已订阅或配置无效）");
        }
    }

    @ApiOperation("获取我的订阅列表")
    @GetMapping
    public CommonResult<List<UserSubscriptionVO>> listMySubscriptions() {
        Long userId = getCurrentUserId(); // Get current user ID
        List<UserSubscriptionVO> subscriptions = userSubscriptionService.listMySubscriptions(userId);
        return CommonResult.success(subscriptions);
    }

    @ApiOperation("取消订阅")
    @DeleteMapping("/{subscriptionId}")
    public CommonResult<String> unsubscribe(
            @ApiParam("订阅ID") @PathVariable Long subscriptionId) {
        Long userId = getCurrentUserId(); // Get current user ID for permission check
        boolean success = userSubscriptionService.unsubscribe(subscriptionId, userId);
        if (success) {
            return CommonResult.success("取消订阅成功");
        } else {
            return CommonResult.failed("取消订阅失败（订阅不存在或无权限）");
        }
    }

    @ApiOperation("更新订阅设置")
    @PutMapping("/{subscriptionId}")
    public CommonResult<String> updateSubscription(
            @ApiParam("订阅ID") @PathVariable Long subscriptionId,
            @ApiParam("更新参数") @Valid @RequestBody UserSubscriptionUpdateDTO updateDTO) {
        Long userId = getCurrentUserId(); // Get current user ID for permission check
        boolean success = userSubscriptionService.updateSubscription(subscriptionId, updateDTO, userId);
        if (success) {
            return CommonResult.success("更新成功");
        } else {
            return CommonResult.failed("更新失败（订阅不存在或无权限）");
        }
    }
} 