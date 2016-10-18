/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smallfe.usergenerator;

/**
 * Write short comment related with created class
 * @author mkucukdemir
 */
class SampleName {
    
    private Integer Id;
    private String Name;
    private char Sex;
    private String Sinifi;

    public SampleName() {
    }

    public SampleName(Integer Id, String Name, char Sex) {
        this.Id = Id;
        this.Name = Name;
        this.Sex = Sex;
        this.Sinifi = null;
    }
    
    public SampleName(Integer Id, String Name, char Sex, String Sinifi) {
        this.Id = Id;
        this.Name = Name;
        this.Sex = Sex;
        this.Sinifi = Sinifi;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public char getSex() {
        return Sex;
    }

    public void setSex(char Sex) {
        this.Sex = Sex;
    }

    @Override
    public String toString() {
        return "SampleName{" + "Id=" + Id + ", Name=" + Name + ", Sex=" + Sex + '}';
    }

    /**
     * @return the Sinifi
     */
    public String getSinifi() {
        return Sinifi;
    }

    /**
     * @param Sinifi the Sinifi to set
     */
    public void setSinifi(String Sinifi) {
        this.Sinifi = Sinifi;
    }
    
}
