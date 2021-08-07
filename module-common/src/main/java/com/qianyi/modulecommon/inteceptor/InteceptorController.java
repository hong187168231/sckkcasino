package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InteceptorController {

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
}
