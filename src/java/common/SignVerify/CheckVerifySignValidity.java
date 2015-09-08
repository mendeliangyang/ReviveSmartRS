/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.SignVerify;

import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public class CheckVerifySignValidity implements Runnable {

    @Override
    public void run() {
        synchronized (SignCommon.SignRecords) {
            Iterator iterator = SignCommon.SignRecords.iterator();
            while (iterator.hasNext()) {
                SignInformationModel next = (SignInformationModel) iterator.next();
                if (new Date().getTime() - next.signDateTime >= (1000 * SignCommon.SignVerifyTimeOut)) {
                    SignCommon.SignRecords.remove(next);
                }
            }
        }
    }

}
