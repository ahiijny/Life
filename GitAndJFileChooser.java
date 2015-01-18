

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class GitAndJFileChooser {

	public static void main(String[] args) {
		System.setOut(System.err); // so they both get buffered the same...
		System.out.println("Before JFileChooser constructor");
		JFileChooser myChooser  = new JFileChooser("c:/");
		System.out.println("Before showOpenDialog");
		int rv = myChooser.showOpenDialog(JOptionPane.getRootFrame());
		System.out.println("After showOpentDialog");
		
		System.out.println("Now trying on a Git repository directory");
		System.out.println("Before JFileChooser constructor [git directory]");
		myChooser  = new JFileChooser("c:/gittest");
		System.out.println("Before showOpenDialog  [git directory]");
		rv = myChooser.showOpenDialog(JOptionPane.getRootFrame());
		System.out.println("After showOpentDialog  [git directory]");
		
	}

}
