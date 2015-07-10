/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class FileDepotParamModel {

    public String token;//登录凭证

    public String rsid; //rsid
    public String ownid; //wonid
    public Set<DepotFileDetailModel> fileDetaile;  //文件信息 。文件名称，文件类型，文件操作是否覆盖
    
    public int selectFlag=0; //查询标记，0，无需构造查询语句无需参照改参数。 1表示按照 fileIds 查询，2表示安装fileTypes 查询

    public FileDepotParamModel() {
        fileDetaile = new HashSet<>();
    }

    public FileDepotParamModel(String pOwnid) {
        this.ownid = pOwnid;
        fileDetaile = new HashSet<>();
    }

    public void addFileDetail(String pFileName, String pFileOwnType, String pFileOperate) {
        fileDetaile.add(new DepotFileDetailModel(pFileName, pFileOwnType, pFileOperate));
    }

    public void addFileDetail(String pFileId) {
        DepotFileDetailModel fileDetailModel = new DepotFileDetailModel();
        fileDetailModel.fileId = pFileId;
        fileDetaile.add(fileDetailModel);
    }

    public void addFileDetail(String pFileName, String pFileOwnType, String pFileOperate, String pFileId) {
        fileDetaile.add(new DepotFileDetailModel(pFileName, pFileOwnType, pFileOperate, pFileId));
    }

    public DepotFileDetailModel getFileDetailModel(String pFileName) {
        for (DepotFileDetailModel fileDetaile1 : fileDetaile) {
            if (fileDetaile1.fileName.equals(pFileName)) {
                return fileDetaile1;
            }
        }
        return null;
    }

    public void destroySelf() {
        rsid = null;
        ownid = null;
        fileDetaile = null;
    }

}
