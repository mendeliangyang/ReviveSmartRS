/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

/**
 *
 * @author Administrator
 */
public class DepotFileDetailModel {

    public String fileName;
    public String fileOwnType;
    public String fileOperate;

    public DepotFileDetailModel() {
    }

    public DepotFileDetailModel(String pFileName, String pFileOwnType, String pFileOperate) {
        this.fileName = pFileName;
        this.fileOwnType = pFileOwnType;
        this.fileOperate = pFileOperate;
    }
}
