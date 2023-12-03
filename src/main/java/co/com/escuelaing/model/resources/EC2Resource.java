package co.com.escuelaing.model.resources;

import co.com.escuelaing.model.Resource;

public final class EC2Resource extends Resource {

    public InstanceType instance_type = InstanceType.MICRO;
    public VPC vpc;
    public OS os = OS.AMAZON_LINUX;

    
    public static enum OS {
        WINDOWS,
        AMAZON_LINUX,
    }

    public static enum InstanceType {
        MICRO
    }
    

    public static class VPC {
        public String name;
    }
}
