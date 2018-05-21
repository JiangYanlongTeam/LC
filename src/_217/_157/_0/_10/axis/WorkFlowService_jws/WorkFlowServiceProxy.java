package _217._157._0._10.axis.WorkFlowService_jws;

public class WorkFlowServiceProxy implements _217._157._0._10.axis.WorkFlowService_jws.WorkFlowService {
  private String _endpoint = null;
  private _217._157._0._10.axis.WorkFlowService_jws.WorkFlowService workFlowService = null;
  
  public WorkFlowServiceProxy() {
    _initWorkFlowServiceProxy();
  }
  
  public WorkFlowServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initWorkFlowServiceProxy();
  }
  
  private void _initWorkFlowServiceProxy() {
    try {
      workFlowService = (new _217._157._0._10.axis.WorkFlowService_jws.WorkFlowServiceServiceLocator()).getWorkFlowService();
      if (workFlowService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)workFlowService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)workFlowService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (workFlowService != null)
      ((javax.xml.rpc.Stub)workFlowService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public _217._157._0._10.axis.WorkFlowService_jws.WorkFlowService getWorkFlowService() {
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService;
  }
  
  public java.lang.String getTaskPortalView(java.lang.String user_id, int num_of_items) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.getTaskPortalView(user_id, num_of_items);
  }
  
  public java.lang.String setAnalogWorkFlow(java.lang.String xml) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.setAnalogWorkFlow(xml);
  }
  
  public java.lang.String checklogin(java.lang.String user, java.lang.String pwd) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.checklogin(user, pwd);
  }
  
  public java.lang.String getExtAccountToOAUserId(java.lang.String extAccount, java.lang.String type) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.getExtAccountToOAUserId(extAccount, type);
  }
  
  public java.lang.String checkSSOTokenForMYSOFT(java.lang.String token, java.lang.String user_id) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.checkSSOTokenForMYSOFT(token, user_id);
  }
  
  public java.lang.String startWorkFlow(int map_id, java.lang.String user_id, java.lang.String dataXml) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.startWorkFlow(map_id, user_id, dataXml);
  }
  
  public java.lang.String createSSOToken(java.lang.String user, java.lang.String app_key) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.createSSOToken(user, app_key);
  }
  
  public java.lang.String checkSSOTokenByUserCode(java.lang.String token, java.lang.String user_code) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.checkSSOTokenByUserCode(token, user_code);
  }
  
  public java.lang.String checkSSOToken(java.lang.String token, java.lang.String user_id) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.checkSSOToken(token, user_id);
  }
  
  public java.lang.String checkSSOTokenXml(java.lang.String token, java.lang.String user_id) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.checkSSOTokenXml(token, user_id);
  }
  
  public java.lang.String startSimpleWorkFlow(int priority_id, java.lang.String workflow_name, java.lang.String create_user_id, int ori_task_id, int ori_request_id, java.lang.String process_template_flag, java.lang.String step_name, int assignee_req_id, java.lang.String assignee_name, java.lang.String assignee_type) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.startSimpleWorkFlow(priority_id, workflow_name, create_user_id, ori_task_id, ori_request_id, process_template_flag, step_name, assignee_req_id, assignee_name, assignee_type);
  }
  
  public java.lang.String finishWorkFlow(int ori_request_id, java.lang.String process_template_flag) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.finishWorkFlow(ori_request_id, process_template_flag);
  }
  
  public java.lang.String finishWorkFlowStep(int ori_task_id, int ori_request_id, java.lang.String process_template_flag) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.finishWorkFlowStep(ori_task_id, ori_request_id, process_template_flag);
  }
  
  public java.lang.String getRsfPortalView(java.lang.String user_id, java.lang.String category, int num_of_items) throws java.rmi.RemoteException{
    if (workFlowService == null)
      _initWorkFlowServiceProxy();
    return workFlowService.getRsfPortalView(user_id, category, num_of_items);
  }
  
  
}