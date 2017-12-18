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
package com.hulles.a1icia.foxtrot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.shared.PurdahKeys;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState;
import com.hulles.a1icia.foxtrot.monitor.LinuxMonitor;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;

/**
 * Foxtrot Room is where A1icia can query herself about her own status and health. 
 * 
 * @author hulles
 *
 */
public final class FoxtrotRoom extends UrRoom {
	private LinuxMonitor sysMonitor;
	
	public FoxtrotRoom(EventBus bus) {
		super(bus);
		
	}

	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "check_warnings":
			case "how_are_you":
			case "inquire_status":
				return createStatusActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private ActionPackage createStatusActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		FoxtrotAction action;
		FoxtrotPhysicalState state;
		
		pkg = new ActionPackage(sparkPkg);
		action = new FoxtrotAction();
		state = sysMonitor.getFoxtrotPhysicalState();
		action.setState(state);
		pkg.setActionObject(action);
		return pkg;
	}

	@Override
	public Room getThisRoom() {

		return Room.FOXTROT;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
		String osName;
		PurdahKeys purdahKeys;
		
		purdahKeys = PurdahKeys.getInstance();
		osName = System.getProperty("os.name");
		switch (osName) {
			case "Linux":
				sysMonitor = new LinuxMonitor();
				sysMonitor.setDatabaseUser(purdahKeys.getDatabaseUser());
				sysMonitor.setDatabasePassword(purdahKeys.getDatabasePassword());
				sysMonitor.setDatabaseServer(purdahKeys.getDatabaseServer());
				sysMonitor.setDatabasePort(purdahKeys.getDatabasePort());
				break;
			default:
				throw new UnsupportedOperationException("This operating system is not supported");
		}
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("check_warnings"));
		sparks.add(Spark.find("how_are_you"));
		sparks.add(Spark.find("inquire_status"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}