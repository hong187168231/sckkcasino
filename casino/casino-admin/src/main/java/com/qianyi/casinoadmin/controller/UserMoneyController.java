//package com.qianyi.casinoadmin.controller;
//
//import com.qianyi.casinocore.util.CommonConst;
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.User;
//import com.qianyi.casinocore.model.UserMoney;
//import com.qianyi.casinocore.service.UserMoneyService;
//import com.qianyi.casinocore.service.UserService;
//import com.qianyi.modulecommon.reponse.ResponseEntity;
//import com.qianyi.modulecommon.reponse.ResponseUtil;
//import com.qianyi.modulecommon.util.CommonUtil;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/userMoney")
//@Api(tags = "客户中心")
//public class UserMoneyController {
//    @Autowired
//    private UserMoneyService userMoneyService;
//    @Autowired
//    private UserService userService;
//    /**
//     * 分页查询用户钱包
//     *
//     * @param pageSize 每页大小(默认10条)
//     * @param pageCode 当前页(默认第一页)
//     * @param account 用户账号
//     * @return
//     */
//    @ApiOperation("分页查询用户钱包")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
//            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
//            @ApiImplicitParam(name = "account", value = "用户账号", required = false),
//    })
//    @GetMapping("/findUserMoneyPage")
//    public ResponseEntity findUserMoneyPage(Integer pageSize, Integer pageCode, String account){
//        UserMoney userMoney = new UserMoney();
//        if (!CommonUtil.checkNull(account)){
//            User user = userService.findByAccount(account);
//            if (user == null){
//                return ResponseUtil.custom("没有这个会员");
//            }
//            userMoney.setUserId(user.getId());
//        }
//        Sort sort = Sort.by("id").descending();
//        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
//        Specification<UserMoney> condition = this.getCondition(userMoney);
//        Page<UserMoney> userMoneyPage = userMoneyService.findUserMoneyPage(condition, pageable);
//        List<UserMoney> userMoneyList = userMoneyPage.getContent();
//        if (userMoneyList != null && userMoneyList.size()== CommonConst.NUMBER_0){
//            return ResponseUtil.success(userMoneyPage);
//        }
//        List<Long> userIds = userMoneyList.stream().map(UserMoney::getUserId).collect(Collectors.toList());
//        List<User> userList = userService.findAll(userIds);
//        userList.stream().forEach(user-> {
//            userMoneyList.stream().forEach(money -> {
//                if(user.getId().equals(money.getUserId())){
//                    money.setCreateBy(user.getAccount());
//                }
//            });
//        });
//        return ResponseUtil.success(userMoneyPage);
//
//    }
//    /**
//     * 查询条件拼接，灵活添加条件
//     * @param
//     * @return
//     */
//    private Specification<UserMoney> getCondition(UserMoney userMoney) {
//        Specification<UserMoney> specification = new Specification<UserMoney>() {
//            @Override
//            public Predicate toPredicate(Root<UserMoney> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
//                Predicate predicate = cb.conjunction();
//                if (userMoney.getUserId()!=null) {
//                    predicate = cb.equal(root.get("userId").as(Long.class), userMoney.getUserId());
//                }
//                return predicate;
//            }
//        };
//        return specification;
//    }
//}
