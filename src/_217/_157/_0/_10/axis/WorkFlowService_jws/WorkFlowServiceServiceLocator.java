/**
 * WorkFlowServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package _217._157._0._10.axis.WorkFlowService_jws;

public class WorkFlowServiceServiceLocator extends org.apache.axis.client.Service implements _217._157._0._10.axis.WorkFlowService_jws.WorkFlowServiceService {

    public WorkFlowServiceServiceLocator() {
    }


    public WorkFlowServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WorkFlowServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WorkFlowService
    private java.lang.String WorkFlowService_address = "http://api.ibtcloud.cn/axis/WorkFlowService.jws";

    public java.lang.String getWorkFlowServiceAddress() {
        return WorkFlowService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WorkFlowServiceWSDDServiceName = "WorkFlowService";

    public java.lang.String getWorkFlowServiceWSDDServiceName() {
        return WorkFlowServiceWSDDServiceName;
    }

    public void setWorkFlowServiceWSDDServiceName(java.lang.String name) {
        WorkFlowServiceWSDDServiceName = name;
    }

    public _217._157._0._10.axis.WorkFlowService_jws.WorkFlowService getWorkFlowService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WorkFlowService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWorkFlowService(endpoint);
    }

    public _217._157._0._10.axis.WorkFlowService_jws.WorkFlowService getWorkFlowService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            _217._157._0._10.axis.WorkFlowService_jws.WorkFlowServiceSoapBindingStub _stub = new _217._157._0._10.axis.WorkFlowService_jws.WorkFlowServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getWorkFlowServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWorkFlowServiceEndpointAddress(java.lang.String address) {
        WorkFlowService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (_217._157._0._10.axis.WorkFlowService_jws.WorkFlowService.class.isAssignableFrom(serviceEndpointInterface)) {
                _217._157._0._10.axis.WorkFlowService_jws.WorkFlowServiceSoapBindingStub _stub = new _217._157._0._10.axis.WorkFlowService_jws.WorkFlowServiceSoapBindingStub(new java.net.URL(WorkFlowService_address), this);
                _stub.setPortName(getWorkFlowServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("WorkFlowService".equals(inputPortName)) {
            return getWorkFlowService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://10.0.157.217/axis/WorkFlowService.jws", "WorkFlowServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://10.0.157.217/axis/WorkFlowService.jws", "WorkFlowService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WorkFlowService".equals(portName)) {
            setWorkFlowServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
