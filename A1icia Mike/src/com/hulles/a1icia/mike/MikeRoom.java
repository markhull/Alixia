/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.mike;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.object.A1iciaClientObject.ClientObjectType;
import com.hulles.a1icia.api.object.AudioObject;
import com.hulles.a1icia.api.object.MediaObject;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.MediaFile;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.media.MediaUtils;
import com.hulles.a1icia.media.audio.SerialAudioFormat;
import com.hulles.a1icia.media.audio.TTSPico;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.MediaAnalysis;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.tools.FuzzyMatch;
import com.hulles.a1icia.tools.FuzzyMatch.Match;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import redis.clients.jedis.Jedis;

/**
 * Mike Room is our media room. Mike has a library of .wav files that he can broadcast to pretty 
 * much anyone via Redis / Jedis. Clients can also request media files; these are also "sent" via 
 * Redis. What actually happens is that they are stored as byte arrays in Redis and retrieved
 * by (possibly remote) Redis clients. I note in passing that, according to Jedis, the maximum 
 * byte array size is 1GB, but there is a Redis hard output buffer limit for pub/sub clients
 * which, if exceeded, causes the Redis client to be terminated. Ouch. The current limit for A1icia 
 * can be found in JebusHub.
 * 
 * @author hulles
 *
 */
public final class MikeRoom extends UrRoom {
	private final static int MAXHEADROOM = JebusHub.getMaxHardOutputBufferLimit();
	private final static Logger LOGGER = Logger.getLogger("A1iciaMike.MikeRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static Level LOGLEVEL = Level.INFO;
	@SuppressWarnings("unused")
	private List<String> acknowledgments;
	private List<String> exclamations;
	private List<String> praise;
	@SuppressWarnings("unused")
	private List<String> musicClips;
	private List<String> prompts;
	private List<String> specialMedia;
	private List<String> notifications;
	private final Random random;
	private final List<String> artists;
	private final List<String> titles;
	private final ApplicationKeys appKeys;
	private byte[] introBytes = null;
	private final JebusPool jebusLocal;
	
	public MikeRoom(EventBus bus) {
		super(bus);
		
		random = new Random();
		artists = new ArrayList<>(3000);
		titles = new ArrayList<>(3000);
		appKeys = ApplicationKeys.getInstance();
		jebusLocal = JebusHub.getJebusLocal();
	}

	private String getRandomFileName(List<String> files) {
		int ix;
		
		ix = random.nextInt(files.size());
		return files.get(ix);
	}
	
	private MediaFile getRandomMediaFile(List<MediaFile> files) {
		int ix;
		
		ix = random.nextInt(files.size());
		return files.get(ix);
	}
	
	public List<Match> matchArtists(String input, int bestNAnswers) {
		List<Match> matches;
		
		A1iciaUtils.checkNotNull(input);
		A1iciaUtils.checkNotNull(bestNAnswers);
		matches = FuzzyMatch.getNBestMatches(input, artists, bestNAnswers);
		return matches;
	}
	
	public List<Match> matchTitles(String input, int bestNAnswers) {
		List<Match> matches;
		
		A1iciaUtils.checkNotNull(input);
		A1iciaUtils.checkNotNull(bestNAnswers);
		matches = FuzzyMatch.getNBestMatches(input, titles, bestNAnswers);
		return matches;
	}
	
	public static void logAudioFormat(String fileName) {
		
		try {
			LOGGER.log(LOGLEVEL, MediaUtils.getAudioFormatString(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateMediaLibrary() {
		List<String> audioList;
		List<String> videoList;
		String updateKey;
		String instantStr;
		Instant timestamp = null;
		Instant now;
		String musicLib;
		String videoLib;
		List<MediaFile> mediaFiles;
		MediaFile mediaFile;
		
		updateKey = JebusBible.getA1iciaMediaFileUpdateKey(jebusLocal);
		now = Instant.now();
		try (Jedis jebus = jebusLocal.getResource()) {
			instantStr = jebus.get(updateKey);
			if (instantStr != null) {
				timestamp = Instant.parse(instantStr);
			}
			if (timestamp == null || timestamp.isBefore(now)) {
				jebus.set(updateKey, now.plus(7, ChronoUnit.DAYS).toString());
				musicLib = appKeys.getKey(ApplicationKey.MUSICLIBRARY);
				
				// get list of audio library files and update the database with them
				audioList = LibraryLister.listFiles(musicLib, "*.mp3");
				audioList.stream().forEach(e -> updateAudioItem(e));
				// now get rid of any audio files in the database that don't exist in the library
				mediaFiles = MediaFile.getAudioFiles();
				for (Iterator<MediaFile> iter = mediaFiles.iterator(); iter.hasNext(); ) {
					mediaFile = iter.next();
					if (!audioList.contains(mediaFile.getFileName())) {
						iter.remove();
					}
				}
				
				// get list of video library files and update the database with them
				videoLib = appKeys.getKey(ApplicationKey.VIDEOLIBRARY);
				videoList = LibraryLister.listFiles(videoLib, "*.{mp4,flv}");
				videoList.stream().forEach(e -> updateVideoItem(e));
				// now get rid of any video files in the database that don't exist in the library
				mediaFiles = MediaFile.getVideoFiles();
				for (Iterator<MediaFile> iter = mediaFiles.iterator(); iter.hasNext(); ) {
					mediaFile = iter.next();
					if (!audioList.contains(mediaFile.getFileName())) {
						iter.remove();
					}
				}
			}
		}
	}
	
	private static void updateAudioItem(String fileName) {
		Mp3File mp3File;
		String artist;
		String title;
		ID3v1 v1Tag;
		ID3v2 v2Tag;
		MediaFile mediaFile;
		boolean updating = false;
		String existingArtist;
		String existingTitle;
		
		A1iciaUtils.checkNotNull(fileName);
		try {
			mp3File = new Mp3File(fileName);
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			A1iciaUtils.error("Exception with MP3 file = " + fileName, e);
			return;
		}
		if (mp3File.hasId3v2Tag()) {
			v2Tag = mp3File.getId3v2Tag();
			artist = v2Tag.getArtist();
			title = v2Tag.getTitle();
		} else if (mp3File.hasId3v1Tag()) {
			v1Tag = mp3File.getId3v1Tag();
			artist = v1Tag.getArtist();
			title = v1Tag.getTitle();
		} else {
			return;
		}
		mediaFile = MediaFile.findMediaFile(fileName);
		if (mediaFile == null) {
			mediaFile = MediaFile.createNew();
			mediaFile.setArtist(artist);
			mediaFile.setTitle(title);
			mediaFile.setFormat(MediaFormat.MP3);
			mediaFile.setFileName(fileName);
			updating = true;
		} else {
			existingArtist = mediaFile.getArtist();
			if (artist != null) {
				if (existingArtist == null || !existingArtist.equals(artist)) {
					LOGGER.log(LOGLEVEL, "Updating existing artist = {0} to {1}", new Object[]{existingArtist, artist});
					mediaFile.setArtist(artist);
					updating = true;
				}
			}
			existingTitle = mediaFile.getTitle();
			if (title != null) {
				if (existingTitle == null || !existingTitle.equals(title)) {
					LOGGER.log(LOGLEVEL, "Updating existing title = {0} to {1}", new Object[]{existingTitle, title});
					mediaFile.setTitle(title);
					updating = true;
				}
			}
			if (mediaFile.getFormat() != MediaFormat.MP3) {
				LOGGER.log(LOGLEVEL, "Updating media format from " + mediaFile.getFormat() + " to MP3");
				mediaFile.setFormat(MediaFormat.MP3);
				updating = true;
			}
		}
		if (updating) {
			LOGGER.log(LOGLEVEL, "Committing change(s)");
			mediaFile.commit();
		}
	}
	
	private static void updateVideoItem(String fileName) {
		MediaFile mediaFile;
		
		A1iciaUtils.checkNotNull(fileName);
		mediaFile = MediaFile.findMediaFile(fileName);
		if (mediaFile == null) {
			mediaFile = MediaFile.createNew();
			mediaFile.setArtist(null);
			mediaFile.setTitle(Files.getNameWithoutExtension(fileName));
			if (fileName.endsWith("mp4")) {
				mediaFile.setFormat(MediaFormat.MP4);
			} else if (fileName.endsWith("flv")) {
				mediaFile.setFormat(MediaFormat.FLV);
			} else {
				A1iciaUtils.error("MikeRoom:updateVideoItem bad media file extension");
				return;
			}
			mediaFile.setFileName(fileName);
			LOGGER.log(LOGLEVEL, "Committing change(s)");
			mediaFile.commit();
		}
	}
	
	@Override
	public Room getThisRoom() {

		return Room.MIKE;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
		List<MediaFile> media;
		String lib;
		String fileName;
		
		lib = appKeys.getKey(ApplicationKey.MIKELIBRARY);
		acknowledgments = LibraryLister.listFiles(lib + "acknowledgment");
		exclamations = LibraryLister.listFiles(lib + "exclamation");
		musicClips = LibraryLister.listFiles(lib + "music_clips");
		prompts = LibraryLister.listFiles(lib + "passing_by");
		praise = LibraryLister.listFiles(lib + "praise");
		specialMedia = LibraryLister.listFiles(lib + "other_responses");
		notifications = LibraryLister.listFiles(lib + "notifications");
		updateMediaLibrary();
		media = MediaFile.getMediaFiles();
		media.stream().forEach(e -> addMediaToLists(e));
		fileName = findMediaFile("AV.mov", specialMedia);
		if (fileName == null) {
			A1iciaUtils.error("MikeRoom: null intro file name");
			return;
		}
		introBytes = MediaUtils.fileToByteArray(fileName);
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	private void addMediaToLists(MediaFile media) {
		String artist;
		String title;
		
		artist = media.getArtist();
		if (artist != null) {
			artists.add(artist);
		}
		title = media.getTitle();
		if (title != null) {
			titles.add(title);
		}
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "play_artist":
				return createArtistActionPackage(sememePkg, request);
			case "play_title":
				return createTitleActionPackage(sememePkg, request);
			case "random_music":
				return createRandomMusicActionPackage(sememePkg, request);
			case "play_video":
				return createVideoActionPackage(sememePkg, request);
			case "speak":
				return createSpeakActionPackage(sememePkg, request);
			case "prompt":
			case "exclamation":
			case "nothing_to_do":
			case "praise":
				return createPromptActionPackage(sememePkg, request);
			case "pronounce_linux":
			case "listen_to_her_heart":
			case "pronounce_alicia":
			case "sorry_for_it_all":
			case "dead_sara":
			case "pronounce_hulles":
				return createSpecialActionPackage(sememePkg, request);
			case "match_artists_and_titles":
				return createAnalysisActionPackage(sememePkg, request);
			case "notification_medium":
				return createNotificationActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}

	protected ActionPackage createArtistActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String fileName = null;
		List<MediaFile> mediaFiles;
		MediaFile mediaFile;
		String matchTarget;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createArtistActionPackage: no sememe object");
		}
		mediaFiles = MediaFile.getMediaFiles(matchTarget);
		if (mediaFiles != null) {
			mediaFile = getRandomMediaFile(mediaFiles);
			fileName = mediaFile.getFileName();
			if (fileName == null) {
				A1iciaUtils.error("MikeRoom: file name is null");
				return null;
			}
			audioFormat = MediaUtils.getAudioFormat(fileName);
			audioObject = new AudioObject();
			audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
			audioObject.setMediaTitle(fileName);
			serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
			audioObject.setAudioFormat(serialFormat);
			mediaBytes = MediaUtils.fileToByteArray(fileName);
			if (mediaBytes.length > MAXHEADROOM) {
				A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
				return null;
			}
			mediaArrays = new byte[][]{mediaBytes};
			audioObject.setMediaBytes(mediaArrays);
			audioObject.setMediaFormat(MediaFormat.MP3);
			if (!audioObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(audioObject);
		}
		pkg.setActionObject(action);
		return pkg;
	}

	protected static ActionPackage createTitleActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String fileName = null;
		MediaFile mediaFile;
		String matchTarget;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createTitleActionPackage: no sememe object");
		}
		mediaFile = MediaFile.getMediaFile(matchTarget);
		if (mediaFile == null) {
			A1iciaUtils.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			A1iciaUtils.error("File name is null in Mike room");
			return null;
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioObject.setMediaTitle(fileName);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		audioObject.setMediaFormat(MediaFormat.MP3);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
		return pkg;
	}

	protected static ActionPackage createRandomMusicActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String fileName = null;
		MediaFile mediaFile;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		mediaFile = MediaFile.getRandomMediaFile();
		if (mediaFile == null) {
			A1iciaUtils.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			A1iciaUtils.error("File name is null in Mike room");
			return null;
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioObject.setMediaTitle(fileName);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		audioObject.setMediaFormat(MediaFormat.MP3);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
		return pkg;
	}

	protected ActionPackage createVideoActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		MediaObject mediaObject;
		String fileName = null;
		MediaFile mediaFile;
		String matchTarget;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createVideoActionPackage: no sememe object");
		}
		mediaFile = MediaFile.getMediaFile(matchTarget);
		if (mediaFile == null) {
			A1iciaUtils.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			A1iciaUtils.error("File name is null in Mike room");
			return null;
		}
		mediaObject = new MediaObject();
		mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
		mediaObject.setMediaTitle(fileName);
		if (fileName.endsWith("MP4")) {
			mediaObject.setMediaFormat(MediaFormat.MP4);
		} else if (fileName.endsWith("FLV")) {
			mediaObject.setMediaFormat(MediaFormat.MP4);
		} else {
			A1iciaUtils.error("MikeRoom: unknown video media type");
			return null;
		}
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		if (introBytes == null) {
			mediaArrays = new byte[][]{mediaBytes};
		} else {
			// TODO need to allow for multiple media formats; here we're combining
			//  a .MOV and a .FLV or a .MP4....
			if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
				A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
				return null;
			}
			mediaArrays = new byte[][] {introBytes, mediaBytes};
		}
		mediaObject.setMediaBytes(mediaArrays);
		if (!mediaObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(mediaObject);
		pkg.setActionObject(action);
		return pkg;
	}

	protected static ActionPackage createSpeakActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String speech;
		File tempFile;
		String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		speech = request.getMessage().trim();
		tempFile = TTSPico.ttsToFile(speech);
		fileName = tempFile.getAbsolutePath();
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject.setMediaTitle(fileName);
		audioObject.setMediaFormat(MediaFormat.WAV);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
		return pkg;
	}

	protected ActionPackage createSpecialActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		MediaObject mediaObject;
		String target = null;
		String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		if (sememePkg.is("pronounce_linux")) {
			target = "Linus-linux.wav"; 
		} else if (sememePkg.is("pronounce_hulles")) {
			target = "hulles_hulles.wav";
		} else if (sememePkg.is("listen_to_her_heart")) {
			target = "listen_to_her_heart.wav";
		} else if (sememePkg.is("sorry_for_it_all")) {
			target = "sorry_for_it_all.mp4";
		} else if (sememePkg.is("dead_sara")) {
			target = "masse_color1.jpg";
		} else if (sememePkg.is("pronounce_alicia")) {
			if (random.nextBoolean()) {
				target = "Sv-Alicia_Vikander.wav";
			} else {
				target = "pronounce_alicia.mov";
			}
		}
		if (target == null) {
			throw new A1iciaException();
		}
		fileName = findMediaFile(target, specialMedia);
		if (fileName == null) {
			A1iciaUtils.error("MikeRoom: null file name");
			return null;
		}
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		if (fileName.endsWith("wav")) {
			audioObject = new AudioObject();
			audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
			audioFormat = MediaUtils.getAudioFormat(fileName);
			audioObject.setMediaTitle(fileName);
			audioObject.setMediaFormat(MediaFormat.WAV);
			serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
			audioObject.setAudioFormat(serialFormat);
			mediaArrays = new byte[][]{mediaBytes};
			audioObject.setMediaBytes(mediaArrays);
			if (!audioObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(audioObject);
		} else if (fileName.endsWith("jpg")) {
			mediaObject = new MediaObject();
			mediaObject.setClientObjectType(ClientObjectType.IMAGEBYTES);
			mediaObject.setMediaFormat(MediaFormat.JPG);
			mediaArrays = new byte[][]{mediaBytes};
			mediaObject.setMediaBytes(mediaArrays);
			mediaObject.setMediaTitle("Dead Sara Poster");
			if (!mediaObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(mediaObject);
			if (sememePkg.is("dead_sara")) {
				action.setMessage("Dead Sara is super bueno.");
			}
		} else if (fileName.endsWith("mov")) {
			mediaObject = new MediaObject();
			mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
			mediaObject.setMediaTitle(fileName);
			mediaObject.setMediaFormat(MediaFormat.MOV);
			if (introBytes == null) {
				mediaArrays = new byte[][]{mediaBytes};
				mediaObject.setMediaBytes(mediaArrays);
			} else {
				if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
					A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
					return null;
				}
				mediaArrays = new byte[][]{introBytes, mediaBytes};
				mediaObject.setMediaBytes(mediaArrays);
			}
			if (!mediaObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(mediaObject);
		} else if (fileName.endsWith("mp4")) {
			mediaObject = new MediaObject();
			mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
			mediaObject.setMediaTitle(fileName);
			mediaObject.setMediaFormat(MediaFormat.MP4);
			if (introBytes == null) {
				mediaArrays = new byte[][]{mediaBytes};
				mediaObject.setMediaBytes(mediaArrays);
			} else {
				if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
					A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
					return null;
				}
				mediaArrays = new byte[][]{introBytes, mediaBytes};
				System.out.println("MIKE: intro length = " + introBytes.length);
				System.out.println("MIKE: media length = " + mediaBytes.length);
				mediaObject.setMediaBytes(mediaArrays);
			}
			if (!mediaObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(mediaObject);
			if (sememePkg.is("sorry_for_it_all")) {
				action.setMessage("Dead Sara kicks my ass.");
			}
		} else {
			A1iciaUtils.error("MikeRoom: unsupported media file -- support it");
			return null;
		}
		pkg.setActionObject(action);
		return pkg;
	}

	protected ActionPackage createNotificationActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String matchTarget = null;
		String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		// SememeObjectType s/b AUDIOTITLE, btw
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createArtistActionPackage: no sememe object");
		}
		fileName = findMediaFile(matchTarget, notifications);
		if (fileName == null) {
			A1iciaUtils.error("MikeRoom: null file name");
			return null;
		}
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject.setMediaTitle(fileName);
		audioObject.setMediaFormat(MediaFormat.WAV);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
			}
		action = new ClientObjectWrapper(audioObject);
		action.setMessage(request.getMessage()); // this is the kluged long timer id
		pkg.setActionObject(action);
		return pkg;
	}
	
	protected ActionPackage createPromptActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String fileName = null;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		if (sememePkg.is("prompt")) {
			fileName = getRandomFileName(prompts);
		} else if (sememePkg.is("praise")) {
			fileName = getRandomFileName(praise);
		} else {
			fileName = getRandomFileName(exclamations);
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioObject.setMediaTitle(fileName);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		audioObject.setMediaFormat(MediaFormat.WAV);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
		return pkg;
	}

	private ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		MediaAnalysis analysis;
		List<Match> artistMatches;
		List<Match> titleMatches;
		String name;
		ActionPackage pkg;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		analysis = new MediaAnalysis();
		name = request.getMessage();
		artistMatches = matchArtists(name, 3);
		titleMatches = matchTitles(name, 3);
		analysis.setArtists(artistMatches);
		analysis.setTitles(titleMatches);
		analysis.setInputToMatch(name);
		pkg.setActionObject(analysis);
		return pkg;
	}
	
	private static String findMediaFile(String name, List<String> mediaFiles) {
		
		for (String fnm : mediaFiles) {
			LOGGER.log(LOGLEVEL, "findMediaFile : " + fnm);
			if (fnm.contains(name)) {
				return fnm;
			}
		}
		return null;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("match_artists_and_titles"));
		sememes.add(SerialSememe.find("play_artist"));
		sememes.add(SerialSememe.find("play_title"));
		sememes.add(SerialSememe.find("play_video"));
		sememes.add(SerialSememe.find("prompt"));
		sememes.add(SerialSememe.find("speak"));
		sememes.add(SerialSememe.find("praise"));
		sememes.add(SerialSememe.find("exclamation"));
		sememes.add(SerialSememe.find("nothing_to_do"));
		sememes.add(SerialSememe.find("pronounce_linux"));
		sememes.add(SerialSememe.find("pronounce_alicia"));
		sememes.add(SerialSememe.find("pronounce_hulles"));
		sememes.add(SerialSememe.find("listen_to_her_heart"));
		sememes.add(SerialSememe.find("sorry_for_it_all"));
		sememes.add(SerialSememe.find("dead_sara"));
		sememes.add(SerialSememe.find("notification_medium"));
		sememes.add(SerialSememe.find("random_music"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}
}
