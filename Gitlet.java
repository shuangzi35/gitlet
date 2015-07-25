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

	private HashSet<String> untracking = new HashSet<String>();
	private HashMap<Integer, CommitNode> commitID = new HashMap<Integer, CommitNode>();
	private LinkedList commitTree;
	private  HashSet<String> stagingArea = new HashSet<String>();
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
		//head.tracking.add(name);
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
		head.fileTransfer(source, target); // from stagingArea to commit/id/
		HashMap<String, Integer> Newtrack  = new HashMap(head.tracking);
		for(String path : untracking){
			Newtrack.remove(path);
		}
		
		for(String path : stagingArea){
			Newtrack.put(path, currentid);
		}
		CommitNode parent;
		parent = head; // advancing
		head = new CommitNode(message, currentTime, currentid);
		commitID.put(currentid, head);
		commitTree.add(head);
		head.prev = parent;
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
		private String message;
		private String date;
		private int id = 0;
		private HashMap<String, Integer> tracking = new HashMap<String, Integer>(); //know what you have
		// parent.tracking + stagingArea - untracked  = me.tracking

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

	public void rm(String name){
		//FIXME
		if (! isTracked(name)){
			System.out.println("No reason to remove the file.");
			return;
		}
		
		if(stagingArea.contains(name)){
			try{
			Files.delete(GITLET_DIR.resolve("stagingArea").resolve(name));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		untracking.add(name);
		
	}
	
	public boolean isTracked(String name){
			if(stagingArea.contains(name)){
				return true;
			}
			if(untracking.contains(name)){
				return false;
			}
			
			return head.tracking.containsKey(name);
	}
	
	public void log(){
		
	}
	
	public void find(String message){
		CommitNode T = commitID.get(message);
		if(T ==null){
			System.out.println("Found no commit with that message.");
		}
		System.out.println(T.id);
	}
	
	public void checkout(String[] args){
		if(args[0].equals("init"));
		
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
