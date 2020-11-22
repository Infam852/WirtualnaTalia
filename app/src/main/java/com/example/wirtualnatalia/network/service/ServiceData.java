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

    public void setServiceStarted(boolean val) { serviceStarted = val; }
    public boolean getServiceStarted(){ return serviceStarted; }

    public void setServiceName(String val) { serviceName = val; }
    public String getServiceName(){ return serviceName; }

}