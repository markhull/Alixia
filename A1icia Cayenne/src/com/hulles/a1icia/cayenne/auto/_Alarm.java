/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.hulles.a1icia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _Alarm was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Alarm extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ALARM_ID_PK_COLUMN = "alarm_ID";

    public static final Property<LocalDateTime> DATE_TIME = Property.create("dateTime", LocalDateTime.class);
    public static final Property<String> DESCRIPTION = Property.create("description", String.class);
    public static final Property<Integer> PERSON_ID = Property.create("personId", Integer.class);

    protected LocalDateTime dateTime;
    protected String description;
    protected int personId;


    public void setDateTime(LocalDateTime dateTime) {
        beforePropertyWrite("dateTime", this.dateTime, dateTime);
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        beforePropertyRead("dateTime");
        return this.dateTime;
    }

    public void setDescription(String description) {
        beforePropertyWrite("description", this.description, description);
        this.description = description;
    }

    public String getDescription() {
        beforePropertyRead("description");
        return this.description;
    }

    public void setPersonId(Integer personId) {
        beforePropertyWrite("personId", this.personId, personId);
        this.personId = personId;
    }

    public Integer getPersonId() {
        beforePropertyRead("personId");
        return this.personId;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "dateTime":
                return this.dateTime;
            case "description":
                return this.description;
            case "personId":
                return this.personId;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "dateTime":
                this.dateTime = (LocalDateTime)val;
                break;
            case "description":
                this.description = (String)val;
                break;
            case "personId":
                this.personId = val == null ? 0 : (int)val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.dateTime);
        out.writeObject(this.description);
        out.writeInt(this.personId);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.dateTime = (LocalDateTime)in.readObject();
        this.description = (String)in.readObject();
        this.personId = in.readInt();
    }

}