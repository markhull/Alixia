/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.alixia.charlie;

import com.hulles.alixia.api.shared.AlixiaException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.NLPAnalysis;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Charlie Room is a busy place. It uses the Apache OpenNLP library to dissect input data from CLIENT,
 * turning them into sentences, lemmata, etc. It works really well; kudos to the OpenNLP team.
 * 
 * @author hulles
 *
 */
public final class CharlieRoom extends UrRoom {
	CharlieDocumentProcessor processor;

	public CharlieRoom() {
		super();
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "nlp_analysis":
				return createNlpActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private ActionPackage createNlpActionPackage(SememePackage sememePkg, RoomRequest request) {
		NLPAnalysis analysis;
		ActionPackage pkg;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		analysis = processor.processDocument(request);
		pkg.setActionObject(analysis);
		postProcessAnalysis(analysis);
		return pkg;
	}
	
	private static void postProcessAnalysis(NLPAnalysis analysis) {
		Thread postProc;
		
		postProc = new Thread() {
			@Override
			public void run() {
				CharlieDocumentProcessor.postProcessAnalysis(analysis);
			}
		};
		postProc.start();
	}
	
	@Override
	public Room getThisRoom() {

		return Room.CHARLIE;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
//		Thread loader;
//		
//		loader = new Thread() {
//			@Override
//			public void run() {
				processor = new CharlieDocumentProcessor();
//			}
//		};
//		loader.start();
	}
	
	@Override
	protected void roomShutdown() {
		
	}

	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("nlp_analysis"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
