/**
 * A1icia Lima
 * 
 * history
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.lima {
	exports com.hulles.a1icia.lima to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires transitive com.hulles.a1icia.cayenne;
	requires guava;
	requires java.logging;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.lima.LimaRoom;
}