/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc;

import common.model.OperateTypeEnum;
import common.model.ReviveRSParamModel;
import common.model.SignModel;

/**
 *
 * @author Administrator
 */
public interface IAnalyzeReviceParamModel {
    public ReviveRSParamModel transferReviveRSParamModel(String param,OperateTypeEnum operateType) throws Exception;  
    public SignModel transferReviveRSSignModel(String param,OperateTypeEnum operateType) throws Exception;  
}
