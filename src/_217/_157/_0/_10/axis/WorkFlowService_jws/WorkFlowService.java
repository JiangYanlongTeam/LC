/**
 * WorkFlowService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package _217._157._0._10.axis.WorkFlowService_jws;

public interface WorkFlowService extends java.rmi.Remote {
    public java.lang.String getTaskPortalView(java.lang.String user_id, int num_of_items) throws java.rmi.RemoteException;
    public java.lang.String setAnalogWorkFlow(java.lang.String xml) throws java.rmi.RemoteException;
    public java.lang.String checklogin(java.lang.String user, java.lang.String pwd) throws java.rmi.RemoteException;
    public java.lang.String getExtAccountToOAUserId(java.lang.String extAccount, java.lang.String type) throws java.rmi.RemoteException;
    public java.lang.String checkSSOTokenForMYSOFT(java.lang.String token, java.lang.String user_id) throws java.rmi.RemoteException;
    public java.lang.String startWorkFlow(int map_id, java.lang.String user_id, java.lang.String dataXml) throws java.rmi.RemoteException;
    public java.lang.String createSSOToken(java.lang.String user, java.lang.String app_key) throws java.rmi.RemoteException;
    public java.lang.String checkSSOTokenByUserCode(java.lang.String token, java.lang.String user_code) throws java.rmi.RemoteException;
    public java.lang.String checkSSOToken(java.lang.String token, java.lang.String user_id) throws java.rmi.RemoteException;
    public java.lang.String checkSSOTokenXml(java.lang.String token, java.lang.String user_id) throws java.rmi.RemoteException;
    public java.lang.String startSimpleWorkFlow(int priority_id, java.lang.String workflow_name, java.lang.String create_user_id, int ori_task_id, int ori_request_id, java.lang.String process_template_flag, java.lang.String step_name, int assignee_req_id, java.lang.String assignee_name, java.lang.String assignee_type) throws java.rmi.RemoteException;
    public java.lang.String finishWorkFlow(int ori_request_id, java.lang.String process_template_flag) throws java.rmi.RemoteException;
    public java.lang.String finishWorkFlowStep(int ori_task_id, int ori_request_id, java.lang.String process_template_flag) throws java.rmi.RemoteException;
    public java.lang.String getRsfPortalView(java.lang.String user_id, java.lang.String category, int num_of_items) throws java.rmi.RemoteException;
}
