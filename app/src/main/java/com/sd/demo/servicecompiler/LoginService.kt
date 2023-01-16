package com.sd.demo.servicecompiler

import com.sd.demo.service.app_common.LoginService
import com.sd.demo.service.app_common.logMsg
import com.sd.lib.service.FServiceImpl

@FServiceImpl
class LoginServiceApp : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}