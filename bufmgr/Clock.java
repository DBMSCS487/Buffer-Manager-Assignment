package bufmgr;

/**
 * Created by brandonbauley on 1/24/18.
 */
public class Clock {

    //Keeps track of the current frame the clock algorithm is on
    private int current;

    public Clock() {

        current = 0;
    }

    /** Returns an index for which frame needs to be replaced in the buffer pool
     *  Failure if returned value is -1
     */
    public int pickVictim(FrameDesc [] bufferPool) {

        int size = bufferPool.length;
        for(int counter = 0; counter <  size * 2; ++counter) {

            if(bufferPool[current].getValid() == false)
                return counter;

            if(bufferPool[counter].getPinCount() == 0) {

                if(bufferPool[current].getRefbit() == true)
                    bufferPool[current].setRefbit(false);
                else
                    return current;
            }
            //Increment current but make sure to keep it in the scope of the array size
            current = (current + 1) % size;
        }

        return -1;
    }
}
