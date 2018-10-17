package gitlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.List;
import java.util.Arrays;
import java.util.Formatter;


/**
 * Assorted utilities.
 *
 * @author P. N. Hilfinger
 */
class Utils {

    /* SHA-1 HASH VALUES. */

    /**
     * The length of a complete SHA-1 UID as a hexadecimal numeral.
     */
    static final int UID_LENGTH = 40;
    /**
     * Filter out all but plain files.
     */
    private static final FilenameFilter PLAIN_FILES =
            new FilenameFilter() {
            @Override
                public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    /**
     * Returns the SHA-1 hash of the concatenation of VALS, which may
     * be any mixture of byte arrays and Strings.
     */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /* FILE DELETION */

    /**
     * Returns the SHA-1 hash of the concatenation of the strings in
     * VALS.
     */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /**
     * Deletes FILE if it exists and is not a directory.  Returns true
     * if FILE was deleted, and false otherwise.  Refuses to delete FILE
     * and throws IllegalArgumentException unless the directory designated by
     * FILE also contains a directory named .gitlet.
     */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /* READING AND WRITING FILE CONTENTS */

    /**
     * Deletes the file named FILE if it exists and is not a directory.
     * Returns true if FILE was deleted, and false otherwise.  Refuses
     * to delete FILE and throws IllegalArgumentException unless the
     * directory designated by FILE also contains a directory named .gitlet.
     */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /**
     * Return the entire contents of FILE as a byte array.  FILE must
     * be a normal file.  Throws IllegalArgumentException
     * in case of problems.
     */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Return the entire contents of FILE as a String.  FILE must
     * be a normal file.  Throws IllegalArgumentException
     * in case of problems.
     */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /**
     * Write the result of concatenating the bytes in CONTENTS to FILE,
     * creating or overwriting it as needed.  Each object in CONTENTS may be
     * either a String or a byte array.  Throws IllegalArgumentException
     * in case of problems.
     */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw new IllegalArgumentException(
                        "cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(
                            Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     * Throws IllegalArgumentException in case of problems.
     */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /* DIRECTORIES */

    /**
     * Write OBJ to FILE.
     */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /**
     * Returns a list of the names of all plain files in the directory DIR, in
     * lexicographic order as Java Strings.  Returns null if DIR does
     * not denote a directory.
     */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /**
     * Returns a list of the names of all plain files in the directory DIR, in
     * lexicographic order as Java Strings.  Returns null if DIR does
     * not denote a directory.
     */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */

    /**
     * Return the concatentation of FIRST and OTHERS into a File designator,
     * analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     * method.
     */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /**
     * Return the concatentation of FIRST and OTHERS into a File designator,
     * analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     * method.
     */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES */

    /**
     * Returns a byte array containing the serialized contents of OBJ.
     */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /**
     * Return a GitletException whose message is composed from MSG and ARGS as
     * for the String.format method.
     */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /**
     * Print a message composed from MSG and ARGS as for the String.format
     * method, followed by a newline.
     */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    /** FUNCTIONS */

    /**
     * Check whether or not the input is valid.
     * @param args input
     * @param expected should be
     */
    static void checkInput(String[] args, int expected) {
        if (args.length > expected) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        if (!args[0].equals("init") && !(new File(".gitlet").exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args[0].equals("commit") && (args.length < expected
                || args[1].isEmpty())) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else if (args.length < expected) {
            System.out.println("Incorrect operands");
            System.exit(0);
        }

    }

    /**
     * check checkout input.
     * @param args input
     */
    static void checkCheckoutInput(String[] args) {
        if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length > 4 || args.length < 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands");
                System.exit(0);
            }
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands");
                System.exit(0);
            }
        }
    }

    /**
     * deep copy a treemap.
     * @param original eh
     * @return copied
     */
    public static TreeMap<String, String> copytreemap(TreeMap<String,
            String> original) {
        TreeMap<String, String> result = new TreeMap<>();
        for (String s : original.keySet()) {
            result.put(s, original.get(s));
        }
        return result;
    }

    /**
     * return a copied string set from a given set SS.
     */
    public static Set<String> copyhashset(Set<String> ss) {
        Set<String> result = new HashSet<>();
        for (String s : ss) {
            result.add(s);
        }
        return result;
    }

    /**
     * return a sorted array of String from a hashset ORIGINAL.
     */
    public static String[] sorthashset(HashSet<String> original) {
        String[] result = new String[original.size()];
        int i = 0;
        for (String s : original) {
            result[i] = s;
            i += 1;
        }
        Arrays.sort(result);
        return result;
    }

    /**
     * return a sorted array of String from a set ORIGINAL.
     */
    public static String[] sortset(Set<String> original) {
        String[] result = new String[original.size()];
        int i = 0;
        for (String s : original) {
            result[i] = s;
            i += 1;
        }
        Arrays.sort(result);
        return result;
    }


    /**
     * Represents a function from T1 -> T2.  The apply method contains the
     * code of the function.  The 'foreach' method applies the function to all
     * items of an Iterable.  This is an interim class to allow use of Java 7
     * with Java 8-like constructs.
     */
    abstract static class Function<T1, T2> {
        /**
         * Returns the value of this function on X.
         */
        abstract T2 apply(T1 x);
    }

}
