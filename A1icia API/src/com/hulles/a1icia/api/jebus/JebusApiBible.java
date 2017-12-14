package com.hulles.a1icia.api.jebus;

import com.hulles.a1icia.api.jebus.JebusPool.JebusPoolType;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * The JebusBible simply provides a strongly-typed way of retrieving the various Jebus strings
 * we use for keys in A1icia III. This is the API pocket version of the bible, the one they 
 * give you at AA meetings.
 * 
 * @author hulles
 *
 */
public final class JebusApiBible {
	
    private final static String ALICIACHANNELKEY = "a1icia:channel:";
	private final static String FROMCHANNEL = "from";
	private final static String TOCHANNEL = "to";
	private final static String CHANNELINCR = "a1icia:channel:next_console";

	private final static String ALICIANCOUNTERKEY = "a1icia:a1ician:next_a1ician";
	
	private final static String ALICIAAESKEY = "a1icia:aes";
    
	private final static String ALICIAAPPSKEY = "a1icia:appkeys";
	private final static String ALICIAPURDAHKEY = "a1icia:purdah";
    
    private final static String ALICIASTATIONKEY = "a1icia:station";
//    private final static String STATION_ID = "station_ID";
//    private final static String STATION_CENTRAL_HOST = "central_host";
//    private final static String STATION_CENTRAL_PORT = "central_port";
//    private final static String STATION_OS = "os";
//    private final static String STATION_PICO = "pico";
//    private final static String STATION_MPV = "mpv";
//    private final static String STATION_PRETTY_LIGHTS = "pretty_lights";
//    private final static String STATION_LEDS = "leds";
//    private final static String STATION_IRON = "iron";
	
    private final static String ALICIAMEDIACACHEKEY = "a1icia:mediacache";
    private final static String MEDIAFORMATFIELD = "mediafmt";
    private final static String MEDIABYTESFIELD = "mediabytes";
    
	/*************************/
	/***** ALICIA CRYPTO *****/
	/*************************/

	public static String getA1iciaAESKey(JebusPool pool) {

		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAAESKEY;
	}
	
	/******************************/
	/***** ALICIA MEDIA CACHE *****/
	/******************************/
	
	public static String getA1iciaMediaCacheHashKey(Long val, JebusPool pool) {
		
		SharedUtils.checkNotNull(val);
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAMEDIACACHEKEY + ":" + val.toString();
	}
	
	public static String getA1iciaMediaCacheCounterKey(JebusPool pool) {
	
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAMEDIACACHEKEY + ":nextKey";
	}
	
	public static String getMediaFormatField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return MEDIAFORMATFIELD;
	}
	
	public static String getMediaBytesField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return MEDIABYTESFIELD;
	}
	
	/******************************/
	/***** ALICIA APPLICATION *****/
	/******************************/

	public static String getA1iciaAppsKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAAPPSKEY;
	}

	public static String getA1iciaPurdahKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAPURDAHKEY;
	}

	/**************************/
	/***** ALICIA STATION *****/
	/**************************/

	public static String getA1iciaStationKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY;
	}
	
/*	public static String getStationIdKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_ID;
	}

	public static String getStationCentralHostKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_CENTRAL_HOST;
	}

	public static String getStationCentralPortKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_CENTRAL_PORT;
	}

	public static String getStationOsKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_OS;
	}

	public static String getStationPicoKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_PICO;
	}

	public static String getStationMpvKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_MPV;
	}

	public static String getStationIronKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_IRON;
	}

	public static String getStationLightsKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_PRETTY_LIGHTS;
	}

	public static String getStationLedsKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY + ":" + STATION_LEDS;
	}
*/
	/*******************/
	/***** ALICIAN *****/
    /*******************/
	
	public static String getA1icianCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIANCOUNTERKEY;
	}
	
	/**************************/
	/***** ALICIA CHANNEL *****/
    /**************************/
	
	public static String getA1iciaChannelCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return CHANNELINCR;
	}
	
	/**
	 * This Redis channel is from A1icia to the (unspecified) console. It is implemented
	 * as BinaryJedisPubSub, vs. JedisPubSub.
	 * 
	 * @return The channel key
	 */
	public static byte[] getA1iciaFromChannelBytes(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return (ALICIACHANNELKEY + FROMCHANNEL).getBytes();
	}
	
	/**
	 * This Redis channel is to A1icia from the (unspecified) console. It is implemented
	 * as BinaryJedisPubSub, vs. JedisPubSub.
	 * 
	 * @return The channel key
	 */
	public static byte[] getA1iciaToChannelBytes(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return (ALICIACHANNELKEY + TOCHANNEL).getBytes();
	}
	
	private static void matchPool(JebusPool pool, JebusPoolType type) {
		
		SharedUtils.checkNotNull(pool);
		SharedUtils.checkNotNull(type);
		if (pool.getPoolType() != type) {
			throw new A1iciaAPIException("JebusBible: JebusPool doesn't match expected type of " + type.toString());
		}
	}
	
}
