package com.example.wirtualnatalia.network.service;


public class ServiceData {
    // Singleton
    private static final ServiceData INSTANCE = new ServiceData();
    private ServiceData() {}
    public static ServiceData getInstance(){
        return INSTANCE;
    }

    // data
    private boolean serviceStarted;
    private String serviceName;
    private boolean connectedToService;
    private String connectedToServiceName;
    private ServiceConnection connection;

    public void setServiceStarted(boolean val) { serviceStarted = val; }
    public boolean getServiceStarted(){ return serviceStarted; }

    public void setServiceName(String val) { serviceName = val; }
    public String getServiceName(){ return serviceName; }

    public void setConnectedToService(boolean val){ connectedToService = val; }
    public boolean getConnectedToService(){ return connectedToService; }

    public void setConnectedToServiceName(String val){ connectedToServiceName = val; }
    public String getConnectedToServiceName(){ return connectedToServiceName; }

    public void setConnection(ServiceConnection val) { connection = val; }
    public ServiceConnection getConnection() { return connection; }
}