package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.util.HashMap;
import java.util.Map;


/**
 * <h3>Minibase Buffer Manager</h3>
 * The buffer manager manages an array of main memory pages.  The array is
 * called the buffer pool, each page is called a frame.  
 * It provides the following services:
 * <ol>
 * <li>Pinning and unpinning disk pages to/from frames
 * <li>Allocating and deallocating runs of disk pages and coordinating this with
 * the buffer pool
 * <li>Flushing pages from the buffer pool
 * <li>Getting relevant data
 * </ol>
 * The buffer manager is used by access methods, heap files, and
 * relational operators.
 */
public class BufMgr implements GlobalConst {

    private FrameDesc [] frametab;
    private int numframes;
    private Clock replPolicy;
    protected HashMap<PageId, FrameDesc> bufmap;

  /**
   * Constructs a buffer manager by initializing member data.  
   * 
   * @param numframes number of frames in the buffer pool
   */
  public BufMgr(int numframes) {

    try {

      if(numframes < 0) {

        throw new IndexOutOfBoundsException();

      }

    }
    catch(IndexOutOfBoundsException e) {

        System.err.println("Function: BufMgr constructor\n catch: IndexOutOfBoundsException\n " + e.getMessage() + "\n");

    }

    frametab = new FrameDesc[numframes];

    for(int i = 0; i < numframes; ++i) {

        frametab[i] = new FrameDesc();
        //frametab[i] = null;

    }

    this.numframes = numframes;
    this.replPolicy = new Clock();
    this.bufmap = new HashMap<>();

  } // public BufMgr(int numframes)

  /**
   * The result of this call is that disk page number pageno should reside in
   * a frame in the buffer pool and have an additional pin assigned to it, 
   * and mempage should refer to the contents of that frame. <br><br>
   * 
   * If disk page pageno is already in the buffer pool, this simply increments 
   * the pin count.  Otherwise, this<br> 
   * <pre>
   * 	uses the replacement policy to select a frame to replace
   * 	writes the frame's contents to disk if valid and dirty
   * 	if (contents == PIN_DISKIO)
   * 		read disk page pageno into chosen frame
   * 	else (contents == PIN_MEMCPY)
   * 		copy mempage into chosen frame
   * 	[omitted from the above is maintenance of the frame table and hash map]
   * </pre>		
   * @param pageno identifies the page to pin
   * @param mempage An output parameter referring to the chosen frame.  If
   * contents==PIN_MEMCPY it is also an input parameter which is copied into
   * the chosen frame, see the contents parameter. 
   * @param contents Describes how the contents of the frame are determined.<br>  
   * If PIN_DISKIO, read the page from disk into the frame.<br>  
   * If PIN_MEMCPY, copy mempage into the frame.<br>  
   * If PIN_NOOP, copy nothing into the frame - the frame contents are irrelevant.<br>
   * Note: In the cases of PIN_MEMCPY and PIN_NOOP, disk I/O is avoided.
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned.
   * @throws IllegalStateException if all pages are pinned (i.e. pool is full)
   */
  public void pinPage(PageId pageno, Page mempage, int contents) {

      try {
          if (bufmap.containsKey(pageno)) {
              FrameDesc temp = bufmap.get(pageno);
              temp.incPinCount();
              return;
          }

          int frameno = replPolicy.pickVictim(frametab);

          if (frameno == -1)
              System.err.print("Frame Number does not exist");
          else{
              for (Map.Entry<PageId, FrameDesc> entry : bufmap.entrySet()) {
                  if (entry.getValue() == frametab[frameno]){
                      flushPage(entry.getKey());
                  }
              }
          }

          if(bufmap.size() > 0 && getNumUnpinned() == 0)
              throw new IllegalStateException();

          // put into hash map
          // put it in the array
          // increment pin count
          // set dirty to false
          // return address of frame
          if (contents == PIN_DISKIO) {
              Minibase.DiskManager.read_page(pageno, mempage);
              frametab[frameno].copyPage(mempage);
              frametab[frameno].setDirty(false);
              frametab[frameno].setDiskPageNumber(pageno.pid);
              bufmap.put(pageno, frametab[frameno]);

          } else if (contents == PIN_MEMCPY) {
              if(frametab[frameno].getPinCount() > 0)
                  throw new IllegalArgumentException();
              frametab[frameno].setDiskPageNumber(pageno.pid);
              frametab[frameno].copyPage(mempage);
              frametab[frameno].setValid(true);
              bufmap.put(pageno, frametab[frameno]);
          }
      }
      catch(IllegalArgumentException e) {
          // System.err.println("Function: pinPage\n catch: IllegalArgumentException\n " + e.getMessage() + "\n");
      }
      catch(IllegalStateException e) {
          // System.err.println("Function: pinPage\n catch: IllegalStateException\n " + e.getMessage() + "\n");
      }

  } // public void pinPage(PageId pageno, Page page, int contents)
  
  /**
   * Unpins a disk page from the buffer pool, decreasing its pin count.
   * 
   * @param pageno identifies the page to unpin
   * @param dirty UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherwise
   * @throws IllegalArgumentException if the page is not in the buffer pool
   *  or not pinned
   */
  public void unpinPage(PageId pageno, boolean dirty) {

      try {

          if(!bufmap.containsKey(pageno))
              throw new IllegalArgumentException();

          FrameDesc frame = bufmap.get(pageno);

          if(frame.getPinCount() == 0)
              throw new IllegalArgumentException();


          frame.setDirty(frame.getDirty() || dirty);
          frame.decPinCount();

      }
      catch(IllegalArgumentException e) {

          // System.err.println("Function: unpinPage\n catch: IllegalArgumentException\n " + e.getMessage() + "\n");

      }
      return;
  } // public void unpinPage(PageId pageno, boolean dirty)


  /**
   * Allocates a run of new disk pages and pins the first one in the buffer pool.
   * The pin will be made using PIN_MEMCPY.  Watch out for disk page leaks.
   * 
   * @param firstpg input and output: holds the contents of the first allocated page
   * and refers to the frame where it resides
   * @param run_size input: number of pages to allocate
   * @return page id of the first allocated page
   * @throws IllegalArgumentException if firstpg is already pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */
  public PageId newPage(Page firstpg, int run_size) {

      try {

          PageId tempPageID = Minibase.DiskManager.allocate_page(run_size);
          if(!bufmap.containsKey(tempPageID))
              throw new IllegalArgumentException();
          else {
              if(getNumUnpinned() == 0)
                  throw new IllegalStateException();
              else {
                  pinPage(tempPageID, firstpg, PIN_MEMCPY);
                  return tempPageID;
              }
          }
      }
      catch(IllegalArgumentException e) {

          System.err.println("Function: newPage\n catch: IllegalArgumentException\n " + e.getMessage() + "\n");
      }
      catch(IllegalStateException e) {

          System.err.println("Function: newPage\n catch: IllegalStateException\n " + e.getMessage() + "\n");

      }

      return null;
  } // public PageId newPage(Page firstpg, int run_size)

  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   * 
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) {
      try {

          if(!bufmap.containsKey(pageno))
              return;

          FrameDesc temp = bufmap.get(pageno);
          for (int z = 0; z < numframes; ++z) {
              if (frametab[z] == temp) {
                  if (frametab[z].getPinCount() > 0) {
                      throw new IllegalArgumentException();
                  }
                  bufmap.remove(pageno);
                  Minibase.DiskManager.deallocate_page(pageno);
                  frametab[z].resetFrame();
              }
          }
      }
      catch(IllegalArgumentException e){

          System.err.println("Function: freePage\n catch: IllegalArgumentException\n " + e.getMessage() + "\n");

      }
      return;

  } // public void freePage(PageId firstid)

  /**
   * Write all valid and dirty frames to disk.
   * Note flushing involves only writing, not unpinning or freeing
   * or the like.
   * 
   */
  public void flushAllFrames() {

      for (Map.Entry<PageId, FrameDesc> entry : bufmap.entrySet()) {

          if (entry.getValue().getDirty() == true && entry.getValue().getValid() == true){

              flushPage(entry.getKey());
          }

      }
      return;

  } // public void flushAllFrames()

  /**
   * Write a page in the buffer pool to disk, if dirty.
   * 
   * @throws IllegalArgumentException if the page is not in the buffer pool
   */
  public void flushPage(PageId pageno) {

      try {

          if(!bufmap.containsKey(pageno))
              throw new IllegalArgumentException();

          FrameDesc temp = bufmap.get(pageno);

          if(temp.getDirty() == true && temp.getValid() == true) {
              Minibase.DiskManager.write_page(pageno, temp.getaPage());
          }
      }
      catch(IllegalArgumentException e) {

         // System.err.println("Function: flushPage\n catch: IllegalArgumentException\n " + e.getMessage() + "\n");
      }

      return;
  }

   /**
   * Gets the total number of buffer frames.
   */
  public int getNumFrames() {

    return this.numframes;
  }

  /**
   * Gets the total number of unpinned buffer frames.
   */
  public int getNumUnpinned() {


      int count = 0;
      for (Map.Entry<PageId, FrameDesc> entry : bufmap.entrySet()) {

          if (entry.getValue().getPinCount() == 0)
              ++count;
      }
      return count;
  }

} // public class BufMgr implements GlobalConst
