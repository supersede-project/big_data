package eu.supersede.bdma.validation.batchprocessing;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.supersede.bdma.validation.batchprocessing.senercon.ErrorStatistics;

@Path("senercon")
public class SenerconResource {

	/**
	 * 1.	Statistic about the amount of errors with a specific attribute over a period of time. These attributes are:
		a.	Position in the source code (first line of the stack trace as long as it is not �/.*ErrorHandler.�)
		b.	Type of the error
		c.	Error code
		d.	portal_id
		e.	user_id
		f.	path of the error
	 */
	@GET @Path("error_statistics")
	@Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String error_statistics() {
		return ErrorStatistics.process();
    }
	
	@GET @Path("error_statistics/csv")
	@Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String error_statistics_csv() {
		return Main.cache_statistics_csv;
    }
	
}
