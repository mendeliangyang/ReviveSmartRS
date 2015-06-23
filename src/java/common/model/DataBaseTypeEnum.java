/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

public enum DataBaseTypeEnum {

    number("decimal"), charset("char"),date("date"),datetime("datetime"),time("time"),decimal("decimal1");
    
    private String describe;

    DataBaseTypeEnum(String idx) {
        this.describe = idx;
    }

    public String getDescribe() {
        return describe;
    }

    @Override
    public String toString() {
        return describe;
    }
    
}
