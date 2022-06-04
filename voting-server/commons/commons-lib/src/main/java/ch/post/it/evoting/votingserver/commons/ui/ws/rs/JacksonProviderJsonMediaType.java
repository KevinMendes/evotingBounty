/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * Provider class that extends the default Produces/Consumes of {@link JacksonJaxbJsonProvider}.
 * <p>
 * By default Jackson has a tailored matching strategy using the wildcard "{*}/{*}" for the Produces/Consumes mediaType.
 * <p>
 * If any other provider registered within the Context of the application uses as default "application/json" , it will be preferred over Jackson to
 * read/write json.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JacksonProviderJsonMediaType extends JacksonJaxbJsonProvider {

}
