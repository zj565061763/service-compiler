package com.sd.demo.servicecompiler

import com.sd.demo.servicecompiler.service.LoginService
import com.sd.demo.servicecompiler.service.logMsg
import com.sd.lib.service.FServiceImpl

@FServiceImpl
class LoginServiceApp : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}