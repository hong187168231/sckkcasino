package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InteceptorController implements ErrorController {

    @NoAuthentication
    @RequestMapping("error")
    public ResponseEntity error() {
        return ResponseUtil.error();
    }

    @NoAuthentication
    @RequestMapping("authenticationNopass")
    public ResponseEntity authenticationNopass() {
        return ResponseUtil.authenticationNopass();
    }

    @RequestMapping("authenticationBan")
    public ResponseEntity authenticationBan() {
        return ResponseUtil.custom("帐号被封");
    }

    @NoAuthentication
    @RequestMapping("authenticationIpLimit")
    public ResponseEntity authenticationIpLimit() {
        return ResponseUtil.custom("ip访问受限");
    }

    @NoAuthentication
    @RequestMapping("risk")
    public ResponseEntity risk() {
        return ResponseUtil.risk();
    }
}
