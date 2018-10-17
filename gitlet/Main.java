package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Siyan Shen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Gitlet g = read();
        checkAllInput(args);
        try {
            switch (args[0]) {
            case "init":
                g.init();
                break;
            case "add":
                g.addFile(args[1]);
                break;
            case "commit":
                g.commitCmnd(args[1], false, null);
                break;
            case "rm":
                g.removeFile(args[1]);
                break;
            case "log":
                g.log();
                break;
            case "global-log":
                g.globalLog();
                break;
            case "find":
                g.find(args[1]);
                break;
            case "status":
                g.status();
                break;
            case "checkout":
                g.checkout(args);
                break;
            case "branch":
                g.branch(args[1]);
                break;
            case "rm-branch":
                g.rmbranch(args[1]);
                break;
            case "reset":
                g.reset(args[1]);
                break;
            case "merge":
                g.merge(args[1]);
                break;
            default:
            }
            Gitlet.save(g);
        } catch (IOException e) {
            System.exit(0);
        }
        System.exit(0);
    }

    /** check the validity of input.
     * @param args input
     */
    public static void checkAllInput(String[] args) {
        switch (args[0]) {
        case "find":
        case "add":
        case "commit":
        case "rm":
        case "branch":
        case "rm-branch":
        case "reset":
        case "merge":
            Utils.checkInput(args, 2);
            break;
        case "status":
        case "init":
        case "log":
        case "global-log":
            Utils.checkInput(args, 1);
            break;

        case "checkout":
            Utils.checkCheckoutInput(args);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    /** read history into gitlet object.
     * @return history
     */
    public static Gitlet read() {
        Gitlet g = new Gitlet();
        if (new File(".gitlet").exists()
                && new File(".gitlet/history.ser").exists()) {
            g = Utils.readObject(new File(".gitlet/history.ser"),
                    Gitlet.class);
        }
        return g;
    }

}

