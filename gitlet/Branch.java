package gitlet;

import java.io.File;
import java.io.Serializable;


/**
 * Branch.
 *
 * @author Siyan Shen
 */
public class Branch implements Serializable {


    /**
     * My name.
     */
    private String name;

    /**
     * the id of the commit that I am pointing to.
     */
    private String head;

    /**
     * Constructor for Branch.
     * @param bhead branch head
     * @param bname branch name
     */
    public Branch(String bname, String bhead) {
        head = bhead;
        this.name = bname;

    }

    /**
     * my name.
     * @return my name
     */
    public String getName() {
        return name;
    }

    /**
     * get the real commit.
     * @return real commit
     */
    public Commit getCommit() {
        File mycommit = new File(".gitlet/objects/" + head + ".ser");
        Commit myrealcommit = Utils.readObject(mycommit, Commit.class);
        return myrealcommit;
    }


    /**
     * Sets the head for THIS.
     * @param id set head as id
     */
    public void setHead(String id) {
        head = id;
    }

    /**
     * get the head of THIS.
     * @return my head.
     */
    public String getHead() {
        return head;
    }

}


