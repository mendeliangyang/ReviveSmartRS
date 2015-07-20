/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class MsgClientPushParam {
    public Set<String> pushIds = new HashSet<>();
    public String rsid;
    public String userName;
    public String userPwd;
    public Session sessionClient;
}
