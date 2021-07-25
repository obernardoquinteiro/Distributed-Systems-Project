package pt.tecnico.bicloin.hub.domain;

public class User {

    private String username;
    private String name;
    private String cellphone;

    public User (String username, String name, String cellphone){
        this.username = username;
        this.name = name;
        this.cellphone = cellphone;
    }

    public String getUsername(){
        return this.username;
    }

    public String getCellphone(){
        return cellphone;
    }

}
