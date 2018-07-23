package com.hulles.a1icia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

import com.hulles.a1icia.cayenne.Person;
import com.hulles.a1icia.cayenne.TaskPriority;
import com.hulles.a1icia.cayenne.TaskStatus;
import com.hulles.a1icia.cayenne.TaskType;

/**
 * Class _Task was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Task extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String TASK_ID_PK_COLUMN = "task_ID";

    public static final Property<LocalDateTime> DATE_COMPLETED = Property.create("dateCompleted", LocalDateTime.class);
    public static final Property<LocalDateTime> DATE_CREATED = Property.create("dateCreated", LocalDateTime.class);
    public static final Property<LocalDateTime> DATE_DUE = Property.create("dateDue", LocalDateTime.class);
    public static final Property<String> DESCRIPTION = Property.create("description", String.class);
    public static final Property<String> NOTES = Property.create("notes", String.class);
    public static final Property<String> TASK_UUID = Property.create("taskUuid", String.class);
    public static final Property<Person> PERSON = Property.create("person", Person.class);
    public static final Property<TaskPriority> TASK_PRIORITY = Property.create("taskPriority", TaskPriority.class);
    public static final Property<TaskStatus> TASK_STATUS = Property.create("taskStatus", TaskStatus.class);
    public static final Property<TaskType> TASK_TYPE = Property.create("taskType", TaskType.class);

    protected LocalDateTime dateCompleted;
    protected LocalDateTime dateCreated;
    protected LocalDateTime dateDue;
    protected String description;
    protected String notes;
    protected String taskUuid;

    protected Object person;
    protected Object taskPriority;
    protected Object taskStatus;
    protected Object taskType;

    public void setDateCompleted(LocalDateTime dateCompleted) {
        beforePropertyWrite("dateCompleted", this.dateCompleted, dateCompleted);
        this.dateCompleted = dateCompleted;
    }

    public LocalDateTime getDateCompleted() {
        beforePropertyRead("dateCompleted");
        return this.dateCompleted;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        beforePropertyWrite("dateCreated", this.dateCreated, dateCreated);
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateCreated() {
        beforePropertyRead("dateCreated");
        return this.dateCreated;
    }

    public void setDateDue(LocalDateTime dateDue) {
        beforePropertyWrite("dateDue", this.dateDue, dateDue);
        this.dateDue = dateDue;
    }

    public LocalDateTime getDateDue() {
        beforePropertyRead("dateDue");
        return this.dateDue;
    }

    public void setDescription(String description) {
        beforePropertyWrite("description", this.description, description);
        this.description = description;
    }

    public String getDescription() {
        beforePropertyRead("description");
        return this.description;
    }

    public void setNotes(String notes) {
        beforePropertyWrite("notes", this.notes, notes);
        this.notes = notes;
    }

    public String getNotes() {
        beforePropertyRead("notes");
        return this.notes;
    }

    public void setTaskUuid(String taskUuid) {
        beforePropertyWrite("taskUuid", this.taskUuid, taskUuid);
        this.taskUuid = taskUuid;
    }

    public String getTaskUuid() {
        beforePropertyRead("taskUuid");
        return this.taskUuid;
    }

    public void setPerson(Person person) {
        setToOneTarget("person", person, true);
    }

    public Person getPerson() {
        return (Person)readProperty("person");
    }

    public void setTaskPriority(TaskPriority taskPriority) {
        setToOneTarget("taskPriority", taskPriority, true);
    }

    public TaskPriority getTaskPriority() {
        return (TaskPriority)readProperty("taskPriority");
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        setToOneTarget("taskStatus", taskStatus, true);
    }

    public TaskStatus getTaskStatus() {
        return (TaskStatus)readProperty("taskStatus");
    }

    public void setTaskType(TaskType taskType) {
        setToOneTarget("taskType", taskType, true);
    }

    public TaskType getTaskType() {
        return (TaskType)readProperty("taskType");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "dateCompleted":
                return this.dateCompleted;
            case "dateCreated":
                return this.dateCreated;
            case "dateDue":
                return this.dateDue;
            case "description":
                return this.description;
            case "notes":
                return this.notes;
            case "taskUuid":
                return this.taskUuid;
            case "person":
                return this.person;
            case "taskPriority":
                return this.taskPriority;
            case "taskStatus":
                return this.taskStatus;
            case "taskType":
                return this.taskType;
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
            case "dateCompleted":
                this.dateCompleted = (LocalDateTime)val;
                break;
            case "dateCreated":
                this.dateCreated = (LocalDateTime)val;
                break;
            case "dateDue":
                this.dateDue = (LocalDateTime)val;
                break;
            case "description":
                this.description = (String)val;
                break;
            case "notes":
                this.notes = (String)val;
                break;
            case "taskUuid":
                this.taskUuid = (String)val;
                break;
            case "person":
                this.person = val;
                break;
            case "taskPriority":
                this.taskPriority = val;
                break;
            case "taskStatus":
                this.taskStatus = val;
                break;
            case "taskType":
                this.taskType = val;
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
        out.writeObject(this.dateCompleted);
        out.writeObject(this.dateCreated);
        out.writeObject(this.dateDue);
        out.writeObject(this.description);
        out.writeObject(this.notes);
        out.writeObject(this.taskUuid);
        out.writeObject(this.person);
        out.writeObject(this.taskPriority);
        out.writeObject(this.taskStatus);
        out.writeObject(this.taskType);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.dateCompleted = (LocalDateTime)in.readObject();
        this.dateCreated = (LocalDateTime)in.readObject();
        this.dateDue = (LocalDateTime)in.readObject();
        this.description = (String)in.readObject();
        this.notes = (String)in.readObject();
        this.taskUuid = (String)in.readObject();
        this.person = in.readObject();
        this.taskPriority = in.readObject();
        this.taskStatus = in.readObject();
        this.taskType = in.readObject();
    }

}
