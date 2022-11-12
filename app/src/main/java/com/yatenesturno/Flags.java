package com.yatenesturno;

public interface Flags {

    boolean DEBUG = false;
    boolean LOCAL = false;

    boolean SANDBOX = false;
    boolean LAN = false;

    boolean SAVE_IMAGES = !DEBUG && !SANDBOX;
}
