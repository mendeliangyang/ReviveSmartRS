/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.Map;

/**
 *
 * @author Administrator
 */
public class DataVaryModel {

    public String tbName; //表名

    /**
     * 1insert，2update，4delete //2的倍数，做异或比较
     */
    public int varyType;

    public Map<String, String> pkValues_insert; //主键值
    
    public Map<String, String> pkValues_update; //主键值
    
    public Map<String, String> pkValues_delete; //主键值

}
