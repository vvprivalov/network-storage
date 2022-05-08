package ru.gb.storage.commons.message;

public class SignAnswer extends Message{
    private boolean bAnswer = false;

    public boolean isbAnswer() {

        return bAnswer;
    }

    public void setbAnswer(boolean bAnswer) {

        this.bAnswer = bAnswer;
    }
}
