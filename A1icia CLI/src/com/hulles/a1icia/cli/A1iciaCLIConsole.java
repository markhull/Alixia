/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.hulles.a1icia.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.object.LoginObject;
import com.hulles.a1icia.api.remote.A1iciaRemote;
import com.hulles.a1icia.api.remote.A1iciaRemoteDisplay;
import com.hulles.a1icia.api.remote.Station;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.api.shared.SharedUtils;

public class A1iciaCLIConsole extends AbstractExecutionThreadService implements A1iciaRemoteDisplay {
	private A1iciaRemote remote;
	private final Console javaConsole;
	private final ConsoleType whichConsole;
	@SuppressWarnings("unused")
	private volatile boolean serverUp;
	private final String host;
	private final Integer port;
	private final Station station;
	
	public A1iciaCLIConsole(String host, Integer port) {

		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		this.host = host;
		this.port = port;
		station = Station.getInstance();
		station.ensureStationExists();
		javaConsole = System.console();
        if (javaConsole != null) {
        	whichConsole = ConsoleType.JAVACONSOLE; 
        } else {
        	whichConsole = ConsoleType.STANDARDIO;
        }
		System.out.println("Welcome to " + getConsoleName() + ".");
		System.out.println("This station connects to A1icia server at " + host + 
				" on port " + port);
		System.out.println("The default language is " + station.getDefaultLanguage().getDisplayName());
		System.out.println("We currently " + (station.isQuiet() ? "are" : "are not") + 
				" in quiet mode.");
		showHelp();
		System.out.println("Running console " + whichConsole);
		System.out.println();
	}
	
	protected String getConsoleName() {
	
		return "the A1icia command-line interface";
	}
	
	@Override
	protected void startUp() {
		
		remote = new A1iciaRemote(host, port, this);
		remote.startAsync();
		remote.awaitRunning();
		
		remote.setUseTTS(true);
		remote.setPlayAudio(true);
		remote.setShowImage(true);
		remote.setShowText(true);
		remote.setPlayVideo(true);
	}
	
	@Override
	protected void shutDown() {
		
		remote.stopAsync();
		remote.awaitTerminated();
	}
	
	@Override
	protected void run() {
		
		switch (whichConsole) {
			case JAVACONSOLE:
				runJavaConsole();
				break;
			case STANDARDIO:
				runStandardIn();
				break;
			default:
				System.err.println("System error: bad console type, exiting");
				this.stopAsync();
		}
	}
	
	protected A1iciaRemote getRemote() {
	
		return remote;
	}
	
	private void runJavaConsole() {
		String input;
		String userName;
		String prompt;
		
		while (isRunning()) {
			userName = remote.getUserName();
			if (userName == null) {
				prompt = "Me: ";
			} else {
				prompt = "Me (" + userName + "): ";
			}
			input = javaConsole.readLine(prompt);
			if (input == null) {
				continue;
			}
			if (command(input)) {
				continue;
			}
			if (!remote.sendText(input)) {
				System.err.println("Can't communicate with server");
			}
		}
	}
	
	private void runStandardIn() {
		InputStreamReader stdIn;
		String input;
		
		stdIn = new InputStreamReader(System.in);
		try (BufferedReader reader = new BufferedReader(stdIn)) {
			while (isRunning()) {
				System.out.print("Me: ");
				input = reader.readLine();
				if (input == null) {
					continue;
				}
				if (command(input)) {
					continue;
				}
				if (!remote.sendText(input)) {
					System.err.println("Can't communicate with server");
				}
			}
		} catch (IOException e) {
			System.err.println("System error: IO error, exiting");
			this.stopAsync();
		}
	}

	@Override
	public void receiveText(String text) {

		if (!remote.useTTS()) {
			System.out.println("A1icia: " + text);
		}
	}

	private void showHelp() {
		
		System.out.print("· Type 'quit' or 'exit' to quit the CLI ");
		System.out.println("(or just use CTRL+C to quit Java).");
		System.out.println("· Type 'test' to check connection to server.");
    	if (whichConsole == ConsoleType.JAVACONSOLE) { 
			System.out.println("· Type 'login' to log in.");
			System.out.println("· Type 'logout' to log out.");
    	}
		System.out.println("· Type 'text on' to display console log.");
		System.out.println("· Type 'text off' to not display console.");
		System.out.println("· Type 'images on' to display image content.");
		System.out.println("· Type 'images off' to not display images.");
		System.out.println("· Type 'TTS on' to enable text-to-speech.");
		System.out.println("· Type 'TTS off' to disable text-to-speech.");
		System.out.println("· Type 'audio on' to play audio content.");
		System.out.println("· Type 'audio off' to disable audio content.");
		System.out.println("· Type 'help console' to repeat these commands.");		
	}

	private boolean command(String text) {
		boolean connected;
		
		if (text.equalsIgnoreCase("test")) {
			connected = remote.reachableHost();
			System.out.print("We are " + (connected ? "" : "NOT ") + "connected to the server ");
			System.out.println("at " + host + 
					" on port " + port);
			System.out.println("The language is currently " + remote.getCurrentLanguage().getDisplayName());
			System.out.println("We currently " + (station.isQuiet() ? "are" : "are not") + 
					" in quiet mode.");
			return true;
		}
		if (text.equalsIgnoreCase("tts on")) {
			remote.setUseTTS(true);
			System.out.println("Using TTS");
			return true;
		}
		if (text.equalsIgnoreCase("tts off")) {
			remote.setUseTTS(false);
			System.out.println("Not using TTS");
			return true;
		}
		if (text.equalsIgnoreCase("audio on")) {
			remote.setPlayAudio(true);
			System.out.println("Audio is on");
			return true;
		}
		if (text.equalsIgnoreCase("audio off")) {
			remote.setPlayAudio(false);
			System.out.println("Audio is off");
			return true;
		}
		if (text.equalsIgnoreCase("images on")) {
			remote.setShowImage(true);
			System.out.println("Images will be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("images off")) {
			remote.setShowImage(false);
			System.out.println("Images will not be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("text on")) {
			remote.setShowText(true);
			System.out.println("Console log will be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("text off")) {
			remote.setShowText(false);
			System.out.println("Console log will not be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("help console")) {
			showHelp();
			return true;
		}
    	if (whichConsole == ConsoleType.JAVACONSOLE) { 
			if (text.equalsIgnoreCase("login")) {
				login();
				return true;
			}
			if (text.equalsIgnoreCase("logout")) {
				logout();
				return true;
			}
    	}
		if (text.equalsIgnoreCase("quit")) {
			this.stopAsync();
			return true;
		}
		if (text.equalsIgnoreCase("exit")) {
			this.stopAsync();
			return true;
		}
		return false;
	}

	// not very secure...
	private void login() {
		LoginObject obj;
		String userName = null;
		char[] passwordChars;
		String password = null;
		
		switch (whichConsole) {
			case JAVACONSOLE:
				userName = javaConsole.readLine("Enter user name: ");
				passwordChars = javaConsole.readPassword("Enter password: ");
				password = String.copyValueOf(passwordChars);
				break;
			case STANDARDIO:
				System.err.println("System error: login is not supported for STANDARDIO");
				break;
			default:
				System.err.println("System error: bad console type, exiting");
				System.exit(2);
		}
		obj = new LoginObject();
		obj.setUserName(userName);
		obj.setPassword(password);
		if (!remote.sendLogin(obj)) {
			System.err.println("Can't communicate with server");
		}
	}
	
	private void logout() {
		LoginObject obj;
		
		obj = new LoginObject();
		obj.setUserName(null);
		obj.setPassword(null);
		if (!remote.sendLogin(obj)) {
			System.err.println("Can't communicate with server");
		}
	}
	
	@Override
	public boolean receiveCommand(SerialSpark command) {
		
		switch (command.getName()) {
			case "server_startup":
				serverUp = true;
				return true;
			case "server_shutdown":
				serverUp = false;
				return true;
			default:
				break;
		}
		return false;
	}

	@Override
	public void receiveExplanation(String text) {

		// we don't do anything with the explanation in the command line client, currently
	}

	enum ConsoleType {
		JAVACONSOLE,
		STANDARDIO
	}

	@Override
	public boolean receiveObject(A1iciaClientObject object) {

		// we let A1iciaConsole handle the heavy lifting with media objects
		return false;
	}
}