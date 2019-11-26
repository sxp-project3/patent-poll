package com.suixingpay.service;

import com.alibaba.fastjson.JSON;
import com.suixingpay.mapper.PatentInfoMapper;
import com.suixingpay.pojo.PatentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author 詹文良
 * @program: patent-pool-3th
 * @description: 专利信息的业务逻辑服务具体实现
 * <p>
 * Created by Jalr4ever on 2019/11/21.
 */

@Service
@Slf4j
public class PatentInfoServiceImpl implements PatentInfoService {

    /**
     * 暂时写死管理员的 id ， 值为 2
     */
    private static final int ROOT_USER_ID = 2;

    @Resource
    private PatentInfoMapper patentInfoMapper;

    @Override
    public String createNewPatent(PatentInfo patentInfo) {

        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 在新建专利的服务逻辑层初始化其申请日期、创建日期、修改日期
        Date date = new Date();
        patentInfo.setApplyDate(date);
        patentInfo.setCreateDate(date);
        patentInfo.setModifyDate(date);


        // 通过 mapper 的执行来决定是否成功，插入失败，则返回结果为 0；插入成功，则 mapper 执行结果为 1
        int insertResult = 0;
        try {
            insertResult = patentInfoMapper.insertPatentInfo(patentInfo);
        } catch (Exception e) {
            log.error("数据库插入专利错误！");
            log.error(e.getMessage());
            e.printStackTrace();
        }

        // 取得执行的结果状态串, 并返回为 JSON 串
        returnJsonStr = getExecuteJsonMessage(insertResult);

        return returnJsonStr;
    }

    @Override
    public String searchPatentAnyCondition(PatentInfo patentInfo) {

        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        List<PatentInfo> searchPatentList = patentInfoMapper.selectPatentFuzzy(patentInfo);

        // 根据 mapper 的执行来判断，如果为空，则返回失败状态 JSON 串，如果不为空，返回 LIST JSON 串
        if (searchPatentList.isEmpty()) {

            // 写入错误的 status 信息
            Map<String, String> resutlStatusInfo = new HashMap<>(1);
            resutlStatusInfo.put("patentStatus", "failed");
            returnJsonStr = JSON.toJSONString(resutlStatusInfo);

        } else {
            returnJsonStr = JSON.toJSONString(searchPatentList);
        }
        return returnJsonStr;
    }

    @Override
    public String editPatent(PatentInfo patentInfo, Integer editUserId) {

        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 根据专利的 id 查询，controller 层传入的 patentinfo 实体应当只包含专利 id
        List<PatentInfo> patentInfos = executeSelectPatentAnyCondition(patentInfo);

        // 查询不到指定的专利或者专利的拥有者未空
        Integer originalPatentOwnerId = patentInfos.isEmpty() ? null : patentInfos.get(0).getOwnerUserId();

        // TODO: 2019/11/25 决定在 controller 获取 id，在这里需要判断 null 的 editUserId 能不能传进来
        // 查不到指定的专利或当前的编辑用户 id 对不上认领者 id
        if (!editUserId.equals(originalPatentOwnerId)) {
            returnJsonStr = getExecuteJsonMessage(0);
        } else {
            returnJsonStr = executeUpdatePatent(patentInfo);
        }

        return returnJsonStr;
    }

    @Override
    public String receivePatent(PatentInfo patentInfo) {
        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 如果专利还没被认领或者当前的编辑用户或者认领者 id 对不上就不能编辑
        List<PatentInfo> patentInfos = executeSelectPatentAnyCondition(patentInfo);

        // 查询不到指定的专利或者专利的拥有者未空
        Integer originalPatentOwnerId = patentInfos.isEmpty() ? null : patentInfos.get(0).getOwnerUserId();


        // 如果已经有了拥有者, 就是 owner_user_id 不为 0, 则不再二次认领
        if (!originalPatentOwnerId.equals(0)) {
            returnJsonStr = getExecuteJsonMessage(0);
        } else {
            returnJsonStr = executeUpdatePatent(patentInfo);
        }

        return returnJsonStr;
    }

    @Override
    public String searchNavigationInfo(int id) {
        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 如果是管理员 id，返回全部, 因为没有一个 role 字段，暂时写死为 2
        if (id != ROOT_USER_ID) {
            returnJsonStr = JSON.toJSONString(patentInfoMapper.selectPatentNormalUser(id));
        } else {
            returnJsonStr = JSON.toJSONString(patentInfoMapper.selectPatentRootUser());
        }

        return returnJsonStr;
    }

    @Override
    public String searchPatentByUserType(PatentInfo patentInfo, int id) {
        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 0 表示未登录，ROOT_USER_ID 表示管理员，其他用户 id 表示普通用户
        if (id == 0) {
            returnJsonStr = getExecuteJsonMessage(0);
        } else if (id == ROOT_USER_ID) {
            returnJsonStr = JSON.toJSONString(patentInfoMapper.selectPatentRootUserCondition(patentInfo));
        } else {
            returnJsonStr = JSON.toJSONString(patentInfoMapper.selectPatentNormalUserCondition(patentInfo));
        }

        return returnJsonStr;
    }

    @Override
    public String searchPatentByCurrentStatusList(List<Integer> statusList, Integer userId) {
        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 返回执行结果
        returnJsonStr = JSON.toJSONString(patentInfoMapper.selectPatentByStatusList(statusList, userId));

        return returnJsonStr;
    }

    /**
     * 封装一系列条件到实体，查询指定条件的记录返回成一个 LIST，此方法只对该服务的所有 查询专利 的方法适用
     *
     * @param patentInfo 专利实体
     * @return 专利记录 LIST
     */
    private List<PatentInfo> executeSelectPatentAnyCondition(PatentInfo patentInfo) {
        PatentInfo patentInfoSearching = new PatentInfo();
        patentInfoSearching.setId(patentInfo.getId());
        List<PatentInfo> patentInfos = patentInfoMapper.selectPatentFuzzy(patentInfoSearching);
        return patentInfos;
    }

    /**
     * 用来获取更新相关操作的执行结果，此方法只对该服务的所有 更新专利 的方法适用
     *
     * @param patentInfo 专利实体
     * @return 返回状态 JSON 串
     */
    private String executeUpdatePatent(PatentInfo patentInfo) {
        // 定义要返回的 JSON 结果
        String returnJsonStr = "";

        // 在更新专利的时候在服务层更新其修改时间
        Date date = new Date();
        patentInfo.setModifyDate(date);

        // 根据 mapper 的执行来判断, 如果为空, 则返回失败状态 JSON 串, 如果不为空, 返回 LIST JSON 串
        int updateResult = 0;
        try {
            updateResult = patentInfoMapper.updatePatentInfoById(patentInfo);
        } catch (Exception e) {
            log.error("数据库更新专利错误");
            log.error(e.getMessage());
            e.printStackTrace();
        }

        // 取得执行结果的状态串, 并返回为 JSON
        returnJsonStr = getExecuteJsonMessage(updateResult);

        return returnJsonStr;
    }

    /**
     * 用来获取执行 SQL 的结果，此方法对该服务的所有方法适用
     *
     * @param num SQL 执行结果影响数
     * @return 返回状态 JSON 串
     */
    private String getExecuteJsonMessage(int num) {
        // 决定返回的 status 返回结果，num 如果为 1 则表示成功, 0 为失败
        String executeMessage = num == 1 ? "success" : "failed";
        Map<String, String> executeStatus = new HashMap<>(1);
        executeStatus.put("patentStatus", executeMessage);
        return JSON.toJSONString(executeStatus);
    }
}