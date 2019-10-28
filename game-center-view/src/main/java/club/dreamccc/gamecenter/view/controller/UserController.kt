package club.dreamccc.gamecenter.view.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController {


    private val URL_PRE = "123";

    @RequestMapping("/test")
    fun test(username: String?, password: String?): Any {

        return "test";
    }

}


