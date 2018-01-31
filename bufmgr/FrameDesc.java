package bufmgr;


import global.Page;


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
        pinCount = 0;
        refbit = true;
    }

    public FrameDesc(Page aPage) {

        this.aPage = new Page();
        this.aPage.copyPage(aPage);
        dirty = false;
        valid = false;
        diskPageNumber = -1;
        pinCount = 0;// might have to change this later
        refbit = true;


    }

    public void copyPage(Page to_copy){
        
        this.aPage.copyPage(to_copy);
        return;
    }
    /** Gives back the boolean value of if the frame contains data */
    public boolean getValid() {

        return valid;
    }

    /** Gives back the pincount of the frame */
    public int getPinCount() {

        return pinCount;
    }

    public void incPinCount() {

        ++pinCount;
    }

    public void decPinCount() {

        --pinCount;
    }

    public boolean getRefbit() {

        return refbit;
    }

    public void setRefbit(boolean refbit) {

        this.refbit = refbit;
        return;
    }

    public boolean getDirty() {

        return dirty;
    }

    public void setDiskPageNumber(int diskPageNumber) {

        this.diskPageNumber = diskPageNumber;
        return;
    }

    public void setDirty(boolean dirty) {

        this.dirty = dirty;
        return;
    }

    public boolean comparePage(Page toCheck) {

        if (aPage.getData() == toCheck.getData())
            return true;
        else
            return false;
    }

    public Page getaPage() {

        return this.aPage;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public void resetFrame() {

        aPage = null;
        aPage = new Page();
        dirty = false;
        valid = false;
        diskPageNumber = -1;
        pinCount = -1;
        refbit = true;
        return;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }
}
