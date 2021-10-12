package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.service.CustomerService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@Api(tags = "客服中心")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     * 新增和修改客服联系方式
     * @param qq 客服qq号
     * @param telegram 客服telegram号
     * @param skype 客服skype号
     * @param whatsApp 客服whatsApp号
     * @param facebook 客服facebook号
     * @param onlineUrl 客服onlineUrl号
     * @return
     */
    @ApiOperation("新增和修改客服联系方式")
    @PostMapping("/updateKeyCustomer")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "qq", value = "客服qq号", required = false),
            @ApiImplicitParam(name = "telegram", value = "客服telegram号", required = false),
            @ApiImplicitParam(name = "skype", value = "客服skype号", required = false),
            @ApiImplicitParam(name = "whatsApp", value = "客服whatsApp号", required = false),
            @ApiImplicitParam(name = "facebook", value = "客服facebook号", required = false),
            @ApiImplicitParam(name = "onlineUrl", value = "客服onlineUrl号", required = false),
            @ApiImplicitParam(name = "wechat", value = "客服wechat号", required = false),
            @ApiImplicitParam(name = "meiqia", value = "客服meiqia号", required = false),
            @ApiImplicitParam(name = "telephone", value = "手机号", required = false),
    })
    public ResponseEntity updateKeyCustomer(String qq,String telegram,String skype,String whatsApp,String facebook,String onlineUrl,
                                            String wechat,String meiqia,String telephone){
        Customer customer = customerService.findFirst();
        if (LoginUtil.checkNull(customer)){
            customer = new Customer();
        }
        if (!CommonUtil.checkNull(qq)){
            customer.setQq(qq);
        }
        if (!CommonUtil.checkNull(telegram)){
            customer.setTelegram(telegram);
        }
        if (!CommonUtil.checkNull(skype)){
            customer.setSkype(skype);
        }
        if (!CommonUtil.checkNull(whatsApp)){
            customer.setWhatsApp(whatsApp);
        }
        if (!CommonUtil.checkNull(facebook)){
            customer.setFacebook(facebook);
        }
        if (!CommonUtil.checkNull(onlineUrl)){
            customer.setOnlineUrl(onlineUrl);
        }
        if (!CommonUtil.checkNull(wechat)){
            customer.setWechat(wechat);
        }
        if (!CommonUtil.checkNull(meiqia)){
            customer.setMeiqia(meiqia);
        }
        if (!CommonUtil.checkNull(telephone)){
            customer.setTelephone(telephone);
        }
        customerService.save(customer);
        return ResponseUtil.success();
    }
    @ApiOperation("查询客服联系方式")
    @GetMapping("/findCustomer")
    public ResponseEntity<Customer> findCustomer(){
        Customer customer = customerService.findFirst();
        return ResponseUtil.success(customer);
    }
}
