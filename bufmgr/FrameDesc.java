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

    //TODO add more data items for clock algorithm

    //Default constructor
    public FrameDesc(){

        aPage = null;
        dirty = false;
        valid = false;
        diskPageNumber = -1;
        pinCount = -1;
    }




}
