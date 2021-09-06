package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserDetail;
import com.qianyi.casinocore.repository.UserDetailRepository;
import com.qianyi.casinocore.vo.request.UserDetailRequest;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import jdk.nashorn.internal.runtime.Specialization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户中心信息
 */
@Service
@Transactional
public class UserDetailService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    /**
     * 分页查询用户详细数据
     *
     * @param userDetailRequest
     * @return
     */
    public Page<UserDetail> findUserPage(UserDetailRequest userDetailRequest) {
        Pageable pageable = PageRequest.of(userDetailRequest.getCurrent(), userDetailRequest.getSize(), Sort.Direction.DESC, "id");
        return userDetailRepository.findAll(getCondition(userDetailRequest), pageable);
    }

    /**
     * 查询条件封装
     * @param userDetailRequest
     * @return
     */
    private Specification<UserDetail> getCondition(UserDetailRequest userDetailRequest) {
        Specification<UserDetail> specification = new Specification<UserDetail>(){
            @Override
            public Predicate toPredicate(Root<UserDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<UserDetail> userDetailList = new ArrayList<>();
                if(StringUtils.isNotBlank(userDetailRequest.getUserName())){
                    userDetailList.add((UserDetail) cb.equal(root.get("userName").as(String.class), userDetailRequest.getUserName()));
                }
                if(StringUtils.isNotBlank(userDetailRequest.getUserId())){
                    userDetailList.add((UserDetail) cb.equal(root.get("userId").as(String.class), userDetailRequest.getUserId()));
                }
                if(userDetailRequest.getStatus() != null){
                    userDetailList.add((UserDetail) cb.equal(root.get("status").as(String.class), userDetailRequest.getStatus()));
                }
                if(userDetailRequest.getVipLevel() != null){
                    userDetailList.add((UserDetail) cb.equal(root.get("vipLevel").as(String.class), userDetailRequest.getVipLevel()));
                }
                if(userDetailRequest.getRiskLevel() != null){
                    userDetailList.add((UserDetail) cb.equal(root.get("riskLevel").as(String.class), userDetailRequest.getRiskLevel()));
                }
                if(userDetailRequest.getAgentLevel() != null){
                    userDetailList.add((UserDetail) cb.equal(root.get("agentLevel").as(String.class), userDetailRequest.getAgentLevel()));
                }
                if(userDetailRequest.getRegisterStartTime() != null && userDetailRequest.getRegisterEndTime() != null){
                    userDetailList.add((UserDetail) cb.ge(root.get("registerTime").as(Long.class), userDetailRequest.getRegisterStartTime().getTime()));
                    userDetailList.add((UserDetail) cb.le(root.get("registerTime").as(Long.class), userDetailRequest.getRegisterEndTime().getTime()));
                }
                return cb.and(userDetailList.toArray(new Predicate[userDetailList.size()]));
            }
        };
        return specification;
    }

    public ResponseEntity lockUser(String userName, Integer status) {
        UserDetail userDetail = userDetailRepository.findByUserName(userName);
        if(userDetail == null){
            return ResponseUtil.userError();
        }
        if(status == Constants.USER_NORMAL && userDetail.getStatus() != Constants.USER_NORMAL){ //正常
            userDetail.setStatus(status);
            //查询冻结余额是否有被冻结
        }
        if(status == 1){//

        }
        userDetailRepository.save(userDetail);
        return null;
    }
}
