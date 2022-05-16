package ru.gb.storage.commons.message;

public class AnswerActionFileMessage extends Message {
    String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
