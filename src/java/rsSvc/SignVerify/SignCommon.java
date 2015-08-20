/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.SignVerify;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */
public class SignCommon {

    public final static Set<SignInformationModel> SignRecords = Collections.synchronizedSet(new HashSet<SignInformationModel>());

    public static final int SignVerifyTimeOut = 30;

    public static boolean initialSignVerify() {

        // start up check validity thread pool to  clear up invalid verify email
        startUpCheckSignValidityPool();
        return true;

    }

    public final static ExecutorService SignRecordPutThreadPool = Executors.newSingleThreadExecutor();

    private static void startUpCheckSignValidityPool() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new CheckVerifySignValidity(), 10, 10, TimeUnit.MINUTES);
    }

    /**
     * signIn
     *
     * @param uId
     * @param mac
     * @param device
     * @return
     */
    public static SignInformationModel SignIn(String uId, String mac, String device) {
        SignInformationModel model = CreateSignInformatin(uId, mac, device);
        SignRecordPutThreadPool.execute(new SyncPutSignVerifyToken(model));
        return model;
    }

    private static SignInformationModel CreateSignInformatin(String uId, String mac, String device) {
        SignInformationModel model = new SignInformationModel(uId, mac, device);
        model.encodeToken();
        return model;
    }

    /**
     * signOut
     *
     * @param token
     */
    public static void SignOut(String token) {
        SignRecordPutThreadPool.execute(() -> {
            SignInformationModel tempModel = null;
            for (SignInformationModel SignRecord : SignRecords) {
                if (SignRecord.token.equals(token)) {
                    tempModel = SignRecord;
                    break;
                }
            }
            synchronized (SignRecords) {
                SignRecords.remove(tempModel);
            }
        });
    }

    /**
     *
     * @param token
     * @return
     */
    public static boolean verifySign(String token) {
        //TODO，需要验证去掉如下的token空判断
        if (token == null || token.isEmpty()) {
            return true;
        }
        SignInformationModel tempEvm = null;
        try {
            for (SignInformationModel SignRecord : SignRecords) {
                if (SignRecord.token.equals(token)) {
                    tempEvm = SignRecord;
                    break;
                }
            }
            if (tempEvm == null) {
                return false;
            }
            return new Date().getTime() - tempEvm.signDateTime < (1000 * SignVerifyTimeOut);
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("verify sign error." + e.getLocalizedMessage(), e);
            return false;
        } finally {
            if (tempEvm != null) {
                synchronized (SignRecords) {
                    tempEvm.signDateTime = new Date().getTime();
                }
            }
        }

    }
}
