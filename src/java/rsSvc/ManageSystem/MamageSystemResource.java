/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.ManageSystem;

import common.DBHelper;
import common.DeployInfo;
import common.FormationResult;
import common.UtileSmart;
import common.model.ExecuteResultParam;
import common.model.ParamBaseModel;
import common.model.ResponseResultCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import rsSvc.SignVerify.SignInformationModel;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("mamageSystem")
public class MamageSystemResource {

    MamageSystemAnalyzeParam mamageSysAnalyze = new MamageSystemAnalyzeParam();
    private final FormationResult formationResult = new FormationResult();
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MamageSystemResource
     */
    public MamageSystemResource() {
    }

    @POST
    @Path("DeleteOrg")
    public String DeleteOrg(String param) {
        String paramKey_orgNum = "orgNum";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_orgNum, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);
            // verify token
            rsSvc.SignVerify.SignCommon.verifySign(mamageSysAnalyze.getToken(), true);

            sqlStr = String.format("SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr == null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构不存在", param));
                //new ExecuteResultParam("该问题已经处理，不能再进行操作", param)
            } else if (Integer.parseInt(selectResultStr) != 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构下存在子机构不能删除", param));
            }
            sqlStr = String.format("SELECT count(*) as deviceCount FROM device where deviceOrgNo='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr != null && Integer.parseInt(selectResultStr) != 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构下存设备不能删除", param));
            }
            sqlStr = String.format("SELECT count(*) userCount FROM userInfo where userOrgNum ='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr != null && Integer.parseInt(selectResultStr) != 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构下存用户不能删除", param));
            }

            sqlStr = String.format("delete organization where orgNum ='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));
            resultParam = DBHelper.ExecuteSql(mamageSysAnalyze.getRSID(), sqlStr);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, resultParam);
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("InvalidDevice")
    public String InvalidDevice(String param) {
        String paramKey_id = "id";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_id, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);
            // verify token
            rsSvc.SignVerify.SignCommon.verifySign(mamageSysAnalyze.getToken(), true);

            //SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'
            sqlStr = String.format("select deviceUser from device where id='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_id));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr != null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该设备存在使用者请，不能删除。", param));
                //new ExecuteResultParam("该问题已经处理，不能再进行操作", param)
            }

            sqlStr = String.format("update device set deviceValid='0' where id='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_id));
            resultParam = DBHelper.ExecuteSql(mamageSysAnalyze.getRSID(), sqlStr);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, resultParam);
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("SignInMM")
    public String SignInMM(String param) {
        String paramKey_userIdcd = "tellerIdcd";
        String paramKey_userPwd = "tellerPwd";
        String paramKey_deviceMac = "deviceMac";

        ExecuteResultParam resultParam = null;
        String sqlStr = null;
        Map<String, Object> paramMap = null;
        Map<String, Map<String, String>> resultMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_userIdcd, null);
            paramMap.put(paramKey_userPwd, null);
            paramMap.put(paramKey_deviceMac, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);

            //SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'
            sqlStr = String.format("SELECT t.*,o.orgName ,o.orgLevel,o.orgUpNum ,d.deviceMac,d.deviceOrgNo FROM teller t left join organization o on t.tellerOrgNo=o.orgNum "
                    + "left join device d on d.deviceUser=t.id where t.tellerIdcd='%s' and t.tellerPwd='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_userIdcd), UtileSmart.getStringFromMap(paramMap, paramKey_userPwd));

            resultMap = DBHelper.ExecuteSqlSelectReturnMap(mamageSysAnalyze.getRSID(), sqlStr, "teller");
            if (resultMap != null && resultMap.size() == 1) {

                //, UtileSmart.getStringFromMap(paramMap, paramKey_deviceMac)
                for (Map.Entry<String, Map<String, String>> entrySet : resultMap.entrySet()) {
                    Map<String, String> value = entrySet.getValue();
                    if (!UtileSmart.getStringFromMap(paramMap, paramKey_deviceMac).equals(value.get("deviceMac"))) {
                        return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("输入用户名或密码错误或设备mac值不匹配.", param));
                    } else if (value.get("tellerOrgNo").equals(value.get("deviceOrgNo"))) {
                        return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("输入用户名或密码错误或机构值不匹配.", param));
                    }
                }

                SignInformationModel signModel = rsSvc.SignVerify.SignCommon.SignIn(resultMap.keySet().iterator().next(), null, null);
                return formationResult.formationResult(ResponseResultCode.Success, signModel.token, new ExecuteResultParam(resultMap, false));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("输入用户名或密码错误或设备mac值不匹配.", param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.cleanMapTDString(resultMap);
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("PutClientInfo")
    public String PutClientInfo(String param) {
        String wordInfo = "wordInfo";
        String personalInfo = "personalInfo";
        String personal_chinaName = "chinaName";
        String personal_country = "country";
        String personal_nameSpell = "nameSpell";
        String personal_readingDegree = "readingDegree";
        String personal_sex = "sex";
        String personal_idType = "idType";
        String personal_idTypeName = "idTypeName";
        String personal_residentYear = "residentYear";
        String personal_annualIncome = "annualIncome";
        String personal_company_dep = "company_dep";
        String personal_schoolName = "schoolName";
        String personal_readingGrade = "readingGrade";
        String personal_expectCompDate = "expectCompDate";
        String personal_workYear = "workYear";
        String personal_profession = "profession";
        String personal_business = "business";
        String personal_professionTitle = "professionTitle";
        String personal_address = "address";
        String personal_housingAdd = "housingAdd";
        String personal_engLastName = "engLastName";
        String personal_engFirstName = "engFirstName";
        String personal_certName = "certName";
        String personal_houMonLoan = "houMonLoan";
        String personal_prePost = "prePost";
        String personal_preZoneNo = "preZoneNo";
        String personal_prePhone = "prePhone";
        String personal_preMobile = "preMobile";
        String personal_nation = "nation";
        String personal_photoData = "photoData";
        String personal_Referee_No = "Referee_No";
        String personal_isCurentTJ = "isCurentTJ";
        String personal_expectGrantMoney = "expectGrantMoney";
        String personal_birthDate2 = "birthDate2";
        String personal_birthYear = "birthYear";
        String personal_birthMonth = "birthMonth";
        String personal_birthDay = "birthDay";
        String personal_issuing = "issuing";
        String personal_validDate = "validDate";
        String personal_nation2 = "nation2";
        String personal_cardId = "cardId";
        String personal_preAddrProv = "preAddrProv";
        String personal_marryStaus = "marryStaus";
        String personal_educateStatus = "educateStatus";
        String personal_houseCondition = "houseCondition";
        String personal_preAddrCity = "preAddrCity";
        String personal_preAddrCounty = "preAddrCounty";
        String personal_score = "score";

        String applyPerInfo = "applyPerInfo";
        String applyPer_housingAdd = "housingAdd";
        String applyPer_housingAddPost = "housingAddPost";
        String applyPer_companyAdd = "companyAdd";
        String applyPer_compAddPost = "compAddPost";
        String applyPer_schoolAdd = "schoolAdd";
        String applyPer_schoolAddPost = "schoolAddPost";
        String applyPer_housingTel = "housingTel";
        String applyPer_phoneNum = "phoneNum";
        String applyPer_companyTel = "companyTel";

        String linkManInfo = "linkManInfo";
        String linkman_lmName = "lmName";
        String linkman_familylmName = "familylmName";
        String linkman_relation1 = "relation1";
        String linkman_relation2 = "relation2";
        String linkman_lmCompany = "lmCompany";
        String linkman_lmCompTel = "lmCompTel";
        String linkman_lmAddress = "lmAddress";
        String linkman_lmTel = "lmTel";
        String linkman_lmMobileNum = "lmMobileNum";
        String linkman_lmPreZoneno = "lmPreZoneno";
        String linkman_sex = "sex";
        String linkman_relation = "relation";

        String houseHoleInfo = "houseHoleInfo";
        String houseHole_name = "name";
        String houseHole_tel = "tel";
        String houseHole_hukouRelMobile = "hukouRelMobile";
        String houseHole_address = "address";
        String houseHole_hukouRelZoneno = "hukouRelZoneno";
        String houseHole_hukkouRelphone = "hukkouRelphone";
        String houseHole_regAddrCounty = "regAddrCounty";
        String houseHole_regAddrDetail = "regAddrDetail";
        String houseHole_sex = "sex";
        String houseHole_relation = "relation";
        String houseHole_regAddrProv = "regAddrProv";
        String houseHole_regAddrCity = "regAddrCity";

        String suppCardInfo = "suppCardInfo";
        String suppCard_chinaName = "chinaName";
        String suppCard_country = "country";
        String suppCard_nameSpell = "nameSpell";
        String suppCard_idType = "idType";
        String suppCard_idNum = "idNum";
        String suppCard_sex = "sex";
        String suppCard_birthDate = "birthDate";
        String suppCard_relation = "relation";
        String suppCard_mobilePhone = "mobilePhone";
        String suppCard_address = "address";
        String suppCard_addPost = "addPost";
        String suppCard_per = "per";
        String suppCard_engLastName = "engLastName";
        String suppCard_engFirstName = "engFirstName";
        String suppCard_perCerTName = "perCerTName";
        String suppCard_perSex = "perSex";
        String suppCard_perRelZoneno = "perRelZoneno";
        String suppCard_perRelphone = "perRelphone";
        String suppCard_regAddrProv = "regAddrProv";
        String suppCard_regAddrCity = "regAddrCity";
        String suppCard_regAddrCounty = "regAddrCounty";
        String suppCard_regAddrDetail = "regAddrDetail";

        String careerInfo = "careerInfo";
        String career_otherCareer = "otherCareer";
        String career_compName = "compName";
        String career_compAddrDetail = "compAddrDetail";
        String career_compPost = "compPost";
        String career_compZoneNo = "compZoneNo";
        String career_compPhone = "compPhone";
        String career_workYear = "workYear";
        String career_compAddrProv = "compAddrProv";
        String career_compAddrCity = "compAddrCity";
        String career_career = "career";
        String career_techGrade = "techGrade";
        String career_tradeKind = "tradeKind";
        String career_techPosi = "techPosi";
        String career_compAddrCounty = "compAddrCounty";

        String otherInfo = "otherInfo";
        String other_NORMAL_CARD = "NORMAL_CARD";
        String other_EMAIL = "EMAIL";
        String other_REPAY_MARK = "REPAY_MARK";
        String other_REPAY_CARD = "REPAY_CARD";
        String other_MANAGER_CERT = "MANAGER_CERT";
        String other_ACCEPT_INSTITUTION = "ACCEPT_INSTITUTION";
        String other_ACCEPT_INSTITUTION_CODE = "ACCEPT_INSTITUTION_CODE";
        String other_MANAGER = "MANAGER";
        String other_POST_ADDR = "POST_ADDR";
        String other_PRE4 = "PRE4";

        String scanCodeNub = "scanCodeNub";
        String serialId = "serialId";
        String submitStatus = "submitStatus";
        String updateTime = "updateTime";

        ExecuteResultParam resultParam = null;
        String sqlStr = null;

        ParamBaseModel baseModel = null;
        JSONObject wordInfoJson = null, personalInfoJson = null, applyPerInfoJson = null, linkManInfoJson = null, houseHoleInfoJson = null, suppCardInfoJson = null, careerInfoJson = null, otherInfoJson = null;
        try {
            baseModel = mamageSysAnalyze.AnalyzeParamBase(param);
            wordInfoJson = baseModel.jsonBody.getJSONObject(wordInfo);

            personalInfoJson = wordInfoJson.getJSONObject(personalInfo);
            applyPerInfoJson = wordInfoJson.getJSONObject(applyPerInfo);
            linkManInfoJson = wordInfoJson.getJSONObject(linkManInfo);
            houseHoleInfoJson = wordInfoJson.getJSONObject(houseHoleInfo);
            suppCardInfoJson = wordInfoJson.getJSONObject(suppCardInfo);
            careerInfoJson = wordInfoJson.getJSONObject(careerInfo);
            otherInfoJson = wordInfoJson.getJSONObject(otherInfo);

            String sql = "INSERT INTO dbo.clientInfo ( id, personal_chinaName, personal_country, personal_nameSpell, personal_readingDegree, personal_sex, "
                    + "personal_idType, personal_idTypeName, personal_residentYear, personal_annualIncome, personal_company_dep, personal_schoolName, personal_readingGrade,"
                    + " personal_expectCompDate, personal_workYear, personal_profession, personal_business, personal_professionTitle, personal_address, personal_housingAdd, "
                    + "personal_engLastName, personal_engFirstName, personal_certName, personal_houMonLoan, personal_prePost, personal_preZoneNo, personal_prePhone, personal_preMobile, "
                    + "personal_nation, personal_photoData, personal_Referee_No, personal_isCurentTJ, personal_expectGrantMoney, personal_birthDate2, personal_birthYear, personal_birthMonth,"
                    + " personal_birthDay, personal_issuing, personal_validDate, personal_nation2, personal_cardId, personal_preAddrProv, personal_marryStaus, personal_educateStatus,"
                    + " personal_houseCondition, personal_preAddrCity, personal_preAddrCounty, personal_score, applyPer_housingAdd, applyPer_housingAddPost, applyPer_companyAdd, "
                    + "applyPer_compAddPost, applyPer_schoolAdd, applyPer_schoolAddPost, applyPer_housingTel, applyPer_phoneNum, applyPer_companyTel, linkman_familylmName, linkman_lmCompany, "
                    + "linkman_lmCompTel, linkman_lmAddress, linkman_lmTel, linkman_lmMobileNum, linkman_lmPreZoneno, linkman_sex, houseHole_name, houseHole_tel, houseHole_hukouRelMobile, houseHole_address, "
                    + "houseHole_hukouRelZoneno, houseHole_hukkouRelphone, houseHole_regAddrCounty, houseHole_regAddrDetail, houseHole_sex, houseHole_relation, houseHole_regAddrProv, houseHole_regAddrCity, "
                    + "suppCard_chinaName, suppCard_country, suppCard_nameSpell, suppCard_idType, suppCard_idNum, suppCard_sex, suppCard_birthDate, suppCard_relation, suppCard_mobilePhone, suppCard_address,"
                    + " suppCard_addPost, suppCard_per, suppCard_engLastName, suppCard_engFirstName, suppCard_perCerTName, suppCard_perSex, suppCard_perRelZoneno, suppCard_perRelphone, suppCard_regAddrProv, "
                    + "suppCard_regAddrCity, suppCard_regAddrCounty, suppCard_regAddrDetail, career_otherCareer, career_compName, career_compAddrDetail, career_compPost, career_compZoneNo, career_compPhone, "
                    + "career_workYear, career_compAddrProv, career_compAddrCity, career_career, career_techGrade, career_tradeKind, career_techPosi, career_compAddrCounty, other_NORMAL_CARD, other_EMAIL, "
                    + "other_REPAY_MARK, other_REPAY_CARD, other_MANAGER_CERT, other_ACCEPT_INSTITUTION, other_ACCEPT_INSTITUTION_CODE, other_MANAGER, other_POST_ADDR, other_PRE4, scanCodeNub, serialId, "
                    + "submitStatus, updateTime, linkman_relation )VALUES ( ";
//                    + " VALUES ("
//                    + "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', "
//                    + "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', "
//                    + "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', "
//                    + "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', "
//                    + "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s' )";

            String strId = UtileSmart.getUUID();
            StringBuffer sb = new StringBuffer();
            sb.append(sql).append("'").append(strId).append("',").append("'").append(personalInfoJson.getString(personal_chinaName)).append("',").append("'").append(personalInfoJson.getString(personal_country)).append("',").append("'").append(personalInfoJson.getString(personal_nameSpell)).append("',").append("'").append(personalInfoJson.getString(personal_readingDegree)).append("',").append("'").append(
                    personalInfoJson.getString(personal_sex)).append("',").append("'").append(personalInfoJson.getString(personal_idType)).append("',").append("'").append(personalInfoJson.getString(personal_idTypeName)).append("',").append("'").append(personalInfoJson.getString(personal_residentYear)).append("',").append("'").append(
                            personalInfoJson.getString(personal_annualIncome)).append("',").append("'").append(personalInfoJson.getString(personal_company_dep)).append("',").append("'").append(personalInfoJson.getString(personal_schoolName)).append("',").append("'").append(personalInfoJson.getString(personal_readingGrade)).append("',").append("'").append(
                            personalInfoJson.getString(personal_expectCompDate)).append("',").append("'").append(personalInfoJson.getString(personal_workYear)).append("',").append("'").append(personalInfoJson.getString(personal_profession)).append("',").append("'").append(personalInfoJson.getString(personal_business)).append("',").append("'").append(
                            personalInfoJson.getString(personal_professionTitle)).append("',").append("'").append(personalInfoJson.getString(personal_address)).append("',").append("'").append(personalInfoJson.getString(personal_housingAdd)).append("',").append("'").append(personalInfoJson.getString(personal_engLastName)).append("',").append("'").append(
                            personalInfoJson.getString(personal_engFirstName)).append("',").append("'").append(personalInfoJson.getString(personal_certName)).append("',").append("'").append(personalInfoJson.getString(personal_houMonLoan)).append("',").append("'").append(personalInfoJson.getString(personal_prePost)).append("',").append("'").append(
                            personalInfoJson.getString(personal_preZoneNo)).append("',").append("'").append(personalInfoJson.getString(personal_prePhone)).append("',").append("'").append(personalInfoJson.getString(personal_preMobile)).append("',").append("'").append(personalInfoJson.getString(personal_nation)).append("',").append("'").append(
                            personalInfoJson.getString(personal_photoData)).append("',").append("'").append(personalInfoJson.getString(personal_Referee_No)).append("',").append("'").append(personalInfoJson.getString(personal_isCurentTJ)).append("',").append("'").append(personalInfoJson.getString(personal_expectGrantMoney)).append("',").append("'").append(
                            personalInfoJson.getString(personal_birthDate2)).append("',").append("'").append(personalInfoJson.getString(personal_birthYear)).append("',").append("'").append(personalInfoJson.getString(personal_birthMonth)).append("',").append("'").append(personalInfoJson.getString(personal_birthDay)).append("',").append("'").append(
                            personalInfoJson.getString(personal_issuing)).append("',").append("'").append(personalInfoJson.getString(personal_validDate)).append("',").append("'").append(personalInfoJson.getString(personal_nation2)).append("',").append("'").append(personalInfoJson.getString(personal_cardId)).append("',").append("'").append(
                            personalInfoJson.getString(personal_preAddrProv)).append("',").append("'").append(personalInfoJson.getString(personal_marryStaus)).append("',").append("'").append(personalInfoJson.getString(personal_educateStatus)).append("',").append("'").append(personalInfoJson.getString(personal_houseCondition)).append("',").append("'").append(
                            personalInfoJson.getString(personal_preAddrCity)).append("',").append("'").append(personalInfoJson.getString(personal_preAddrCounty)).append("',").append("'").append(personalInfoJson.getString(personal_score)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_housingAdd)).append("',").append("'").append(
                            applyPerInfoJson.getString(applyPer_housingAddPost)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_companyAdd)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_compAddPost)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_schoolAdd)).append("',").append("'").append(
                            applyPerInfoJson.getString(applyPer_schoolAddPost)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_housingTel)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_phoneNum)).append("',").append("'").append(applyPerInfoJson.getString(applyPer_companyTel)).append("',").append("'").append(
                            linkManInfoJson.getString(linkman_familylmName)).append("',").append("'").append(linkManInfoJson.getString(linkman_lmCompany)).append("',").append("'").append(linkManInfoJson.getString(linkman_lmCompTel)).append("',").append("'").append(linkManInfoJson.getString(linkman_lmAddress)).append("',").append("'").append(
                            linkManInfoJson.getString(linkman_lmTel)).append("',").append("'").append(linkManInfoJson.getString(linkman_lmMobileNum)).append("',").append("'").append(linkManInfoJson.getString(linkman_lmPreZoneno)).append("',").append("'").append(linkManInfoJson.getString(linkman_sex)).append("',").append("'").append(
                            houseHoleInfoJson.getString(houseHole_name)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_tel)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_hukouRelMobile)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_address)).append("',").append("'").append(
                            houseHoleInfoJson.getString(houseHole_hukouRelZoneno)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_hukkouRelphone)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_regAddrCounty)).append("',").append("'").append(
                            houseHoleInfoJson.getString(houseHole_regAddrDetail)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_sex)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_relation)).append("',").append("'").append(houseHoleInfoJson.getString(houseHole_regAddrProv)).append("',").append("'").append(
                            houseHoleInfoJson.getString(houseHole_regAddrCity)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_chinaName)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_country)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_nameSpell)).append("',").append("'").append(
                            suppCardInfoJson.getString(suppCard_idType)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_idNum)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_sex)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_birthDate)).append("',").append("'").append(
                            suppCardInfoJson.getString(suppCard_relation)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_mobilePhone)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_address)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_addPost)).append("',").append("'").append(
                            suppCardInfoJson.getString(suppCard_per)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_engLastName)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_engFirstName)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_perCerTName)).append("',").append("'").append(
                            suppCardInfoJson.getString(suppCard_perSex)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_perRelZoneno)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_perRelphone)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_regAddrProv)).append("',").append("'").append(
                            suppCardInfoJson.getString(suppCard_regAddrCity)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_regAddrCounty)).append("',").append("'").append(suppCardInfoJson.getString(suppCard_regAddrDetail)).append("',").append("'").append(careerInfoJson.getString(career_otherCareer)).append("',").append("'").append(
                            careerInfoJson.getString(career_compName)).append("',").append("'").append(careerInfoJson.getString(career_compAddrDetail)).append("',").append("'").append(careerInfoJson.getString(career_compPost)).append("',").append("'").append(careerInfoJson.getString(career_compZoneNo)).append("',").append("'").append(
                            careerInfoJson.getString(career_compPhone)).append("',").append("'").append(careerInfoJson.getString(career_workYear)).append("',").append("'").append(careerInfoJson.getString(career_compAddrProv)).append("',").append("'").append(careerInfoJson.getString(career_compAddrCity)).append("',").append("'").append(
                            careerInfoJson.getString(career_career)).append("',").append("'").append(careerInfoJson.getString(career_techGrade)).append("',").append("'").append(careerInfoJson.getString(career_tradeKind)).append("',").append("'").append(careerInfoJson.getString(career_techPosi)).append("',").append("'").append(
                            careerInfoJson.getString(career_compAddrCounty)).append("',").append("'").append(otherInfoJson.getString(other_NORMAL_CARD)).append("',").append("'").append(otherInfoJson.getString(other_EMAIL)).append("',").append("'").append(otherInfoJson.getString(other_REPAY_MARK)).append("',").append("'").append(
                            otherInfoJson.getString(other_REPAY_CARD)).append("',").append("'").append(otherInfoJson.getString(other_MANAGER_CERT)).append("',").append("'").append(otherInfoJson.getString(other_ACCEPT_INSTITUTION)).append("',").append("'").append(otherInfoJson.getString(other_ACCEPT_INSTITUTION_CODE)).append("',").append("'").append(
                            otherInfoJson.getString(other_MANAGER)).append("',").append("'").append(otherInfoJson.getString(other_POST_ADDR)).append("',").append("'").append(otherInfoJson.getString(other_PRE4)).append("',").append("'").append(wordInfoJson.getString(scanCodeNub)).append("',").append("'").append(
                            baseModel.jsonBody.getString(serialId)).append("',").append("'").append(baseModel.jsonBody.getString(submitStatus)).append("',").append("'").append(baseModel.jsonBody.getString(updateTime)).append("',").
                    append("'").append(linkManInfoJson.getString(linkman_relation)).append("'");
            sb.append(")");
            String sql1 = sb.toString();
//            sql1 = String.format(sql, strId,
//                    personalInfoJson.getString(personal_chinaName), personalInfoJson.getString(personal_country), personalInfoJson.getString(personal_nameSpell), personalInfoJson.getString(personal_readingDegree),
//                    personalInfoJson.getString(personal_sex), personalInfoJson.getString(personal_idType), personalInfoJson.getString(personal_idTypeName), personalInfoJson.getString(personal_residentYear),
//                    personalInfoJson.getString(personal_annualIncome), personalInfoJson.getString(personal_company_dep), personalInfoJson.getString(personal_schoolName), personalInfoJson.getString(personal_readingGrade),
//                    personalInfoJson.getString(personal_expectCompDate), personalInfoJson.getString(personal_workYear), personalInfoJson.getString(personal_profession), personalInfoJson.getString(personal_business),
//                    personalInfoJson.getString(personal_professionTitle), personalInfoJson.getString(personal_address), personalInfoJson.getString(personal_housingAdd), personalInfoJson.getString(personal_engLastName),
//                    personalInfoJson.getString(personal_engFirstName), personalInfoJson.getString(personal_certName), personalInfoJson.getString(personal_houMonLoan), personalInfoJson.getString(personal_prePost),
//                    personalInfoJson.getString(personal_preZoneNo), personalInfoJson.getString(personal_prePhone), personalInfoJson.getString(personal_preMobile), personalInfoJson.getString(personal_nation),
//                    personalInfoJson.getString(personal_photoData), personalInfoJson.getString(personal_Referee_No), personalInfoJson.getString(personal_isCurentTJ), personalInfoJson.getString(personal_expectGrantMoney),
//                    personalInfoJson.getString(personal_birthDate2), personalInfoJson.getString(personal_birthYear), personalInfoJson.getString(personal_birthMonth), personalInfoJson.getString(personal_birthDay),
//                    personalInfoJson.getString(personal_issuing), personalInfoJson.getString(personal_validDate), personalInfoJson.getString(personal_nation2), personalInfoJson.getString(personal_cardId),
//                    personalInfoJson.getString(personal_preAddrProv), personalInfoJson.getString(personal_marryStaus), personalInfoJson.getString(personal_educateStatus), personalInfoJson.getString(personal_houseCondition),
//                    personalInfoJson.getString(personal_preAddrCity), personalInfoJson.getString(personal_preAddrCounty), personalInfoJson.getString(personal_score), applyPerInfoJson.getString(applyPer_housingAdd),
//                    applyPerInfoJson.getString(applyPer_housingAddPost), applyPerInfoJson.getString(applyPer_companyAdd), applyPerInfoJson.getString(applyPer_compAddPost), applyPerInfoJson.getString(applyPer_schoolAdd),
//                    applyPerInfoJson.getString(applyPer_schoolAddPost), applyPerInfoJson.getString(applyPer_housingTel), applyPerInfoJson.getString(applyPer_phoneNum), applyPerInfoJson.getString(applyPer_companyTel),
//                    linkManInfoJson.getString(linkman_familylmName), linkManInfoJson.getString(linkman_lmCompany), linkManInfoJson.getString(linkman_lmCompTel), linkManInfoJson.getString(linkman_lmAddress),
//                    linkManInfoJson.getString(linkman_lmTel), linkManInfoJson.getString(linkman_lmMobileNum), linkManInfoJson.getString(linkman_lmPreZoneno), linkManInfoJson.getString(linkman_sex),
//                    houseHoleInfoJson.getString(houseHole_name), houseHoleInfoJson.getString(houseHole_tel), houseHoleInfoJson.getString(houseHole_hukouRelMobile), houseHoleInfoJson.getString(houseHole_address),
//                    houseHoleInfoJson.getString(houseHole_hukouRelZoneno), houseHoleInfoJson.getString(houseHole_hukkouRelphone), houseHoleInfoJson.getString(houseHole_regAddrCounty), houseHoleInfoJson.getString(houseHole_regAddrDetail),
//                    houseHoleInfoJson.getString(houseHole_sex), houseHoleInfoJson.getString(houseHole_relation), houseHoleInfoJson.getString(houseHole_regAddrProv), houseHoleInfoJson.getString(houseHole_regAddrCity),
//                    suppCardInfoJson.getString(suppCard_chinaName), suppCardInfoJson.getString(suppCard_country), suppCardInfoJson.getString(suppCard_nameSpell),suppCardInfoJson.getString(suppCard_idType), 
//                    suppCardInfoJson.getString(suppCard_idNum), suppCardInfoJson.getString(suppCard_sex), suppCardInfoJson.getString(suppCard_birthDate), suppCardInfoJson.getString(suppCard_relation), 
//                    suppCardInfoJson.getString(suppCard_mobilePhone), suppCardInfoJson.getString(suppCard_address), suppCardInfoJson.getString(suppCard_addPost),suppCardInfoJson.getString(suppCard_per), 
//                    suppCardInfoJson.getString(suppCard_engLastName), suppCardInfoJson.getString(suppCard_engFirstName), suppCardInfoJson.getString(suppCard_perCerTName),suppCardInfoJson.getString(suppCard_perSex), 
//                    suppCardInfoJson.getString(suppCard_perRelZoneno), suppCardInfoJson.getString(suppCard_perRelphone), suppCardInfoJson.getString(suppCard_regAddrProv), suppCardInfoJson.getString(suppCard_regAddrCity), 
//                    suppCardInfoJson.getString(suppCard_regAddrCounty), suppCardInfoJson.getString(suppCard_regAddrDetail), careerInfoJson.getString(career_otherCareer),careerInfoJson.getString(career_compName),
//                    careerInfoJson.getString(career_compAddrDetail), careerInfoJson.getString(career_compPost), careerInfoJson.getString(career_compZoneNo),careerInfoJson.getString(career_compPhone), 
//                    careerInfoJson.getString(career_workYear), careerInfoJson.getString(career_compAddrProv), careerInfoJson.getString(career_compAddrCity),careerInfoJson.getString(career_career), 
//                    careerInfoJson.getString(career_techGrade), careerInfoJson.getString(career_tradeKind), careerInfoJson.getString(career_techPosi), careerInfoJson.getString(career_compAddrCounty),
//                    otherInfoJson.getString(other_NORMAL_CARD), otherInfoJson.getString(other_EMAIL), otherInfoJson.getString(other_REPAY_MARK),otherInfoJson.getString(other_REPAY_CARD), 
//                    otherInfoJson.getString(other_MANAGER_CERT), otherInfoJson.getString(other_ACCEPT_INSTITUTION), otherInfoJson.getString(other_ACCEPT_INSTITUTION_CODE),otherInfoJson.getString(other_MANAGER),
//                    otherInfoJson.getString(other_POST_ADDR), otherInfoJson.getString(other_PRE4), wordInfoJson.getString(scanCodeNub),baseModel.jsonBody.getString(serialId), 
//                    baseModel.jsonBody.getString(submitStatus), baseModel.jsonBody.getString(updateTime),linkManInfoJson.getString(linkman_relation));
//            
            resultParam = DBHelper.ExecuteSql(mamageSysAnalyze.getRSID(), sql1);
            if (resultParam.ResultCode >= 0) {
                //notify data changed

                JSONObject resultJson = new JSONObject();
                resultJson.accumulate("id", strId);
                if (resultParam.ResultJsonObject == null) {
                    resultParam.ResultJsonObject = new JSONObject();
                }
                resultParam.ResultJsonObject.accumulate(DeployInfo.ResultDataTag, resultJson);
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            UtileSmart.FreeObjects(resultParam, param, sqlStr);
        }
    }

    @POST
    @Path("SignInM")
    public String SignInM(String param) {
        String paramKey_userIdcd = "userIdcd";
        String paramKey_userPwd = "userPwd";

        ExecuteResultParam resultParam = null;
        String sqlStr = null;
        Map<String, Object> paramMap = null;
        Map<String, Map<String, String>> resultMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_userIdcd, null);
            paramMap.put(paramKey_userPwd, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);

            //SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'
            sqlStr = String.format("SELECT u.id,u.userAddress,u.userEmail,u.userIdcd,u.userMobilePhone,u.userName,u.userNum,u.userOrgNum,o.orgName as orgName,o.orgLevel,u.userRole,u.userSex,u.userTelephone ,r.pow_id,r.name as rouleName ,p.name powerName,p.id as powerId FROM userInfo u left join organization o on u.userOrgNum=o.orgNum  left join roleInfo r on u.userRole=r.id left join power p on r.pow_id=p.id where u.userIdcd='%s' and u.userPwd='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_userIdcd), UtileSmart.getStringFromMap(paramMap, paramKey_userPwd));

            resultMap = DBHelper.ExecuteSqlSelectReturnMap(mamageSysAnalyze.getRSID(), sqlStr, "userInfo");
            if (resultMap != null && resultMap.size() == 1) {
                SignInformationModel signModel = rsSvc.SignVerify.SignCommon.SignIn(resultMap.keySet().iterator().next(), null, null);
                return formationResult.formationResult(ResponseResultCode.Success, signModel.token, new ExecuteResultParam(resultMap, false));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("输入用户名或密码错误.", param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.cleanMapTDString(resultMap);
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("SignOutM")
    public String SignOutM(String param) {
        try {
            rsSvc.SignVerify.SignCommon.SignOut("");
            return formationResult.formationResult(ResponseResultCode.Success);
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
        }
    }

    @POST
    @Path("SetTableInfoVisable")
    public String SetTableInfoVisable(String param) {
        String paramKey_setUpDatas = "setUpDatas";
        String paramKey_tablename = "tablename";
        String paramKey_name = "name";
        String paramKey_visable = "visable";

        ExecuteResultParam resultParam = null;
        JSONObject tempObject = null;
        Set<String> sqlStrs = null;
        Map<String, Object> paramMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_setUpDatas, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);
            JSONArray jsonArray = UtileSmart.GetJSONArrayFromMap(paramMap, paramKey_setUpDatas);

            sqlStrs = new HashSet<>();
            for (Object temp : jsonArray) {
                tempObject = JSONObject.fromObject(temp);
                sqlStrs.add(String.format("update tableInfo set visable='%s' where tablename='%s' and name='%s' ", UtileSmart.GetJsonString(tempObject, paramKey_visable),
                        UtileSmart.GetJsonString(tempObject, paramKey_tablename),
                        UtileSmart.GetJsonString(tempObject, paramKey_name)));
            }

            resultParam = DBHelper.ExecuteSql(mamageSysAnalyze.getRSID(), sqlStrs);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, resultParam);
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStrs, paramMap);
        }
    }

    @POST
    @Path("SelectAllOrg")
    public String SelectAllOrg(String param) {
        try {
            JSONArray jsonArray = null;
            //SELECT * FROM organization where orgLevel=1
            mamageSysAnalyze.AnalyzeParamBodyToMap(param, null);
            // verify token
            rsSvc.SignVerify.SignCommon.verifySign(mamageSysAnalyze.getToken(), true);
            //分级递归查询
            //jsonArray = SearchOrgRoot(mamageSysAnalyze.getRSID());
            //一次性查询，本地递归组装数据
            jsonArray = SelectRootOrg(mamageSysAnalyze.getRSID());
            JSONObject jsonObj = new JSONObject();
            jsonObj.accumulate(DeployInfo.ResultDataTag, jsonArray);
            return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(jsonObj));
        } catch (Exception ex) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(ex.getLocalizedMessage(), param, ex));
        }
    }

    private JSONArray SearchOrgRoot(String rsid) throws Exception {
        Map<String, Map<String, String>> map = null;
        try {
            map = DBHelper
                    .ExecuteSqlSelectReturnMap(rsid, "SELECT o.orgName [text],o.* FROM organization o  where orgLevel='1'", "organization");
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> value : map.values()) {
                JSONObject rootObj = JSONObject.fromObject(value);
                SearchOrgDown(value.get("orgNum"), rsid, rootObj);
                jsonArray.add(rootObj);
            }
            return jsonArray;
        } catch (Exception ex) {
            throw new Exception("SearchOrgRoot error" + ex.getLocalizedMessage());
        } finally {
            UtileSmart.cleanMapTDString(map);
        }

    }

    private void SearchOrgDown(String upOrgNum, String rsid, JSONObject obj) throws Exception {
        Map<String, Map<String, String>> downMap = null;
        try {
            downMap = DBHelper
                    .ExecuteSqlSelectReturnMap(rsid, String.format("SELECT o.orgName [text],o.* FROM organization o  where orgUpNum='%s'", upOrgNum), "organization");

            if (downMap != null && downMap.size() > 0) {
                for (Map<String, String> value : downMap.values()) {
                    JSONArray downArray = new JSONArray();
                    JSONObject downObj = JSONObject.fromObject(value);
                    SearchOrgDown(value.get("orgNum"), rsid, downObj);
                    downArray.add(downObj);
                    obj.accumulate("children", downArray);
                }
            }

        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("SearchOrgDown error" + ex.getLocalizedMessage() + "upOrgNum:" + upOrgNum, ex);
            throw new Exception("SearchOrgDown error" + ex.getLocalizedMessage() + "upOrgNum:" + upOrgNum);
        } finally {

            UtileSmart.cleanMapTDString(downMap);
        }
    }
//

    public JSONArray SelectRootOrg(String rsid) throws Exception {
        Map<String, Map<String, String>> map = null;
        try {
            map = DBHelper
                    .ExecuteSqlSelectReturnMap(rsid, String.format("SELECT o.orgName [text], o.* FROM organization o "), "organization");
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> value : map.values()) {
                if (value.get("orgNum") == null || value.get("orgUpNum") == null) {
                    continue;
                }
                if (value.get("orgNum").equals(value.get("orgUpNum"))) {
                    JSONObject obj = JSONObject.fromObject(value);
                    findDownOrg(map, obj, value.get("orgNum"));
                    jsonArray.add(obj);
                }
            }
            return jsonArray;
        } finally {
            UtileSmart.cleanMapTDString(map);
        }

    }

    void findDownOrg(Map<String, Map<String, String>> map, JSONObject obj, String upOrgNum) {
        for (Map<String, String> value : map.values()) {
            if (value.get("orgNum") == null || value.get("orgUpNum") == null) {
                continue;
            }
            if (value.get("orgUpNum").equals(upOrgNum) && !value.get("orgNum").equals(value.get("orgUpNum"))) {
                JSONArray downArray = null;
                JSONObject objDown = JSONObject.fromObject(value);
                findDownOrg(map, objDown, value.get("orgNum"));
                if (obj.containsKey("children")) {
                    downArray = obj.getJSONArray("children");
                }
                if (downArray == null) {
                    downArray = new JSONArray();
                }
                downArray.add(objDown);
                obj.remove("children");
                obj.accumulate("children", downArray);
            }
        }
    }

}
