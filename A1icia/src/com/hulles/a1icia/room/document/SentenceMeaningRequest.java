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
package com.hulles.a1icia.room.document;

import java.util.List;

import com.hulles.a1icia.room.document.SentenceMeaningAnalysis.SentenceMeaningQuery;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * A request for semantic analysis of a sentence, made by another room.
 * 
 * @see SentenceMeaningAnalysis
 * 
 * @author hulles
 *
 */
public class SentenceMeaningRequest implements RoomObject {
	private List<String> context;
	private SentenceAnalysis sentenceAnalysis;
	private SentenceMeaningQuery query;
	
	public SentenceMeaningQuery getSentenceMeaningQuery() {
		
		return query;
	}

	public void setSentenceMeaningQuery(SentenceMeaningQuery query) {
		
		A1iciaUtils.checkNotNull(query);
		this.query = query;
	}

	public List<String> getContext() {
		
		return context;
	}

	public void setContext(List<String> context) {
		
		A1iciaUtils.checkNotNull(context);
		this.context = context;
	}

	public SentenceAnalysis getSentenceAnalysis() {
		
		return sentenceAnalysis;
	}

	public void setSentenceAnalysis(SentenceAnalysis analysis) {
		
		A1iciaUtils.checkNotNull(analysis);
		this.sentenceAnalysis = analysis;
	}


	@Override
	public RoomObjectType getRoomObjectType() {

		return RoomObjectType.SENTENCEMEANINGREQUEST;
	}

}
