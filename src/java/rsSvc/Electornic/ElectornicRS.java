/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.Electornic;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("Electornic")
public class ElectornicRS extends ElectornicBase {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ElectornicRS
     */
    public ElectornicRS() {
    }

    /**
     * 登陆
     *
     * @param param
     * @return
     */
    @POST
    @Path("PadLoginOP")
    public String PadLoginOP(String param) {

        return super.ResponseResult(EnumElectornicRetCode.success, "", "", "\"result\": {\n"
                + "    \"teller\": \"9999\",\n"
                + "    \"name\": \"叶斌\",\n"
                + "    \"orgNum\": \"9999\",\n"
                + "    \"orgName\": \"江苏省分行清算中心\"\n"
                + "  }");

    }

    /**
     * 获取主密钥
     *
     * @param param
     * @return
     */
    @POST
    @Path("GetTMKOp")
    public String GetTMKOp(String param) {
        return "\"retCode\": \"0000\",\n"
                + "  \"retMsg\": \"\",\n"
                + "  \"logNo\": \"2015070609227748502\",\n"
                + "  \"sessionId\": \"5te0q4004p4v03ao3kfbanry\",\n"
                + "  \"result\": {\n"
                + "    \"TMK\": \"274BD4EF2C52D6028AC51E6901529A9F\",\n"
                + "    \"checkValue\": \"C8ACEDE1\"\n"
                + "  }";
    }

    /**
     * 获取工作密钥
     *
     * @param param
     * @return
     */
    @POST
    @Path("GetWorkingKeyOp")
    public String GetWorkingKeyOp(String param) {
        return "\"retCode\": \"0000\",\n"
                + "  \"retMsg\": \"\",\n"
                + "  \"logNo\": \"2015070609227748503\",\n"
                + "  \"sessionId\": \"5te0q4004p4v03ao3kfbanry\",\n"
                + "  \"result\": {\n"
                + "    \"encryptedWorkingKeyt\": \"EAA50DB7229FD8B38E6DD894CCC1337A\",\n"
                + "    \"checkValue\": \"703F2B52\"\n"
                + "  }";
    }

    /**
     * 获取ipad版本信息
     *
     * @param param
     * @return
     */
    @POST
    @Path("GetVersionNumByDeviceMacOp")
    public String GetVersionNumByDeviceMacOp(String param) {
        return "\"retCode\": \"9996\",\n"
                + "  \"retMsg\": \"该设备没有绑定版本  \\n\",\n"
                + "  \"logNo\": \"2015070609227748504\",\n"
                + "  \"sessionId\": \"5te0q4004p4v03ao3kfbanry\"";
    }

    //TODO 卡号验密 
    //TODO 联网核查
    //TODO 电子渠道签约查询
    /**
     * 电子渠道签约查询
     *
     * @param param
     * @return
     */
    @POST
    @Path("QryEChannelContractAllOp")
    public String QryEChannelContractAllOp(String param) {
        return "\"retCode\": \"9996\",\n"
                + "  \"retMsg\": \"该设备没有绑定版本  \\n\",\n"
                + "  \"logNo\": \"2015070609227748504\",\n"
                + "  \"sessionId\": \"5te0q4004p4v03ao3kfbanry\"";
    }

    //TODO 电子渠道签约

    /**
     * 电子渠道签约
     *
     * @param param
     * @return
     */
    @POST
    @Path("SignEChannelContractAllOp")
    public String SignEChannelContractAllOp(String param) {
        return "\"retCode\": \"9996\",\n"
                + "  \"retMsg\": \"该设备没有绑定版本  \\n\",\n"
                + "  \"logNo\": \"2015070609227748504\",\n"
                + "  \"sessionId\": \"5te0q4004p4v03ao3kfbanry\"";
    }

}
