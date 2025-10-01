package com.homelab;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.logging.Logger;

@Path("/")
public class EventReceiver {
  private static final Logger LOG = Logger.getLogger(EventReceiver.class);

  @POST
  @Consumes(MediaType.WILDCARD)
  public Response handle(@Context HttpHeaders headers, byte[] body) {
    String id      = headers.getHeaderString("ce-id");
    String type    = headers.getHeaderString("ce-type");
    String source  = headers.getHeaderString("ce-source");
    String subject = headers.getHeaderString("ce-subject");
    String spec    = headers.getHeaderString("ce-specversion");
    
    LOG.infov("CloudEvent received id={0} type={1} source={2} subject={3} spec={4} bytes={5}", id, type, source, subject, spec, body == null ? 0 : body.length);
    
    // for now i'll just print out the headers

    return Response.accepted().build();
  }
}
