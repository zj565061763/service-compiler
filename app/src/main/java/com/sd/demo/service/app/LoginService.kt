package com.sd.demo.service.app

import com.sd.demo.service.app_common.LoginService
import com.sd.demo.service.app_common.logMsg
import com.sd.lib.service.FServiceImpl

@FServiceImpl
class LoginServiceApp : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}

@FServiceImpl(name = "App1")
class LoginServiceApp1 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}

@FServiceImpl(name = "App2")
class LoginServiceApp2 : LoginService {
    override fun login() {
        logMsg { "$this login" }
    }
}