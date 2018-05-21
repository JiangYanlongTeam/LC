package weaver.ofs.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weaver.general.Util;
import weaver.hrm.resource.ResourceComInfo;
import weaver.ofs.bean.OfsLog;
import weaver.ofs.bean.OfsSetting;
import weaver.ofs.bean.OfsTodoData;
import weaver.ofs.bean.OfsWorkflow;
import weaver.ofs.manager.remind.OfsRemindAppManager;
import weaver.ofs.manager.remind.OfsRemindEbridgeManager;
import weaver.ofs.manager.remind.OfsRemindElinkManager;
import weaver.ofs.manager.remind.OfsRemindEmessageManager;
//import weaver.ofs.manager.remind.OfsRemindEmessageManager;
import weaver.ofs.manager.remind.OfsRemindOAManager;
import weaver.ofs.manager.remind.OfsRemindOtherManager;
import weaver.ofs.manager.remind.OfsRemindRTXManager;
import weaver.ofs.service.OfsLogService;
import weaver.ofs.service.OfsSettingService;
import weaver.ofs.service.OfsSysInfoService;
import weaver.ofs.service.OfsTodoDataService;
import weaver.ofs.service.OfsWorkflowService;
import weaver.ofs.util.OfsUtils;
import weaver.rtx.RTXConfig;

public class OfsTodoDataManager {
	private static Log log = LogFactory.getLog(OfsTodoDataManager.class);
	
	private static final String SYSADMIN_ID = "1";
	private static final String RESULT_XML_ROOT = "ResultInfo";
	private static final int DEFAULT_DATA_ID = 0;//缺省数据id
	private static final String LINK_CHAR = "_";
	
	private OfsSettingService ofsSettingService = new OfsSettingService();
	private OfsTodoDataService ofsTodoDataService = new OfsTodoDataService();
	private OfsSysInfoService ofsSysInfoService = new OfsSysInfoService();
	private OfsWorkflowService ofsWorkflowService = new OfsWorkflowService();
	private OfsLogService ofsLogService = new OfsLogService();
	
	private String clientIp;//访问Ip
	
	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	/**
	 * 接收待办流程
	 * @param dataMap
	 * @return
	 */
	public Map<String,String> receiveTodoRequestByMap(Map<String,String> dataMap){
		String syscode = OfsUtils.getStringValueByMapKey(dataMap,"syscode");
		String flowid = OfsUtils.getStringValueByMapKey(dataMap,"flowid");
		String requestname = OfsUtils.getStringValueByMapKey(dataMap,"requestname");
		String workflowname = OfsUtils.getStringValueByMapKey(dataMap,"workflowname");
		String nodename = OfsUtils.getStringValueByMapKey(dataMap,"nodename");
		String pcurl = OfsUtils.getStringValueByMapKey(dataMap,"pcurl");
		String appurl = OfsUtils.getStringValueByMapKey(dataMap,"appurl");
		String createdatetime = OfsUtils.getStringValueByMapKey(dataMap,"createdatetime");
		String creator = OfsUtils.getStringValueByMapKey(dataMap,"creator");
		String receiver = OfsUtils.getStringValueByMapKey(dataMap,"receiver");
		String receivedatetime = OfsUtils.getStringValueByMapKey(dataMap,"receivedatetime");
		
		return receiveTodoRequest(
				syscode,
				flowid,
				requestname,
				workflowname,
				nodename,
				pcurl,
				appurl,
				creator,
				createdatetime,
				receiver,
				receivedatetime);
	}
	
	public String receiveTodoRequestByJson(String json){
		Map<String,String> dataMap = OfsUtils.jsonToMap(json);
		Map<String,String> resultMap = receiveTodoRequestByMap(dataMap);
		String resultJson = OfsUtils.mapToJson(resultMap);
		
		return resultJson;
	}
	
	public String receiveTodoRequestByXml(String xml){
		Map<String,String> dataMap = OfsUtils.xmlToMap(xml);
		Map<String,String> resultMap = receiveTodoRequestByMap(dataMap);
		return OfsUtils.mapToXml(resultMap,RESULT_XML_ROOT);
	}

	public Map<String,String> receiveTodoRequest(
			String syscode,
			String flowid,
			String requestname,
			String workflowname,
			String nodename,
			String pcurl,
			String appurl,
			String creator,
			String createdatetime,
			String receiver,
			String receivedatetime
	){
		/*
		if(异构系统标识存在){
			if(异构系统的【自动创建流程类型】选中){
				if(流程类型不存在){
					创建流程类型；
					记录系统日志：流程类型-自动创建-成功
					记录系统日志：流程类型-自动创建-失败-流程类型【XXXX】保存失败
				} 
				if(流程类型的【接收流程】选中){
					if(流程数据不存在){
						保存流程数据；
						保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
					}else{
						记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】已存在
					}
				}else{
					记录系统日志：流程数据-自动创建-失败-流程类型【XXXX】不允许接收流程
				}
			}
		}else{
			记录系统日志：异构系统-检测-失败-系统标识【XXXX】未注册
		}
		 */
		String createdate = createdatetime.substring(0,10);
		String createtime = createdatetime.substring(11);
		String receivedate = receivedatetime.substring(0,10);
		String receivetime = receivedatetime.substring(11);
		
		Map<String,String> OfsSettingMap = ofsSettingService.getOneMap();
		String OfsSetting_isuse = OfsUtils.getStringValueByMapKey(OfsSettingMap,"isuse","0");//是否启用统一待办中心状态
		String OfsSetting_showsysname = OfsUtils.getStringValueByMapKey(OfsSettingMap,"showsysname");//显示异构系统名称
		String OfsSetting_messagetypeid = OfsUtils.getStringValueByMapKey(OfsSettingMap,"messagetypeid","0");//手机提醒通道号
		String OfsSetting_remindebridgetemplate = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridgetemplate","0");//云桥提醒模板
		
		RTXConfig config = new RTXConfig();
		String RtxOrElinkType = config.getPorp(RTXConfig.RtxOrElinkType);//IM提醒类型
		String isusedtx = config.getPorp("isusedtx");//开启IM集成
		
		int OfsTodoData_id = 0;//待办事宜id
		int OfsWorkflow_workflowid = 0;//流程类型id
		
		if(OfsSetting.IsUse_Yes.equals(OfsSetting_isuse)){//启用统一待办中心状态
			int syscodeCnt = this.ofsSysInfoService.getCnt(syscode);//获取指定异构系统标识的数量
			if(syscodeCnt == 0){//记录系统日志：异构系统-检测-失败-异构系统标识【XXXX】未注册
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"异构系统标识【"+syscode+"】未注册",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						OfsLog.IsRemark_Todo,
						pcurl,
						appurl,
						creator,
						"0",
						createdate,
						createtime,
						receiver,
						"0",
						receivedate,
						receivetime
					);
			}else if(syscodeCnt == 1){//异构系统标识存在
				//根据系统标识获取异构系统信息
				Map<String,String> OfsSysInfoMap = ofsSysInfoService.getOneMap(syscode);
				int OfsSysInfo_sysid = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"sysid",0);//异构系统id
				int OfsSysInfo_autocreatewftype = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"autocreatewftype",0);//异构系统自动创建流程类型
				int OfsSysInfo_receivewfdata = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"receivewfdata",0);//异构系统允许接收流程数据
				
				String OfsSysInfo_pcprefixurl = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"pcprefixurl");//PC地址前缀
				String OfsSysInfo_appprefixurl = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"appprefixurl");//APP地址前缀
				
				String OfsSysInfo_sysshortname = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"sysshortname");//异构系统简称
				String OfsSysInfo_sysfullname = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"sysfullname");//异构系统全称
				
				String OfsSysInfo_sysname = "";
				if(OfsSetting_showsysname.equalsIgnoreCase(OfsSetting.ShowSysName_None)){
					OfsSysInfo_sysname = "";
				}else if(OfsSetting_showsysname.equalsIgnoreCase(OfsSetting.ShowSysName_Short)){
					OfsSysInfo_sysname = OfsSysInfo_sysshortname;
				}else if(OfsSetting_showsysname.equalsIgnoreCase(OfsSetting.ShowSysName_Full)){
					OfsSysInfo_sysname = OfsSysInfo_sysfullname;
				}
				
				String OfsSysInfo_HrmTransRule = OfsUtils.getStringValueByMapKey(OfsSysInfoMap, "hrmtransrule");//人员转换规则
				String creatorid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,creator);
				String receiverid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,receiver);
				
				String OfsSysInfo_securityip = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"securityip");//异构许可ip
				if(!checkIp(OfsSysInfo_securityip,this.clientIp)){//检测当前IP是否在许可IP范围内
					return saveLog(
							"0",
							OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"异构系统标识【"+syscode+"】当前IP（"+this.clientIp+"）未授权",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Todo,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(workflowname.equals("")){//记录系统日志：流程类型-检测-失败-流程类型未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程类型未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Todo,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(requestname.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程标题未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程标题未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Todo,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				int workflownameCnt = ofsWorkflowService.getCnt(OfsSysInfo_sysid,workflowname);//是否新流程类型：1旧，0新
				
				//根据异构系统id和流程类型名称获取异构系统流程类型信息
				Map<String,String>  OfsWorkflowMap = ofsWorkflowService.getOneMap(OfsSysInfo_sysid, workflowname);
				OfsWorkflow_workflowid = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"workflowid",0);//获取异构系统流程类型id
				int OfsWorkflow_receivewfdata = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"receivewfdata",0);//接收流程数据
				
				if(OfsSysInfo_autocreatewftype == 1){//异构系统的【自动创建流程类型】选中
					if(workflownameCnt == 0){//流程类型不存在
						//创建流程类型；
						boolean ofswftypeInsertFlag = ofsWorkflowService.insert(
								OfsSysInfo_sysid+"",workflowname,OfsSysInfo_receivewfdata+"",OfsWorkflow.Cancel_No+"",SYSADMIN_ID);
						
						if(ofswftypeInsertFlag){//记录系统日志：流程类型-自动创建-成功
							//自动创建流程成功后要重新获取流程类型数据
							OfsWorkflowMap = ofsWorkflowService.getOneMap(OfsSysInfo_sysid, workflowname);
							OfsWorkflow_workflowid = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"workflowid",0);//获取异构系统流程类型id
							OfsWorkflow_receivewfdata = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"receivewfdata",0);//接收流程数据
							
							ofsLogService.insert(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfType + LINK_CHAR + OfsWorkflow_workflowid,
									OfsLog.OperType_AutoNew,
									OfsLog.OperResult_Success,
									"流程类型【"+workflowname+"】自动创建成功",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Todo,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//记录系统日志：流程类型-自动创建-失败-流程类型【XXXX】保存失败
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_AutoNew,
									OfsLog.OperResult_Failure,
									"流程类型【"+workflowname+"】自动创建失败",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Todo,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}
					}
					
					if(OfsSysInfo_receivewfdata == 0){//异构系统的【自动创建流程类型】选中 + 异构系统不允许接收流程数据
						return saveLog(
								OfsSysInfo_sysid+"",
								OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
								OfsLog.OperType_Check,
								OfsLog.OperResult_Failure,
								"异构系统标识【"+syscode+"】不允许接收流程数据",
								syscode,
								flowid,
								requestname,
								workflowname,
								nodename,
								OfsLog.IsRemark_Todo,
								pcurl,
								appurl,
								creator,
								creatorid,
								createdate,
								createtime,
								receiver,
								receiverid,
								receivedate,
								receivetime
							);
					}else{//异构系统的【自动创建流程类型】选中 + 异构系统允许接收流程数据
						if(OfsWorkflow_receivewfdata == 0){//异构系统的【自动创建流程类型】选中 + 异构系统允许接收流程数据 + 流程类型不允许接收流程数据
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"流程类型【"+workflowname+"】不允许接收流程数据",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Todo,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//异构系统的【自动创建流程类型】选中 + 异构系统允许接收流程数据 + 流程类型允许接收流程数据
							int tododataCnt = ofsTodoDataService.getTodoDataCnt(
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									pcurl,
									appurl,
									creator,
									createdate,
									createtime,
									receiver,
									receivedate,
									receivetime
								);
							if(tododataCnt == 0){//流程数据不存在
								String flowguid = syscode + LINK_CHAR + OfsWorkflow_workflowid + LINK_CHAR + flowid + LINK_CHAR + receiver;
								int requestid = ofsTodoDataService.buildRequestid(syscode,workflowname,flowid);
								
								if(creatorid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测创建人不存在
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_Check,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】检测创建人不存在",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											OfsLog.IsRemark_Todo,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}
								
								if(receiverid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测接收人不存在
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_Check,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】检测接收人不存在",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											OfsLog.IsRemark_Todo,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}
								
								//更新之前的待办的最后状态为否
								ofsTodoDataService.updateIslasttime(syscode,receiver,workflowname+"",flowid);
								//保存流程数据；
								boolean OfsTodoData_insert_flag = ofsTodoDataService.receiveTodoRequest(
										syscode,
										OfsSysInfo_sysid+"",
										requestid+"",
										flowid,
										flowguid,
										requestname,
										workflowname,
										OfsWorkflow_workflowid+"",
										nodename,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
								
								if(!OfsTodoData_insert_flag){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】自动新增待办失败",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											OfsLog.IsRemark_Todo,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}else{
									//获取主键
									OfsTodoData_id = ofsTodoDataService.getTodoDataId(
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											pcurl,
											appurl,
											creator,
											createdate,
											createtime,
											receiver,
											receivedate,
											receivetime
										);
									
									ofsLogService.insert(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + OfsTodoData_id,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Success,
											"流程数据【"+requestname+"】自动新增待办成功",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											OfsLog.IsRemark_Todo,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
									
									String remindPcUrl = (OfsSysInfo_pcprefixurl+""+pcurl);
									String remindAppUrl = (OfsSysInfo_appprefixurl+""+appurl);
									
									String OfsParam_remindim = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindim","0");//提醒到IM
									if(isusedtx.equals("1") && OfsSetting.RemindIM_Yes.equals(OfsParam_remindim)){//开启提醒到IM
										if(RtxOrElinkType.equals("RTX")){//RTX
											sendMessageByRTX(
													requestname,
													OfsSysInfo_sysname,
													receiverid,
													remindPcUrl);
										}else if(RtxOrElinkType.equals("ELINK")){//ELINK
											sendMessageByElink(
													requestname,
													workflowname,
													receivedate,
													receivetime,
													OfsTodoData_id,
													OfsSysInfo_sysname,
													receiverid,
													remindPcUrl);
										}else {//Other
											sendMessageByOther(
													requestname,
													workflowname,
													OfsSysInfo_sysname,
													creatorid,
													receiverid,
													remindPcUrl);
										}
									}
									
									String OfsParam_remindapp = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindapp","0");//提醒到手机版
									if(OfsSetting.RemindApp_Yes.equals(OfsParam_remindapp)){//开启提醒到手机版
										sendMessageByApp(
												requestname,
												workflowname,
												OfsSysInfo_sysname,
												receiverid,
												remindAppUrl,
												OfsSetting_messagetypeid);
									}
									
									String OfsParam_remindoa = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindoa","0");//提醒到OA
									if(OfsSetting.RemindOA_Yes.equals(OfsParam_remindoa)){//开启提醒到OA
										sendMessageByOA(
												receiverid,
												requestid,
												remindPcUrl);
									}
									
									String OfsParam_remindemessage = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindemessage","0");//提醒到emessage
									if(OfsSetting.RemindEmessage_Yes.equals(OfsParam_remindemessage)){//开启提醒到emessage
										sendMessageByEmessage(
												OfsSysInfo_sysname,
												requestname,
												receiverid,
												remindPcUrl,
												creatorid,
												createdate,
												createtime);
									}
									
									String OfsParam_remindebridge = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridge","0");//提醒到微信
									if(OfsSetting.RemindEbridge_Yes.equals(OfsParam_remindebridge)){//开启提醒到微信
										sendMessageByEbridge(
												receiverid,
												requestid+"",
												requestname,
												remindAppUrl,
												OfsSetting_remindebridgetemplate);
									}
								}
							}else if(tododataCnt > 0){
								//记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】已存在
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_AutoNew,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】已存在",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Todo,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
						}
					}
					
					
				}else{//异构系统的【自动创建流程类型】关闭
					if(OfsSysInfo_receivewfdata == 0){//异构系统的【自动创建流程类型】关闭 + 异构系统不允许接收流程数据
						if(workflownameCnt == 0){//异构系统的【自动创建流程类型】关闭 + 异构系统不允许接收流程数据 + 流程类型不存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"异构系统标识【"+syscode+"】不允许创建流程类型",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Todo,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//异构系统的【自动创建流程类型】关闭 + 异构系统不允许接收流程数据 + 流程类型存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"异构系统标识【"+syscode+"】不允许接收流程数据",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Todo,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
							
						}
					}else{//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据
						if(workflownameCnt == 0){//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型不存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"异构系统标识【"+syscode+"】不允许创建流程类型",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Todo,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型存在
							if(OfsWorkflow_receivewfdata == 1){//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型存在 + 流程类型允许接收流程数据
								int tododataCnt = ofsTodoDataService.getTodoDataCnt(
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										pcurl,
										appurl,
										creator,
										createdate,
										createtime,
										receiver,
										receivedate,
										receivetime
									);
								
								if(tododataCnt == 0){//流程数据不存在
									String flowguid = syscode + LINK_CHAR + OfsWorkflow_workflowid + LINK_CHAR + flowid + LINK_CHAR + receiver;
									int requestid = ofsTodoDataService.buildRequestid(syscode,workflowname,flowid);
									
									if(creatorid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测创建人不存在
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_Check,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】检测创建人不存在",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												OfsLog.IsRemark_Todo,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}
									
									if(receiverid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测接收人不存在
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_Check,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】检测接收人不存在",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												OfsLog.IsRemark_Todo,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}
									
									//更新之前的待办的最后状态为否
									ofsTodoDataService.updateIslasttime(syscode,receiver,workflowname+"",flowid);
									//保存流程数据；
									boolean OfsTodoData_insert_flag = ofsTodoDataService.receiveTodoRequest(
											syscode,
											OfsSysInfo_sysid+"",
											requestid+"",
											flowid,
											flowguid,
											requestname,
											workflowname,
											OfsWorkflow_workflowid+"",
											nodename,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
									
									if(!OfsTodoData_insert_flag){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_AutoNew,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】自动新增待办失败",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												OfsLog.IsRemark_Todo,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}else{
										//获取主键
										OfsTodoData_id = ofsTodoDataService.getTodoDataId(
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												pcurl,
												appurl,
												creator,
												createdate,
												createtime,
												receiver,
												receivedate,
												receivetime
											);
										
										ofsLogService.insert(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + OfsTodoData_id,
												OfsLog.OperType_AutoNew,
												OfsLog.OperResult_Success,
												"流程数据【"+requestname+"】自动新增待办成功",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												OfsLog.IsRemark_Todo,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
										
										String remindPcUrl = (OfsSysInfo_pcprefixurl+""+pcurl);
										String remindAppUrl = (OfsSysInfo_appprefixurl+""+appurl);
										
										String OfsParam_remindim = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindim","0");//提醒到IM
										if(isusedtx.equals("1") && OfsSetting.RemindIM_Yes.equals(OfsParam_remindim)){//开启提醒到IM
											if(RtxOrElinkType.equals("RTX")){//RTX
												sendMessageByRTX(
														requestname,
														OfsSysInfo_sysname,
														receiverid,
														remindPcUrl);
											}else if(RtxOrElinkType.equals("ELINK")){//ELINK
												sendMessageByElink(
														requestname,
														workflowname,
														receivedate,
														receivetime,
														OfsTodoData_id,
														OfsSysInfo_sysname,
														receiverid,
														remindPcUrl);
											}else {//Other
												sendMessageByOther(
														requestname,
														workflowname,
														OfsSysInfo_sysname,
														creatorid,
														receiverid,
														remindPcUrl);
											}
										}
										
										String OfsParam_remindapp = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindapp","0");//提醒到手机版
										if(OfsSetting.RemindApp_Yes.equals(OfsParam_remindapp)){//开启提醒到手机版
											sendMessageByApp(
													requestname,
													workflowname,
													OfsSysInfo_sysname,
													receiverid,
													remindAppUrl,
													OfsSetting_messagetypeid);
										}
										
										String OfsParam_remindoa = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindoa","0");//提醒到OA
										if(OfsSetting.RemindOA_Yes.equals(OfsParam_remindoa)){//开启提醒到OA
											sendMessageByOA(receiverid,
													requestid,
													remindPcUrl);
										}
										
										String OfsParam_remindemessage = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindemessage","0");//提醒到emessage
										if(OfsSetting.RemindEmessage_Yes.equals(OfsParam_remindemessage)){//开启提醒到emessage
											sendMessageByEmessage(
													OfsSysInfo_sysname,
													requestname,
													receiverid,
													remindPcUrl,
													creatorid,
													createdate,
													createtime);
										}
										
										String OfsParam_remindebridge = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridge","0");//提醒到微信
										if(OfsSetting.RemindEbridge_Yes.equals(OfsParam_remindebridge)){//开启提醒到微信
											sendMessageByEbridge(
													receiverid,
													requestid+"",
													requestname,
													remindAppUrl,
													OfsSetting_remindebridgetemplate);
										}
									}
								}else if(tododataCnt > 0){
									//记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】已存在
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】已存在",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											OfsLog.IsRemark_Todo,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}
							}else{//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型存在 + 流程类型不允许接收流程数据
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程类型【"+workflowname+"】不允许接收流程数据",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Todo,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
						}
					}
				}
			}else if(syscodeCnt > 1) {//记录系统日志：异构系统-检测-失败-系统标识【XXXX】重复
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"系统标识【"+syscode+"】重复",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						OfsLog.IsRemark_Todo,
						pcurl,
						appurl,
						creator,
						"0",
						createdate,
						createtime,
						receiver,
						"0",
						receivedate,
						receivetime
					);
			}
		}else{
			return saveLog(
					"0",
					OfsLog.DataType_IsUse + LINK_CHAR + DEFAULT_DATA_ID,
					OfsLog.OperType_Check,
					OfsLog.OperResult_Failure,
					"统一待办中心未启用",
					syscode,
					flowid,
					requestname,
					workflowname,
					nodename,
					OfsLog.IsRemark_Todo,
					pcurl,
					appurl,
					creator,
					"0",
					createdate,
					createtime,
					receiver,
					"0",
					receivedate,
					receivetime
				);
		}
		
		return buildReceiveTodoRequest(
				syscode,
				OfsLog.DataType_WfData,
				OfsLog.OperType_AutoNew,
				OfsLog.OperResult_Success,
				"流程数据【"+requestname+"】自动新增待办成功"
			);
	}

	private Map<String, String> saveLog(
			String sysid,
			String datatype,
			String opertype,
			String operresult,
			String failremark,
			String syscode,
			String flowid,
			String requestname,
			String workflowname,
			String nodename,
			String isremark,
			String pcurl,
			String appurl,
			String creator,
			String Creatorid,
			String createdate,
			String createtime,
			String receiver,
			String userid,
			String receivedate,
			String receivetime
	) {
		String data[] = datatype.split("_");
		datatype = data[0];
		String dataid = data[1];
		
		ofsLogService.insert(
				sysid,
				datatype,
				dataid,
				opertype,
				operresult,
				failremark,
				syscode,
				flowid,
				requestname,
				workflowname,
				nodename,
				isremark,
				pcurl,
				appurl,
				creator,
				Creatorid,
				createdate,
				createtime,
				receiver,
				userid,
				receivedate,
				receivetime
			);
		return buildReceiveTodoRequest(
				syscode,
				datatype,
				opertype,
				operresult,
				failremark
			);
	}
	
	private Map<String, String> buildReceiveTodoRequest(
			String syscode,
			String dataType,
			String operType,
			String operResult,
			String message
	) {
		Map<String, String> map = new HashMap<String,String>();
		map.put("syscode", syscode);
		map.put("dataType", dataType);
		map.put("operType", operType);
		map.put("operResult", operResult);
		map.put("message", message);
		
		return map;
	}

	/**
	 * 接收异构系统流程(map格式)
	 * @param json
	 * @return
	 */
	public Map<String,String> receiveRequestInfoByMap (Map<String,String> dataMap){
		String syscode = OfsUtils.getStringValueByMapKey(dataMap,"syscode");
		String flowid = OfsUtils.getStringValueByMapKey(dataMap,"flowid");
		String requestname = OfsUtils.getStringValueByMapKey(dataMap,"requestname");
		String workflowname = OfsUtils.getStringValueByMapKey(dataMap,"workflowname");
		String nodename = OfsUtils.getStringValueByMapKey(dataMap,"nodename");
		String pcurl = OfsUtils.getStringValueByMapKey(dataMap,"pcurl");
		String appurl = OfsUtils.getStringValueByMapKey(dataMap,"appurl");
		String isremark = OfsUtils.getStringValueByMapKey(dataMap,"isremark");
		String viewtype = OfsUtils.getStringValueByMapKey(dataMap,"viewtype");
		String createdatetime = OfsUtils.getStringValueByMapKey(dataMap,"createdatetime");
		String creator = OfsUtils.getStringValueByMapKey(dataMap,"creator");
		String receiver = OfsUtils.getStringValueByMapKey(dataMap,"receiver");
		String receivedatetime = OfsUtils.getStringValueByMapKey(dataMap,"receivedatetime");
		
		return receiveRequestInfo(
				syscode,
				flowid,
				requestname,
				workflowname,
				nodename,
				pcurl,
				appurl,
				isremark,
				viewtype,
				creator,
				createdatetime,
				receiver,
				receivedatetime);
	}
		/**
	 * 删除异构系统流程(map格式)
	 * @param json
	 * @return
	 */
	public Map<String,String> deleteRequestInfoByMap (Map<String,String> dataMap){
		String syscode = OfsUtils.getStringValueByMapKey(dataMap,"syscode");
		String flowid = OfsUtils.getStringValueByMapKey(dataMap,"flowid");
		String requestname = OfsUtils.getStringValueByMapKey(dataMap,"requestname");
		if(syscode==null||"".equals(syscode)){
			String OfsLog_OperType = OfsLog.OperType_Del;
			String message = "流程数据删除失败,syscode为空";
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}
		if(flowid==null||"".equals(flowid)){
			String OfsLog_OperType = OfsLog.OperType_Del;
			String message = "流程数据删除失败,flowid为空";
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}
		
		return deleteRequestInfo(
				syscode,
				flowid
				);
	}
	/**
	 * 删除异构系统流程(json格式)
	 * @param json
	 * @return
	 */
	public String deleteRequestInfoByJson (String json){
		Map<String,String> dataMap = OfsUtils.jsonToMap(json);
		Map<String,String> resultMap = deleteRequestInfoByMap(dataMap);
		String resultJson = OfsUtils.mapToJson(resultMap);
		
		return resultJson;
	}
	/**
	 * 删除异构系统流程(xml格式)
	 * @param json
	 * @return
	 */
	public String deleteRequestInfoByXML (String xml){
		Map<String,String> dataMap = OfsUtils.xmlToMap(xml);
		Map<String,String> resultMap = deleteRequestInfoByMap(dataMap);
		return OfsUtils.mapToXml(resultMap,RESULT_XML_ROOT);
	}
	/**
	 * 根据用户id删除异构系统流程(map格式)
	 * @param json
	 * @return
	 */
	public Map<String,String> deleteUserRequestInfoByMap (Map<String,String> dataMap){
		String syscode = OfsUtils.getStringValueByMapKey(dataMap,"syscode");
		String flowid = OfsUtils.getStringValueByMapKey(dataMap,"flowid");
		String userid=OfsUtils.getStringValueByMapKey(dataMap,"userid");
		if(syscode==null||"".equals(syscode)){
			String OfsLog_OperType = OfsLog.OperType_Del;
			String message = "流程数据删除失败,syscode为空";
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}
		if(flowid==null||"".equals(flowid)){
			String OfsLog_OperType = OfsLog.OperType_Del;
			String message = "流程数据删除失败,flowid为空";
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}
		if(userid==null||"".equals(userid)){
			String OfsLog_OperType = OfsLog.OperType_Del;
			String message = "流程数据删除失败,userid为空";
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}
		
		return deleteUserRequestInfo(
				syscode,
				flowid,
				userid
				);
	}
	/**
	 * 根据用户id删除异构系统流程(json格式)
	 * @param json
	 * @return
	 */
	public String deleteUserRequestInfoByJson (String json){
		Map<String,String> dataMap = OfsUtils.jsonToMap(json);
		Map<String,String> resultMap = deleteUserRequestInfoByMap(dataMap);
		String resultJson = OfsUtils.mapToJson(resultMap);
		
		return resultJson;
	}
	
	/**
	 * 根据用户id删除异构系统流程(xml格式)
	 * @param json
	 * @return
	 */
	public String deleteUserRequestInfoByXML (String xml){
		Map<String,String> dataMap = OfsUtils.xmlToMap(xml);
		Map<String,String> resultMap = deleteUserRequestInfoByMap(dataMap);
		return OfsUtils.mapToXml(resultMap,RESULT_XML_ROOT);
	}


	/**
	 * 接收异构系统流程(json格式)
	 * @param json
	 * @return
	 */
	public String receiveRequestInfoByJson (String json){
		Map<String,String> dataMap = OfsUtils.jsonToMap(json);
		Map<String,String> resultMap = receiveRequestInfoByMap(dataMap);
		return OfsUtils.mapToJson(resultMap);
	}
	
	/**
	 * 接收异构系统流程(xml格式)
	 * @param xml
	 * @return
	 */
	public String receiveRequestInfoByXml (String xml){
		Map<String,String> dataMap = OfsUtils.xmlToMap(xml);
		Map<String,String> resultMap = receiveRequestInfoByMap(dataMap);
		return OfsUtils.mapToXml(resultMap,RESULT_XML_ROOT);
	}
	

	public Map<String, String> receiveRequestInfo(
			String syscode,
			String flowid,
			String requestname,
			String workflowname,
			String nodename,
			String pcurl,
			String appurl,
			String isremark,
			String viewtype,
			String creator,
			String createdatetime,
			String receiver,
			String receivedatetime
	) {
		/*
		if(异构系统标识存在){
			if(异构系统的【自动创建流程类型】选中){
				if(流程类型不存在){
					创建流程类型；
					记录系统日志：流程类型-自动创建-成功
					记录系统日志：流程类型-自动创建-失败-流程类型【XXXX】保存失败
				} 
				if(流程类型的【接收流程】选中){
					if(流程数据不存在){
						保存流程数据；
						保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
					}else{
						记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】已存在
					}
				}else{
					记录系统日志：流程数据-自动创建-失败-流程类型【XXXX】不允许接收流程
				}
			}
		}else{
			记录系统日志：异构系统-检测-失败-系统标识【XXXX】未注册
		}
		 */
		String createdate = createdatetime.substring(0,10);
		String createtime = createdatetime.substring(11);
		String receivedate = receivedatetime.substring(0,10);
		String receivetime = receivedatetime.substring(11);
		
		viewtype = viewtype.equals("")?OfsTodoData.ViewType_None:viewtype;//viewtype为空赋值为未读
		
		Map<String,String> OfsSettingMap = ofsSettingService.getOneMap();
		String OfsSetting_isuse = OfsUtils.getStringValueByMapKey(OfsSettingMap,"isuse","0");//是否启用统一待办中心状态
		String OfsSetting_showsysname = OfsUtils.getStringValueByMapKey(OfsSettingMap,"showsysname");//显示异构系统名称
		String OfsSetting_messagetypeid = OfsUtils.getStringValueByMapKey(OfsSettingMap,"messagetypeid","0");//手机提醒通道号
		String OfsSetting_remindebridgetemplate = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridgetemplate","0");//云桥提醒模板
		
		RTXConfig config = new RTXConfig();
		String RtxOrElinkType = config.getPorp(RTXConfig.RtxOrElinkType);//IM提醒类型
		String isusedtx = config.getPorp("isusedtx");//开启IM集成
		
		int OfsTodoData_id = 0;//待办事宜id
		int OfsWorkflow_workflowid = 0;//流程类型id
		
		String OfsLog_OperType = "";//日志操作类型
		
		if(OfsSetting.IsUse_Yes.equals(OfsSetting_isuse)){//启用统一待办中心状态
			int syscodeCnt = this.ofsSysInfoService.getCnt(syscode);//获取指定异构系统标识的数量
			if(syscodeCnt == 0){//记录系统日志：异构系统-检测-失败-异构系统标识【XXXX】未注册
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"异构系统标识【"+syscode+"】未注册",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						isremark,
						pcurl,
						appurl,
						creator,
						"0",
						createdate,
						createtime,
						receiver,
						"0",
						receivedate,
						receivetime
					);
			}else if(syscodeCnt == 1){//异构系统标识存在
				//根据系统标识获取异构系统信息
				Map<String,String> OfsSysInfoMap = ofsSysInfoService.getOneMap(syscode);
				int OfsSysInfo_sysid = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"sysid",0);//异构系统id
				int OfsSysInfo_autocreatewftype = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"autocreatewftype",0);//异构系统自动创建流程类型
				int OfsSysInfo_receivewfdata = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"receivewfdata",0);//异构系统允许接收流程数据
				
				String OfsSysInfo_pcprefixurl = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"pcprefixurl");//PC地址前缀
				String OfsSysInfo_appprefixurl = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"appprefixurl");//APP地址前缀
				
				String OfsSysInfo_sysshortname = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"sysshortname");//异构系统简称
				String OfsSysInfo_sysfullname = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"sysfullname");//异构系统全称
				
				String OfsSysInfo_sysname = "";
				if(OfsSetting_showsysname.equalsIgnoreCase(OfsSetting.ShowSysName_None)){
					OfsSysInfo_sysname = "";
				}else if(OfsSetting_showsysname.equalsIgnoreCase(OfsSetting.ShowSysName_Short)){
					OfsSysInfo_sysname = OfsSysInfo_sysshortname;
				}else if(OfsSetting_showsysname.equalsIgnoreCase(OfsSetting.ShowSysName_Full)){
					OfsSysInfo_sysname = OfsSysInfo_sysfullname;
				}
				
				String OfsSysInfo_HrmTransRule = OfsUtils.getStringValueByMapKey(OfsSysInfoMap, "hrmtransrule");//人员转换规则
				String creatorid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,creator);
				String receiverid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,receiver);
				
				String OfsSysInfo_securityip = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"securityip");//异构许可ip
				if(!checkIp(OfsSysInfo_securityip,this.clientIp)){//检测当前IP是否在许可IP范围内
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"异构系统标识【"+syscode+"】当前IP（"+this.clientIp+"）未授权",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							isremark,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(workflowname.equals("")){//记录系统日志：流程类型-检测-失败-流程类型未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程类型未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							isremark,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(requestname.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程标题未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程标题未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							isremark,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				int workflownameCnt = ofsWorkflowService.getCnt(OfsSysInfo_sysid,workflowname);//是否新流程类型：1旧，0新
				
				//根据异构系统id和流程类型名称获取异构系统流程类型信息
				Map<String,String>  OfsWorkflowMap = ofsWorkflowService.getOneMap(OfsSysInfo_sysid, workflowname);
				OfsWorkflow_workflowid = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"workflowid",0);//获取异构系统流程类型id
				int OfsWorkflow_receivewfdata = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"receivewfdata",0);//接收流程数据
				
				if(OfsSysInfo_autocreatewftype == 1){//异构系统的【自动创建流程类型】选中
					if(workflownameCnt == 0){//流程类型不存在
						//创建流程类型；
						boolean ofswftypeInsertFlag = ofsWorkflowService.insert(
								OfsSysInfo_sysid+"",workflowname,OfsSysInfo_receivewfdata+"",OfsWorkflow.Cancel_No+"",SYSADMIN_ID);
						
						if(ofswftypeInsertFlag){//记录系统日志：流程类型-自动创建-成功
							//自动创建流程成功后要重新获取流程类型数据
							OfsWorkflowMap = ofsWorkflowService.getOneMap(OfsSysInfo_sysid, workflowname);
							OfsWorkflow_workflowid = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"workflowid",0);//获取异构系统流程类型id
							OfsWorkflow_receivewfdata = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"receivewfdata",0);//接收流程数据
							
							ofsLogService.insert(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfType + LINK_CHAR + OfsWorkflow_workflowid,
									OfsLog.OperType_AutoNew,
									OfsLog.OperResult_Success,
									"流程类型【"+workflowname+"】自动创建成功",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									isremark,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//记录系统日志：流程类型-自动创建-失败-流程类型【XXXX】保存失败
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_AutoNew,
									OfsLog.OperResult_Failure,
									"流程类型【"+workflowname+"】自动创建失败",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									isremark,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}
					}
					
					if(OfsSysInfo_receivewfdata == 0){//异构系统的【自动创建流程类型】选中 + 异构系统不允许接收流程数据
						return saveLog(
								OfsSysInfo_sysid+"",
								OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
								OfsLog.OperType_Check,
								OfsLog.OperResult_Failure,
								"异构系统标识【"+syscode+"】不允许接收流程数据",
								syscode,
								flowid,
								requestname,
								workflowname,
								nodename,
								isremark,
								pcurl,
								appurl,
								creator,
								creatorid,
								createdate,
								createtime,
								receiver,
								receiverid,
								receivedate,
								receivetime
							);
					}else{//异构系统的【自动创建流程类型】选中 + 异构系统允许接收流程数据
						if(OfsWorkflow_receivewfdata == 0){//异构系统的【自动创建流程类型】选中 + 异构系统允许接收流程数据 + 流程类型不允许接收流程数据
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"流程类型【"+workflowname+"】不允许接收流程数据",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									isremark,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//异构系统的【自动创建流程类型】选中 + 异构系统允许接收流程数据 + 流程类型允许接收流程数据
							if(creatorid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测创建人不存在
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】检测创建人不存在",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										isremark,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
							
							if(receiverid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测接收人不存在
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】检测接收人不存在",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										isremark,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
							
							//检查isremark值是否合法
							if(!isremark.equals(OfsTodoData.IsRemark_Todo) && 
									!isremark.equals(OfsTodoData.IsRemark_Done) && 
									!isremark.equals(OfsTodoData.IsRemark_Over)){
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】检测isremark不合法",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										isremark,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
							
							//检查viewtype值是否合法
							if(!viewtype.equals(OfsTodoData.ViewType_None) && 
									!viewtype.equals(OfsTodoData.ViewType_Over) ){
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】检测viewtype不合法",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										isremark,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
							
							int tododataCnt = ofsTodoDataService.getTodoDataCnt(
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									pcurl,
									appurl,
									creator,
									createdate,
									createtime,
									receiver,
									receivedate,
									receivetime
								);
							
							if(tododataCnt == 0){//流程数据不存在
								String flowguid = syscode + LINK_CHAR + OfsWorkflow_workflowid + LINK_CHAR + flowid + LINK_CHAR + receiver;
								int requestid = ofsTodoDataService.buildRequestid(syscode,workflowname,flowid);
								
								//更新之前的待办的最后状态为否
								ofsTodoDataService.updateIslasttime(syscode,receiver,workflowname+"",flowid);
								//保存流程数据；
								boolean OfsTodoData_insert_flag = ofsTodoDataService.receiveRequestInfoInsert(
										syscode,
										OfsSysInfo_sysid+"",
										requestid+"",
										flowid,
										flowguid,
										requestname,
										workflowname,
										OfsWorkflow_workflowid+"",
										nodename,
										isremark,
										viewtype,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
								
								boolean OfsTodoData_update_iscomplete_flag = true;
								if(isremark.equals(OfsTodoData.IsRemark_Done)){//已办的处理
									//todo
								}else if(isremark.equals(OfsTodoData.IsRemark_Over)){//办结的处理
									//将所有已办且iscomplete=0变更为iscomplete=1；不处理待办情况
									OfsTodoData_update_iscomplete_flag = ofsTodoDataService.processOverRequestComplete(
											syscode,
											flowid,
											requestname,
											workflowname
										);
								}
								
								if(!(OfsTodoData_insert_flag && OfsTodoData_update_iscomplete_flag)){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】保存失败",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											isremark,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}else{
									//获取主键
									OfsTodoData_id = ofsTodoDataService.getTodoDataId(
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											pcurl,
											appurl,
											creator,
											createdate,
											createtime,
											receiver,
											receivedate,
											receivetime
										);
									
									OfsLog_OperType = OfsLog.OperType_AutoNew;
									
									ofsLogService.insert(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData+ LINK_CHAR + OfsTodoData_id,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Success,
											"流程数据【"+requestname+"】自动创建成功",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											isremark,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
									
									if(isremark.equals(OfsTodoData.IsRemark_Todo)){//待办才发送消息提醒
										String remindPcUrl = (OfsSysInfo_pcprefixurl+""+pcurl);
										String remindAppUrl = (OfsSysInfo_appprefixurl+""+appurl);
										
										String OfsParam_remindim = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindim","0");//提醒到IM
										if(isusedtx.equals("1") && OfsSetting.RemindIM_Yes.equals(OfsParam_remindim)){//开启提醒到IM
											if(RtxOrElinkType.equals("RTX")){//RTX
												sendMessageByRTX(
														requestname,
														OfsSysInfo_sysname,
														receiverid,
														remindPcUrl);
											}else if(RtxOrElinkType.equals("ELINK")){//ELINK
												sendMessageByElink(requestname,
														workflowname,
														receivedate,
														receivetime,
														OfsTodoData_id,
														OfsSysInfo_sysname,
														receiverid,
														remindPcUrl);
											}else {//Other
												sendMessageByOther(
														requestname,
														workflowname,
														OfsSysInfo_sysname,
														creatorid,
														receiverid,
														remindPcUrl);
											}
										}
										
										String OfsParam_remindapp = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindapp","0");//提醒到手机版
										if(OfsSetting.RemindApp_Yes.equals(OfsParam_remindapp)){//开启提醒到手机版
											sendMessageByApp(
													requestname,
													workflowname,
													OfsSysInfo_sysname,
													receiverid,
													remindAppUrl,
													OfsSetting_messagetypeid);
										}
										
										String OfsParam_remindoa = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindoa","0");//提醒到OA
										if(OfsSetting.RemindOA_Yes.equals(OfsParam_remindoa)){//开启提醒到OA
											sendMessageByOA(
													receiverid,
													requestid,
													remindPcUrl);
										}
										
										String OfsParam_remindemessage = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindemessage","0");//提醒到emessage
										if(OfsSetting.RemindEmessage_Yes.equals(OfsParam_remindemessage)){//开启提醒到emessage
											sendMessageByEmessage(
													OfsSysInfo_sysname,
													requestname,
													receiverid,
													remindPcUrl,
													creatorid,
													createdate,
													createtime);
										}
										
										String OfsParam_remindebridge = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridge","0");//提醒到微信
										if(OfsSetting.RemindEbridge_Yes.equals(OfsParam_remindebridge)){//开启提醒到微信
											sendMessageByEbridge(
													receiverid,
													requestid+"",
													requestname,
													remindAppUrl,
													OfsSetting_remindebridgetemplate);
										}
									}
								}
							}else if(tododataCnt == 1){//数据存在，做更新操作
								String flowguid = syscode + LINK_CHAR + OfsWorkflow_workflowid + LINK_CHAR + flowid + LINK_CHAR + receiver;
								int requestid = ofsTodoDataService.getRequestid(syscode,workflowname,flowid);
								
								OfsTodoData_id = ofsTodoDataService.getTodoDataId(
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										pcurl,
										appurl,
										creator,
										createdate,
										createtime,
										receiver,
										receivedate,
										receivetime
									);
								
								//保存流程数据；
								boolean OfsTodoData_update_flag = ofsTodoDataService.receiveRequestInfoUpdate(
										OfsTodoData_id+"",
										syscode,
										OfsSysInfo_sysid+"",
										requestid+"",
										flowid,
										flowguid,
										requestname,
										workflowname,
										OfsWorkflow_workflowid+"",
										nodename,
										isremark,
										viewtype,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
								
								boolean OfsTodoData_update_iscomplete_flag = true;
								if(isremark.equals(OfsTodoData.IsRemark_Done)){//已办的处理
									//todo
								}else if(isremark.equals(OfsTodoData.IsRemark_Over)){//办结的处理
									//将所有已办且iscomplete=0变更为iscomplete=1；不处理待办情况
									OfsTodoData_update_iscomplete_flag = ofsTodoDataService.processOverRequestComplete(
											syscode,
											flowid,
											requestname,
											workflowname
										);
								}
								
								if(!(OfsTodoData_update_flag && OfsTodoData_update_iscomplete_flag)){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】保存失败",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											isremark,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}else{
									//获取主键
									OfsTodoData_id = ofsTodoDataService.getTodoDataId(
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											pcurl,
											appurl,
											creator,
											createdate,
											createtime,
											receiver,
											receivedate,
											receivetime
										);
									
									OfsLog_OperType = OfsLog.OperType_AutoEdit;
									
									ofsLogService.insert(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + OfsTodoData_id,
											OfsLog.OperType_AutoEdit,
											OfsLog.OperResult_Success,
											"流程数据【"+requestname+"】自动更新成功",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											isremark,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
									
									if(isremark.equals(OfsTodoData.IsRemark_Todo)){//待办才发送消息提醒
										String remindPcUrl = (OfsSysInfo_pcprefixurl+""+pcurl);
										String remindAppUrl = (OfsSysInfo_appprefixurl+""+appurl);
										
										String OfsParam_remindim = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindim","0");//提醒到IM
										if(isusedtx.equals("1") && OfsSetting.RemindIM_Yes.equals(OfsParam_remindim)){//开启提醒到IM
											if(RtxOrElinkType.equals("RTX")){//RTX
												sendMessageByRTX(
														requestname,
														OfsSysInfo_sysname,
														receiverid, remindPcUrl);
											}else if(RtxOrElinkType.equals("ELINK")){//ELINK
												sendMessageByElink(
														requestname,
														workflowname,
														receivedate,
														receivetime,
														OfsTodoData_id,
														OfsSysInfo_sysname,
														receiverid, remindPcUrl);
											}else {//Other
												sendMessageByOther(
														requestname,
														workflowname,
														OfsSysInfo_sysname,
														creatorid,
														receiverid,
														remindPcUrl);
											}
										}
										
										String OfsParam_remindapp = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindapp","0");//提醒到手机版
										if(OfsSetting.RemindApp_Yes.equals(OfsParam_remindapp)){//开启提醒到手机版
											sendMessageByApp(
													requestname,
													workflowname,
													OfsSysInfo_sysname,
													receiverid,
													remindAppUrl,
													OfsSetting_messagetypeid);
										}
										
										String OfsParam_remindoa = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindoa","0");//提醒到OA
										if(OfsSetting.RemindOA_Yes.equals(OfsParam_remindoa)){//开启提醒到OA
											sendMessageByOA(
													receiverid,
													requestid,
													remindPcUrl);
										}
										
										String OfsParam_remindemessage = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindemessage","0");//提醒到emessage
										if(OfsSetting.RemindEmessage_Yes.equals(OfsParam_remindemessage)){//开启提醒到emessage
											sendMessageByEmessage(
													OfsSysInfo_sysname,
													requestname,
													receiverid,
													remindPcUrl,
													creatorid,
													createdate,
													createtime);
										}
										
										String OfsParam_remindebridge = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridge","0");//提醒到微信
										if(OfsSetting.RemindEbridge_Yes.equals(OfsParam_remindebridge)){//开启提醒到微信
											sendMessageByEbridge(
													receiverid,
													requestid+"",
													requestname,
													remindAppUrl,
													OfsSetting_remindebridgetemplate);
										}
									}
								}
							}else if(tododataCnt > 1){
								//记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】已存在
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_AutoNew,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】存在多条",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										isremark,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
						}
					}
					
					
				}else{//异构系统的【自动创建流程类型】关闭
					if(OfsSysInfo_receivewfdata == 0){//异构系统的【自动创建流程类型】关闭 + 异构系统不允许接收流程数据
						if(workflownameCnt == 0){//异构系统的【自动创建流程类型】关闭 + 异构系统不允许接收流程数据 + 流程类型不存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"异构系统标识【"+syscode+"】不允许创建流程类型",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									isremark,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//异构系统的【自动创建流程类型】关闭 + 异构系统不允许接收流程数据 + 流程类型存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"异构系统标识【"+syscode+"】不允许接收流程数据",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									isremark,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
							
						}
					}else{//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据
						if(workflownameCnt == 0){//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型不存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"异构系统标识【"+syscode+"】不允许创建流程类型",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									isremark,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else{//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型存在
							if(OfsWorkflow_receivewfdata == 1){//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型存在 + 流程类型允许接收流程数据
								int tododataCnt = ofsTodoDataService.getTodoDataCnt(
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										pcurl,
										appurl,
										creator,
										createdate,
										createtime,
										receiver,
										receivedate,
										receivetime
									);
								if(tododataCnt == 0){//流程数据不存在
									String flowguid = syscode + LINK_CHAR + OfsWorkflow_workflowid + LINK_CHAR + flowid + LINK_CHAR + receiver;
									int requestid = ofsTodoDataService.buildRequestid(syscode,workflowname,flowid);
									
									if(creatorid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测创建人不存在
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_Check,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】检测创建人不存在",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}
									
									if(receiverid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测接收人不存在
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_Check,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】检测接收人不存在",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}
									//检查isremark值是否合法
									if(!isremark.equals(OfsTodoData.IsRemark_Todo) && 
											!isremark.equals(OfsTodoData.IsRemark_Done) && 
											!isremark.equals(OfsTodoData.IsRemark_Over)){
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_Check,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】检测isremark不合法",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}
									
									//检查viewtype值是否合法
									if(!viewtype.equals(OfsTodoData.ViewType_None) && 
											!viewtype.equals(OfsTodoData.ViewType_Over) ){
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_Check,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】检测viewtype不合法",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}
									
									//更新之前的待办的最后状态为否
									ofsTodoDataService.updateIslasttime(syscode,receiver,workflowname+"",flowid);
									//保存流程数据；
									boolean OfsTodoData_insert_flag = ofsTodoDataService.receiveRequestInfoInsert(
											syscode,
											OfsSysInfo_sysid+"",
											requestid+"",
											flowid,
											flowguid,
											requestname,
											workflowname,
											OfsWorkflow_workflowid+"",
											nodename,
											isremark,
											viewtype,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
									
									boolean OfsTodoData_update_iscomplete_flag = true;
									if(isremark.equals(OfsTodoData.IsRemark_Done)){//已办的处理
										//todo
									}else if(isremark.equals(OfsTodoData.IsRemark_Over)){//办结的处理
										//将所有已办且iscomplete=0变更为iscomplete=1；不处理待办情况
										OfsTodoData_update_iscomplete_flag = ofsTodoDataService.processOverRequestComplete(
												syscode,
												flowid,
												requestname,
												workflowname
											);
									}
									
									if(!(OfsTodoData_insert_flag && OfsTodoData_update_iscomplete_flag)){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_AutoNew,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】保存失败",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}else{
										//获取主键
										OfsTodoData_id = ofsTodoDataService.getTodoDataId(
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												pcurl,
												appurl,
												creator,
												createdate,
												createtime,
												receiver,
												receivedate,
												receivetime
											);
										OfsLog_OperType = OfsLog.OperType_AutoNew;
										
										ofsLogService.insert(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + OfsTodoData_id,
												OfsLog.OperType_AutoNew,
												OfsLog.OperResult_Success,
												"流程数据【"+requestname+"】自动创建成功",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
										
										if(isremark.equals(OfsTodoData.IsRemark_Todo)){//待办才发送消息提醒
											String remindPcUrl = (OfsSysInfo_pcprefixurl+""+pcurl);
											String remindAppUrl = (OfsSysInfo_appprefixurl+""+appurl);
											
											String OfsParam_remindim = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindim","0");//提醒到IM
											if(isusedtx.equals("1") && OfsSetting.RemindIM_Yes.equals(OfsParam_remindim)){//开启提醒到IM
												if(RtxOrElinkType.equals("RTX")){//RTX
													sendMessageByRTX(
															requestname,
															OfsSysInfo_sysname,
															receiverid,
															remindPcUrl);
												}else if(RtxOrElinkType.equals("ELINK")){//ELINK
													sendMessageByElink(
															requestname,
															workflowname,
															receivedate,
															receivetime,
															OfsTodoData_id,
															OfsSysInfo_sysname,
															receiverid,
															remindPcUrl);
												}else {//Other
													sendMessageByOther(
															requestname,
															workflowname,
															OfsSysInfo_sysname,
															creatorid,
															receiverid,
															remindPcUrl);
												}
											}
											
											String OfsParam_remindapp = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindapp","0");//提醒到手机版
											if(OfsSetting.RemindApp_Yes.equals(OfsParam_remindapp)){//开启提醒到手机版
												sendMessageByApp(requestname,
														workflowname,
														OfsSysInfo_sysname,
														receiverid,
														remindAppUrl,
														OfsSetting_messagetypeid);
											}
											
											String OfsParam_remindoa = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindoa","0");//提醒到OA
											if(OfsSetting.RemindOA_Yes.equals(OfsParam_remindoa)){//开启提醒到OA
												sendMessageByOA(receiverid,
														requestid,
														remindPcUrl);
											}
											
											String OfsParam_remindemessage = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindemessage","0");//提醒到emessage
											if(OfsSetting.RemindEmessage_Yes.equals(OfsParam_remindemessage)){//开启提醒到emessage
												sendMessageByEmessage(
														OfsSysInfo_sysname,
														requestname,
														receiverid,
														remindPcUrl,
														creatorid,
														createdate,
														createtime);
											}
											
											String OfsParam_remindebridge = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridge","0");//提醒到微信
											if(OfsSetting.RemindEbridge_Yes.equals(OfsParam_remindebridge)){//开启提醒到微信
												sendMessageByEbridge(
														receiverid,
														requestid+"",
														requestname,
														remindAppUrl,
														OfsSetting_remindebridgetemplate);
											}
										}
									}
								}else if(tododataCnt == 1){//数据存在，做更新操作
									String flowguid = syscode + LINK_CHAR + OfsWorkflow_workflowid + LINK_CHAR + flowid + LINK_CHAR + receiver;
									int requestid = ofsTodoDataService.getRequestid(syscode,workflowname,flowid);
									
									OfsTodoData_id = ofsTodoDataService.getTodoDataId(
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											pcurl,
											appurl,
											creator,
											createdate,
											createtime,
											receiver,
											receivedate,
											receivetime
										);
									
									
									//保存流程数据；
									boolean OfsTodoData_update_flag = ofsTodoDataService.receiveRequestInfoUpdate(
											OfsTodoData_id+"",
											syscode,
											OfsSysInfo_sysid+"",
											requestid+"",
											flowid,
											flowguid,
											requestname,
											workflowname,
											OfsWorkflow_workflowid+"",
											nodename,
											isremark,
											viewtype,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
									
									boolean OfsTodoData_update_iscomplete_flag = true;
									if(isremark.equals(OfsTodoData.IsRemark_Done)){//已办的处理
										//todo
									}else if(isremark.equals(OfsTodoData.IsRemark_Over)){//办结的处理
										//将所有已办且iscomplete=0变更为iscomplete=1；不处理待办情况
										OfsTodoData_update_iscomplete_flag = ofsTodoDataService.processOverRequestComplete(
												syscode,
												flowid,
												requestname,
												workflowname
											);
									}
									
									if(!(OfsTodoData_update_flag && OfsTodoData_update_iscomplete_flag)){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
										return saveLog(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
												OfsLog.OperType_AutoNew,
												OfsLog.OperResult_Failure,
												"流程数据【"+requestname+"】保存失败",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
									}else{
										//获取主键
										OfsTodoData_id = ofsTodoDataService.getTodoDataId(
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												pcurl,
												appurl,
												creator,
												createdate,
												createtime,
												receiver,
												receivedate,
												receivetime
											);
										
										OfsLog_OperType = OfsLog.OperType_AutoEdit;
										
										ofsLogService.insert(
												OfsSysInfo_sysid+"",
												OfsLog.DataType_WfData+ LINK_CHAR + OfsTodoData_id,
												OfsLog.OperType_AutoEdit,
												OfsLog.OperResult_Success,
												"流程数据【"+requestname+"】自动更新成功",
												syscode,
												flowid,
												requestname,
												workflowname,
												nodename,
												isremark,
												pcurl,
												appurl,
												creator,
												creatorid,
												createdate,
												createtime,
												receiver,
												receiverid,
												receivedate,
												receivetime
											);
										
										if(isremark.equals(OfsTodoData.IsRemark_Todo)){//待办才发送消息提醒
											String remindPcUrl = (OfsSysInfo_pcprefixurl+""+pcurl);
											String remindAppUrl = (OfsSysInfo_appprefixurl+""+appurl);
											
											String OfsParam_remindim = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindim","0");//提醒到IM
											if(isusedtx.equals("1") && OfsSetting.RemindIM_Yes.equals(OfsParam_remindim)){//开启提醒到IM
												if(RtxOrElinkType.equals("RTX")){//RTX
													sendMessageByRTX(
															requestname,
															OfsSysInfo_sysname,
															receiverid,
															remindPcUrl);
												}else if(RtxOrElinkType.equals("ELINK")){//ELINK
													sendMessageByElink(
															requestname,
															workflowname,
															receivedate,
															receivetime,
															OfsTodoData_id,
															OfsSysInfo_sysname,
															receiverid,
															remindPcUrl);
												}else {//Other
													sendMessageByOther(
															requestname,
															workflowname,
															OfsSysInfo_sysname,
															creatorid,
															receiverid,
															remindPcUrl);
												}
											}
											
											String OfsParam_remindapp = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindapp","0");//提醒到手机版
											if(OfsSetting.RemindApp_Yes.equals(OfsParam_remindapp)){//开启提醒到手机版
												sendMessageByApp(
														requestname,
														workflowname,
														OfsSysInfo_sysname,
														receiverid,
														remindAppUrl,
														OfsSetting_messagetypeid);
											}
											
											String OfsParam_remindoa = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindoa","0");//提醒到OA
											if(OfsSetting.RemindOA_Yes.equals(OfsParam_remindoa)){//开启提醒到OA
												sendMessageByOA(
														receiverid,
														requestid,
														remindPcUrl);
											}
											
											String OfsParam_remindemessage = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindemessage","0");//提醒到emessage
											if(OfsSetting.RemindEmessage_Yes.equals(OfsParam_remindemessage)){//开启提醒到emessage
												sendMessageByEmessage(
														OfsSysInfo_sysname,
														requestname,
														receiverid,
														remindPcUrl,
														creatorid,
														createdate,
														createtime);
											}
											
											String OfsParam_remindebridge = OfsUtils.getStringValueByMapKey(OfsSettingMap,"remindebridge","0");//提醒到微信
											if(OfsSetting.RemindEbridge_Yes.equals(OfsParam_remindebridge)){//开启提醒到微信
												sendMessageByEbridge(
														receiverid,
														requestid+"",
														requestname,
														remindAppUrl,
														OfsSetting_remindebridgetemplate);
											}
										}
									}
								}else if(tododataCnt > 0){
									//记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】已存在
									return saveLog(
											OfsSysInfo_sysid+"",
											OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
											OfsLog.OperType_AutoNew,
											OfsLog.OperResult_Failure,
											"流程数据【"+requestname+"】已存在",
											syscode,
											flowid,
											requestname,
											workflowname,
											nodename,
											isremark,
											pcurl,
											appurl,
											creator,
											creatorid,
											createdate,
											createtime,
											receiver,
											receiverid,
											receivedate,
											receivetime
										);
								}
							}else{//异构系统的【自动创建流程类型】关闭 + 异构系统允许接收流程数据 + 流程类型存在 + 流程类型不允许接收流程数据
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程类型【"+workflowname+"】不允许接收流程数据",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										isremark,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
						}
					}
				}
			}else if(syscodeCnt > 1) {//记录系统日志：异构系统-检测-失败-系统标识【XXXX】重复
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"系统标识【"+syscode+"】重复",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						isremark,
						pcurl,
						appurl,
						creator,
						"0",
						createdate,
						createtime,
						receiver,
						"0",
						receivedate,
						receivetime
					);
			}
		}else{
			return saveLog(
					"0",
					OfsLog.DataType_IsUse + LINK_CHAR + DEFAULT_DATA_ID,
					OfsLog.OperType_Check,
					OfsLog.OperResult_Failure,
					"统一待办中心未启用",
					syscode,
					flowid,
					requestname,
					workflowname,
					nodename,
					isremark,
					pcurl,
					appurl,
					creator,
					"0",
					createdate,
					createtime,
					receiver,
					"0",
					receivedate,
					receivetime
				);
		}
		
		String message = "";
		if(OfsLog_OperType == OfsLog.OperType_AutoNew){
			message = "流程数据【"+requestname+"】自动新增成功";
		}else if(OfsLog_OperType == OfsLog.OperType_AutoEdit){
			message = "流程数据【"+requestname+"】自动更新成功";
		}
		
		return buildReceiveTodoRequest(
				syscode,
				OfsLog.DataType_WfData,
				OfsLog_OperType,
				OfsLog.OperResult_Success,
				message
			);
	}
		public Map<String, String> deleteRequestInfo(
			String syscode,
			String flowid
	) {
		int tododataCnt = ofsTodoDataService.getTodoDataCnt(syscode,flowid); 
		if(tododataCnt==0){
			String message = "流程数据删除失败,流程数据不存在";
			String OfsLog_OperType = OfsLog.OperType_Del;
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}else{
			ofsTodoDataService.receiveRequestInfoDelete(syscode,flowid);
			String message = "流程数据删除成功";
			String OfsLog_OperType = OfsLog.OperType_Del;
			
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Success,
					message
				);
		}
	}
	
	public Map<String, String> deleteUserRequestInfo(
			String syscode,
			String flowid,
			String userid
	) {
		int tododataCnt = ofsTodoDataService.getTodoDataCnt(syscode,flowid,userid);
		if(tododataCnt==0){
			String message = "流程数据删除失败,流程数据不存在";
			String OfsLog_OperType = OfsLog.OperType_Del;
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Failure,
					message
				);
		}else{
			ofsTodoDataService.receiveRequestInfoUserDelete(syscode, flowid, userid);
			
			String message = "流程数据删除成功";
			String OfsLog_OperType = OfsLog.OperType_Del;
			
			return buildReceiveTodoRequest(
					syscode,
					OfsLog.DataType_WfData,
					OfsLog_OperType,
					OfsLog.OperResult_Success,
					message
				);
		}
	}
	
	/**
	 * 
	 * @param dataMap
	 * @return
	 */
	public Map<String,String> processDoneRequestByMap(Map<String,String> dataMap){
		String syscode = OfsUtils.getStringValueByMapKey(dataMap,"syscode");
		String flowid = OfsUtils.getStringValueByMapKey(dataMap,"flowid");
		String requestname = OfsUtils.getStringValueByMapKey(dataMap,"requestname");
		String workflowname = OfsUtils.getStringValueByMapKey(dataMap,"workflowname");
		String nodename = OfsUtils.getStringValueByMapKey(dataMap,"nodename");
		String receiver = OfsUtils.getStringValueByMapKey(dataMap,"receiver");
		
		return processDoneRequest(
				syscode,
				flowid,
				requestname,
				workflowname,
				nodename,
				receiver
			);
	}
	
	/**
	 * 处理待办流程（变为已办）
	 * @param json
	 * @return
	 */
	public String processDoneRequestByJson (String json){
		Map<String,String> dataMap = OfsUtils.jsonToMap(json);
		Map<String,String> resultMap = processDoneRequestByMap(dataMap);
		return OfsUtils.mapToJson(resultMap);
	}
	
	/**
	 * 处理待办流程（变为已办）
	 * @param xml
	 * @return
	 */
	public String processDoneRequestByXml (String xml){
		Map<String,String> dataMap = OfsUtils.xmlToMap(xml);
		Map<String,String> resultMap = processDoneRequestByMap(dataMap);
		return OfsUtils.mapToXml(resultMap,RESULT_XML_ROOT);
	}
	
	public Map<String,String> processDoneRequest(
			String syscode,
			String flowid,
			String requestname,
			String workflowname,
			String nodename,
			String receiver
	){
		/*
		if(异构系统标识存在){
			if(流程类型存在){
				if(流程类型的【接收流程】选中){
					if(流程数据存在){
						if(集成参数【显示已办】开启){
							更新流程数据为已办；
							保存失败，记录系统日志：流程数据-自动更新-失败-流程数据【XXXX】流程状态变更为已办失败
						}else{
							删除数据。
						}
					}else{
						记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】不存在
					}
				}else{
					记录系统日志：流程数据-自动更新-失败-流程类型【XXXX】不允许接收流程
				}
			}else{
				记录系统日志：流程数据-自动更新-失败-流程类型【XXXX】不存在
			}
		}else{
			记录系统日志：异构系统-检测-失败-异构系统标识【XXXX】未注册
		}

		 */
		Map<String,String> OfsSettingMap = ofsSettingService.getOneMap();
		String OfsSetting_isuse = OfsUtils.getStringValueByMapKey(OfsSettingMap,"isuse","0");//是否启用统一待办中心状态
		
		String pcurl = "";
		String appurl = "";
		String creator = "";
		String creatorid = "0";
		String createdate = "";
		String createtime = "";
		String receiverid = "0";
		String receivedate = "";
		String receivetime = "";
		if(OfsSetting.IsUse_Yes.equals(OfsSetting_isuse)){//启用统一待办中心状态
			int syscodeCnt = this.ofsSysInfoService.getCnt(syscode);//获取指定异构系统标识的数量
			if(syscodeCnt == 0){//记录系统日志：异构系统-检测-失败-异构系统标识【XXXX】未注册
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"异构系统标识【"+syscode+"】未注册",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						OfsLog.IsRemark_Done,
						pcurl,
						appurl,
						creator,
						creatorid,
						createdate,
						createtime,
						receiver,
						receiverid,
						receivedate,
						receivetime
					);
			}else if(syscodeCnt == 1){//异构系统标识存在
				//根据系统标识获取异构系统信息
				Map<String,String> OfsSysInfoMap = ofsSysInfoService.getOneMap(syscode);
				int OfsSysInfo_sysid = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"sysid",0);//异构系统id
				
				String OfsSysInfo_HrmTransRule = OfsUtils.getStringValueByMapKey(OfsSysInfoMap, "hrmtransrule");//人员转换规则
				creatorid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,creator);
				receiverid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,receiver);
				
				String OfsSysInfo_securityip = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"securityip");//异构许可ip
				if(!checkIp(OfsSysInfo_securityip,this.clientIp)){//检测当前IP是否在许可IP范围内
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"异构系统标识【"+syscode+"】当前IP（"+this.clientIp+"）未授权",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Done,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(workflowname.equals("")){//记录系统日志：流程类型-检测-失败-流程类型未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程类型未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Done,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(requestname.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程标题未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程标题未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Done,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				int workflownameCnt = ofsWorkflowService.getCnt(OfsSysInfo_sysid,workflowname);
				if(workflownameCnt == 0){//记录系统日志：流程类型-检测-失败-流程类型【XXXX】未注册
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程类型【"+workflowname+"】未注册",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Done,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}else{//流程类型存在
					//根据异构系统id和流程类型名称获取异构系统流程类型信息
					Map<String,String>  OfsWorkflowMap = ofsWorkflowService.getOneMap(OfsSysInfo_sysid, workflowname);
					int OfsWorkflow_receivewfdata = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"receivewfdata",0);//接收流程数据
					
					if(OfsWorkflow_receivewfdata == 1){//流程类型的【接收流程】选中
						int tododataCnt = ofsTodoDataService.getTodoDataCnt(
								syscode,
								flowid,
								requestname,
								workflowname,
								nodename,
								receiver
							);
						if(tododataCnt == 0){//异构系统流程数据不存在
							//记录系统日志：流程数据-检测-失败-流程数据【XXXX】不存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"流程数据【"+requestname+"】不存在",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Done,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}else if(tododataCnt == 1){//异构系统流程数据存在
							if(receiverid.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测接收人不存在
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】检测接收人不存在",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Done,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
							
							//更新待办变为已办
							boolean OfsTodoData_update_flag = ofsTodoDataService.processDoneRequest(
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									receiver
								);
							
							if(!OfsTodoData_update_flag){//保存失败，记录系统日志：流程数据-自动更新-失败-流程数据【XXXX】保存失败
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_AutoEdit,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】自动更新已办失败",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Done,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}else{
								int OfsTodoData_id = ofsTodoDataService.getTodoDataId(
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										pcurl,
										appurl,
										creator,
										createdate,
										createtime,
										receiver,
										receivedate,
										receivetime
									);
								
								ofsLogService.insert(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + OfsTodoData_id,
										OfsLog.OperType_AutoEdit,
										OfsLog.OperResult_Success,
										"流程数据【"+requestname+"】自动更新已办成功",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Done,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
						}else if(tododataCnt > 1){//异构系统流程数据重复
							//记录系统日志：流程数据-检测-失败-流程数据【XXXX】重复
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfData+ LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"流程数据【"+requestname+"】重复",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Done,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}
					}else{
						//记录系统日志：流程类型-检测-失败-流程类型【XXXX】不允许接收流程
						return saveLog(
								OfsSysInfo_sysid+"",
								OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
								OfsLog.OperType_Check,
								OfsLog.OperResult_Failure,
								"流程类型【"+workflowname+"】不允许接收流程",
								syscode,
								flowid,
								requestname,
								workflowname,
								nodename,
								OfsLog.IsRemark_Done,
								pcurl,
								appurl,
								creator,
								creatorid,
								createdate,
								createtime,
								receiver,
								receiverid,
								receivedate,
								receivetime
							);
					}
				}
			}else if(syscodeCnt > 1) {//记录系统日志：异构系统-检测-失败-系统标识【XXXX】重复
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"系统标识【"+syscode+"】重复",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						OfsLog.IsRemark_Done,
						pcurl,
						appurl,
						creator,
						creatorid,
						createdate,
						createtime,
						receiver,
						receiverid,
						receivedate,
						receivetime
					);
			}
		}else{
			return saveLog(
					"0",
					OfsLog.DataType_IsUse + LINK_CHAR + DEFAULT_DATA_ID,
					OfsLog.OperType_Check,
					OfsLog.OperResult_Failure,
					"统一待办中心未启用",
					syscode,
					flowid,
					requestname,
					workflowname,
					nodename,
					OfsLog.IsRemark_Done,
					pcurl,
					appurl,
					creator,
					creatorid,
					createdate,
					createtime,
					receiver,
					receiverid,
					receivedate,
					receivetime
				);
		}
		
		return buildReceiveTodoRequest(
				syscode,
				OfsLog.DataType_WfData,
				OfsLog.OperType_AutoEdit,
				OfsLog.OperResult_Success,
				"流程数据【"+requestname+"】自动更新已办成功"
			);
	}
	/**
	 * 
	 * @param dataMap
	 * @return
	 */
	public Map<String,String> processOverRequestByMap(Map<String,String> dataMap){
		String syscode = OfsUtils.getStringValueByMapKey(dataMap,"syscode");
		String flowid = OfsUtils.getStringValueByMapKey(dataMap,"flowid");
		String requestname = OfsUtils.getStringValueByMapKey(dataMap,"requestname");
		String workflowname = OfsUtils.getStringValueByMapKey(dataMap,"workflowname");
		String nodename = OfsUtils.getStringValueByMapKey(dataMap,"nodename");
		String receiver = OfsUtils.getStringValueByMapKey(dataMap,"receiver");
		
		return processOverRequest(
				syscode,
				flowid,
				requestname,
				workflowname,
				nodename,
				receiver
			);
	}
	
	/**
	 * 处理办结流程（变为办结）
	 * @param json
	 * @return
	 */
	public String processOverRequestByJson(String json){
		Map<String,String> dataMap = OfsUtils.jsonToMap(json);
		Map<String,String> resultMap = processOverRequestByMap(dataMap);
		return OfsUtils.mapToJson(resultMap);
	}
	
	public String processOverRequestByXml(String xml){
		Map<String,String> dataMap = OfsUtils.xmlToMap(xml);
		Map<String,String> resultMap = processOverRequestByMap(dataMap);
		return OfsUtils.mapToXml(resultMap,RESULT_XML_ROOT);
	}
	
	public Map<String,String> processOverRequest(
			String syscode,
			String flowid,
			String requestname,
			String workflowname,
			String nodename,
			String receiver
	){
		/*
		if(异构系统标识存在){
			if(流程类型存在){
				if(流程类型的【接收流程】选中){
					if(流程数据存在){
						更新流程数据为办结；
						保存失败，记录系统日志：流程数据-自动更新-失败-流程数据【XXXX】流程状态变更为办结失败
					}else{
						记录系统日志：流程数据-自动更新-失败-流程数据【XXXX】不存在
					}
				}else{
					记录系统日志：流程数据-自动更新-失败-流程类型【XXXX】不允许接收流程
				}
			}else{
				记录系统日志：流程数据-自动更新-失败-流程类型【XXXX】不存在
			}
		}else{
			记录系统日志：异构系统-检测-失败-异构系统标识【XXXX】未注册
		}
		 */
		Map<String,String> OfsSettingMap = ofsSettingService.getOneMap();
		String OfsSetting_isuse = OfsUtils.getStringValueByMapKey(OfsSettingMap,"isuse","0");//是否启用统一待办中心状态
		
		String pcurl = "";
		String appurl = "";
		String creator = "";
		String creatorid = "0";
		String createdate = "";
		String createtime = "";
		String receiverid = "0";
		String receivedate = "";
		String receivetime = "";
		
		if(OfsSetting.IsUse_Yes.equals(OfsSetting_isuse)){//启用统一待办中心状态
			int syscodeCnt = this.ofsSysInfoService.getCnt(syscode);//获取指定异构系统标识的数量
			if(syscodeCnt == 0){//记录系统日志：异构系统-检测-失败-异构系统标识【XXXX】未注册
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys+ LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"异构系统标识【"+syscode+"】未注册",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						OfsLog.IsRemark_Over,
						pcurl,
						appurl,
						creator,
						creatorid,
						createdate,
						createtime,
						receiver,
						receiverid,
						receivedate,
						receivetime
					);
			}else if(syscodeCnt == 1){//异构系统标识存在
				//根据系统标识获取异构系统信息
				Map<String,String> OfsSysInfoMap = ofsSysInfoService.getOneMap(syscode);
				int OfsSysInfo_sysid = OfsUtils.getIntValueByMapKey(OfsSysInfoMap,"sysid",0);//异构系统id
				
				String OfsSysInfo_HrmTransRule = OfsUtils.getStringValueByMapKey(OfsSysInfoMap, "hrmtransrule");//人员转换规则
				creatorid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,creator);
				receiverid = ofsSysInfoService.getHrmResourceIdByHrmTransRule(OfsSysInfo_HrmTransRule,receiver);
				
				String OfsSysInfo_securityip = OfsUtils.getStringValueByMapKey(OfsSysInfoMap,"securityip");//异构许可ip
				if(!checkIp(OfsSysInfo_securityip,this.clientIp)){//检测当前IP是否在许可IP范围内
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"异构系统标识【"+syscode+"】当前IP（"+this.clientIp+"）未授权",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Over,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(workflowname.equals("")){//记录系统日志：流程类型-检测-失败-流程类型未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程类型未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Done,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				if(requestname.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程标题未填写
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程标题未填写",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Done,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}
				
				int workflownameCnt = ofsWorkflowService.getCnt(OfsSysInfo_sysid,workflowname);
				if(workflownameCnt == 0){//记录系统日志：流程类型-检测-失败-流程类型【XXXX】未注册
					return saveLog(
							OfsSysInfo_sysid+"",
							OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
							OfsLog.OperType_Check,
							OfsLog.OperResult_Failure,
							"流程类型【"+workflowname+"】未注册",
							syscode,
							flowid,
							requestname,
							workflowname,
							nodename,
							OfsLog.IsRemark_Over,
							pcurl,
							appurl,
							creator,
							creatorid,
							createdate,
							createtime,
							receiver,
							receiverid,
							receivedate,
							receivetime
						);
				}else{//流程类型存在
					//根据异构系统id和流程类型名称获取异构系统流程类型信息
					Map<String,String>  OfsWorkflowMap = ofsWorkflowService.getOneMap(OfsSysInfo_sysid, workflowname);
					int OfsWorkflow_receivewfdata = OfsUtils.getIntValueByMapKey(OfsWorkflowMap,"receivewfdata",0);//接收流程数据
					
					if(OfsWorkflow_receivewfdata == 1){//流程类型的【接收流程】选中
						int tododataCnt = ofsTodoDataService.getTodoDataCnt(
								syscode,
								flowid,
								requestname,
								workflowname,
								receiver
							);
						if(tododataCnt == 0){//异构系统流程数据不存在
							//记录系统日志：流程数据-检测-失败-流程数据【XXXX】不存在
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"流程数据【"+requestname+"】不存在",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Over,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						//}else if(tododataCnt == 1){//异构系统流程数据存在
						}else{
							if(receiver.equals("")){//保存失败，记录系统日志：流程数据-检测-失败-流程数据【"+requestname+"】检测接收人不存在
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_Check,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】检测接收人不存在",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Over,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
							
							//更新已办变为办结
							boolean OfsTodoData_update_flag = ofsTodoDataService.processOverRequest(
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									receiver
								);
							
							//将所有已办且iscomplete=0变更为iscomplete=1；不处理待办情况
							boolean OfsTodoData_update_iscomplete_flag = ofsTodoDataService.processOverRequestComplete(
									syscode,
									flowid,
									requestname,
									workflowname
								);
							
							if(!(OfsTodoData_update_flag&&OfsTodoData_update_iscomplete_flag)){//保存失败，记录系统日志：流程数据-自动创建-失败-流程数据【XXXX】保存失败
								return saveLog(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
										OfsLog.OperType_AutoNew,
										OfsLog.OperResult_Failure,
										"流程数据【"+requestname+"】自动更新办结失败",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Over,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}else{
								//获取主键
								int OfsTodoData_id = ofsTodoDataService.getTodoDataId(
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										pcurl,
										appurl,
										creator,
										createdate,
										createtime,
										receiver,
										receivedate,
										receivetime
									);
								
								ofsLogService.insert(
										OfsSysInfo_sysid+"",
										OfsLog.DataType_WfData + LINK_CHAR + OfsTodoData_id,
										OfsLog.OperType_AutoEdit,
										OfsLog.OperResult_Success,
										"流程数据【"+requestname+"】自动更新办结成功",
										syscode,
										flowid,
										requestname,
										workflowname,
										nodename,
										OfsLog.IsRemark_Over,
										pcurl,
										appurl,
										creator,
										creatorid,
										createdate,
										createtime,
										receiver,
										receiverid,
										receivedate,
										receivetime
									);
							}
						}/*else if(tododataCnt > 1){//异构系统流程数据重复
							//记录系统日志：流程数据-检测-失败-流程数据【XXXX】重复
							return saveLog(
									OfsSysInfo_sysid+"",
									OfsLog.DataType_WfData + LINK_CHAR + DEFAULT_DATA_ID,
									OfsLog.OperType_Check,
									OfsLog.OperResult_Failure,
									"流程数据【"+requestname+"】重复",
									syscode,
									flowid,
									requestname,
									workflowname,
									nodename,
									OfsLog.IsRemark_Over,
									pcurl,
									appurl,
									creator,
									creatorid,
									createdate,
									createtime,
									receiver,
									receiverid,
									receivedate,
									receivetime
								);
						}*/
					}else{
						//记录系统日志：流程类型-检测-失败-流程类型【XXXX】不允许接收流程
						return saveLog(
								OfsSysInfo_sysid+"",
								OfsLog.DataType_WfType + LINK_CHAR + DEFAULT_DATA_ID,
								OfsLog.OperType_Check,
								OfsLog.OperResult_Failure,
								"流程类型【"+workflowname+"】不允许接收流程",
								syscode,
								flowid,
								requestname,
								workflowname,
								nodename,
								OfsLog.IsRemark_Over,
								pcurl,
								appurl,
								creator,
								creatorid,
								createdate,
								createtime,
								receiver,
								receiverid,
								receivedate,
								receivetime
							);
					}
				}
			}else if(syscodeCnt > 1) {//记录系统日志：异构系统-检测-失败-系统标识【XXXX】重复
				return saveLog(
						"0",
						OfsLog.DataType_OtherSys + LINK_CHAR + DEFAULT_DATA_ID,
						OfsLog.OperType_Check,
						OfsLog.OperResult_Failure,
						"系统标识【"+syscode+"】重复",
						syscode,
						flowid,
						requestname,
						workflowname,
						nodename,
						OfsLog.IsRemark_Over,
						pcurl,
						appurl,
						creator,
						creatorid,
						createdate,
						createtime,
						receiver,
						receiverid,
						receivedate,
						receivetime
					);
			}
		}else{
			return saveLog(
					"0",
					OfsLog.DataType_IsUse + LINK_CHAR + DEFAULT_DATA_ID,
					OfsLog.OperType_Check,
					OfsLog.OperResult_Failure,
					"统一待办中心未启用",
					syscode,
					flowid,
					requestname,
					workflowname,
					nodename,
					OfsLog.IsRemark_Over,
					pcurl,
					appurl,
					creator,
					creatorid,
					createdate,
					createtime,
					receiver,
					receiverid,
					receivedate,
					receivetime
				);
		}

		return buildReceiveTodoRequest(
				syscode,
				OfsLog.DataType_WfData,
				OfsLog.OperType_AutoEdit,
				OfsLog.OperResult_Success,
				"流程数据【"+requestname+"】自动更新办结成功"
			);
	}
	
	/**
	 * ip检测
	 * securityip为空，表示任何地址者可以访问；
	 * securityip不为空，则检查clientIp是否在securityip中指定了；
	 * @param securityip 指定的ip范围
	 * @param clientIp 当前访问ip
	 * @return
	 */
	private boolean checkIp(final String securityip,final String clientIp){
		return !securityip.equals("")?(","+securityip+",").indexOf(","+clientIp+",") > -1 : true; 
	}
	
	/**
	 * 发送RTX消息
	 * @param requestname
	 * @param sysname
	 * @param receiverid
	 * @param remindPcUrl
	 */
	private void sendMessageByRTX(
			String requestname,
			String sysname, 
			String receiverid, 
			String remindPcUrl
	) {
		new Thread(new OfsRemindRTXManager(Integer.parseInt(receiverid),sysname.equals("")?requestname:"("+sysname+"):"+requestname,remindPcUrl)).start();
	}
	
	/**
	 * 发送Elink消息
	 * @param requestname
	 * @param workflowname
	 * @param receivedate
	 * @param receivetime
	 * @param id
	 * @param sysname
	 * @param receiverid
	 * @param remindPcUrl
	 */
	private void sendMessageByElink(
			String requestname,
			String workflowname,
			String receivedate,
			String receivetime,
			int id,
			String sysname,
			String receiverid,
			String remindPcUrl) {
		StringBuffer messageJson = new StringBuffer();
		messageJson.append("{");
		messageJson.append("\"id\":\""+id+"\",");
		messageJson.append("\"sysname\":\""+sysname+"\",");
		messageJson.append("\"requestname\":\""+requestname+"\",");
		messageJson.append("\"workflowname\":\""+workflowname+"\",");
		messageJson.append("\"receivedate\":\""+receivedate+"\",");
		messageJson.append("\"receivetime\":\""+receivetime+"\"");
		messageJson.append("}");
		
		new Thread(new OfsRemindElinkManager(Integer.parseInt(receiverid),messageJson.toString(),remindPcUrl)).start();
	}

	/**
	 * 向其他IM发送消息
	 * @param requestname
	 * @param workflowname
	 * @param sysname
	 * @param creatorid
	 * @param receiverid
	 * @param remindPcUrl
	 */
	private void sendMessageByOther(
			String requestname,
			String workflowname,
			String sysname,
			String creatorid,
			String receiverid,
			String remindPcUrl
	) {
		StringBuffer messageJson = new StringBuffer();
		messageJson.append("{");
		messageJson.append("\"sender\":\""+creatorid+"\",");
		if(sysname.equals("")){
			messageJson.append("\"title\":\""+workflowname+"\",");
		}else{
			messageJson.append("\"title\":\""+sysname+":"+workflowname+"\",");
		}
		messageJson.append("\"content\":\""+requestname+"\"");
		messageJson.append("}");
		
		new Thread(new OfsRemindOtherManager(Integer.parseInt(receiverid),messageJson.toString(),remindPcUrl)).start();
	}
	
	/**
	 * 发送APP消息
	 * @param requestname
	 * @param workflowname
	 * @param sysname
	 * @param receiverid
	 * @param remindAppUrl
	 */
	private void sendMessageByApp(
			String requestname,
			String workflowname,
			String sysname,
			String receiverid,
			String remindAppUrl,
			String messagetypeid
	) {
		/*StringBuffer messageJson = new StringBuffer();
		messageJson.append("{");
		if(sysname.equals("")){
			messageJson.append("\"ftitle\":\""+workflowname+"\",");
		}else{
			messageJson.append("\"ftitle\":\""+sysname+":"+workflowname+"\",");
		}
		messageJson.append("\"fcontent\":\""+requestname+"\",");
		messageJson.append("\"fsendtime\":\""+TimeUtil.getCurrentTimeString()+"\"");
		messageJson.append("}");*/
		
		new Thread(new OfsRemindAppManager(Integer.parseInt(receiverid),requestname,remindAppUrl,messagetypeid)).start();
	}

	/**
	 * 发送OA提醒消息
	 * @param receiverid
	 * @param requestid
	 * @param remindPcUrl
	 */
	private void sendMessageByOA(
			String receiverid,
			int requestid,
			String remindPcUrl
	) {
		new Thread(new OfsRemindOAManager(Integer.parseInt(receiverid),requestid+"",remindPcUrl)).start();
	}
	
	/**
	 * 发送emessage提醒消息
	 * @param receiverid
	 * @param requestid
	 * @param remindPcUrl
	 */
	private void sendMessageByEmessage(
			String sysname,
			String requestname,
			String receiverid,
			String remindPcUrl,
			String creatorid,
			String createdate,
			String createtime
	) {
		ResourceComInfo rci = null;
		try {
			rci = new ResourceComInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String lastname = Util.null2String(rci.getLastname(creatorid));
		String requestdetails = "创建人："+lastname+"<br>创建时间："+createdate+" "+createtime;
        new Thread(new OfsRemindEmessageManager(Integer.parseInt(receiverid),sysname,requestname,requestdetails,remindPcUrl)).start();
	}
	
	/**
	 * 发送微信提醒消息
	 * @param receiverid
	 * @param requestid
	 * @param remindPcUrl
	 */
	private void sendMessageByEbridge(
			String receiverid,
			String requestid,
			String requestname,
			String remindAppUrl,
			String remindebridgetemplate
	) {
		log.error("sendMessageByEbridge");
		new Thread(new OfsRemindEbridgeManager(Integer.parseInt(receiverid),requestid,requestname,remindAppUrl,remindebridgetemplate)).start();
	}
}
