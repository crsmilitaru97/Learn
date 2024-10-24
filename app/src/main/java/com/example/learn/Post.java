package com.example.learn;

public class Post implements Comparable<Post> {

    String userUid;
    String date;
    String text;
    String image;
    boolean hasImage;
    boolean hasComments;
    double score;

    @Override
    public int compareTo(Post o) {
        return this.date.compareTo(o.getDate());
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getHasComments() {
        return hasComments;
    }

    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}
