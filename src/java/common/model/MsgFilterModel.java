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
public class MsgFilterModel {
    public MsgFilterModel(){}
    public String pushMsgId;//推送消息Id
    public String dbTable;//需要操作的表
    public String sqlFilter;//条件
    public short pageSize;//每次推送多少条数据 ，防止数据量过大，所以设置一个阀值，  //消息中包含总数据的Count。
    public Set<String> dbColumns = new HashSet<>();//需要推送的列 select * 会推送大量无用数据，所以最好指定需要推送的列
    public Set<String> dbURLColumns = new HashSet<>();//需要转换 url 的列
    
    
    public String buildStr; //根据条件构造的执行参数
    
    public String rsid; //
    
    
    public DataVaryModel varyData;// 传递  vary ，每次 处理完成，清除该属性
}
