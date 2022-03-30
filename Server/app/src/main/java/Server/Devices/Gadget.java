package Server.Devices;

public class Gadget {

    public String state;
    public String type;
    public String name;
    
    public Gadget(String state, String type, String name)
    {
        this.state = state;
        this.type = type;
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    
}
