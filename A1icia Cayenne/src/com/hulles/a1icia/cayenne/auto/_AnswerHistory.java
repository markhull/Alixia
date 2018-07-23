package com.hulles.a1icia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

import com.hulles.a1icia.cayenne.AnswerChunk;
import com.hulles.a1icia.cayenne.Spark;

/**
 * Class _AnswerHistory was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _AnswerHistory extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ANSWER_HISTORY_ID_PK_COLUMN = "answer_history_ID";

    public static final Property<String> LEMMATIZED_QUESTION = Property.create("lemmatizedQuestion", String.class);
    public static final Property<String> ORIGINAL_QUESTION = Property.create("originalQuestion", String.class);
    public static final Property<String> POS_TAGS = Property.create("posTags", String.class);
    public static final Property<Integer> SATISFACTION = Property.create("satisfaction", Integer.class);
    public static final Property<String> SPARK_OBJECT = Property.create("sparkObject", String.class);
    public static final Property<List<AnswerChunk>> ANSWER_CHUNKS = Property.create("answerChunks", List.class);
    public static final Property<Spark> SPARK = Property.create("spark", Spark.class);

    protected String lemmatizedQuestion;
    protected String originalQuestion;
    protected String posTags;
    protected Integer satisfaction;
    protected String sparkObject;

    protected Object answerChunks;
    protected Object spark;

    public void setLemmatizedQuestion(String lemmatizedQuestion) {
        beforePropertyWrite("lemmatizedQuestion", this.lemmatizedQuestion, lemmatizedQuestion);
        this.lemmatizedQuestion = lemmatizedQuestion;
    }

    public String getLemmatizedQuestion() {
        beforePropertyRead("lemmatizedQuestion");
        return this.lemmatizedQuestion;
    }

    public void setOriginalQuestion(String originalQuestion) {
        beforePropertyWrite("originalQuestion", this.originalQuestion, originalQuestion);
        this.originalQuestion = originalQuestion;
    }

    public String getOriginalQuestion() {
        beforePropertyRead("originalQuestion");
        return this.originalQuestion;
    }

    public void setPosTags(String posTags) {
        beforePropertyWrite("posTags", this.posTags, posTags);
        this.posTags = posTags;
    }

    public String getPosTags() {
        beforePropertyRead("posTags");
        return this.posTags;
    }

    public void setSatisfaction(Integer satisfaction) {
        beforePropertyWrite("satisfaction", this.satisfaction, satisfaction);
        this.satisfaction = satisfaction;
    }

    public Integer getSatisfaction() {
        beforePropertyRead("satisfaction");
        return this.satisfaction;
    }

    public void setSparkObject(String sparkObject) {
        beforePropertyWrite("sparkObject", this.sparkObject, sparkObject);
        this.sparkObject = sparkObject;
    }

    public String getSparkObject() {
        beforePropertyRead("sparkObject");
        return this.sparkObject;
    }

    public void addToAnswerChunks(AnswerChunk obj) {
        addToManyTarget("answerChunks", obj, true);
    }

    public void removeFromAnswerChunks(AnswerChunk obj) {
        removeToManyTarget("answerChunks", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<AnswerChunk> getAnswerChunks() {
        return (List<AnswerChunk>)readProperty("answerChunks");
    }

    public void setSpark(Spark spark) {
        setToOneTarget("spark", spark, true);
    }

    public Spark getSpark() {
        return (Spark)readProperty("spark");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "lemmatizedQuestion":
                return this.lemmatizedQuestion;
            case "originalQuestion":
                return this.originalQuestion;
            case "posTags":
                return this.posTags;
            case "satisfaction":
                return this.satisfaction;
            case "sparkObject":
                return this.sparkObject;
            case "answerChunks":
                return this.answerChunks;
            case "spark":
                return this.spark;
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
            case "lemmatizedQuestion":
                this.lemmatizedQuestion = (String)val;
                break;
            case "originalQuestion":
                this.originalQuestion = (String)val;
                break;
            case "posTags":
                this.posTags = (String)val;
                break;
            case "satisfaction":
                this.satisfaction = (Integer)val;
                break;
            case "sparkObject":
                this.sparkObject = (String)val;
                break;
            case "answerChunks":
                this.answerChunks = val;
                break;
            case "spark":
                this.spark = val;
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
        out.writeObject(this.lemmatizedQuestion);
        out.writeObject(this.originalQuestion);
        out.writeObject(this.posTags);
        out.writeObject(this.satisfaction);
        out.writeObject(this.sparkObject);
        out.writeObject(this.answerChunks);
        out.writeObject(this.spark);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.lemmatizedQuestion = (String)in.readObject();
        this.originalQuestion = (String)in.readObject();
        this.posTags = (String)in.readObject();
        this.satisfaction = (Integer)in.readObject();
        this.sparkObject = (String)in.readObject();
        this.answerChunks = in.readObject();
        this.spark = in.readObject();
    }

}
