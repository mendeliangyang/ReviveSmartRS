/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.comInterface;

import common.model.ResponseResultCode;

/**
 *
 * @author Administrator
 */
public interface IFormationResult {
    public String formationResult(ResponseResultCode resultCode, String errMsg, Object... result);
    public String formationResult(ResponseResultCode resultCode, String errMsg,String token, Object... result);
    public String formationResult(ResponseResultCode resultCode, String errMsg,String token,String pushId, Object... result);
}
