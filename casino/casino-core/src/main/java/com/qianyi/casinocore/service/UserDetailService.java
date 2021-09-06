package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserBalanceBlock;
import com.qianyi.casinocore.model.UserDetail;
import com.qianyi.casinocore.repository.UserDetailRepository;
import com.qianyi.casinocore.vo.UserMoreDetailVo;
import com.qianyi.casinocore.vo.request.UserDetailRequest;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
import java.util.List;

/**
 * 用户中心信息
 */
@Service
@Transactional
public class UserDetailService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserBalanceBlockService userBalanceBlockService;

    @Autowired
    private UserService userService;

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
                List<Predicate> list = new ArrayList<Predicate>();
                if(StringUtils.isNotBlank(userDetailRequest.getUserName())){
                    Predicate userName = cb.equal(root.get("userName").as(String.class), userDetailRequest.getUserName());
                    list.add( cb.equal(root.get("userName").as(String.class), userDetailRequest.getUserName()));
                }
                if(StringUtils.isNotBlank(userDetailRequest.getUserId())){
                    list.add(cb.equal(root.get("userId").as(String.class), userDetailRequest.getUserId()));
                }
                if(userDetailRequest.getStatus() != null){
                    list.add(cb.equal(root.get("status").as(String.class), userDetailRequest.getStatus()));
                }
                if(userDetailRequest.getVipLevel() != null){
                    list.add(cb.equal(root.get("vipLevel").as(String.class), userDetailRequest.getVipLevel()));
                }
                if(userDetailRequest.getRiskLevel() != null){
                    list.add(cb.equal(root.get("riskLevel").as(String.class), userDetailRequest.getRiskLevel()));
                }
                if(userDetailRequest.getAgentLevel() != null){
                    list.add(cb.equal(root.get("agentLevel").as(String.class), userDetailRequest.getAgentLevel()));
                }
                if(userDetailRequest.getRegisterStartTime() != null && userDetailRequest.getRegisterEndTime() != null){
                    list.add(cb.ge(root.get("registerTime").as(Long.class), userDetailRequest.getRegisterStartTime().getTime()));
                    list.add(cb.le(root.get("registerTime").as(Long.class), userDetailRequest.getRegisterEndTime().getTime()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    /**
     * 冻结用户
     *
     * @param userName
     * @param status
     * @return
     */
    public ResponseEntity lockUser(String userName, Integer status) {
        UserDetail userDetail = userDetailRepository.findByUserName(userName);
        if(userDetail == null){
            return ResponseUtil.userError();
        }
        if(status == Constants.USER_NORMAL && userDetail.getStatus() != Constants.USER_NORMAL){ //正常
            userDetail.setStatus(status);
            //查询冻结余额是否有被冻结
            UserBalanceBlock userBalanceBlock = userBalanceBlockService.findByUserName(userName);
            if(userBalanceBlock != null && userBalanceBlock.getStatus() == Constants.USER_LOCK_BALANCE){
                userBalanceBlock.setStatus(Constants.USER_NORMAL);//解冻
                userBalanceBlockService.updateUserBalanceBlock(userBalanceBlock);
                userDetail.setAvailableBalance(userDetail.getAvailableBalance().add(userBalanceBlock.getMoney()));
                userDetail.setFrozenBalance(userDetail.getFrozenBalance().subtract(userBalanceBlock.getMoney()));
            }
        }
        if(status == Constants.USER_LOCK_BALANCE && userDetail.getStatus() == Constants.USER_NORMAL){//冻结资金
            BigDecimal availableBalance = userDetail.getAvailableBalance();
            userDetail.setAvailableBalance(BigDecimal.ZERO);
            userDetail.setFrozenBalance(userDetail.getFrozenBalance().add(availableBalance));
            UserBalanceBlock userBalanceBlock = new UserBalanceBlock();
            userBalanceBlock.setMoney(availableBalance);
            userBalanceBlock.setStatus(Constants.USER_LOCK_BALANCE);//冻结金额
            userBalanceBlock.setUserName(userName);
            userBalanceBlock.setUserId(userDetail.getUserId());
            userBalanceBlockService.save(userBalanceBlock);
        }
        if(status == Constants.USER_LOCK_ACCOUNT && userDetail.getStatus() != Constants.USER_LOCK_ACCOUNT){
            userDetail.setStatus(Constants.USER_LOCK_ACCOUNT);
        }
        userDetailRepository.save(userDetail);
        return ResponseUtil.success();
    }


    /**
     * 修改风险等级
     *
     * @param userName
     * @param riskLevel
     * @return
     */
    public ResponseEntity updateRiskLevel(String userName, Integer riskLevel) {
        UserDetail userDetail = userDetailRepository.findByUserName(userName);
        if(userDetail == null){
            return ResponseUtil.userError();
        }
        userDetail.setRiskLevel(riskLevel);
        userDetailRepository.save(userDetail);
        return ResponseUtil.success();
    }

    /**
     * 查询用户详细信息
     *
     * @param userName
     * @return
     */
    public ResponseEntity getUserDetail(String userName) {
        UserDetail userDetail = userDetailRepository.findByUserName(userName);
        if(userDetail == null){
            return ResponseUtil.userError();
        }

        User user = userService.findByAccount(userName);
        if(user == null){
            return ResponseUtil.userError();
        }
        UserMoreDetailVo userMoreDetailVo = new UserMoreDetailVo();
        BeanUtils.copyProperties(userDetail, userMoreDetailVo, UserMoreDetailVo.class);
        userMoreDetailVo.setPhone(user.getPhone() + "");

        //查询用户的下线人数
        if(userDetail.getAgentLevel() != 0){
            long agentLineCount = getDownLineCount(userDetail);
            userMoreDetailVo.setDownLineCount(agentLineCount);
        }
        return ResponseUtil.success(userMoreDetailVo);
    }

    private long getDownLineCount(UserDetail userDetail) {
        Specification<UserDetail> specification = new Specification<UserDetail>(){
            @Override
            public Predicate toPredicate(Root<UserDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if(StringUtils.isNotBlank(userDetail.getUserId())){
                    list.add(cb.like(root.get("agentLine").as(String.class), "%" + userDetail.getAgentLine() + "%"));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return userDetailRepository.count(specification);
    }
}
