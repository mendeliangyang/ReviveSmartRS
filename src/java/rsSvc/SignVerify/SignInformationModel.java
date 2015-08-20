/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.SignVerify;

import java.util.Date;

/**
 *
 * @author Administrator
 */
public class SignInformationModel {

    public SignInformationModel() {
    }

    public SignInformationModel(String pId, String pMac, String pDeviceType ) {
        this.deviceType = pDeviceType;
        this.Id = pId;
        this.mac = pMac;
        this.signDateTime = new Date().getTime();
    }

    public String token; //encode mac ,deviceType,Id
    public String mac; //device mac address
    public String deviceType; //1 pc,2 andorid,3 ios
    public String Id;// sign user id
    public long signDateTime;

    /**
     * 加密token id,mac,deviceType 合并计算
     *
     * @return
     */
    public String encodeToken() {
        //
        token = Id;
        return token;
    }

    /**
     * 解析token
     */
    public void decodeToken() {
        //

    }
}
