/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.Electornic;

/**
 *
 * @author Administrator
 */
public enum EnumElectornicRetCode {

    success("0000",""), paramErr("9999","参数错误"), unknownErr("9998","未知错误"), logicErr("9997","业务错误");

    private String retCode;
    private String DefaultDesc;

    EnumElectornicRetCode(String pRetCode,String pDefaultDesc) {
        this.retCode = pRetCode;
        this.DefaultDesc = pDefaultDesc;
    }

    public String getDescribe() {
        return DefaultDesc;
    }
    
    public String getRetCode(){
        return retCode;
    }
    

    @Override
    public String toString() {
        return DefaultDesc;
    }

}
