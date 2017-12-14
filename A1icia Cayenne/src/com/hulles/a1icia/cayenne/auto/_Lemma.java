package com.hulles.a1icia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _Lemma was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Lemma extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String LEMMA_ID_PK_COLUMN = "lemma_ID";

    public static final Property<String> LEMMA = Property.create("lemma", String.class);
    public static final Property<String> POS = Property.create("pos", String.class);
    public static final Property<String> WORD = Property.create("word", String.class);

    protected String lemma;
    protected String pos;
    protected String word;


    public void setLemma(String lemma) {
        beforePropertyWrite("lemma", this.lemma, lemma);
        this.lemma = lemma;
    }

    public String getLemma() {
        beforePropertyRead("lemma");
        return this.lemma;
    }

    public void setPos(String pos) {
        beforePropertyWrite("pos", this.pos, pos);
        this.pos = pos;
    }

    public String getPos() {
        beforePropertyRead("pos");
        return this.pos;
    }

    public void setWord(String word) {
        beforePropertyWrite("word", this.word, word);
        this.word = word;
    }

    public String getWord() {
        beforePropertyRead("word");
        return this.word;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "lemma":
                return this.lemma;
            case "pos":
                return this.pos;
            case "word":
                return this.word;
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
            case "lemma":
                this.lemma = (String)val;
                break;
            case "pos":
                this.pos = (String)val;
                break;
            case "word":
                this.word = (String)val;
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
        out.writeObject(this.lemma);
        out.writeObject(this.pos);
        out.writeObject(this.word);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.lemma = (String)in.readObject();
        this.pos = (String)in.readObject();
        this.word = (String)in.readObject();
    }

}
