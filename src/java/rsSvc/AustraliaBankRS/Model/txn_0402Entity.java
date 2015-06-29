/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.AustraliaBankRS.Model;

import common.DatagramCoder;

/**
 *
 * @author Administrator
 */
public class txn_0402Entity implements ISocketModel {

    public char[] CardNo;
    public final int iCardNoLen = 200;

    public byte[] OldPasswd;
    public final int iOldPasswdLen = 30;

    public byte[] NewPasswd;
    public final int iNewPasswdLen = 30;

    public char[] IdCardType;
    public final int iIdCardTypeLen = 3;

    public char[] IdCardNo;
    public final int iIdCardNoLen = 20;

    public final int iSelfByteTotalLen = iCardNoLen + iOldPasswdLen + iNewPasswdLen + iIdCardTypeLen + iIdCardNoLen;

    @Override
    public byte[] toBytesFromSelf() {
        byte[] byteSelf = new byte[iSelfByteTotalLen];

        System.arraycopy(DatagramCoder.getBytes(this.CardNo), 0, byteSelf, 0, iCardNoLen);
        System.arraycopy(this.OldPasswd, 0, byteSelf, iCardNoLen, 30);
        System.arraycopy(this.NewPasswd, 0, byteSelf, iCardNoLen + iOldPasswdLen, 30);
        System.arraycopy(DatagramCoder.getBytes(this.IdCardType), 0, byteSelf, iCardNoLen + iOldPasswdLen + iNewPasswdLen, 3);
        System.arraycopy(DatagramCoder.getBytes(this.IdCardNo), 0, byteSelf, iCardNoLen + iOldPasswdLen + iNewPasswdLen + iIdCardTypeLen, 20);

        return byteSelf;
    }

}
