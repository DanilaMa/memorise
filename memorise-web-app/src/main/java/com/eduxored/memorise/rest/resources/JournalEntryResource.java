package com.eduxored.memorise.rest.resources;

import com.eduxored.memorise.JsonViews;
import com.eduxored.memorise.api.journal.Journal;
import com.eduxored.memorise.impl.journal.JournalEntryDao;
import com.eduxored.memorise.api.user.Role;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;


@Component
@Path("/journal")
public class JournalEntryResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JournalEntryDao journalEntryDao;

	@Autowired
	private ObjectMapper mapper;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String list() throws JsonGenerationException, JsonMappingException, IOException {
		this.logger.info("list()");

		ObjectWriter viewWriter;
		if (this.isPublisher()) {
			viewWriter = this.mapper.writerWithView(JsonViews.Publisher.class);
		} else {
			viewWriter = this.mapper.writerWithView(JsonViews.User.class);
		}
		List<Journal> allEntries = this.journalEntryDao.findAll();

		return viewWriter.writeValueAsString(allEntries);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Journal read(@PathParam("id") Long id) {
		this.logger.info("read(id)");

		Journal journalsEntry = this.journalEntryDao.find(id);
		if (journalsEntry == null) {
			throw new WebApplicationException(404);
		}
		return journalsEntry;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Journal create(Journal journalsEntry) {
		this.logger.info("create(): " + journalsEntry);

		return this.journalEntryDao.save(journalsEntry);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Journal update(@PathParam("id") Long id, Journal journalsEntry) {
		this.logger.info("update(): " + journalsEntry);

		return this.journalEntryDao.save(journalsEntry);
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public void delete(@PathParam("id") Long id) {
		this.logger.info("delete(id)");

		this.journalEntryDao.delete(id);
	}

	private boolean isPublisher() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		if ((principal instanceof String) && ((String) principal).equals("anonymousUser")) {
			return false;
		}
		UserDetails userDetails = (UserDetails) principal;

		return userDetails.getAuthorities().contains(Role.PUBLISHER);
	}

}