package com.ahmadnawaz.i160020_150069;

class ContactModel {
    String name,address,phno;
    String image_url,coordinates,active_status;


    public ContactModel(){

    }

    public ContactModel(String name, String address, String phno,String image_url,String coordinates,String active_status) {
        this.name = name;
        this.address = address;
        this.phno = phno;
        this.image_url=image_url;
        this.coordinates=coordinates;
        this.active_status=active_status;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhno() {
        return phno;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getActivestatus() {
        return active_status;
    }


}
