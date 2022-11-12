package com.yatenesturno.activities.tutorial_screen;

public class ScreenImpl implements Screen {

    private int contentId;

    public ScreenImpl(int contentId) {
        this.contentId = contentId;
    }

    @Override
    public int getContentId() {
        return contentId;
    }

    @Override
    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

}
