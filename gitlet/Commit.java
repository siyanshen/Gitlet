package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;


import java.util.TreeMap;


/**
 * consist of a log message, timestamp, a mapping of  le names to blob
 * references, a parent reference, and (for merges) a second parent reference.
 *
 * @author Siyan Shen
 */
public class Commit implements Serializable {

    /** My id. */
    private String myID;

    /** My message. */
    private String message;

    /** My git. */
    private Gitlet myGit;

    /** My date. */
    private Date d;

    /** My date as a string. */
    private String dateFormatted;

    /** My parent. */
    private String parent;

    /** im a conflict. */
    private boolean conflict;

    /** my second parent. */
    private String parent2;
    /**
     * key: blob name. val: blob id
     */
    private TreeMap<String, String> myBlobs;

    /** construct ad new commit with GIT, PARENTID, MSG, INITIAL, CONF. */
    public Commit(Gitlet git, String parentId, String msg, boolean initial,
                  boolean conf) throws IOException {
        if (!initial && (git.getRmFiles().isEmpty()
                && git.getStagedFiles().keySet().isEmpty())) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        myGit = git;
        parent = parentId;
        message = msg;
        conflict = conf;
        SimpleDateFormat A = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        if (!initial) {
            d = new Date();
            dateFormatted = A.format(d);
        } else {
            d = new Date(0);
            dateFormatted = A.format(d);
        }
        myBlobs = new TreeMap<>();
        if (parentId == null) {
            return;
        } else {
            Commit myparent = Utils.readObject(new File(String.format("%s%s%s",
                    ".gitlet/objects/", parent, ".ser")), Commit.class);
            TreeMap<String, String> parentblobs = Utils
                    .copytreemap(myparent.myBlobs);
            for (String filenames : parentblobs.keySet()) {
                if (!myGit.getRmFiles().contains(filenames)) {
                    if (!myGit.getStagedFiles().containsKey(filenames)) {
                        myBlobs.put(filenames, myparent.myBlobs.get(filenames));
                    } else {
                        String blobidstring =
                                myGit.getStagedFiles().get(filenames);
                        String blobid = Utils.sha1(blobidstring);
                        File newblob = new File(String.format("%s%s%s",
                                ".gitlet/blobs/", blobid, ".ser"));
                        newblob.createNewFile();
                        Utils.writeContents(newblob, blobidstring);
                        myBlobs.put(filenames, blobid);
                        myGit.getStagedFiles().keySet().remove(filenames);
                    }
                }
            }
            TreeMap<String, String> stagedfiles1 =
                    Utils.copytreemap(myGit.getStagedFiles());
            for (String stagedfiles : stagedfiles1.keySet()) {
                String newblobid = Utils.sha1(myGit.getStagedFiles()
                        .get(stagedfiles));
                myBlobs.put(stagedfiles, newblobid);
                File newblob = new File(".gitlet/blobs/"
                        + newblobid + ".ser");
                newblob.createNewFile();
                Utils.writeContents(newblob,
                        myGit.getStagedFiles().get(stagedfiles));
                myGit.getStagedFiles().keySet().remove(stagedfiles);
            }
            myGit.getRmFiles().clear();
        }

    }


    /**
     * Returns the ID of parent commit.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Returns the ID of parent2 commit.
     */
    public String getParent2() {
        return parent2;
    }

    /**
     * Set the second parent as P2.
     */
    protected void setParent2(String p2) {
        parent2 = p2;
    }

    /**
     * Returns message of this commit.
     */
    public String getMsg() {
        return message;
    }

    /**
     * Set SHA1 id.
     */
    public void setID() {
        myID = Utils.sha1(Utils.serialize(this));
    }

    /**
     * Returns SHA1 value of this commit.
     */
    public String getID() {
        return this.myID;
    }

    /**
     * Return a blobid with given name FILENAME.
     */
    public String getBlobid(String filename) {
        return myBlobs.get(filename);
    }

    /**
     * Return whether the commit has a second parent or not.
     */
    public boolean isConflict() {
        return conflict;
    }

    /**
     * Returns my blobs.
     */
    public TreeMap<String, String> getMyBlobs() {
        return myBlobs;
    }

    /** return BLOBID content as string. */
    public String blobcontent(String blobid) {
        File blobdir = new File(".gitlet/blobs/"
                + blobid + ".ser");
        return Utils.readContentsAsString(blobdir);
    }


    /**
     * Prints info of THIS commit as is needed for log function.
     */
    public void printInfo() {
        System.out.println("===");
        System.out.println("commit " + getID());
        if (conflict) {
            System.out.println("Merge: " + parent.substring(0, 7) + " "
                    + parent2.substring(0, 7));
        }
        System.out.println("Date: " + this.dateFormatted);
        System.out.println(message + "\n");
    }

}

