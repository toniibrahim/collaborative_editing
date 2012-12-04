/**
 * 
 */
package controller;

import gui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import model.Model;

/**
 * @author gyz
 * 
 *         Controller class, every time a action performed, open an new thread
 *         to preform action
 * 
 *         I will start with a slow implementation (read all string in the text
 *         field every time)
 * 
 * 
 */
public class Controller implements ActionListener, Serializable {
	private final Model model;
	private List<GUI> views;

	public Controller() {
		this.model = new Model(this);
	}

	public Model getModel() {
		return this.model;
	}

	public void addView(GUI v) {
		views.add(v);
	}

	public void actionPerformed(ActionEvent e) {
		BackEndThread thread = new BackEndThread(e, model);
		thread.start();

	}

	public void updateFontEnd() {
		for (GUI v : views) {
			FrontEndThread thread = new FrontEndThread(v, model);
			thread.start();
		}
	}
}
