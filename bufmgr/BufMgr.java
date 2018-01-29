package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

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

      System.out.println(e.getMessage());

    }

    frametab = new FrameDesc[numframes];

    for(int i = 0; i < numframes; ++i) {

      frametab[i] = null;

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

      int frameno = replPolicy.pickVictim(frametab);

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

          FrameDesc frame = bufmap.get(pageno);
          if(frame == null)
              throw new IllegalArgumentException();

          frame.setDirty(frame.getDirty() || dirty);

      }
      catch(IllegalArgumentException e) {

          System.err.print(e.getMessage());
      }

  } // public void unpinPage(PageId pageno, boolean dirty)



    public boolean checkPool(Page toCheck) {

      for(int i = 0; i < numframes; ++i) {

          if(frametab[i] != null && frametab[i].comparePage(toCheck))
              return true;

      }
      return false;
    }
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
          if (checkPool(firstpg))
              throw new IllegalArgumentException();

          PageId tempPageID = Minibase.DiskManager.allocate_page(run_size);
          int i = 0;
          boolean flag = true;
          while (i < numframes && flag) {

              if (frametab[i] == null) {
                  frametab[i] = new FrameDesc(firstpg);
                  frametab[i].setDiskPageNumber(tempPageID.hashCode());
                  pinPage(tempPageID, firstpg, PIN_MEMCPY);
                  bufmap.put(tempPageID, frametab[i]);
                  flag = false;
              }
              ++i;
          }

          if (i == numframes)
              throw new IllegalStateException();
          else
              return tempPageID;
      }
      catch(IllegalArgumentException e) {

          System.err.println(e.getMessage());
      }
      catch(IllegalStateException e) {

          System.err.println(e.getMessage());

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


     FrameDesc temp = bufmap.get(pageno);

    //Find the pageno
      //call remove function for diskmanager
      //remove from hash map

  } // public void freePage(PageId firstid)

  /**
   * Write all valid and dirty frames to disk.
   * Note flushing involves only writing, not unpinning or freeing
   * or the like.
   * 
   */
  public void flushAllFrames() {


      for(int i = 0; i < numframes; ++i)
      {
          //if(frametab[i] frametab[i].getDirty() == true && frametab[i].getValid() == true) {


          //}

      }

  } // public void flushAllFrames()

  /**
   * Write a page in the buffer pool to disk, if dirty.
   * 
   * @throws IllegalArgumentException if the page is not in the buffer pool
   */
  public void flushPage(PageId pageno) {
	  


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

    return 1;
  }

} // public class BufMgr implements GlobalConst
