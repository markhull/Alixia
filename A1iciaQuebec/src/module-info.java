/**
 * A1icia Quebec
 * 
 * semantic analysis
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.quebec {

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.quebec.QuebecRoom;
}