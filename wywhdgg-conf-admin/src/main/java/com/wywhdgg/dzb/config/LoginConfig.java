package com.wywhdgg.dzb.config;

import com.wywhdgg.dzb.constants.LoginConfigConstants;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.serivce.ConfUserService;
import com.wywhdgg.dzb.util.CookieUtil;
import com.wywhdgg.dzb.util.JacksonUtil;
import com.wywhdgg.dzb.util.Result;
import java.math.BigInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;

/***
 *@author dzb
 *@date 2019/7/21 22:48
 *@Description:
 *@version 1.0
 */
@Configuration
public class LoginConfig {

    @Autowired
    private ConfUserService confUserService;

    private String makeToken(ConfUser confUser){
        String tokenJson = JacksonUtil.writeValueAsString(confUser);
        String tokenHex = new BigInteger(tokenJson.getBytes()).toString(16);
        return tokenHex;
    }


    private ConfUser parseToken(String tokenHex){
        ConfUser confUser = null;
        if (tokenHex != null) {
            String tokenJson = new String(new BigInteger(tokenHex, 16).toByteArray());      // username_password(md5)
            confUser = JacksonUtil.readValue(tokenJson, ConfUser.class);
        }
        return confUser;
    }

    /**
     * login
     *
     * @param response
     * @param usernameParam
     * @param passwordParam
     * @param ifRemember
     * @return
     */
    public Result<String> login(HttpServletResponse response, String usernameParam, String passwordParam, boolean ifRemember){

        ConfUser confUser = confUserService.findNameByConfUser(usernameParam);
        if (confUser == null) {
            return new Result<String>(500, "账号或密码错误");
        }

        String passwordParamMd5 = DigestUtils.md5DigestAsHex(passwordParam.getBytes());
        if (!confUser.getPassword().equals(passwordParamMd5)) {
            return new Result<String>(500, "账号或密码错误");
        }

        String loginToken = makeToken(confUser);

        // do login
        CookieUtil.set(response, LoginConfigConstants.LOGIN_IDENTITY, loginToken, ifRemember);
        return Result.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.remove(request, response, LoginConfigConstants.LOGIN_IDENTITY);
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public ConfUser ifLogin(HttpServletRequest request){
        String cookieToken = CookieUtil.getValue(request, LoginConfigConstants.LOGIN_IDENTITY);
        if (cookieToken != null) {
            ConfUser cookieUser = parseToken(cookieToken);
            if (cookieUser != null) {
                ConfUser dbUser = confUserService.findNameByConfUser(cookieUser.getUsername());
                if (dbUser != null) {
                    if (cookieUser.getPassword().equals(dbUser.getPassword())) {
                        return dbUser;
                    }
                }
            }
        }
        return null;
    }
}
