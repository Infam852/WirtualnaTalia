package com.example.wirtualnatalia.network.service;


public class ServiceData {
    // Singleton
    private static final ServiceData INSTANCE = new ServiceData();
    private ServiceData() {}
    public static ServiceData getInstance(){
        return INSTANCE;
    }

    // data
    private String serviceName;
    private String connectedToServiceName;
    private ServiceConnection connection;

    public void setServiceName(String val) { serviceName = val; }
    public String getServiceName(){ return serviceName; }

    public void setConnectedToServiceName(String val){ connectedToServiceName = val; }
    public String getConnectedToServiceName(){ return connectedToServiceName; }

    public void setConnection(ServiceConnection val) { connection = val; }
    public ServiceConnection getConnection() { return connection; }
}