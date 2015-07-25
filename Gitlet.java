import java.io.*;
import java.nio.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.nio.file.StandardCopyOption.*;
import java.util.Calendar;

public class Gitlet implements Serializable {
	private static final Path GITLET_DIR = Paths.get(".gitlet");
	private CommitNode head;
	private CommitNode parent;
	private HashSet<String> untracking = new HashSet<String>();
	private HashMap<String, CommitNode> commitID = new HashMap();
	private LinkedList commitTree;
	private HashSet<String> stagingArea = new HashSet<String>();
	private int lastId;

	public void init() {
		boolean path_exists = Files.exists(GITLET_DIR);
		System.out.println("Exists? " + path_exists);
		if (path_exists) {
			System.out
					.println("A gitlet version control system already exists in the current directory");
			return;
		}
		try {
			Files.createDirectory(GITLET_DIR);
			Files.createDirectory(GITLET_DIR.resolve("staging"));
			Files.createDirectory(GITLET_DIR.resolve("HEAD"));
			Files.createDirectory(GITLET_DIR.resolve("files"));
			Files.createDirectory(GITLET_DIR.resolve("commit"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		commit("intial commit");
		lastId = 0;
	}

	public void add(String name) {
		boolean path_exists = Files.exists(Paths.get(name));
		System.out.println("Exists? " + path_exists);
		if (path_exists) {
			System.out.println("File does not exist");
			return;
		}
		if (untracking.contains(name)) {
			untracking.remove(name);
		}
		// copy the files from working directory to the staging area
		Path source = Paths.get(".");
		Path target = GITLET_DIR.resolve("staging");
		try {
			// Create the empty file with default permissions, etc.
			Files.copy(source, target, REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		stagingArea.add(name);
	}

	public void commit(String message) {
		if (message == null) {
			System.out.println("Please enter a commit message");
			return;
		}
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String currentTime = sdf.toString();
		int currentid = lastId++;
		String idName = "" + currentid;
		// Path myPath = Paths.get("whatever");
		// Files.createDirectories(myPath.getParent());
		if(stagingArea.isEmpty()){
			System.out.println("No changes added to the commit ");
			return;
		}
		try {
			Files.createDirectory(GITLET_DIR.resolve("commit").resolve(idName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// FIXME
		Path source = Paths.get(".");
		Path target = GITLET_DIR.resolve("commit").resolve(idName);
		parent.fileTransfer(source, target); // from stagingArea to commit/id/
		parent = head; // advancing
		head = new CommitNode(message, currentTime, currentid);
		commitTree.add(head);
		head.prev = parent;
		parent.next = head;
		head.next = null;
		reset();
	}

	// reset the staging area and untracking after commit
	public void reset() {
		untracking.clear(); // file were marked for untracking
		for (String path : stagingArea) {// the staging area is clear after
			try {
				Files.delete(GITLET_DIR.resolve("stagingArea").resolve(path));// resolve
			} catch (IOException e) {
				// File permission problems are caught here.
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	private class CommitNode {
		private CommitNode prev;
		private CommitNode next;
		private String message;
		private String date;
		private int id = 0;

		public CommitNode(String m, String d, int i) {
			message = m;
			date = d;
			id = i;
		}

		// in commit : from stagingArea to commit/id
		public void fileTransfer(Path source, Path target) {
			// TODO check if there are files are untracking
			for (String path : stagingArea) { // the staging area is clear after
				Path ss = Paths.get("staging").resolve(path);
				source = GITLET_DIR.resolve(ss);
				try {
					Files.copy(source, target, REPLACE_EXISTING);
				} catch (IOException e) {
					// File permission problems are caught here.
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}

	}


	public static void main(String[] args) {
		Gitlet g = new Gitlet();
		System.err.println(args);
		
		
		if (args[0].equals("init")) {
			g.init();
		}
		if (args[0].equals("add")) {
			g.add(args[1]);
		}
		
		if(args[0].equals("commit")){
			g.commit(args[1]);
		}

	}

}
