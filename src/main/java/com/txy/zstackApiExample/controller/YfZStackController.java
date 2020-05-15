package com.txy.zstackApiExample.controller;

import com.google.gson.Gson;
import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import com.txy.zstackApiExample.entity.WorkOrderApiBody;
import com.txy.zstackApiExample.entity.WorkOrderRequest;
import com.txy.zstackApiExample.service.YfZStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zstack.sdk.iam2.entity.IAM2ProjectInventory;
import org.zstack.sdk.iam2.entity.IAM2VirtualIDInventory;
import org.zstack.sdk.identity.role.RoleInventory;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/yfZStack")
public class YfZStackController {

    @Autowired
    private YfZStackService yfZStackService;

    @RequestMapping(value = "/create")
    @ResponseBody
    public String createZStack(HttpServletRequest request) {
        String createRes = null;
        String name = request.getParameter( "name" );// 需求单号 对应TWP需求表的strSn
        String instanceOfferingUuid = request.getParameter( "instanceOfferingUuid" );// 需求单号 对应TWP需求表的strSn
        String imageUuid = request.getParameter( "imageUuid" );// 需求单号 对应TWP需求表的strSn
        String l3NetworkUuids = request.getParameter( "l3NetworkUuids" );// 需求单号 对应TWP需求表的strSn

        System.out.println( name );
        System.out.println( instanceOfferingUuid );
        System.out.println( imageUuid );
        System.out.println( l3NetworkUuids );
        try {
            String accountSessionId = yfZStackService.logInByAccountNameAndPassword();
            createRes = yfZStackService.CreateMachine( accountSessionId, name, instanceOfferingUuid, imageUuid, l3NetworkUuids );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createRes;
    }


    @ResponseBody
    @RequestMapping("/createWorkOrder")
    public String createWorkOrder(HttpServletRequest request) throws Exception {
        String createOrderRes = null;
        String name = request.getParameter( "name" );                    //云主机名称
        String sqr = request.getParameter( "sqr" );                         //申请人的名称
        String instanceOfferingUuid = request.getParameter( "instanceOfferingUuid" );
        String imageUuid = request.getParameter( "imageUuid" );
        String l3NetworkUuids = request.getParameter( "l3NetworkUuids" );
        String password = "password";
        Map<String, String> accountSystemContext = new HashMap<String, String>();
        String projectUuid = null;
        String virtualIDUuid = null;
        List<String> roleUuidList = new ArrayList<>();
        String zstackAccountSessionId = yfZStackService.logInByAccountNameAndPassword();
        //平台管理中添加用戶
        Boolean userWheatherExist = yfZStackService.checkZstackUserForLoginExist( sqr, zstackAccountSessionId );
        if (false == userWheatherExist) {
            yfZStackService.createUserForLoginZstack( sqr, password, zstackAccountSessionId );
        }
        //高級功能中添加用戶以及賦予項目，以及項目權限
        Boolean IAM2UserWheatherExist = yfZStackService.checkZstackIAM2UserExist( sqr, zstackAccountSessionId );
        //假如不存在就赋予项目以及项目管理员角色
        if (false == IAM2UserWheatherExist) {
            List<IAM2ProjectInventory> projectList = yfZStackService.getProjectList( zstackAccountSessionId );
            List<String> virtualIDUuidList = null;
            for (IAM2ProjectInventory project : projectList) {
                if ("云主机申请".equals( project.getName() )) {
                    projectUuid = project.getUuid();
                    IAM2VirtualIDInventory zstackIAM2User = yfZStackService.createZstackIAM2User( sqr, password, zstackAccountSessionId );
                    virtualIDUuid = zstackIAM2User.getUuid();
                    virtualIDUuidList = new ArrayList<>();
                    virtualIDUuidList.add( virtualIDUuid );
                    yfZStackService.addIAM2VirtualIDsToProject( projectUuid, virtualIDUuidList, zstackAccountSessionId );
                }
            }
            List<RoleInventory> projectRoleList = yfZStackService.getAllProjectRoleList( zstackAccountSessionId );
            for (RoleInventory role : projectRoleList) {
                roleUuidList = new ArrayList<>();
                if ("PROJECT_OPERATOR_ROLE".equals( role.getName() )) {
                    roleUuidList.add( role.getUuid() );
                    yfZStackService.addRolesToIAM2VirtualID( roleUuidList, virtualIDUuid, zstackAccountSessionId );
                }
            }
        } else {
            //如果高级功能中存在该用户，就查看该用户是否在《云主机申请》这个项目中，且权限是否是项目管理员
            List<IAM2ProjectInventory> projectList = yfZStackService.getProjectList( zstackAccountSessionId );
            List<String> virtualIDUuidList = null;
            for (IAM2ProjectInventory project : projectList) {
                if ("云主机申请".equals( project.getName() )) {
                    projectUuid = project.getUuid();
                    String IAM2UserSessionId = yfZStackService.getZstackIAM2UserSessionId( sqr, zstackAccountSessionId );
                    virtualIDUuid = IAM2UserSessionId;
                    virtualIDUuidList = new ArrayList<>();
                    virtualIDUuidList.add( virtualIDUuid );
                    yfZStackService.addIAM2VirtualIDsToProject( projectUuid, virtualIDUuidList, zstackAccountSessionId );
                }
            }
            List<RoleInventory> projectRoleList = yfZStackService.getAllProjectRoleList( zstackAccountSessionId );
            for (RoleInventory role : projectRoleList) {
                roleUuidList = new ArrayList<>();
                if ("PROJECT_OPERATOR_ROLE".equals( role.getName() )) {
                    roleUuidList.add( role.getUuid() );
                    yfZStackService.addRolesToIAM2VirtualID( roleUuidList, virtualIDUuid, zstackAccountSessionId );
                }
            }
        }

        //管理员登出，刚刚注册的用户登录进去
        yfZStackService.logOut( zstackAccountSessionId );
        String commonUserSessionId = yfZStackService.logInByUsernameAndPassword( sqr, password );

        List<WorkOrderRequest> workOrderRequests = new ArrayList<>();
        WorkOrderApiBody workOrderApiBody = new WorkOrderApiBody();
        workOrderApiBody.setImageUuid( imageUuid );
        workOrderApiBody.setInstanceOfferingUuid( instanceOfferingUuid );
        workOrderApiBody.setHypervisorType( "KVM" );
        workOrderApiBody.setDefaultL3NetworkUuid( l3NetworkUuids );
        workOrderApiBody.setName( name );

        List netSmallList = new ArrayList();
        netSmallList.add( l3NetworkUuids );
        workOrderApiBody.setL3NetworkUuids( netSmallList );

        WorkOrderRequest workOrderRequest = new WorkOrderRequest();
        workOrderRequest.setApiName( "ecology" );
        workOrderRequest.setExecuteTimes( "1" );
        workOrderRequest.setApiName( "org.zstack.header.vm.APICreateVmInstanceMsg" );
        workOrderRequest.setapiBody( workOrderApiBody );

        workOrderRequests.add( workOrderRequest );
        createOrderRes = yfZStackService.CreateWorkOrder( projectUuid, virtualIDUuid, name, commonUserSessionId, workOrderRequests );

        if (createOrderRes == null) {
            return null;
        } else {
            return createOrderRes;
        }
    }

    @RequestMapping("/remainResource")
    @ResponseBody
    public String  findRemainingResource() {
        String accountSessionId = YfZStackService.logInByAccountNameAndPassword();
        Map<String,String> maps = new HashMap<>(  );
        maps = yfZStackService.getRemainingResource(accountSessionId);
//        System.out.println(maps + "我脾气好差差差差差差差差差差差差差差差差差差差差差差差差差差差差差差差差差差差");
/*        List labellabel = yfZStackService.getAllMetricResouce(accountSessionId);
        System.out.println(labellabel);*/
        Date date1 = new Date();
        long dateTime = date1.getTime();
        long time = (long) 1.519829246005E12;
        String date11 = "2017-01-18 16:50:50";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time1 =(long) 1.519829246005E12 ;
        try{
            time1 = sdf1.parse(date11).getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        String datalist =  yfZStackService.getMetricData(accountSessionId,time1);
        String fa = yfZStackService.getMetricData1(accountSessionId,time1);
        String fa2 = yfZStackService.getMetricData2(accountSessionId,time1);
        String fa3 = yfZStackService.getMetricData3(accountSessionId,time1);
        String fa4 = yfZStackService.getMetricData4(accountSessionId,time1);
        String fa5 = yfZStackService.getMetricData5(accountSessionId,time1);
//        List undada = yfZStackService.getAllMetricResouce(accountSessionId);
        return "查询完毕";
    }

    @GetMapping("remain")
    @ResponseBody
    public String getRemainResource(){
        String accountSessionId = YfZStackService.logInByAccountNameAndPassword();
        Map<String,String> totalInfoMap = yfZStackService.getCpuMemoryCapacity(accountSessionId);
        String[] availablePrimaryStorageArgs = yfZStackService.getPrimaryStorageCapacity(accountSessionId);
        totalInfoMap.put( "availablePrimaryStorage",availablePrimaryStorageArgs[0] );
        totalInfoMap.put( "availablePrimaryStoragePercent",availablePrimaryStorageArgs[1]);
        Gson gson = new Gson();
        String totalInfoJson = gson.toJson( totalInfoMap );
        return totalInfoJson;
    }
}
