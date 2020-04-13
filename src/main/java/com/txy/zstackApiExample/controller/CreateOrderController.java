package com.txy.zstackApiExample.controller;

import com.txy.zstackApiExample.entity.WorkOrderApiBody;
import com.txy.zstackApiExample.entity.WorkOrderRequest;
import com.txy.zstackApiExample.util.ZStackUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zstack.sdk.iam2.entity.IAM2ProjectInventory;
import org.zstack.sdk.iam2.entity.IAM2VirtualIDInventory;
import org.zstack.sdk.identity.role.RoleInventory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("ZStack")
public class CreateOrderController {

    /***
     * 创建云主机
     * @param name     云主机的名称
     * @param instanceOfferingUuid   计算规格id
     * @param imageUuid   镜像id
     * @param l3NetworkUuids  可用网络id
     * @return  云主机id
     */
    @RequestMapping(value = "/create")
    @ResponseBody
    public String createCloudMachine(@RequestParam("name")String name,@RequestParam("instanceOfferingUuid")String instanceOfferingUuid,@RequestParam("imageUuid")String imageUuid,@RequestParam(
            "l3NetworkUuids")String l3NetworkUuids){
        String createRes = null;
        try {
            String accountSessionId  = ZStackUtil.logInByAccountNameAndPassword();
            createRes = ZStackUtil.CreateMachine(accountSessionId,name,instanceOfferingUuid,imageUuid,l3NetworkUuids);
        }catch (Exception e){
            e.printStackTrace();
        }
        return createRes;
    }

    /***
     * 创建工单
     * @param name  工单的名字和云主机的名字
     * @param instanceOfferingUuid     计算规格id
     * @param imageUuid     镜像id
     * @param l3NetworkUuids   可选用的网络的id
     * @param applicant     创建者
     * @return 工单id
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/createWorkOrder")
    public String createWorkOrder (@RequestParam("name")String name,@RequestParam("instanceOfferingUuid")String instanceOfferingUuid,@RequestParam("imageUuid")String imageUuid,
                                   @RequestParam("l3NetworkUuids")String l3NetworkUuids,@RequestParam("applicant")String applicant)  throws  Exception {
        String createOrderRes = null;
        String password = "password";
        Map<String, String> accountSystemContext = new HashMap<String, String>();
        String projectUuid = null;
        String virtualIDUuid = null;
        List<String> roleUuidList = new ArrayList<>();
        String zstackAccountSessionId = ZStackUtil.logInByAccountNameAndPassword();
        //平台管理中添加用戶
        Boolean userWheatherExist = ZStackUtil.checkZstackUserForLoginExist( applicant, zstackAccountSessionId );
        if (false == userWheatherExist) {
            ZStackUtil.createUserForLoginZstack( applicant, password, zstackAccountSessionId );
        }
        //高級功能中添加用戶以及賦予項目，以及項目權限
        Boolean IAM2UserWheatherExist = ZStackUtil.checkZstackIAM2UserExist( applicant, zstackAccountSessionId );
        //假如不存在就赋予项目以及项目管理员角色
        if (false == IAM2UserWheatherExist) {
            List<IAM2ProjectInventory> projectList = ZStackUtil.getProjectList( zstackAccountSessionId );
            List<String> virtualIDUuidList = null;
            for (IAM2ProjectInventory project : projectList) {
                if ("云主机申请".equals( project.getName() )) {
                    projectUuid = project.getUuid();
                    IAM2VirtualIDInventory zstackIAM2User = ZStackUtil.createZstackIAM2User( applicant, password, zstackAccountSessionId );
                    virtualIDUuid = zstackIAM2User.getUuid();
                    virtualIDUuidList = new ArrayList<>();
                    virtualIDUuidList.add( virtualIDUuid );
                    ZStackUtil.addIAM2VirtualIDsToProject( projectUuid, virtualIDUuidList, zstackAccountSessionId );
                }
            }
            List<RoleInventory> projectRoleList = ZStackUtil.getAllProjectRoleList( zstackAccountSessionId );
            for (RoleInventory role : projectRoleList) {
                roleUuidList = new ArrayList<>();
                if ("PROJECT_OPERATOR_ROLE".equals( role.getName() )) {
                    roleUuidList.add( role.getUuid() );
                    ZStackUtil.addRolesToIAM2VirtualID( roleUuidList, virtualIDUuid, zstackAccountSessionId );
                }
            }
        } else {
            //如果高级功能中存在该用户，就查看该用户是否在《云主机申请》这个项目中，且权限是否是项目管理员
            List<IAM2ProjectInventory> projectList = ZStackUtil.getProjectList( zstackAccountSessionId );
            List<String> virtualIDUuidList = null;
            for (IAM2ProjectInventory project : projectList) {
                if ("云主机申请".equals( project.getName() )) {
                    projectUuid = project.getUuid();
                    String IAM2UserSessionId = ZStackUtil.getZstackIAM2UserSessionId( applicant, zstackAccountSessionId );
                    virtualIDUuid = IAM2UserSessionId;
                    virtualIDUuidList = new ArrayList<>();
                    virtualIDUuidList.add( virtualIDUuid );
                    ZStackUtil.addIAM2VirtualIDsToProject( projectUuid, virtualIDUuidList, zstackAccountSessionId );				}
            }
            List<RoleInventory> projectRoleList = ZStackUtil.getAllProjectRoleList( zstackAccountSessionId );
            for (RoleInventory role : projectRoleList) {
                roleUuidList = new ArrayList<>();
                if ("PROJECT_OPERATOR_ROLE".equals( role.getName() )) {
                    roleUuidList.add( role.getUuid() );
                    ZStackUtil.addRolesToIAM2VirtualID( roleUuidList, virtualIDUuid, zstackAccountSessionId );
                }
            }
        }

        //管理员登出，刚刚注册的用户登录进去
        ZStackUtil.logOut( zstackAccountSessionId );
        String commonUserSessionId = ZStackUtil.logInByUsernameAndPassword(applicant,password);

        List<WorkOrderRequest> workOrderRequests = new ArrayList<>(  );
        WorkOrderApiBody workOrderApiBody = new WorkOrderApiBody();
        workOrderApiBody.setImageUuid(imageUuid);
        workOrderApiBody.setInstanceOfferingUuid(instanceOfferingUuid );
        workOrderApiBody.setHypervisorType( "KVM" );
        workOrderApiBody.setDefaultL3NetworkUuid( l3NetworkUuids );
        workOrderApiBody.setName( name );

        List netSmallList = new ArrayList(  );
        netSmallList.add( l3NetworkUuids );
        workOrderApiBody.setL3NetworkUuids( netSmallList );

        WorkOrderRequest workOrderRequest = new WorkOrderRequest();
        workOrderRequest.setApiName( "ecology" );
        workOrderRequest.setExecuteTimes( "1" );
        workOrderRequest.setApiName( "org.zstack.header.vm.APICreateVmInstanceMsg" );
        workOrderRequest.setapiBody( workOrderApiBody );

        workOrderRequests.add( workOrderRequest );
        createOrderRes = ZStackUtil.CreateWorkOrder( projectUuid,virtualIDUuid,name,commonUserSessionId,workOrderRequests);

        if(createOrderRes == null){
            return null;
        }else{
            return createOrderRes;
        }
    }
}
