package com.txy.zstackApiExample.service;

import com.txy.zstackApiExample.entity.WorkOrderRequest;
import org.springframework.stereotype.Service;
import org.zstack.sdk.*;
import org.zstack.sdk.iam2.api.*;
import org.zstack.sdk.iam2.entity.IAM2ProjectInventory;
import org.zstack.sdk.iam2.entity.IAM2VirtualIDInventory;
import org.zstack.sdk.identity.role.RoleInventory;
import org.zstack.sdk.identity.role.api.QueryRoleAction;
import org.zstack.sdk.ticket.api.CreateTicketAction;
import org.zstack.sdk.zwatch.api.GetAllMetricMetadataAction;
import org.zstack.sdk.zwatch.api.GetMetricDataAction;
import org.zstack.sdk.zwatch.api.GetMetricLabelValueAction;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @desc
 * @author
 * @version 1.0.0
 * @date 2020-01-15
 * @Copyright:江苏金恒信息科技股份有限公司
 */
@Service
public class YfZStackService {


	private static final int ztackPort = 8080;
	private static final String  zstackAccountName = "admin";
	private static final String zstackAccountPassword = "CloudOs@2020";
	private static final String zstackContextPath = "zstack";
	private static final String zstackAddress = "172.18.210.30";

	static {
		ZSClient.configure(
				new ZSConfig.Builder()
						.setHostname(zstackAddress)
						.setPort(ztackPort)
						.setContextPath(zstackContextPath)
						.build()
		);
	}

	/**
	 * 普通用户登录，根据用户名和密码登录，账户为admin
	 *
	 * @param username
	 * @param password
	 * @return 普通用户的SessionId
	 */
	public static String logInByUsernameAndPassword(String username, String password) {
		LogInByUserAction logInByUserAction = new LogInByUserAction();
		logInByUserAction.accountName = zstackAccountName;
		logInByUserAction.userName = username;
		logInByUserAction.password = encryptToSHA512(password);
		LogInByUserAction.Result LogInResult = logInByUserAction.call();
		LogInResult.throwExceptionIfError();
		return LogInResult.value.getInventory().getUuid();
	}

	/**
	 * 管理员账号登录，根据账户和密码登录，使用的是配置文件里的账户密码
	 * @return  登录成功后返回sessionID
	 */
	public static String logInByAccountNameAndPassword() {
		LogInByAccountAction logInByAccountAction = new LogInByAccountAction();
		logInByAccountAction.accountName = zstackAccountName;
		logInByAccountAction.password = encryptToSHA512(zstackAccountPassword);
		LogInByAccountAction.Result LogInByAccountResult = logInByAccountAction.call();
		LogInByAccountResult.throwExceptionIfError();
		String sessionUuid = LogInByAccountResult.value.getInventory().getUuid();

		QueryVmInstanceAction action = new QueryVmInstanceAction();
		action.sessionId = sessionUuid;
		QueryVmInstanceAction.Result result = action.call();
		result.throwExceptionIfError();

		List<VmInstanceInventory> vmList = result.value.getInventories();
		System.out.println(String.format("查询云主机列表成功，查询到%s台云主机",
				vmList != null ? vmList.size() : 0));
		return  sessionUuid;
	}

	/**
	 * 根据账户和密码登录
	 * 记住这里是账户不是用户
	 * @return  登录成功后返回sessionID
	 */
	public static String logInByAccountNameAndPassword(String accountName,String accountPassword) {
		LogInByAccountAction logInByAccountAction = new LogInByAccountAction();
		logInByAccountAction.accountName = accountName;
		logInByAccountAction.password = encryptToSHA512(accountPassword);
		LogInByAccountAction.Result LogInByAccountResult = logInByAccountAction.call();
		LogInByAccountResult.throwExceptionIfError();
		String sessionUuid = LogInByAccountResult.value.getInventory().getUuid();
		return  sessionUuid;
	}

	/**
	 * 密码转换
	 */
	private static String encryptToSHA512(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.reset();
			md.update(input.getBytes( StandardCharsets.UTF_8 ));
			BigInteger bigInteger = new BigInteger(1, md.digest());
			return String.format("%0128x", bigInteger);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/***
	 * 创建云主机
	 * @param sessionId
	 * @param name
	 * @param instanceOfferingUuid
	 * @param imageUuid
	 * @param l3NetworkUuids
	 * @return 已经创建好的云主机的Uuid
	 * @throws Exception
	 */
	public static String CreateMachine(String sessionId ,String name ,String instanceOfferingUuid , String imageUuid,String l3NetworkUuids ) throws Exception {
		try {
			CreateVmInstanceAction action = new CreateVmInstanceAction();
			action.name = name;
			action.instanceOfferingUuid = instanceOfferingUuid;
			action.imageUuid = imageUuid;
			List netIdList = new ArrayList();
			netIdList.add( l3NetworkUuids );
			action.l3NetworkUuids = netIdList;
			action.sessionId = sessionId;
			CreateVmInstanceAction.Result res = action.call();
			res.throwExceptionIfError();
			String machineId = res.value.getInventory().uuid;
			if (!"".equals( machineId ) && machineId != null) {
				return machineId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 注销当前用户
	 * @param sessionUuid
	 * @throws Exception
	 */
	public static void logOut(String sessionUuid) {
		LogOutAction logOutAction = new LogOutAction();
		logOutAction.sessionUuid = sessionUuid;
		LogOutAction.Result LogOutResult = logOutAction.call();
	}

	/***
	 * 创建用户 ，此用户为平台管理的用户，用于登录,前提是必须先登陆进账户
	 * @param employeeNumber
	 * @param password
	 * @return 用户
	 */
	public static UserInventory createUserForLoginZstack(String employeeNumber,String password,String zstackAccountSessionId){
		CreateUserAction createUserAction = new CreateUserAction();
		createUserAction.sessionId = zstackAccountSessionId;
		createUserAction.name = employeeNumber;
		createUserAction.password = encryptToSHA512(password);
		CreateUserAction.Result createUserResult = createUserAction.call();
		createUserResult.throwExceptionIfError();
		UserInventory userInventory = createUserResult.value.getInventory();
		return userInventory;
	}

	/**
	 * 查看该账户在平台管理一栏里下是否有此用户
	 * @param employeeNumber
	 * @param zstackAccountSessionId
	 * @return true为存在 false为不存在
	 */
	public static Boolean checkZstackUserForLoginExist(String employeeNumber,String zstackAccountSessionId){
		QueryUserAction queryUserAction = new QueryUserAction();
		queryUserAction.sessionId = zstackAccountSessionId;
		QueryUserAction.Result queryUserActionResult = queryUserAction.call();
		queryUserActionResult.throwExceptionIfError();
		List<UserInventory> userList = queryUserActionResult.value.getInventories();
		for (UserInventory user : userList){
			if (employeeNumber.equals(user.getName())){
				return true;
			}
		}
		return false;
	}

	/***
	 * 获取当前账户下的项目列表
	 * @param zstackAccountSessionId
	 * @return
	 */
	public static List<IAM2ProjectInventory> getProjectList(String zstackAccountSessionId){
		QueryIAM2ProjectAction queryIAM2ProjectAction = new QueryIAM2ProjectAction();
		queryIAM2ProjectAction.sessionId = zstackAccountSessionId;
		QueryIAM2ProjectAction.Result queryResult = queryIAM2ProjectAction.call();
		List<IAM2ProjectInventory> projectInventoryList = queryResult.value.getInventories();
		return projectInventoryList;
	}

	/**
	 * 將用戶添加到某個工程下
	 * @param projectUuid
	 * @param virtualIDUuidList
	 * @param zstackAccountSessionId
	 * @return
	 */
	public static Boolean addIAM2VirtualIDsToProject(String projectUuid,List<String> virtualIDUuidList,String zstackAccountSessionId){
		AddIAM2VirtualIDsToProjectAction addIAM2VirtualIDsToProjectAction = new AddIAM2VirtualIDsToProjectAction();
		addIAM2VirtualIDsToProjectAction.sessionId = zstackAccountSessionId;
		addIAM2VirtualIDsToProjectAction.projectUuid = projectUuid;
		addIAM2VirtualIDsToProjectAction.virtualIDUuids = virtualIDUuidList;
		AddIAM2VirtualIDsToProjectAction.Result addIAM2VirtualIDsToProjectResult = addIAM2VirtualIDsToProjectAction.call();
		addIAM2VirtualIDsToProjectResult.throwExceptionIfError();
		return true;
	}

	/**
	 * 將項目的某個角色賦予項目中的某個用戶 true表示成功
	 * @param virtualIDUuid
	 * @param zstackAccountSessionId
	 * @return
	 */
	public static Boolean addRolesToIAM2VirtualID(List<String> roleUuidList,String virtualIDUuid,String zstackAccountSessionId){
		AddRolesToIAM2VirtualIDAction addRolesToIAM2VirtualIDAction = new AddRolesToIAM2VirtualIDAction();
		addRolesToIAM2VirtualIDAction.virtualIDUuid = virtualIDUuid;
		addRolesToIAM2VirtualIDAction.roleUuids = roleUuidList;
		addRolesToIAM2VirtualIDAction.sessionId = zstackAccountSessionId;
		AddRolesToIAM2VirtualIDAction.Result addRolesToIAM2VirtualIDResult = addRolesToIAM2VirtualIDAction.call();
		addRolesToIAM2VirtualIDResult.throwExceptionIfError();
		return true;
	}

	/**
	 * 獲取指定賬戶下項目的所有角色
	 * @param zstackAccountSessionId
	 * @return
	 */
	public  static List<RoleInventory>  getAllProjectRoleList(String zstackAccountSessionId){
		QueryRoleAction queryRoleAction = new QueryRoleAction();
		queryRoleAction.sessionId = zstackAccountSessionId;
		QueryRoleAction.Result queryRoleResult = queryRoleAction.call();
		queryRoleResult.throwExceptionIfError();
		return queryRoleResult.value.getInventories();
	}

	/**
	 * 检查高级功能下的用户列表是否有该用户,必须是工号，姓名可能一样，但是工号绝对不一样
	 * @param employeeNumber
	 * @param zstackAccountSessionId
	 * @return true 代表有，false代表没有
	 */
	public static Boolean checkZstackIAM2UserExist(String employeeNumber,String zstackAccountSessionId){
		QueryIAM2VirtualIDAction queryIAM2VirtualIDAction = new QueryIAM2VirtualIDAction();
		queryIAM2VirtualIDAction.sessionId = zstackAccountSessionId;
		QueryIAM2VirtualIDAction.Result QueryIAM2VirtualIDResult = queryIAM2VirtualIDAction.call();
		QueryIAM2VirtualIDResult.throwExceptionIfError();
		List<IAM2VirtualIDInventory> IAM2VirtualIDList = QueryIAM2VirtualIDResult.value.getInventories();
		for (IAM2VirtualIDInventory IAM2VirtualID : IAM2VirtualIDList){
			if (employeeNumber.equals(IAM2VirtualID.getName())){
				return true;
			}
		}
		return  false;
	}

	/**
	 * 获取VirtualID
	 * @param employeeNumber
	 * @param zstackAccountSessionId
	 * @return
	 */
	public static String getZstackIAM2UserSessionId(String employeeNumber,String zstackAccountSessionId){
		QueryIAM2VirtualIDAction queryIAM2VirtualIDAction = new QueryIAM2VirtualIDAction();
		queryIAM2VirtualIDAction.sessionId = zstackAccountSessionId;
		QueryIAM2VirtualIDAction.Result QueryIAM2VirtualIDResult = queryIAM2VirtualIDAction.call();
		QueryIAM2VirtualIDResult.throwExceptionIfError();
		List<IAM2VirtualIDInventory> IAM2VirtualIDList = QueryIAM2VirtualIDResult.value.getInventories();
		for (IAM2VirtualIDInventory IAM2VirtualID : IAM2VirtualIDList){
			if (employeeNumber.equals(IAM2VirtualID.getName())){
				return IAM2VirtualID.getUuid();
			}
		}
		return  null;
	}

	/**
	 *
	 * @param employeeNumber 用于登录，唯一性！！一个一个名字可以有多个employeeNumber只对应一个名字
	 * @param password 密码
	 * @param zstackAccountSessionId 当前账户
	 * @return
	 */
	public static IAM2VirtualIDInventory createZstackIAM2User(String employeeNumber, String password, String zstackAccountSessionId){
		CreateIAM2VirtualIDAction createIAM2VirtualIDAction = new CreateIAM2VirtualIDAction();
		createIAM2VirtualIDAction.name = employeeNumber;
		createIAM2VirtualIDAction.password = encryptToSHA512(password);
		createIAM2VirtualIDAction.sessionId = zstackAccountSessionId;
		CreateIAM2VirtualIDAction.Result createIAM2VirtualIDResult = createIAM2VirtualIDAction.call();
		createIAM2VirtualIDResult.throwExceptionIfError();
		return createIAM2VirtualIDResult.value.getInventory();
	}

	/**
	 * 获取当前登录用户下的可使用计算规格
	 * @param sessionId
	 * @return
	 */
	public  static List<InstanceOfferingInventory> getInstanceOfferingList(String sessionId){
		QueryInstanceOfferingAction queryInstanceOfferingAction = new QueryInstanceOfferingAction();
		queryInstanceOfferingAction.sessionId = sessionId;
		QueryInstanceOfferingAction.Result queryInstanceOfferingResult = queryInstanceOfferingAction.call();
		queryInstanceOfferingResult.throwExceptionIfError();
		return queryInstanceOfferingResult.value.getInventories();
	}

	/**
	 * 创建工单
	 * @param projectUuid
	 * @param virtualIDUuid
	 * @param name
	 * @param commonUserSessionId
	 * @param workOrderRequests
	 * @return
	 */
	public static String CreateWorkOrder(String projectUuid,String virtualIDUuid,String name,String commonUserSessionId, List<WorkOrderRequest> workOrderRequests){
		Map<String, String> accountSystemContext = new HashMap<String, String>();
		CreateTicketAction createTicketAction = new CreateTicketAction();
		accountSystemContext.put("projectUuid", projectUuid);
		accountSystemContext.put("virtualIDUuid", virtualIDUuid);
		WorkOrderRequest workOrderRequest = new WorkOrderRequest();
		workOrderRequest.setApiName( "创建云主机" );
		workOrderRequest.setExecuteTimes( "1" );
		createTicketAction.name = name;
		createTicketAction.sessionId = commonUserSessionId;
		createTicketAction.accountSystemType = "iam2";
		createTicketAction.accountSystemContext = accountSystemContext;
		createTicketAction.requests = workOrderRequests;
		CreateTicketAction.Result res = createTicketAction.call();
		res.throwExceptionIfError();
		String workOrderUuid = res.value.getInventory().getUuid();
		System.out.println(	workOrderUuid);
		return workOrderUuid;
	}

	/***
	 * 查询剩余的云端ZStack资源
	 * @param sessionId
	 * @return
	 */
	public static Map<String,String> getRemainingResource(String sessionId){
		Map<String,String> maps = new HashMap<>(  );
		GetAllMetricMetadataAction action = new GetAllMetricMetadataAction();
		action.sessionId = sessionId;
		GetAllMetricMetadataAction.Result res = action.call();
		res.throwExceptionIfError();
		String RemainResouce = res.toString();
		String RemainCPU = res.value.metrics.toString();
		System.out.println(RemainCPU);
		return maps;
	}

	/***
	 * 查询剩余云端的ZStack云端的CPU资源   label
	 */
	public static List getAllMetricResouce(String sessionId){
		GetMetricLabelValueAction action = new GetMetricLabelValueAction();
		action.namespace = "ZStack/Host";
		action.metricName = "CPUUsedUtilization";
		List list1 = new ArrayList(  );
		list1.add( "CPUNum" );
		action.labelNames = list1;
		List list2 = new ArrayList(  );
		list2.add( "VMUuid=f9b6ee3213924a8d919e02c4f25e36b2" );
		action.sessionId = sessionId;
		GetMetricLabelValueAction.Result res = action.call();
		res.throwExceptionIfError();
		List labels = res.value.getLabels();
		return labels;
	}


	/**
	 * 查询剩余的资源 data值
	 */
	public static String getMetricData(String sessionId,long dateTime){
		GetMetricDataAction action = new GetMetricDataAction();
		action.namespace = "ZStack/VM";
		action.metricName = "MemoryFreeBytes";
		action.sessionId = sessionId;
//		action.startTime = Long.valueOf( 1588750503 );
		GetMetricDataAction.Result res = action.call();
		return res.toString();
	}

	public static String getMetricData1(String sessionId,long dateTime){
		GetMetricDataAction action = new GetMetricDataAction();
		action.namespace = "ZStack/VM";
		action.metricName = "CPUAllUsedUtilization";
		action.sessionId = sessionId;
//		action.startTime = Long.valueOf( 1588750503 );
		GetMetricDataAction.Result res = action.call();
		return res.toString();
	}

	public static String getMetricData2(String sessionId,long dateTime){
		GetMetricDataAction action = new GetMetricDataAction();
		action.namespace = "ZStack/VM";
		action.metricName = "CPUAllUsedUtilization";
		action.sessionId = sessionId;
//		action.startTime = Long.valueOf( 1588750503 );
		GetMetricDataAction.Result res = action.call();
		return res.toString();
	}

	public static String getMetricData3(String sessionId,long dateTime){
		GetMetricDataAction action = new GetMetricDataAction();
		action.namespace = "ZStack/VM";
		action.metricName = "MemoryFreeInPercent";
		action.sessionId = sessionId;
//		action.startTime = Long.valueOf( 1588750503 );
		GetMetricDataAction.Result res = action.call();
		return res.toString();
	}

	public static String getMetricData4(String sessionId,long dateTime){
		GetMetricDataAction action = new GetMetricDataAction();
		action.namespace = "ZStack/VM";
		action.metricName = "RunningVMInPercent";
		action.sessionId = sessionId;
//		action.startTime = Long.valueOf( 1588750503 );
		GetMetricDataAction.Result res = action.call();
		return res.toString();
	}


	public static String getMetricData5(String sessionId,long dateTime){
		GetMetricDataAction action = new GetMetricDataAction();
		action.namespace = "ZStack/VM";
		action.metricName = "OtherStateVMCount";
		action.sessionId = sessionId;
//		action.startTime = Long.valueOf( 1588750503 );
		GetMetricDataAction.Result res = action.call();
		return res.toString();
	}

	public static Map<String,String> getCpuMemoryCapacity(String accountSessionId){
		DecimalFormat df=new DecimalFormat("0.00");
		List zoneList = new ArrayList(  );
		zoneList.add( "905a5ae5ad4946afae7cb7609b1ca835" );
		GetCpuMemoryCapacityAction action = new GetCpuMemoryCapacityAction();
		action.zoneUuids = zoneList;
		action.sessionId = accountSessionId;
		action.hypervisorType = "KVM";
		GetCpuMemoryCapacityAction.Result res = action.call();
		long availableCpu = res.value.getAvailableCpu();
		long totalCpu = res.value.getTotalCpu();
		long totalMemory = res.value.getTotalMemory();
		long availableMemory = res.value.getAvailableMemory();
		double availableCpuPercent = availableCpu*1.0/totalCpu;
		double availableMemoryPercent = availableMemory*1.0/totalMemory;
		Map<String,String> map1 = new HashMap<>(  );
		map1.put( "availableCpu",Long.toString( availableCpu ));
		map1.put( "availableMemory",df.format( availableMemory*1.0/1024/1024/1024/1024 ));
		map1.put( "availableCpuPercent",df.format( availableCpuPercent*100));
		map1.put( "availableMemoryPercent",df.format( availableMemoryPercent*100));
		return map1;
	}

	public static String[] getPrimaryStorageCapacity(String accountSessionID){
		DecimalFormat df=new DecimalFormat("0.00");
		List zoneList1 = new ArrayList(  );
		zoneList1.add( "905a5ae5ad4946afae7cb7609b1ca835" );
		List primaryStorageList = new ArrayList(  );
		primaryStorageList.add( "2f4c49b046c343feaf7b7d2f9b64d375" );
		GetPrimaryStorageCapacityAction action = new GetPrimaryStorageCapacityAction();
		action.zoneUuids = zoneList1;
		action.primaryStorageUuids = primaryStorageList;
		action.all = false;
		action.sessionId = accountSessionID;
		GetPrimaryStorageCapacityAction.Result res = action.call();
		long totalPrimaryStorage = res.value.getTotalCapacity();
		long availablePrimaryStorage = res.value.getAvailableCapacity();
		double availablePrimaryStoragePercent = availablePrimaryStorage*1.0/totalPrimaryStorage;
		String[] availablePrimaryStorageArgs = new String[2];
		availablePrimaryStorageArgs[0] = df.format(availablePrimaryStorage*1.0/1024/1024/1024/1024);
		availablePrimaryStorageArgs[1] = df.format(availablePrimaryStoragePercent*100);
		return availablePrimaryStorageArgs;
	}
}
