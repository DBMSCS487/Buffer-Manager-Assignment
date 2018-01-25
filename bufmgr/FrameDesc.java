package bufmgr;


import global.Page;

import java.util.HashMap;

/**
 * Created by brandonbauley on 1/23/18.
 */
public class FrameDesc{

    private boolean dirty; // is the page dirty
    private boolean valid; // does it include valid data
    private int diskPageNumber; // if the data is valid, there is should be a number
    private int pinCount;
    Page aPage;


    // variable for the clock algorithm to give the frame a "second chance"
    boolean refbit;


    /** Default constructor */
    public FrameDesc(){

        aPage = new Page();
        dirty = false;
        valid = false;
        diskPageNumber = -1;
        pinCount = -1;
        refbit = true;
    }

    /** Gives back the boolean value of if the frame contains data */
    public boolean getValid() {

        return valid;
    }

    /** Gives back the pincount of the frame */
    public int getPinCount() {

        return pinCount;
    }

    public boolean getRefbit() {

        return refbit;
    }

    public void setRefbit(boolean refbit) {

        this.refbit = refbit;
    }


}
