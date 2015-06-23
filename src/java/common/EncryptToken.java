/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.Arrays;

/**
 *
 * @author Administrator
 */
public class EncryptToken {

    private static char secretStr[] ="hxsmart".toCharArray();
    
    public static String EncryptLoginToken(String pLoginToken) {
        char loginToken[]= pLoginToken.toCharArray();
        int iLoginToken = loginToken.length;
        for (int i = 0; i <iLoginToken ; i++) {
            loginToken[i] =(char)(loginToken[i]^secretStr[i]);
        }
        return Arrays.toString(loginToken);
    }
    
    public static String DissectLoginToken(String pLoginToken) {
        char loginToken[]= pLoginToken.toCharArray();
        int iLoginToken = loginToken.length;
        for (int i = 0; i <iLoginToken ; i++) {
            loginToken[i] =(char)(loginToken[i]^secretStr[i]);
        }
        return Arrays.toString(loginToken);
    }
}