package com.suixingpay.controller;

import com.suixingpay.pojo.PatentInfo;
import com.suixingpay.pojo.User;
import com.suixingpay.service.PatentInfoService;
import com.suixingpay.service.UserDescriptionService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 詹文良
 * @program: patent-pool-3th
 * @description: 专利接口 Controller
 * <p>
 * Created by Jalr4ever on 2019/11/21.
 */

@RestController
@Slf4j

public class PatentInfoController {

    /**
     * 专利信息服务
     */
    @Autowired
    private PatentInfoService patentInfoService;

    /**
     * 用户信息服务
     */
    @Autowired
    private UserDescriptionService userDescriptionService;

    /**
     * HttpServletRequest 服务
     */
    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/addPatent", method = RequestMethod.POST)
    public String addNewPatent(@RequestBody PatentInfo patentInfo) {

        // 插入一条专利
        return patentInfoService.createNewPatent(patentInfo);

    }

    @RequestMapping(value = "/searchPatent", method = RequestMethod.POST)
    public String searchPatentFuzzy(@RequestBody PatentInfo patentInfo) {

        // TODO: 2019/12/2 Feature update - 字段模糊搜索完成，下一步优化状态条件查询

        // 获取用户信息
        User user = userDescriptionService.userDescription(httpServletRequest.getSession());

        // 判断用户是否登录，若没登录，就赋值 id 为 0 表示没有登录
        int userId = user == null ? 0 : user.getId();
        return patentInfoService.searchPatentByUserType(patentInfo, userId);

    }

    // TODO: 2019/12/3 模糊搜索分离 - 已经认领的专利搜索
    @RequestMapping(value = "/searchPatentReceive", method = RequestMethod.POST)
    public String searchPatentFuzzyReceive(@RequestBody PatentInfo patentInfo) {

        // 获取用户信息
        User user = userDescriptionService.userDescription(httpServletRequest.getSession());

        // 判断用户是否登录，若没登录，就赋值 id 为 0 表示没有登录
        int userId = user == null ? 0 : user.getId();
        return patentInfoService.searchPatentByUserAndReceive(patentInfo, userId);

    }

    // TODO: 2019/12/3 模糊搜索分离 - 未被认领的专利搜索
    @RequestMapping(value = "searchPatentNoReceive", method = RequestMethod.POST)
    public String searchPatentFuzzyNoReceive(@RequestBody PatentInfo patentInfo) {

        // 获取用户信息
        User user = userDescriptionService.userDescription(httpServletRequest.getSession());

        // 判断用户是否登录，若没登录，就赋值 id 为 0 表示没有登录
        int userId = user == null ? 0 : user.getId();
        return patentInfoService.searchPatentByUserAndNoReceive(patentInfo, userId);
    }


    @RequestMapping(value = "/searchPatentPool", method = RequestMethod.GET)
    public String searchPatentPool() {

        // 获取用户信息
        User user = userDescriptionService.userDescription(httpServletRequest.getSession());

        // 查询专利池
        return patentInfoService.searchNavigationInfo(user.getId());

    }

    @RequestMapping(value = "/receivePatent", method = RequestMethod.POST)
    public String receiveOnePatent(@RequestBody PatentInfo patentInfo) {

        // 获取用户信息
        User user = userDescriptionService.userDescription(httpServletRequest.getSession());
        patentInfo.setOwnerUserId(user.getId());

        // 领取一个专利
        return patentInfoService.receivePatent(patentInfo);

    }

    @RequestMapping(value = "/editPatent", method = RequestMethod.POST)
    public String editPatentById(@RequestBody PatentInfo patentInfo) {

        // 获取用户信息
        User user = userDescriptionService.userDescription(httpServletRequest.getSession());

        // 编辑对应用户类型的专利
        return patentInfoService.editPatent(patentInfo, user.getId());

    }

    @RequestMapping(value = "/getPatentDetail", method = RequestMethod.POST)
    public String showPatentDetails(@RequestBody PatentInfo patentInfo) {

        // 返回专利的详细信息
        return patentInfoService.searchPatentAnyCondition(patentInfo);

    }

}
