package gitlet;

import java.io.File;

import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashSet;

import java.io.Serializable;

/** hahaha.
 * @author Siyan Shen
 */
public class Gitlet implements Serializable {
    /** magic number. */
    static final int M = 40;

    /** working directory. */
    static final File W = new File(".");

    /** my head commit id. */
    private String head;

    /**
     * key: name of the files. val: contents of files.
     */
    private TreeMap<String, String> stagedFiles;

    /** files to be removed.*/
    private HashSet<String> rmFiles;
    /**
     * name of current branch.
     */
    private Branch currentBranch;
    /**
     * name of the branch val: Branch id.
     */
    private HashSet<Branch> branches;


    /**
     * Constructor that initializes all variables.
     */
    public Gitlet() {
        head = null;
        currentBranch = null;
        branches = null;

        stagedFiles = new TreeMap<>();
        rmFiles = new HashSet<>();

    }

    /**
     * Saves the current state of Gitlet G.
     */
    public static void save(Gitlet g) throws IOException {
        File savedGitlet = new File(".gitlet/history.ser");
        if (!savedGitlet.exists()) {
            savedGitlet.createNewFile();
            Utils.writeContents(savedGitlet, Utils.serialize(g));
        } else {
            Utils.writeContents(savedGitlet, Utils.serialize(g));
        }

    }


    /**
     * Creates .gitlet directory if it doesn't already exist.
     */
    public void init() throws IOException {
        File dir = new File(".gitlet");
        if (!dir.exists()) {
            dir.mkdir();
            Commit initial = new Commit(this, null,
                    "initial commit", true, false);
            initial.setID();
            new File(".gitlet/objects").mkdir();
            head = initial.getID();
            File newcommit = new File(".gitlet/objects/"
                    + head + ".ser");
            newcommit.createNewFile();
            Utils.writeObject(newcommit, initial);
            Branch master = new Branch("master", initial.getID());
            new File(".gitlet/index").mkdir();
            File newbranch = new File(".gitlet/index/master.ser");
            newbranch.createNewFile();
            Utils.writeObject(newbranch, master);
            new File(".gitlet/blobs").mkdir();
            branches = new HashSet<>();
            branches.add(master);
            currentBranch = master;
        } else {
            System.out.println("A gitlet version control system already exists "
                    + "in the current directory.");
        }
    }

    /**
     * Add a file FILENAME to the staging area. If the current working version
     * of the file is identical to the version in the current commit,
     * do not stage it to be added, and remove it from the staging area
     * if it is already there (as can happen when a file is changed,
     * added, and then changed back). If the file had been marked to
     * be removed, delete that mark.
     */
    public void addFile(String filename) {
        if (!(new File(filename).exists())) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String key = filename;
        String val = Utils.readContentsAsString(new File(filename));
        if (rmFiles.contains(filename)) {
            rmFiles.remove(filename);
        }
        File headcommit = new File(String.format("%s%s%s", ".gitlet/objects/",
                head, ".ser"));
        Commit realhead = Utils.readObject(headcommit, Commit.class);

        if (realhead.getMyBlobs().keySet().contains(filename)) {
            File file = new File(String.format("%s%s%s", ".gitlet/blobs/",
                    realhead.getMyBlobs().get(filename), ".ser"));
            if (val.equals(Utils.readContentsAsString(file))) {
                if (stagedFiles.containsKey(filename)) {
                    stagedFiles.keySet().remove(filename);
                }
                return;
            }
        }
        stagedFiles.put(key, val);
    }

    /**
     * Commit with message MSG, CONFLICT, P2.
     */
    public void commitCmnd(String msg, boolean conflict, String p2)
            throws IOException {
        Commit newhead = new Commit(this, head, msg, false, conflict);
        newhead.setID();
        if (conflict) {
            newhead.setParent2(p2);
        }
        head = newhead.getID();

        File object = new File(".gitlet/objects/" + head + ".ser");

        object.createNewFile();

        Utils.writeObject(object, newhead);

        currentBranch.setHead(head);
        File branch = new File(String.format("%s%s%s", ".gitlet/index/",
                currentBranch.getName(), ".ser"));
        Utils.writeObject(branch, currentBranch);
    }

    /**
     * rm command.
     * Unstage the file FILENAME if it is currently staged. If the file is
     * tracked in the current commit, mark it to indicate that it is not to
     * be included in the next commit, and remove the file from the
     * working directory if the user has not already done so (do not remove
     * it unless it is tracked in the current commit).
     */
    public void removeFile(String filename) {
        File headcommit = new File(String.format("%s%s%s", ".gitlet/objects/",
                head, ".ser"));
        Commit realhead = Utils.readObject(headcommit, Commit.class);
        if (!stagedFiles.keySet().contains(filename)
                && !realhead.getMyBlobs().keySet().contains(filename)) {
            System.out.println(" No reason to remove the file.");
            System.exit(0);
        }
        if (stagedFiles.keySet().contains(filename)) {
            stagedFiles.keySet().remove(filename);
        }
        if (realhead.getMyBlobs().containsKey(filename)) {
            rmFiles.add(filename);
            File unwantedfile = new File(filename);
            if (unwantedfile.exists()) {
                unwantedfile.delete();
            }
        }

    }

    /**
     * log command.
     * Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents
     * found in merge commits.
     * For merge commits (those that have two parent commits), add a line
     * just below the first.
     */
    public void log() {
        File headcommit = new File(String.format("%s%s%s", ".gitlet/objects/",
                head, ".ser"));
        Commit realhead = Utils.readObject(headcommit, Commit.class);
        Commit p = realhead;
        while (p != null) {
            p.printInfo();
            if (p.getParent() != null) {
                File parentcommit = new File(String.format("%s%s%s",
                        ".gitlet/objects/", p.getParent(), ".ser"));
                Commit realparent = Utils.readObject(parentcommit,
                        Commit.class);
                p = realparent;
            } else {
                p = null;
            }
        }
    }

    /**
     * global-log command.
     * displays information about all commits ever made. The order of the
     * commits does not matter.
     */
    public void globalLog() {
        File dir = new File(".gitlet/objects");
        File[] directoryListing = dir.listFiles();
        for (File child : directoryListing) {
            Commit realchild = Utils.readObject(child, Commit.class);
            realchild.printInfo();
        }

    }

    /**
     * find command.
     * Prints out the ids of all commits that have the given commit
     * message MSG, one per line.
     */
    public void find(String msg) {
        File dir = new File(".gitlet/objects");
        File[] directoryListing = dir.listFiles();
        boolean exist = false;
        for (File child : directoryListing) {
            Commit realchild = Utils.readObject(child, Commit.class);
            if (realchild.getMsg().equals(msg)) {
                System.out.println(realchild.getID());
                exist = true;
            }
        }
        if (!exist) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * status command.
     * Displays what branches currently exist, and marks the current branch
     * with a *. Also displays what files have been staged or marked for
     * untracking.
     */
    public void status() {
        System.out.println("=== Branches ===");
        File dir = new File(".gitlet/index");
        File[] directoryListing = dir.listFiles();
        HashSet<String> workingname = new HashSet<>();
        for (File file : directoryListing) {
            workingname.add(file.getName().substring(0,
                    file.getName().length() - 4));
        }
        String[] b = Utils.sorthashset(workingname);
        for (String child : b) {
            if (child.equals(currentBranch.getName())) {
                System.out.print("*");
            }
            System.out.println(child);
        }

        System.out.println("\n=== Staged Files ===");
        String[] stage = Utils.sortset(stagedFiles.keySet());
        for (String filename : stage) {
            System.out.println(filename);
        }

        System.out.println("\n=== Removed Files ===");
        String[] r = Utils.sorthashset(rmFiles);
        for (String filename : r) {
            System.out.println(filename);
        }
        System.out.println("");

        System.out.println("=== Modifications Not Staged For Commit ===\n");
        System.out.println("=== Untracked Files ===\n");
    }


    /**
     * check out file with FILENAME.
     * Takes the version of the file as it exists in the head commit, the
     * front of the current branch, and puts it in the working directory,
     * overwriting the version of the file that's already there if there is
     * one. The new version of the file is not staged.
     * <p>
     * Failure cases:
     * If the file does not exist in the previous commit, aborts, printing
     * the error message File does not exist in that commit.
     **/
    public void checkoutfile(String filename) throws IOException {

        File headcommit = new File(".gitlet/objects/" + head + ".ser");
        Commit realhead = Utils.readObject(headcommit, Commit.class);

        if (!realhead.getMyBlobs().keySet().contains(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobid = realhead.getMyBlobs().get(filename);
        File newfile = new File(".gitlet/blobs/" + blobid + ".ser");
        String newcontent = Utils.readContentsAsString(newfile);

        File oldfile = new File(filename);
        if (oldfile.exists()) {
            Utils.writeContents(oldfile, newcontent);

        } else {

            oldfile.createNewFile();
            Utils.writeContents(oldfile, newcontent);
        }

    }

    /**
     * check out file in a given commit.
     * Takes the version of the file FILENAME
     * as it exists in the commit with the
     * given id COMMITID1, and puts it in the working directory,
     * overwriting the
     * version of the file that's already there if there is one.
     * The new version of the file is not staged.
     * <p>
     * Failure cases:
     * If no commit with the given id exists, print
     * No commit with that id exists.
     * Else, if the file does not exist in the given commit, print
     * File does not exist in that commit.
     */
    public void checkoutcommit(String commitid1, String filename)
            throws IOException {

        String commitid = null;
        File dir = new File(".gitlet/objects");


        File[] files = dir.listFiles();
        boolean exist = false;
        for (File file : files) {

            String shortid = file.getName().
                    substring(0, commitid1.length());

            if (shortid.equals(commitid1)) {

                commitid = file.getName().substring(0, M);
                exist = true;
                break;
            }

        }

        if (!exist) {
            System.out.println("No commit with that id exists.");
            return;
        }

        File targetcommit = new File(".gitlet/objects/"
                + commitid + ".ser");
        Commit realtarget = Utils.readObject(targetcommit, Commit.class);

        if (!realtarget.getMyBlobs().keySet().contains(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobid = realtarget.getMyBlobs().get(filename);
        File newfile = new File(".gitlet/blobs/" + blobid + ".ser");
        String newcontent = Utils.readContentsAsString(newfile);
        File oldfile = new File(filename);
        if (oldfile.exists()) {
            Utils.writeContents(oldfile, newcontent);

        } else {
            oldfile.createNewFile();
            Utils.writeContents(oldfile, newcontent);
        }
    }


    /**
     * check out branch with BRANCHNAME.
     * Takes all files in the commit at the head of the given branch, and puts
     * them in the working directory, overwriting the versions of the files that
     * are already there if they exist. Also, at the end of this command, the
     * given branch will now be considered the current branch (HEAD). Any files
     * that are tracked in the current branch but are not present in the
     * checked-out branch are deleted. The staging area is cleared, unless the
     * checked-out branch is the current branch (see Failure cases below).
     * <p>
     * Failure cases:
     * 1, If no branch with that name exists, print No such branch exists.
     * 2, If that branch is the current branch, print No need to checkout
     * the current branch.
     * 3, If a working file is untracked in the current branch and would
     * be overwritten by the checkout, print There is an untracked file
     * in the way; delete it or add it first. and exit;
     * perform this check before doing anything else.
     */
    public void checkoutbranch(String branchname) throws IOException {
        if (currentBranch.getName().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        boolean exist = false;
        Branch targetbranch = new Branch(null, null);
        for (Branch branch : branches) {
            if (branch.getName().equals(branchname)) {
                exist = true;
                targetbranch = branch;
                break;
            }
        }
        if (!exist) {
            System.out.println("No such branch exists.");
            return;
        }
        Commit targetcommit = targetbranch.getCommit();
        Commit currentcommit = currentBranch.getCommit();
        File dir = new File(".");
        File[] directoryListing = dir.listFiles();
        for (File child : directoryListing) {
            String filename = child.getName();
            if (!currentcommit.getMyBlobs().keySet().contains(filename)
                    && !filename.equals(".gitlet")
                    && !rmFiles.contains(filename)
                    && !stagedFiles.containsKey(filename)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it or add it first.");
                return;
            }
        }
        stagedFiles.clear();
        rmFiles.clear();
        for (String child : targetcommit.getMyBlobs().keySet()) {
            checkoutcommit(targetcommit.getID(), child);
        }

        for (File child : directoryListing) {
            if (!targetcommit.getMyBlobs().keySet().
                    contains(child.getName())
                    && !child.getName().equals(".gitlet")) {
                child.delete();
            }
        }

        currentBranch = targetbranch;
        head = currentBranch.getHead();
    }


    /**
     * branch.
     * Creates a new branch with BRANCHNAME, and points it at the current
     * head node. A branch is nothing more than a name for a reference (a
     * SHA-1 identifier) to a commit node. This command does NOT immediately
     * switch to the newly created branch (just as in real Git). Before you ever
     * call branch, your code should be running with a default branch called
     * "master".
     */
    public void branch(String branchname) throws IOException {
        for (Branch branch : branches) {
            if (branch.getName().equals(branchname)) {
                System.out.println("A branch with that name already exists");
                System.exit(0);
            }
        }
        Branch newbranch = new Branch(branchname, head);
        branches.add(newbranch);
        new File(".gitlet/index").mkdir();
        File newbranchfile = new File(".gitlet/index/" + branchname + ".ser");
        newbranchfile.createNewFile();
        Utils.writeObject(newbranchfile, newbranch);
    }


    /**
     * rm-branch.
     * Deletes the branch with the given name RMDBRANCH.
     * If a branch with the given name does not exist, aborts. Print the
     * error message A branch with that name does not exist. If you try
     * to remove the branch you're currently on, aborts, printing the error
     * message Cannot remove the current branch.
     */
    public void rmbranch(String rmdbranch) {
        if (currentBranch.getName().equals(rmdbranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        boolean exist = false;
        for (Branch branch : branches) {
            if (branch.getName().equals(rmdbranch)) {
                branches.remove(branch);
                exist = true;
                break;
            }
        }
        if (!exist) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /**
     * reset.
     * Checks out all the files tracked by the given commit COMMITID1.
     * Removes tracked
     * files that are not present in that commit. Also moves the current
     * branch's head to that commit node. See the intro for an example of
     * what happens to the head pointer after using reset. The [commit id]
     * may be abbreviated as for checkout. The staging area is cleared.
     * The command is essentially checkout of an arbitrary commit that also
     * changes the current branch head.
     * Failure case:
     * If no commit with the given id exists, print No commit with that id
     * exists.
     * If a working file is untracked in the current branch and would
     * be overwritten by the reset, print There is an untracked file in the
     * way; delete it or add it first. and exit; perform this check before
     * doing anything else.
     */
    public void reset(String commitid1) throws IOException {
        String commitid = null;
        File dir = new File(".gitlet/objects");
        File[] files = dir.listFiles();
        boolean exist = false;
        for (File file : files) {
            if (file.getName().substring(0, commitid1.length())
                    .equals(commitid1)) {
                commitid = file.getName().substring(0, M);
                exist = true;
                break;
            }

        }
        if (!exist) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File targetcommit = new File(".gitlet/objects/"
                + commitid + ".ser");
        Commit realtarget = Utils.readObject(targetcommit, Commit.class);
        Commit currentcommit = currentBranch.getCommit();

        File dir1 = new File(".");
        File[] directoryListing = dir1.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String filename = child.getName();
                if (!currentcommit.getMyBlobs().keySet().contains(filename)
                        && !filename.equals(".gitlet")
                        && !stagedFiles.containsKey(filename)
                        && !rmFiles.contains(filename)) {

                    System.out.println("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    return;
                }
            }
        }
        stagedFiles.clear();
        rmFiles.clear();
        for (String blobs : realtarget.getMyBlobs().keySet()) {
            checkoutcommit(commitid1, blobs);
        }
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (!realtarget.getMyBlobs().keySet()
                        .contains(file.getName())
                        && !file.getName().equals(".gitlet")) {
                    file.delete();
                }
            }
        }
        currentBranch.setHead(commitid);
        head = commitid;
    }


    /** MERGEBRANCH. */
    public void premerge1(String mergebranch) throws IOException {
        File[] directoryListing = W.listFiles();
        for (File child : directoryListing) {
            if (untracked(child.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
        }
    }
    /** ME, YOU, GCD, return conflict. */
    public boolean mergepart1(Commit me, Commit you, Commit gcd)
            throws IOException {
        boolean result1 = false;
        for (String file : you.getMyBlobs().keySet()) {
            if (!same(file, you, gcd) && same(file, me, gcd)) {
                checkoutcommit(you.getID(), file);
                stagedFiles.put(file, you.getMyBlobs().get(file));
                continue;
            }
            if ((same(file, you, gcd) && !same(file, me, gcd))
                    || same(file, you, me)) {
                continue;
            }
            if (!same(file, you, gcd) && !exist(file, me)) {
                result1 = true;
                File f2 = new File(".gitlet/blobs/"
                        + you.getBlobid(file) + ".ser");
                String v1 = "";
                String v2 = Utils.readContentsAsString(f2);
                String result = "<<<<<<< HEAD\n" + v1
                        + "=======\n" + v2 + ">>>>>>>\n";
                File working = new File(file);
                if (working.exists()) {
                    Utils.writeContents(working, result);
                } else {
                    working.createNewFile();
                    Utils.writeContents(working, result);
                }
                stagedFiles.put(file, result);
                if (rmFiles.contains(file)) {
                    rmFiles.remove(file);
                }
            }
        }
        return result1;
    }
    /**
     * Merge.
     * Merges files from the given branch MERGEBRANCH
     * into the current branch.
     */
    public void merge(String mergebranch) throws IOException {
        preMerge(mergebranch);
        Branch target = target(mergebranch);
        Commit me = realcommit(head);
        Commit you = target.getCommit();
        premerge1(mergebranch);
        Commit gcd = splitpoint(me, you);
        boolean conflict = mergepart1(me, you, gcd);
        Set<String> meblobs = Utils.copyhashset(me.getMyBlobs().keySet());
        for (String file : meblobs) {
            if (same(file, gcd, you) && !same(file, gcd, me)) {
                continue;
            }
            if (same(file, gcd, me) && !exist(file, you)) {
                File dir = new File(file);
                dir.delete();
                rmFiles.add(file);
                if (stagedFiles.containsKey(file)) {
                    stagedFiles.remove(file);
                }
                continue;
            }
            String v1, v2 = "";
            if (!same(file, you, gcd) && !same(file, me, gcd)
                    && !same(file, you, me)) {
                conflict = true;
                File f1 = new File(".gitlet/blobs/"
                        + me.getBlobid(file) + ".ser");
                v1 = Utils.readContentsAsString(f1);
                if (exist(file, you)) {
                    File f2 = new File(".gitlet/blobs/"
                            + you.getBlobid(file) + ".ser");
                    v2 = Utils.readContentsAsString(f2);
                }
                String result = "<<<<<<< HEAD\n" + v1
                        + "=======\n" + v2 + ">>>>>>>\n";
                File working = new File(file);
                if (working.exists()) {
                    Utils.writeContents(working, result);
                } else {
                    working.createNewFile();
                    Utils.writeContents(working, result);
                }
                stagedFiles.put(file, result);
                if (rmFiles.contains(file)) {
                    rmFiles.remove(file);
                }
            }
        }
        commitCmnd("Merged " + mergebranch + " into "
                + currentBranch.getName() + ".", conflict, you.getID());
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** return if a file with NAME is untracked. */
    public boolean untracked(String name) {
        Commit commit = currentBranch.getCommit();
        return (!commit.getMyBlobs().containsKey(name)
                && !stagedFiles.containsKey(name)
                && !rmFiles.contains(name)
                && !name.equals(".gitlet"));
    }
    /** prerequisites of merging the given branch MERGEBRANCH. */
    public void preMerge(String mergebranch) {
        if (!stagedFiles.isEmpty() || !rmFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (currentBranch.getName().equals(mergebranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /** return our target branch with the name MERGEBRANCH. */
    public Branch target(String mergebranch) {
        boolean exist = false;
        Branch target = null;
        for (Branch branch : branches) {
            if (branch.getName().equals(mergebranch)) {
                exist = true;
                target = branch;
                break;
            }
        }
        if (!exist) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        return target;
    }

    /**
     * helper to find a splitpoint.
     *
     * @param c1 my head commit
     * @param c2 given commit
     * @return common ancester
     */
    public Commit splitpoint(Commit c1, Commit c2) {
        HashSet<String> c1history = new HashSet<>();
        HashSet<String> c2history = new HashSet<>();
        Commit p1 = c1;
        Commit p2 = c2;

        while (p1 != null) {

            c1history.add(p1.getID());
            Commit pointerparent = realcommit(p1.getParent());
            p1 = pointerparent;
        }


        while (p2 != null) {
            c2history.add(p2.getID());
            Commit pointerparent = realcommit(p2.getParent());
            p2 = pointerparent;
        }

        if (c1history.contains(c2.getID())) {
            System.out.println(" Given branch is an"
                    + " ancestor of the current branch.");
            System.exit(0);
        }

        if (c2history.contains(c1.getID())) {
            System.out.println("Current branch fast-forwarded.");
            head = c2.getID();
            currentBranch.setHead(head);
            System.exit(0);
        }
        Commit p = c2;
        while (p != null) {
            if (c1history.contains(p.getID())) {
                return p;
            }
            p = realcommit(p.getParent());
        }
        return null;
    }

    /**
     * helper method to return whether ME have a FILE or not.
     */
    public boolean exist(String file, Commit me) {
        if (me.getMyBlobs().isEmpty()) {
            return false;
        }
        return me.getMyBlobs().keySet().contains(file);

    }

    /**
     * helper method to return whether a given file FILE is at the same status
     * in ME and one of my ANCESTOR.
     */
    public boolean same(String file, Commit me, Commit ancestor) {

        if ((!exist(file, me)) && (!exist(file, ancestor))) {
            return true;
        }
        if (exist(file, me) && exist(file, ancestor)) {
            return me.getMyBlobs().get(file)
                    .equals(ancestor.getMyBlobs().get(file));
        }
        return false;
    }

    /** using input ARGS to determine input.*/
    public void checkout(String[] args) throws IOException {
        if (args.length == 2) {
            checkoutbranch(args[1]);
        } else if (args.length == 3) {
            checkoutfile(args[2]);
        } else if (args.length == 4) {
            checkoutcommit(args[1], args[3]);
        }
    }

    /**
     * helper method to return a real commit from a given ID.
     */
    public Commit realcommit(String id) {
        if (id == null) {
            return null;
        }
        File dir = new File(".gitlet/objects/" + id + ".ser");
        Commit result = Utils.readObject(dir, Commit.class);
        return result;
    }

    /**
     * return files to be removed.
     */
    public HashSet<String> getRmFiles() {
        return rmFiles;
    }

    /**
     * return files to be staged.
     */
    public TreeMap<String, String> getStagedFiles() {
        return stagedFiles;
    }

}
